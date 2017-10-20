package eu.h2020.symbiote.enablerlogic;

import eu.h2020.symbiote.enabler.messaging.model.EnablerLogicDataAppearedMessage;
import eu.h2020.symbiote.enabler.messaging.model.NotEnoughResourcesAvailable;
import eu.h2020.symbiote.enabler.messaging.model.ResourcesUpdated;

public class ProcessingLogicAdapter implements ProcessingLogic {

    @Override
    public void initialization(EnablerLogic enablerLogic) {
    }

    @Override
    public void measurementReceived(EnablerLogicDataAppearedMessage dataAppearedMessage) {
    }

    @Override
    public void notEnoughResources(NotEnoughResourcesAvailable notEnoughResourcesAvailableMessage) {
    }

    @Override
    public void resourcesUpdated(ResourcesUpdated resourcesUpdatedMessage) {
    }
}
