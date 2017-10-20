package eu.h2020.symbiote.enablerlogic;

import eu.h2020.symbiote.enabler.messaging.model.EnablerLogicDataAppearedMessage;
import eu.h2020.symbiote.enabler.messaging.model.NotEnoughResourcesAvailable;
import eu.h2020.symbiote.enabler.messaging.model.ResourcesUpdated;

/**
 * Processing Logic is interface that specific Enabler Logic needs to implement.
 *
 * Those methods are called when some event occurred (e.g. enabler is starting or
 * message received from other components).
 *
 * @author Mario Kusek
 *
 */
public interface ProcessingLogic {
    /**
     * Initialization of processing logic. It is called when enabler is started.
     *
     * @param enablerLogic reference to EnablerLogic so messages to other components can be send
     */
    void initialization(EnablerLogic enablerLogic);

    /**
     * This method is called when data from Platform Proxy component is received.
     *
     * @param dataAppearedMessage data from Platform Proxy component
     */
    void measurementReceived(EnablerLogicDataAppearedMessage dataAppearedMessage);

    /**
     * This method is called when Resource Manager component can not find enough resources for 
     * specified acquisition taskId.
     * 
     * @param notEnoughResourcesAvailableMessage message with all details about resources and acquisition task
     */
    void notEnoughResources(NotEnoughResourcesAvailable notEnoughResourcesAvailableMessage);

    /**
     * This method is called when Resource Manager component has updated resources for 
     * specified acquisition taskId.
     * 
     * @param resourcesUpdatedMessage updated resources and acquisition taskId
     */
    void resourcesUpdated(ResourcesUpdated resourcesUpdatedMessage);
}
