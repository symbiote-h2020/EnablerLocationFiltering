package eu.h2020.symbiote.enablerlogic.messaging.properties;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.enablerlogic.messaging.properties.ExchangeProperties;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(ExchangeProperties.class)
@TestPropertySource(locations = "classpath:empty.properties")
public class EnablerLogicExchangeDefaultPropertiesTests {
    @Autowired
    private ExchangeProperties props;

    @Test
    public void shouldLoadDefaultEnablerLogicProperties() {
        assertThat(props.getEnablerLogic().getName()).isEqualTo("symbIoTe.enablerLogic");
        assertThat(props.getEnablerLogic().getType()).isEqualTo("topic");
        assertTrue(props.getEnablerLogic().isDurable());
        assertFalse(props.getEnablerLogic().isAutodelete());
        assertFalse(props.getEnablerLogic().isInternal());
    }

    @Test
    public void shouldLoadDefaultExcangeNames() throws Exception {
        assertThat(props.getEnablerPlatformProxy().getName()).isEqualTo("symbIoTe.enablerPlatformProxy");
        assertThat(props.getResourceManager().getName()).isEqualTo("symbIoTe.resourceManager");
    }
}
