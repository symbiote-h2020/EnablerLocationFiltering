package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import eu.h2020.symbiote.enablerlogic.messaging.WrongRequestException;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.AsyncMessageFromEnablerLogicConsumer;

public class AsyncMessageFromEnablerLogicConsumerIntegrationTest {
    private AsyncMessageFromEnablerLogicConsumer consumer;
    private MessageConverter messageConverter;
    private String sendMessage;
    private String receivedMessage;
    private Message msg;

    @Before
    public void before() {
        consumer = new AsyncMessageFromEnablerLogicConsumer();
        messageConverter = new Jackson2JsonMessageConverter();
        sendMessage = "message";
        msg = messageConverter.toMessage(sendMessage, null);
    }

    @Test
    public void consumer_shouldBeCalledUpponReceivingMessage() throws Exception {
        // given

        // when
        consumer.registerReceiver(String.class, (m) -> receivedMessage = m);
        consumer.receivedAsyncMessage(msg);

        //then
        assertThat(receivedMessage).isEqualTo(sendMessage);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void consumer_shouldLogExceptionForNotRegisteredType() throws Exception {
        //given
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        final Appender mockAppender = Mockito.mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);

        // when
        consumer.registerReceiver(Long.class, (l) -> { });
        consumer.receivedAsyncMessage(msg);

        //then
        ArgumentCaptor<Object> varArgs = ArgumentCaptor.forClass(Object.class);
        verify(mockAppender, atLeastOnce()).doAppend(varArgs.capture());

        assertThat(varArgs.getAllValues()).extracting("level", "message", "throwableProxy.throwable")
            .contains(
                tuple(
                    Level.ERROR,
                    "Can not handle request.",
                    new WrongRequestException(
                        "Asynchronous consumer can not find consumer for handling this request type.",
                        "message",
                        "java.lang.String"
                    )
                )
            );
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void consumer_shouldLogExceptionForUnregisteredType() throws Exception {
        //given
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        final Appender mockAppender = Mockito.mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);

        consumer.registerReceiver(String.class, (m) -> { });

        // when
        consumer.unregisterReceiver(String.class);
        consumer.receivedAsyncMessage(msg);

        //then
        ArgumentCaptor<Object> varArgs = ArgumentCaptor.forClass(Object.class);
        verify(mockAppender, atLeastOnce()).doAppend(varArgs.capture());

        assertThat(varArgs.getAllValues()).extracting("level", "message", "throwableProxy.throwable")
            .contains(
                tuple(
                    Level.ERROR,
                    "Can not handle request.",
                    new WrongRequestException(
                        "Asynchronous consumer can not find consumer for handling this request type.",
                        "message",
                        "java.lang.String"
                    )
                )
            );
    }
}
