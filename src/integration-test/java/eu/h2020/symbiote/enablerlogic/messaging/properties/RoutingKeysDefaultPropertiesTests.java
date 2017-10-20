package eu.h2020.symbiote.enablerlogic.messaging.properties;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.enablerlogic.messaging.properties.RoutingKeysProperties;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(RoutingKeysProperties.class)
@TestPropertySource(locations = "classpath:empty.properties")
public class RoutingKeysDefaultPropertiesTests {
    @Autowired
    private RoutingKeysProperties props;

    @Test
    public void shouldLoadEnablerLogicProperties() {
        assertThat(props.getEnablerLogic().getAcquireMeasurements())
            .isEqualTo("symbIoTe.enablerLogic.acquireMeasurements");
        assertThat(props.getEnablerLogic().getDataAppeared())
            .isEqualTo("symbIoTe.enablerLogic.dataAppeared");
        assertThat(props.getEnablerLogic().getResourcesUpdated())
        .isEqualTo("symbIoTe.enablerLogic.resourcesUpdated");
        assertThat(props.getEnablerLogic().getNotEnoughResources())
        .isEqualTo("symbIoTe.enablerLogic.notEnoughResources");
        assertThat(props.getEnablerLogic().getAsyncMessageToEnablerLogic())
            .isEqualTo("symbIoTe.enablerLogic.asyncMessageToEnablerLogic");
        assertThat(props.getEnablerLogic().getSyncMessageToEnablerLogic())
            .isEqualTo("symbIoTe.enablerLogic.syncMessageToEnablerLogic");
    }

    @Test
    public void shouldLoadResourceManagerProperties() {
        assertThat(props.getResourceManager().getStartDataAcquisition())
            .isEqualTo("symbIoTe.resourceManager.startDataAcquisition");
        assertThat(props.getResourceManager().getCancelTask())
            .isEqualTo("symbIoTe.resourceManager.cancelTask");
        assertThat(props.getResourceManager().getUpdateTask())
            .isEqualTo("symbIoTe.resourceManager.updateTask");
        assertThat(props.getResourceManager().getWrongData())
            .isEqualTo("symbIoTe.resourceManager.wrongData");
    }

    @Test
    public void shouldLoadPlatformProxyProperties() {
        assertThat(props.getEnablerPlatformProxy().getAcquisitionStartRequested())
            .isEqualTo("symbIoTe.enablerPlatformProxy.acquisitionStartRequested");
        assertThat(props.getEnablerPlatformProxy().getAcquisitionStopRequested())
            .isEqualTo("symbIoTe.enablerPlatformProxy.acquisitionStopRequested");
        assertThat(props.getEnablerPlatformProxy().getSingleReadRequested())
            .isEqualTo("symbIoTe.enablerPlatformProxy.singleReadRequested");
    }
}
