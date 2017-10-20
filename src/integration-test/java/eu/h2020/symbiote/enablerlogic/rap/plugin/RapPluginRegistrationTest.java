package eu.h2020.symbiote.enablerlogic.rap.plugin;

import static eu.h2020.symbiote.util.json.JsonPathAssert.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.rabbitmq.client.Channel;

import eu.h2020.symbiote.cloud.model.data.observation.Observation;
import eu.h2020.symbiote.enablerlogic.messaging.RabbitManager;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.EmbeddedRabbitFixture;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.TestingRabbitConfig;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.ExchangeProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.PluginProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RabbitConnectionProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RoutingKeysProperties;
import eu.h2020.symbiote.enablerlogic.rap.plugin.RapPlugin;
import eu.h2020.symbiote.enablerlogic.rap.resources.RapDefinitions;

@RunWith(SpringRunner.class)
@Import({RabbitManager.class,
    TestingRabbitConfig.class,
    EnablerLogicProperties.class})
@EnableConfigurationProperties({RabbitConnectionProperties.class, ExchangeProperties.class, RoutingKeysProperties.class, PluginProperties.class})
@DirtiesContext
public class RapPluginRegistrationTest extends EmbeddedRabbitFixture {
    private static final String PLUGIN_REGISTRATION_EXCHANGE = RapDefinitions.PLUGIN_REGISTRATION_EXCHANGE_OUT;
    private static final String PLUGIN_REGISTRATION_QUEUE_NAME = "test_plugin_registration";
    private static final String PLUGIN_REGISTRATION_KEY = RapDefinitions.PLUGIN_REGISTRATION_KEY;
    
    private static final int RECEIVE_TIMEOUT = 20_000;

    
    @Configuration
    public static class TestConfiguration {
        @Bean
        public RapPlugin rapPlugin(RabbitManager manager) {
            return new RapPlugin(manager, "platId", false, true);
        }
    }
    
    @Autowired
    private RapPlugin rapPlugin;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConnectionFactory factory;
    
    private Connection connection;
    private Channel channel;

    @Before
    public void initialize() throws Exception {
        connection = factory.createConnection();
        channel = connection.createChannel(false);

        cleanRabbitResources();
        createRabbitResources();
    }

    private void createRabbitResources() throws IOException {
        channel.exchangeDeclare(PLUGIN_REGISTRATION_EXCHANGE, "topic", true, true, false, null);
        channel.queueDeclare(PLUGIN_REGISTRATION_QUEUE_NAME, true, false, false, null);
        channel.queueBind(PLUGIN_REGISTRATION_QUEUE_NAME, PLUGIN_REGISTRATION_EXCHANGE, PLUGIN_REGISTRATION_KEY);
    }

    private void cleanRabbitResources() throws IOException {
        channel.queueDelete(PLUGIN_REGISTRATION_QUEUE_NAME);
        channel.exchangeDelete(PLUGIN_REGISTRATION_EXCHANGE);
    }


    @Test
    public void platformRegistration_shouldSendMessageToRatStartup() throws Exception {
        //given
    
        // when
        rapPlugin.start();
    
        //then
        Message message = rabbitTemplate.receive(PLUGIN_REGISTRATION_QUEUE_NAME, RECEIVE_TIMEOUT);
        assertNotNull(message);
        
        String jsonBody = new String(message.getBody(), StandardCharsets.UTF_8);
        DocumentContext ctx = JsonPath.parse(jsonBody);
        assertThat(ctx).jsonPathAsString("type").isEqualTo("REGISTER_PLUGIN");
        assertThat(ctx).jsonPathAsString("platformId").isEqualTo("platId");
        assertThat(ctx).jsonPathAsBoolean("hasFilters").isFalse();
        assertThat(ctx).jsonPathAsBoolean("hasNotifications").isTrue();
    }
}
