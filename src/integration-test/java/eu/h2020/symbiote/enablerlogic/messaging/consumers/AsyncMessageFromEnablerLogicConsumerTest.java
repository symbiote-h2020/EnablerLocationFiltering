package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.enablerlogic.EmbeddedRabbitFixture;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.AsyncMessageFromEnablerLogicConsumer;
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
        AsyncMessageFromEnablerLogicConsumer.class})
@EnableConfigurationProperties({RabbitConnectionProperties.class, ExchangeProperties.class, RoutingKeysProperties.class, PluginProperties.class})
@TestPropertySource(locations = "classpath:empty.properties")
@DirtiesContext
public class AsyncMessageFromEnablerLogicConsumerTest extends EmbeddedRabbitFixture {

    @Autowired
    private EnablerLogicProperties props;

    @Autowired
    private AsyncMessageFromEnablerLogicConsumer consumer;

    @AllArgsConstructor
    public static class CustomMessage {
        @Getter
        private String message;
    }

    private CustomMessage receivedMessage = null;

    @Test
    public void asyncMessage_shouldCallLambda() {
        // given
        CustomMessage message = new CustomMessage("some custom text");
        consumer.registerReceiver(CustomMessage.class, (m) -> receivedMessage = m);

        // when
        rabbitTemplate.convertAndSend(
                props.getEnablerLogicExchange().getName(),
                props.getKey().getEnablerLogic().getAsyncMessageToEnablerLogic() + "." + props.getEnablerName(),
                message);

        // then
        await().until(() -> receivedMessage != null);
        assertThat(receivedMessage.getMessage()).isEqualTo(message.getMessage());
    }   
}
