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
import eu.h2020.symbiote.enablerlogic.models.SspResource;
import eu.h2020.symbiote.model.cim.Actuator;
import eu.h2020.symbiote.model.cim.Device;
import eu.h2020.symbiote.model.cim.Property;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.model.cim.Sensor;
import eu.h2020.symbiote.model.cim.Service;
import eu.h2020.symbiote.model.cim.WGS84Location;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
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
    
    @Value("${useSSP}")
    public Boolean useSSP;
    
    @Value("${SSP.url}")
    private String sspUrl;
    
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
        if(useSSP && platformId.equals("SSP_NXW_1"))
            return callSSP(sspUrl,"SSP_NXW_1");
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

    
    public static List<QueryResourceResult> callSSP(String sspUrl, String ssp){
        SspResource[] sspResources = null;
        List<QueryResourceResult> queryResourceResults = null;
        RestTemplate restTemplate = new RestTemplate();
        String ssp_url = sspUrl + "/innkeeper/public_resources";
        log.info("callSSP sspUrl: "+sspUrl +" ,ssp: "+ssp);
        ResponseEntity<SspResource[]> responseEntity = null;
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity httpEntity = new HttpEntity(headers);

            responseEntity = restTemplate.exchange(ssp_url, HttpMethod.GET, httpEntity, SspResource[].class);
            sspResources = responseEntity.getBody();
            if(sspResources != null){
                queryResourceResults = new ArrayList<>();
                for(SspResource sr: sspResources){
                    QueryResourceResult qrr = new QueryResourceResult();
                    Resource res = sr.getResource();
                    
                    Actuator actuator = null;
                    if(res instanceof Actuator)
                        actuator = (Actuator) res;
                    Device device = null;
                    if(res instanceof Device)
                        device = (Device) res;
                    Sensor sensor = null;
                    if(res instanceof Sensor)
                        sensor = (Sensor) res;
                    Service service = null;
                    if(res instanceof Service)
                        service = (Service) res;
                    
                    qrr.setPlatformId("SSP_NXW_1");
                    qrr.setPlatformName("SSP_NXW_1");
                    qrr.setResourceType(sr.getResourceType());
                    qrr.setDescription(String.join(", ",res.getDescription()));
                    qrr.setId(res.getId());
                    qrr.setName(res.getName());
                    
                    if(actuator != null)
                        qrr.setCapabilities(actuator.getCapabilities());
                    
                    if(service != null)
                        qrr.setInputParameters(service.getParameters());
                    
                    if(device != null){
                        eu.h2020.symbiote.model.cim.Location location = device.getLocatedAt();
                        if(location != null){
                            qrr.setLocationAltitude(((WGS84Location)location).getAltitude());
                            qrr.setLocationLatitude(((WGS84Location)location).getLatitude());
                            qrr.setLocationLongitude(((WGS84Location)location).getLongitude());
                            qrr.setLocationName(location.getName());
                        }
                    }
                    
                    if(sensor != null){
                        List<String> observes = sensor.getObservesProperty();
                        List<Property> properties = null;
                        if(observes != null){
                            properties = new ArrayList<>();
                            for(String ob: observes){
                                Property p = new Property(ob,null,null);
                                properties.add(p);
                            }
                        }
                        qrr.setObservedProperties(properties);
                    }
                    
                    
                    queryResourceResults.add(qrr);
                }
            }
        } catch(RestClientResponseException e) {
            e.printStackTrace();
            log.error("callSSP ", e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("callSSP ", e);
        }
        return queryResourceResults;
    }
}
