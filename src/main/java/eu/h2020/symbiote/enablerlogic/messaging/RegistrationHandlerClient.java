package eu.h2020.symbiote.enablerlogic.messaging;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;

@FeignClient(value = "RegistrationHandler")
public interface RegistrationHandlerClient {

    @RequestMapping(method = RequestMethod.POST, path = "/resource")
    public ResponseEntity<String> registerResource(@RequestBody CloudResource resource);

    @RequestMapping(method = RequestMethod.POST, path = "/resources")
    public ResponseEntity<String> registerResources(@RequestBody List<CloudResource> resources);

//    @RequestMapping(method = RequestMethod.POST, path = "/rdf-resources")
//    public ResponseEntity<?> registerRdfResources(@RequestBody RdfCloudResorceList resources);
    
    @RequestMapping(method = RequestMethod.PUT, path = "/resource")
    public ResponseEntity<String> updateResource(@RequestBody CloudResource resource);
    
    @RequestMapping(method = RequestMethod.PUT, path = "/resources")
    public ResponseEntity<String> updateResources(@RequestBody List<CloudResource> resources);
    
    @RequestMapping(method = RequestMethod.DELETE, path = "/resource")
    public ResponseEntity<String> unregisterResource(@RequestParam("resourceInternalId") String resourceInternalId);
    
    @RequestMapping(method = RequestMethod.DELETE, path = "/resources")
    public ResponseEntity<String> unregisterResources(@RequestParam("resourceInternalId") List<String> resourceInternalIds);

    @RequestMapping(method = RequestMethod.GET, path = "/resources")
    public List<CloudResource> getAllResources();
}
