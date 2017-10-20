package eu.h2020.symbiote.enablerlogic.messaging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "enablerLogic.plugin")
public class PluginProperties {
    private boolean filtersSupported = false;
    private boolean notificationsSupported = false;
}