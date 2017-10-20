package eu.h2020.symbiote.enablerlogic.messaging.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component("enablerLogicProperties")
public class EnablerLogicProperties {
    @Getter
    @Value("${spring.application.name:DefaultEnablerName}")
    private String enablerName = "DefaultEnablerName";

    @Getter
    private RabbitConnectionProperties rabbitConnection;
    private ExchangeProperties exchangeProperties;
    private RoutingKeysProperties routingKeyProperties;
    private PluginProperties pluginProperties;

    public EnablerLogicProperties() {
        rabbitConnection = new RabbitConnectionProperties();
        exchangeProperties = new ExchangeProperties();
        routingKeyProperties = new RoutingKeysProperties();
        pluginProperties = new PluginProperties();
    }

    @Autowired
    public EnablerLogicProperties(RabbitConnectionProperties rabbitConnection, ExchangeProperties exchangeProperties,
            RoutingKeysProperties routingKeyProperties, PluginProperties pluginProperties) {
        this.rabbitConnection = rabbitConnection;
        this.exchangeProperties = exchangeProperties;
        this.routingKeyProperties = routingKeyProperties;
        this.pluginProperties = pluginProperties;
    }

    public FullExchangeProperties getEnablerLogicExchange() {
        return exchangeProperties.getEnablerLogic();
    }

    public RoutingKeysProperties getKey() {
        return routingKeyProperties;
    }

    public ExchangeProperties getExchange() {
        return exchangeProperties;
    }
    
    public PluginProperties getPlugin() {
        return pluginProperties;
    }
}
