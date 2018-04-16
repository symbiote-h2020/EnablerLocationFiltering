package eu.h2020.symbiote.enablerlogic.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;

@Service
public class RegistrationHandlerClientService {
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationHandlerClientService.class);
    
    private RegistrationHandlerClient client;
    private ObjectMapper mapper;
    private boolean checkForRegistrationHandlerInEureka;
    private boolean registrationHandlerChecked = false;
    private DiscoveryClient discoveryClient;
    
    public RegistrationHandlerClientService(RegistrationHandlerClient client, 
            @Value("${enablerLogic.checkForRegistrationHandlerInEureka:false}") boolean checkForRegistrationHandlerInEureka, 
            DiscoveryClient discoveryClient) {
        this.client = client;
        this.checkForRegistrationHandlerInEureka = checkForRegistrationHandlerInEureka;
        this.discoveryClient = discoveryClient;
        mapper = new ObjectMapper();
    }
    
    private void chackAndWaitRegistrationHandler() {
        if(registrationHandlerChecked)
            return;
        
        if(!checkForRegistrationHandlerInEureka) {
            LOG.info("Don't need to wait for RegistrationHandler in Eureka");
            registrationHandlerChecked = true;
            return;
        }
        
        // wait for discovery client
        List<ServiceInstance> si = discoveryClient.getInstances("RegistrationHandler");
        int counter = 0;
        while(si.isEmpty()) {
            if((counter % 10) == 0)
               LOG.debug("Waiting for RegistrationHandler in Eureka. counter={}", counter);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            si = discoveryClient.getInstances("RegistrationHandler");
            if(counter >= 3000) {
                throw new RuntimeException("Waiting maximal time of 5min for RegistartionHandler in Eureka");
            }
        }
        
        LOG.debug("Found RegistrationHandler in Eureka at {}", si.get(0).getUri());           
    }
    
    public List<CloudResource> registerResource(CloudResource resource) {
        chackAndWaitRegistrationHandler();
        return convertPayload(client.registerResource(resource).getBody());
    }

    public List<CloudResource> registerResources(List<CloudResource> resources) {
        chackAndWaitRegistrationHandler();
        return convertPayload(client.registerResources(resources).getBody());
    }
    
// TODO test & implementation RDF registration
//    @RequestMapping(method = RequestMethod.POST, path = "/rdf-resources")
//    public ResponseEntity<?> registerRdfResources(@RequestBody RdfCloudResorceList resources);

    public List<CloudResource> updateResource(@RequestBody CloudResource resource) {
        chackAndWaitRegistrationHandler();
        return convertPayload(client.updateResource(resource).getBody());
    }
    
    public List<CloudResource> updateResources(List<CloudResource> resources) {
        chackAndWaitRegistrationHandler();
        return convertPayload(client.updateResources(resources).getBody());
    }
    
    public List<CloudResource> unregisterResource(String resourceInternalId) {
        chackAndWaitRegistrationHandler();
        return convertPayload(client.unregisterResource(resourceInternalId).getBody());
    }
    
    public List<CloudResource> unregisterResources(List<String> resourceInternalIds) {
        chackAndWaitRegistrationHandler();
        return convertPayload(client.unregisterResources(resourceInternalIds).getBody());
    }
    
    private List<CloudResource> convertPayload(String responsePayload) {
        try {
            if(responsePayload.startsWith("[")) {
                return mapper.readValue(responsePayload, new TypeReference<List<CloudResource>>() { });
            } else {
                List<CloudResource> l = new ArrayList<>(1);
                l.add(mapper.readValue(responsePayload, CloudResource.class));
                return l;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
