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
@TestPropertySource(locations = "classpath:custom.properties")
public class ExchangePropertiesTests {
    @Autowired
    private ExchangeProperties props;

    @Test
    public void shouldLoadEnablerLogicProperties() {
        assertThat(props.getEnablerLogic().getName()).isEqualTo("c_el");
        assertThat(props.getEnablerLogic().getType()).isEqualTo("c_t");
        assertFalse(props.getEnablerLogic().isDurable());
        assertTrue(props.getEnablerLogic().isAutodelete());
        assertTrue(props.getEnablerLogic().isInternal());
    }

    @Test
    public void shouldLoadExchangeNames() throws Exception {
        assertThat(props.getEnablerPlatformProxy().getName()).isEqualTo("c_pp");
        assertThat(props.getResourceManager().getName()).isEqualTo("c_rm");
    }
}
