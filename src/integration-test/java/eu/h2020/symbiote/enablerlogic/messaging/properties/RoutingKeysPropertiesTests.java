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
@TestPropertySource(locations = "classpath:custom.properties")
public class RoutingKeysPropertiesTests {
    @Autowired
    private RoutingKeysProperties props;

    @Test
    public void shouldLoadEnablerLogicProperties() {
        assertThat(props.getEnablerLogic().getAcquireMeasurements()).isEqualTo("c_am");
        assertThat(props.getEnablerLogic().getDataAppeared()).isEqualTo("c_da");
        assertThat(props.getEnablerLogic().getResourcesUpdated()).isEqualTo("c_ra");
        assertThat(props.getEnablerLogic().getNotEnoughResources()).isEqualTo("c_ner");

        assertThat(props.getEnablerLogic().getAsyncMessageToEnablerLogic())
            .isEqualTo("asyncMessageToEnablerLogic");
        assertThat(props.getEnablerLogic().getSyncMessageToEnablerLogic())
            .isEqualTo("syncMessageToEnablerLogic");
    }
    
    @Test
    public void shouldLoadResourceManagerProperties() {
        assertThat(props.getResourceManager().getStartDataAcquisition())
            .isEqualTo("c_sda");
        assertThat(props.getResourceManager().getCancelTask())
            .isEqualTo("c_ct");
        assertThat(props.getResourceManager().getUpdateTask())
            .isEqualTo("c_ut");
        assertThat(props.getResourceManager().getWrongData())
        .isEqualTo("c_wd");
    }

    @Test
    public void shouldLoadPlatformProxyProperties() {
        assertThat(props.getEnablerPlatformProxy().getAcquisitionStartRequested())
            .isEqualTo("c_asr");
        assertThat(props.getEnablerPlatformProxy().getAcquisitionStopRequested())
            .isEqualTo("c_asr2");
        assertThat(props.getEnablerPlatformProxy().getSingleReadRequested())
            .isEqualTo("c_srr");
    }

}
