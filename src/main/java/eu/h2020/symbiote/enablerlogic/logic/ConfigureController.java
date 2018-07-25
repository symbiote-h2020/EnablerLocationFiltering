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
import eu.h2020.symbiote.core.ci.SparqlQueryRequest;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerAcquisitionStartResponse;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTaskInfoRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTaskInfoResponse;
import eu.h2020.symbiote.enablerlogic.EnablerLogic;
import eu.h2020.symbiote.enablerlogic.db.Location;
import eu.h2020.symbiote.enablerlogic.db.LocationRepository;
import eu.h2020.symbiote.enablerlogic.models.LocationGraphic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
@Controller
public class ConfigureController {
    private static final Logger log = LoggerFactory.getLogger(ConfigureController.class);
    
    @Autowired
    private EnablerLogic enablerLogic;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @RequestMapping("/configure")
    public ModelAndView Configure(@RequestParam(value = "platformId", required = true) String platformId){
        ModelAndView modelAndView = new ModelAndView("configure");
        modelAndView.addObject("platformId", platformId);
        return modelAndView;
    }
    
    @RequestMapping(value = "/Locations/Get",method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<LocationGraphic> LocationGet(@RequestParam(value = "platformId", required = true) String platformId){
        return takeLocationGraphicFromDB(platformId);
    }
    
    @RequestMapping(value = "/Locations/GetAll",method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<LocationGraphic> LocationGetFromSSP(@RequestParam(value = "platformId", required = true) String platformId){
        addLocationOfSSP(platformId);
        return takeLocationGraphicFromDB(platformId);
    }
    
    @RequestMapping(value = "/Locations/ChangeNodePosition",method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody ResponseEntity LocationChangePosition(int id, Integer parentId, int orderNumber){
        Optional<Location> lOpt = locationRepository.updateParentId(id,parentId);
        if(lOpt == null || !lOpt.isPresent())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    

    @RequestMapping(value = "/Locations/Delete",method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody List<LocationGraphic> LocationRemove(Integer[] ids,@RequestParam(value = "platformId", required = true) String platformId){
        for(int id : ids){
            //locationRepository.delete(id);
            String a = String.valueOf(id);
        }
        return takeLocationGraphicFromDB(platformId);
    }
    
    private List<LocationGraphic> takeLocationGraphicFromDB(String platformId){
        List<LocationGraphic> locationsGraphics = new ArrayList<LocationGraphic>();
        List<Location> locations = locationRepository.getLocationStructure(platformId);
        if(locations != null){
            for(Location l : locations){
                LocationGraphic lg = fromLocationToLocationGraphic(l);
                locationsGraphics.add(lg);
            }
        }
        return locationsGraphics;
    }
    
    private LocationGraphic fromLocationToLocationGraphic(Location l){
        List<Location> childrenL = l.getChildren();
        if(childrenL == null || childrenL.isEmpty())
            return new LocationGraphic(l.getId(),l.getLocationName(),null);
        
        List<LocationGraphic> childrenLG = new ArrayList<LocationGraphic>();
        for(Location lCh : childrenL){
            LocationGraphic lg = fromLocationToLocationGraphic(lCh);
            childrenLG.add(lg);
        }
        return new LocationGraphic(l.getId(),l.getLocationName(),childrenLG);
    } 
    
    
    public static ResourceManagerTaskInfoRequest createResourceManagerTaskInfoRequest(String taskId, String platformId){
        DateTime now = DateTime.now(DateTimeZone.UTC);
        Period period = new Period(now,now.plusHours(1));
        String dateStr = ISOPeriodFormat.alternateExtended().print(period);


        CoreQueryRequest coreQueryRequest = new CoreQueryRequest();
        /*coreQueryRequest.setLocation_lat(48.208174);
        coreQueryRequest.setLocation_long(16.373819);
        coreQueryRequest.setMax_distance(10_000); // radius 10km
        coreQueryRequest.setObserved_property(Arrays.asList("NOx"));*/
        coreQueryRequest.setPlatform_id(platformId);
        
        
        int minNoResources = 1;
        String queryInterval = dateStr;
        Boolean allowCaching = false;
        String cachingInterval = dateStr;
        Boolean informPlatformProxy = true;
        String enablerLogicName = "EnablerLocationFiltering";
        SparqlQueryRequest sparqlQueryRequest = null;
        ResourceManagerTaskInfoRequest request = new ResourceManagerTaskInfoRequest
                    (taskId, minNoResources, coreQueryRequest, queryInterval, allowCaching, 
                    cachingInterval, informPlatformProxy, enablerLogicName, sparqlQueryRequest);
        return request;
    }
    
    private List<QueryResourceResult> callResourceManager(String platformId) {
        List<QueryResourceResult> qrr = new ArrayList<>();
        String taskId = "someId";
        ResourceManagerTaskInfoRequest request = createResourceManagerTaskInfoRequest(taskId,platformId);
        
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
    
    private void addLocationOfSSP(String platformId){
        List<QueryResourceResult> qrrList = callResourceManager(platformId);
        List<String> locationTake = new ArrayList<String>();
        for(QueryResourceResult qrr: qrrList){
            String locationName = qrr.getLocationName();
            if(locationName != null && locationName != ""){
                if(!locationTake.contains(locationName)){
                    Optional<Location> lOpt = locationRepository.findByLocationName(locationName,platformId);
                    if(lOpt == null || !lOpt.isPresent()){
                        Location l = new Location(-1,qrr.getLocationName(),qrr.getLocationLatitude(),
                                qrr.getLocationLongitude(), qrr.getLocationAltitude(), null, platformId);
                        locationRepository.save(l);
                    }
                    locationTake.add(locationName);
                }
            }
        }
    }
    
    /*
    private static List<QueryResourceResult> privateCallResources(){
        List<QueryResourceResult> qrr = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = "https://symbiote-dev.man.poznan.pl:8100/coreInterface/v1/query?platform_name=OpenIoTZg";
        QueryResponse queryResponse = restTemplate.getForObject(url, QueryResponse.class);
        if(queryResponse != null){
            qrr = queryResponse.getResources();
        }
        return qrr;
    }
    */
}
