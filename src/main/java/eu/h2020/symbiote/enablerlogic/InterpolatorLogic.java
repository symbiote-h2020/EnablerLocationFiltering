/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.enabler.messaging.model.EnablerLogicDataAppearedMessage;
import eu.h2020.symbiote.enabler.messaging.model.NotEnoughResourcesAvailable;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerAcquisitionStartResponse;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTaskInfoRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourcesUpdated;
import eu.h2020.symbiote.enablerlogic.db.Location;
import eu.h2020.symbiote.enablerlogic.db.LocationRepository;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
@Component
public class InterpolatorLogic implements ProcessingLogic {

    private static final Logger log = LoggerFactory.getLogger(InterpolatorLogic.class);
    private EnablerLogic enablerLogic;

    @Autowired
    private LocationRepository locationRepository;
    
    @Override
    public void initialization(EnablerLogic enablerLogic) {
        this.enablerLogic = enablerLogic;
        //insertDBFake();
    }

    @Override
    public void measurementReceived(EnablerLogicDataAppearedMessage dataAppearedMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notEnoughResources(NotEnoughResourcesAvailable notEnoughResourcesAvailableMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resourcesUpdated(ResourcesUpdated resourcesUpdatedMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void insertDBFake(){
        locationRepository.deleteAll();
        locationRepository.insertFake();
    }
}
