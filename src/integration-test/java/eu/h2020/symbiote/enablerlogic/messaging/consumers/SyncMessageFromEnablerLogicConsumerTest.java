package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.enablerlogic.EmbeddedRabbitFixture;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.SyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.ExchangeProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.PluginProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RabbitConnectionProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RoutingKeysProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@RunWith(SpringRunner.class)
@Import({TestingRabbitConfig.class,
        EnablerLogicProperties.class,
        SyncMessageFromEnablerLogicConsumer.class})
@EnableConfigurationProperties({RabbitConnectionProperties.class, ExchangeProperties.class, RoutingKeysProperties.class, PluginProperties.class})
@TestPropertySource(locations = "classpath:empty.properties")
@DirtiesContext
public class SyncMessageFromEnablerLogicConsumerTest extends EmbeddedRabbitFixture {

    @Autowired
    private EnablerLogicProperties props;

    @Autowired
    private SyncMessageFromEnablerLogicConsumer consumer;

    @AllArgsConstructor
    public static class CustomRequestMessage {
        @Getter
        private String request;
    }

    @AllArgsConstructor
    public static class CustomResponseMessage {
        @Getter
        private String response;
    }

    @Test
    public void syncMessage_shouldCallFunction() {
        // given
        CustomRequestMessage message = new CustomRequestMessage("some custom text");
        consumer.registerReceiver(CustomRequestMessage.class, (m) -> new CustomResponseMessage("response: " + m.getRequest()));

        // when
        Object response = rabbitTemplate.convertSendAndReceive(
            props.getEnablerLogicExchange().getName(),
            props.getKey().getEnablerLogic().getSyncMessageToEnablerLogic() + "." + props.getEnablerName(),
            message);

        // then
        assertThat(response).isInstanceOf(CustomResponseMessage.class);
        CustomResponseMessage responseMessage = (CustomResponseMessage) response;
        assertThat(responseMessage.getResponse()).isEqualTo("response: " + message.getRequest());
    }
}
