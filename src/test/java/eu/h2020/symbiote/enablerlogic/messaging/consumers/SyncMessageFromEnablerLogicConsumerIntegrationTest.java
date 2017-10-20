package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import eu.h2020.symbiote.enablerlogic.messaging.VoidResponse;
import eu.h2020.symbiote.enablerlogic.messaging.WrongRequestException;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.SyncMessageFromEnablerLogicConsumer;

public class SyncMessageFromEnablerLogicConsumerIntegrationTest {
    private SyncMessageFromEnablerLogicConsumer consumer;
    private MessageConverter messageConverter;
    private String message;
    private Message msg;

    @Before
    public void before() {
        consumer = new SyncMessageFromEnablerLogicConsumer();
        messageConverter = new Jackson2JsonMessageConverter();
        message = "message";
        msg = messageConverter.toMessage(message, null);
    }

    @Test
    public void function_shouldBeCalledUpponReceivingMessage() throws Exception {
        // given

        // when
        consumer.registerReceiver(String.class, (m) -> "return: " + m);
        String result = (String) consumer.receivedSyncMessage(msg);

        //then
        assertThat(result).isEqualTo("return: " + message);
    }

    @Test
    public void voidReturnfunction_shouldBeCalledUpponReceivingMessageAndReturnVoidResponse() throws Exception {
        // given

        // when
        consumer.registerReceiver(String.class, (m) -> {
            someVoidMethod(m);
            return new VoidResponse();
        });
        VoidResponse result = (VoidResponse) consumer.receivedSyncMessage(msg);

        //then
        assertThat(result).isNotNull();
    }
    
    public void someVoidMethod(String s) {
    }

    @Test
    public void function_shouldReturnExceptionForNotRegisteredType() throws Exception {
        //given

        // when
        consumer.registerReceiver(Long.class, (l) -> l + 1);
        WrongRequestException result = (WrongRequestException) consumer.receivedSyncMessage(msg);

        //then
        assertThat(result.getRequestClassName()).isEqualTo("java.lang.String");
        assertThat(result.getRequest()).isEqualTo(message);
    }

    @Test
    public void function_shouldReturnExceptionForUnregisteredType() throws Exception {
        //given
        consumer.registerReceiver(String.class, (m) -> "response: + m");

        // when
        consumer.unregisterReceiver(String.class);
        WrongRequestException result = (WrongRequestException) consumer.receivedSyncMessage(msg);

        //then
        assertThat(result.getRequestClassName()).isEqualTo("java.lang.String");
        assertThat(result.getRequest()).isEqualTo(message);
    }
}
