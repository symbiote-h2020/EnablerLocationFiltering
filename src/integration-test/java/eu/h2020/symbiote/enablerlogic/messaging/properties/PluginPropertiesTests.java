package eu.h2020.symbiote.enablerlogic.messaging.properties;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(PluginProperties.class)
@TestPropertySource(locations = "classpath:empty.properties")
public class PluginPropertiesTests {
    @Autowired
    private PluginProperties props;

    @Test
    public void shouldLoadFirstLevelProperties() {
        assertThat(props.isFiltersSupported()).isFalse();
        assertThat(props.isNotificationsSupported()).isFalse();
    }
}
