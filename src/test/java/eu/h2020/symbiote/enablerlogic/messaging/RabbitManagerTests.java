package eu.h2020.symbiote.enablerlogic.messaging;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import eu.h2020.symbiote.enablerlogic.messaging.RabbitManager;

@RunWith(MockitoJUnitRunner.class)
public class RabbitManagerTests {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitManager rabbitManager;

    @Before
    public void setup() {
        rabbitManager = new RabbitManager(rabbitTemplate);
    }

    @Test
    public void sendMessageWithMessage_shouldCallRabbitTemplate() throws Exception {
        // given
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        String exchange = "e";
        String key = "k";

        // when
        rabbitManager.sendMessage(exchange, key, "m");

        // then
        verify(rabbitTemplate).send(eq(exchange), eq(key), messageCaptor.capture());
        Message message = messageCaptor.getValue();
        assertThat(message.getBody()).isEqualTo("m".getBytes(StandardCharsets.UTF_8));
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("plain/text");
    }

    @Test
    public void sendMessageWithObject_shouldCallRabbitTemplate() throws Exception {
        // given
        String exchange = "e";
        String key = "k";
        Object obj = new Object();

        // when
        rabbitManager.sendMessage(exchange, key, obj);

        // then
        verify(rabbitTemplate).convertAndSend(exchange, key, obj);
    }

    @Test
    public void sendRpcMessageWithMessage_shouldCallRabbitTemplate() throws Exception {
        // given
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        String exchange = "e";
        String key = "k";
        String expectedResult = "result";
        Message resultMessage = new Message(expectedResult.getBytes(StandardCharsets.UTF_8), null);

        when(rabbitTemplate.sendAndReceive(eq(exchange), eq(key), any())).thenReturn(resultMessage);

        // when
        String result = rabbitManager.sendRpcMessage(exchange, key, "m");

        // then
        verify(rabbitTemplate).sendAndReceive(eq(exchange), eq(key), messageCaptor.capture());
        Message sendMessage = messageCaptor.getValue();
        assertThat(sendMessage.getBody()).isEqualTo("m".getBytes(StandardCharsets.UTF_8));
        assertThat(sendMessage.getMessageProperties().getContentType()).isEqualTo("plain/text");

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void sendRpcMessageWithMessage_shouldReturnNullWhenTimeout() throws Exception {
        // given
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        String exchange = "e";
        String key = "k";
        when(rabbitTemplate.sendAndReceive(eq(exchange), eq(key), any())).thenReturn(null);

        // when
        String result = rabbitManager.sendRpcMessage(exchange, key, "m");

        // then
        verify(rabbitTemplate).sendAndReceive(eq(exchange), eq(key), messageCaptor.capture());
        Message sendMessage = messageCaptor.getValue();
        assertThat(sendMessage.getBody()).isEqualTo("m".getBytes(StandardCharsets.UTF_8));
        assertThat(sendMessage.getMessageProperties().getContentType()).isEqualTo("plain/text");

        assertThat(result).isEqualTo(null);
    }

    public static class TestObject { }

    @Test
    public void sendRpcMessageWithObject_shouldCallRabbitTemplate() throws Exception {
        // given
        ArgumentCaptor<TestObject> messageCaptor = ArgumentCaptor.forClass(TestObject.class);
        String exchange = "e";
        String key = "k";
        TestObject sendObject = new TestObject();
        TestObject resultObject = new TestObject();

        when(rabbitTemplate.convertSendAndReceive(
            eq(exchange),
            eq(key),
            any(TestObject.class),
            any(CorrelationData.class))
        ).thenReturn(resultObject);

        // when
        Object result = rabbitManager.sendRpcMessage(exchange, key, sendObject);

        // then
        verify(rabbitTemplate).convertSendAndReceive(
            eq(exchange),
            eq(key),
            messageCaptor.capture(),
            any(CorrelationData.class));
        TestObject sendMessage = messageCaptor.getValue();
        assertThat(sendMessage).isEqualTo(sendObject);

        assertThat(result).isEqualTo(resultObject);
    }

    @Test
    public void sendRpcMessageWithObject_shouldReturnNullOnTimeout() throws Exception {
        // given
        ArgumentCaptor<TestObject> messageCaptor = ArgumentCaptor.forClass(TestObject.class);
        String exchange = "e";
        String key = "k";
        TestObject sendObject = new TestObject();

        when(rabbitTemplate.convertSendAndReceive(eq(exchange), eq(key),
            any(TestObject.class),
            any(CorrelationData.class))
        ).thenReturn(null);

        // when
        Object result = rabbitManager.sendRpcMessage(exchange, key, sendObject);

        // then
        verify(rabbitTemplate).convertSendAndReceive(
            eq(exchange),
            eq(key),
            messageCaptor.capture(),
            any(CorrelationData.class));
        TestObject sendMessage = messageCaptor.getValue();
        assertThat(sendMessage).isEqualTo(sendObject);

        assertThat(result).isEqualTo(null);
    }
}
