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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
@RequestMapping("enabler")
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
    
    
    @RequestMapping(value="locationFilter", method=RequestMethod.GET)
    public ResponseEntity<List<QueryResourceResult>> readResourceAll( 
            @RequestParam(value = "locationName", required = false) String locationName,
            @RequestParam(value = "platformId", required = true) List<String> platformId,HttpServletRequest request) throws Exception {
        List<QueryResourceResult> qrrList = filterLocation(locationName,platformId);
        return new ResponseEntity<List<QueryResourceResult>>(qrrList,HttpStatus.OK);
    }
    
    
    private List<QueryResourceResult> filterLocation(String locationName,List<String> platformIdList){
        List<QueryResourceResult> qrrListResult = new ArrayList<>();
        List<String> locationNameAccepted = new ArrayList<>();
        if(locationName != null && !locationName.equals("")){
            try {
                List<Location> locationAccepted = locationRepository.getLocationChildren(locationName,platformIdList);
                for(Location l : locationAccepted){
                    locationNameAccepted.add(l.getLocationName());
                }
            } catch (Exception ex) {
                log.error(ex.toString(),ex);
                Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        /*
        List<QueryResourceResult> qrrList = new ArrayList<>();
        for(String platformId: platformIdList){
            qrrList.addAll(callResourceManager(platformId));
        }
        */
        List<QueryResourceResult> qrrList = callResourceManagerTask(platformIdList);
        
        
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
    
    
    private List<QueryResourceResult> callResourceManagerTask(List<String> platformIdList){
        List<QueryResourceResult> qrrList = new ArrayList<>();
        try{
            ExecutorService executor = Executors.newWorkStealingPool();
            List<Callable<List<QueryResourceResult>>> callables = new ArrayList<>();
            for(String platformId: platformIdList){
                callables.add(new CallableTask(enablerLogic,platformId));
            }
            List<Future<List<QueryResourceResult>>> futures = executor.invokeAll(callables);
            for (Future<List<QueryResourceResult>> futreQueryResList : futures)
            {
                qrrList.addAll(futreQueryResList.get());
            }
            executor.shutdown();
            }
        catch(Exception e)
        {
            Logger.getLogger(RequestController.class.getName()).log(Level.SEVERE, null, e);
        }
        return qrrList;
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
    
    class CallableTask implements Callable<List<QueryResourceResult>>
    {
        String platformId;
        EnablerLogic enablerLogic;

        public CallableTask(EnablerLogic enablerLogic, String platformId)
        {
            this.platformId = platformId;
            this.enablerLogic = enablerLogic;
        }

        @Override
        public List<QueryResourceResult> call() throws Exception
        {
            return callResourceManager(this.enablerLogic, this.platformId);
        }
        
        private List<QueryResourceResult> callResourceManager(EnablerLogic enablerLogic, String platformId) {
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
}
