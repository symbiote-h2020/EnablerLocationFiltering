package eu.h2020.symbiote.enablerlogic;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.enablerlogic.EnablerLogic;
import eu.h2020.symbiote.enablerlogic.messaging.RabbitManager;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.AsyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.SyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@RunWith(SpringRunner.class)
@Import({EnablerLogic.class})
public class EnablerLogicInjectionTest {
    @Configuration
    public static class AsyncMessageFromEnablerLogicConsumerTestConfiguration {
        @Bean
        public AsyncMessageFromEnablerLogicConsumer asyncMessageFromEnablerLogicConsumer() {
            return Mockito.mock(AsyncMessageFromEnablerLogicConsumer.class);
        }

        @Bean
        public SyncMessageFromEnablerLogicConsumer syncMessageFromEnablerLogicConsumer() {
            return Mockito.mock(SyncMessageFromEnablerLogicConsumer.class);
        }

        @Bean
        public EnablerLogicProperties enablerLogicProperties() {
            return new EnablerLogicProperties();
        }

        @Bean
        public RabbitManager rabbitManager() {
            return Mockito.mock(RabbitManager.class);
        }
    }

    @Autowired
    private EnablerLogic enablerLogic;

    @Autowired
    private AsyncMessageFromEnablerLogicConsumer consumer;

    @AllArgsConstructor
    public static class CustomMessage {
        @Getter
        private String message;
    }

    public static class CustomMessageConsumer implements Consumer<CustomMessage> {
        @Override
        public void accept(CustomMessage m) {
        }
    }

    @Test
    public void testThatAsyncMessageFromEnablerLogicRegistration_isCalledInConsumer() throws Exception {
        // given
        CustomMessageConsumer lambda = new CustomMessageConsumer();

        // when
        enablerLogic.registerAsyncMessageFromEnablerLogicConsumer(CustomMessage.class, lambda);

        // then
        ArgumentCaptor<CustomMessageConsumer> captor = ArgumentCaptor.forClass(CustomMessageConsumer.class);
        verify(consumer).registerReceiver(eq(CustomMessage.class), captor.capture());
        CustomMessageConsumer argument = captor.getValue();
        assertThat(argument).isSameAs(lambda);
    }

    @Test
    public void testThatAsyncMessageFromEnablerLogicUnregistration_isCalledInConsumer() throws Exception {
        // given

        // when
        enablerLogic.unregisterAsyncMessageFromEnablerLogicConsumer(CustomMessage.class);

        // then
        verify(consumer).unregisterReceiver(eq(CustomMessage.class));
    }
}
