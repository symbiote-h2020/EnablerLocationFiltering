/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerAcquisitionStartResponse;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTaskInfoRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTaskInfoResponse;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTasksStatus;
import eu.h2020.symbiote.enablerlogic.EnablerLogic;
import eu.h2020.symbiote.enablerlogic.InterpolatorLogic;
import eu.h2020.symbiote.enablerlogic.db.Location;
import eu.h2020.symbiote.enablerlogic.db.LocationRepository;
import static eu.h2020.symbiote.enablerlogic.logic.ConfigureController.createResourceManagerTaskInfoRequest;
import eu.h2020.symbiote.enablerlogic.models.LocationGraphic;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
@RestController
@RequestMapping("locationFilter")
public class RequestController {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RequestController.class);
    
    @Autowired
    private EnablerLogic enablerLogic;
    
    @Autowired
    private LocationRepository locationRepository;
    
    /*@RequestMapping(value="/{locationName}", method=RequestMethod.GET)
    public ResponseEntity<ResourceManagerAcquisitionStartResponse> readResource(@PathVariable String locationName, HttpServletRequest request) throws Exception {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ResourceManagerTaskInfoRequest requestResourceManager = new ResourceManagerTaskInfoRequest();
        requestResourceManager.setTaskId("someId");
        requestResourceManager.setEnablerLogicName("exampleEnabler");
        requestResourceManager.setMinNoResources(1);
        //request.setCachingInterval_ms(3600L);

        CoreQueryRequest coreQueryRequest = new CoreQueryRequest();
        //coreQueryRequest.setLocation_lat(48.208174);
        //coreQueryRequest.setLocation_long(16.373819);
        //coreQueryRequest.setMax_distance(10_000); // radius 10km
        //coreQueryRequest.setObserved_property(Arrays.asList("NOx"));
        coreQueryRequest.setPlatform_name("OpenIoTZg");
        requestResourceManager.setCoreQueryRequest(coreQueryRequest);
        ResourceManagerAcquisitionStartResponse response = enablerLogic.queryResourceManager(requestResourceManager);
        
        if(response != null && response.getStatus().equals(ResourceManagerTasksStatus.SUCCESS))
            httpStatus = HttpStatus.OK;
        return new ResponseEntity<>(response,httpStatus);
    }
    */
    
    @RequestMapping(value="", method=RequestMethod.GET)
    public ResponseEntity<List<QueryResourceResult>> readResourceAll( 
            @RequestParam(value = "locationName", required = false) String locationName,
            @RequestParam(value = "platformId", required = true) String platformId,HttpServletRequest request) throws Exception {
        List<QueryResourceResult> qrrList = filterLocation(locationName,platformId);
        return new ResponseEntity<List<QueryResourceResult>>(qrrList,HttpStatus.OK);
    }
    
    /*
    @RequestMapping(value="/{locationName}", method=RequestMethod.GET)
    public ResponseEntity<List<QueryResourceResult>> readResource(@PathVariable String locationName, 
            @RequestParam(value = "platformId", required = true) String platformId,HttpServletRequest request) throws Exception {
        List<QueryResourceResult> qrrList = filterLocation(locationName,platformId);
        return new ResponseEntity<List<QueryResourceResult>>(qrrList,HttpStatus.OK);
    }
    */
    
    private List<QueryResourceResult> filterLocation(String locationName,String platformId){
        List<QueryResourceResult> qrrListResult = new ArrayList<>();
        List<String> locationNameAccepted = new ArrayList<>();
        if(locationName != null && !locationName.equals("")){
            try {
                List<Location> locationAccepted = locationRepository.getLocationChildren(locationName,platformId);
                for(Location l : locationAccepted){
                    locationNameAccepted.add(l.getLocationName());
                }
            } catch (Exception ex) {
                log.error(ex.toString(),ex);
                Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        List<QueryResourceResult> qrrList = callResourceManager(platformId);
        if(locationName != null && !locationName.equals("")){
            for(QueryResourceResult qrr: qrrList){
                if(locationNameAccepted.contains(qrr.getLocationName()))
                    qrrListResult.add(qrr);
            }
        }
        else{
            qrrListResult = qrrList;
        }
        return qrrListResult;
    }
    
    
    private List<QueryResourceResult> callResourceManager(String platformId) {
        List<QueryResourceResult> qrr = new ArrayList<>();
        String taskId = UUID.randomUUID().toString();
        ResourceManagerTaskInfoRequest request = ConfigureController.createResourceManagerTaskInfoRequest(taskId,platformId);
        
        ResourceManagerAcquisitionStartResponse response = enablerLogic.queryResourceManager(request);

        try {
            log.info("querying fixed resources: {}", new ObjectMapper().writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.error("Problem with deserializing ResourceManagerAcquisitionStartResponse", e);
        }
        
        if(response != null){
            String message = response.getMessage();
            List<ResourceManagerTaskInfoResponse> infoResponseList = response.getTasks();
            for(ResourceManagerTaskInfoResponse infoResponse: infoResponseList){
                if(infoResponse.getTaskId().equals(taskId))
                    qrr = infoResponse.getResourceDescriptions();
            }
        }
        return qrr;
    }

}
