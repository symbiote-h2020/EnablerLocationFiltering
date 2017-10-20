package eu.h2020.symbiote.enablerlogic.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;

@Service
public class RegistrationHandlerClientService {
    private RegistrationHandlerClient client;
    private ObjectMapper mapper;
    
    
    public RegistrationHandlerClientService(RegistrationHandlerClient client) {
        this.client = client;
        mapper = new ObjectMapper();
    }
    
    public List<CloudResource> registerResource(CloudResource resource) {
        return convertPayload(client.registerResource(resource).getBody());
    }

    public List<CloudResource> registerResources(List<CloudResource> resources) {
        return convertPayload(client.registerResources(resources).getBody());
    }
    
// TODO test & implementation RDF registration
//    @RequestMapping(method = RequestMethod.POST, path = "/rdf-resources")
//    public ResponseEntity<?> registerRdfResources(@RequestBody RdfCloudResorceList resources);

    public List<CloudResource> updateResource(@RequestBody CloudResource resource) {
        return convertPayload(client.updateResource(resource).getBody());
    }
    
    public List<CloudResource> updateResources(List<CloudResource> resources) {
        return convertPayload(client.updateResources(resources).getBody());
    }
    
    public List<CloudResource> unregisterResource(String resourceInternalId) {
        return convertPayload(client.unregisterResource(resourceInternalId).getBody());
    }
    
    public List<CloudResource> unregisterResources(List<String> resourceInternalIds) {
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
