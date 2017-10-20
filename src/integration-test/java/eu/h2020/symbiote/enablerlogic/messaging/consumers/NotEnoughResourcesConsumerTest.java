package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.enabler.messaging.model.NotEnoughResourcesAvailable;
import eu.h2020.symbiote.enablerlogic.ProcessingLogicAdapter;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.ExchangeProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.PluginProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RabbitConnectionProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RoutingKeysProperties;
import lombok.Getter;
import lombok.Setter;

@RunWith(SpringRunner.class)
@Import({TestingRabbitConfig.class,
    EnablerLogicProperties.class,
    NotEnoughResourcesConsumer.class})
@EnableConfigurationProperties({RabbitConnectionProperties.class, ExchangeProperties.class, RoutingKeysProperties.class, PluginProperties.class})
@TestPropertySource(locations = "classpath:empty.properties")
@DirtiesContext
public class NotEnoughResourcesConsumerTest extends EmbeddedRabbitFixture {

    @Configuration
    @EnableRabbit
    public static class ProcessingLogicConfig {
        @Bean
        public ProcessingLogicTestImpl processingLogic() {
            return new ProcessingLogicTestImpl();
        }
        
        @Bean
        public EnablerLogicProperties enablerLogicProperties() {
            return new EnablerLogicProperties();
        }
    }

    @Autowired
    private EnablerLogicProperties props;

    @Autowired
    private ProcessingLogicTestImpl processingLogic;

    public static class ProcessingLogicTestImpl extends ProcessingLogicAdapter {
        @Getter
        @Setter
        private NotEnoughResourcesAvailable notEnoughtResourcesAvailableMessage;

        @Override
        public void notEnoughResources(NotEnoughResourcesAvailable notEnoughResourcesAvailableMessage) {
            this.notEnoughtResourcesAvailableMessage = notEnoughResourcesAvailableMessage;
        }
    }

    @Test
    public void notEnoughResources_shouldReceiveMessage() {
        // given
        NotEnoughResourcesAvailable message = new NotEnoughResourcesAvailable();
        message.setTaskId("taskId");

        // when
        rabbitTemplate.convertAndSend(
            props.getEnablerLogicExchange().getName(),
            props.getKey().getEnablerLogic().getNotEnoughResources(),
            message);

        // then
        await().until(() -> processingLogic.notEnoughtResourcesAvailableMessage != null);
        NotEnoughResourcesAvailable receivedMessage = processingLogic.notEnoughtResourcesAvailableMessage;
        assertThat(receivedMessage.getTaskId()).isEqualTo("taskId");
    }
}
