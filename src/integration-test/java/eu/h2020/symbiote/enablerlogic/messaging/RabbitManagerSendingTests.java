package eu.h2020.symbiote.enablerlogic.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static com.revinate.assertj.json.JsonPathAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.core.ReceiveAndReplyMessageCallback;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.rabbitmq.client.Channel;

import eu.h2020.symbiote.enablerlogic.messaging.RabbitManager;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.EmbeddedRabbitFixture;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.TestingRabbitConfig;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.ExchangeProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.PluginProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RabbitConnectionProperties;
import eu.h2020.symbiote.enablerlogic.messaging.properties.RoutingKeysProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test for RPC communication with Resource Manager.
 * @author PetarKrivic
 *
 */
@RunWith(SpringRunner.class)
@Import({RabbitManager.class,
    TestingRabbitConfig.class,
    EnablerLogicProperties.class})
@EnableConfigurationProperties({RabbitConnectionProperties.class, ExchangeProperties.class, RoutingKeysProperties.class, PluginProperties.class})
@DirtiesContext
public class RabbitManagerSendingTests extends EmbeddedRabbitFixture {
    private static Logger log = LoggerFactory.getLogger(RabbitManagerSendingTests.class);
    private static final int TIMEOUT = 10_000;
    private static final int RECEIVE_TIMEOUT = 20_000;

    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String RECEIVING_QUEUE_NAME = "queueName";
    private static final String RECEIVING_ROUTING_KEY = "receivingQueue";

    private Connection connection;
    private Channel channel;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitManager rabbitManager;

    @Autowired
    private ConnectionFactory factory;

    /**
     * Cleaning up after previous test and creation of new connection and channel to RabbitMQ.
     * @throws Exception
     */
    @Before
    public void initialize() throws Exception {
        connection = factory.createConnection();
        channel = connection.createChannel(false);

        cleanRabbitResources();
        createRabbitResources();
    }

    private void createRabbitResources() throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true, true, false, null);
        channel.queueDeclare(RECEIVING_QUEUE_NAME, true, false, false, null);
        channel.queueBind(RECEIVING_QUEUE_NAME, EXCHANGE_NAME, RECEIVING_ROUTING_KEY);
    }

    private void cleanRabbitResources() throws IOException {
        channel.queueDelete(RECEIVING_QUEUE_NAME);
        channel.exchangeDelete(EXCHANGE_NAME);
    }

    @Test
    public void sendingAcyncMessage_shouldReceiveAsyncMessage() throws Exception {
        // given

        // when
        rabbitManager.sendMessage(EXCHANGE_NAME, RECEIVING_ROUTING_KEY, "Some text");

        // then
        Message message = rabbitTemplate.receive(RECEIVING_QUEUE_NAME, TIMEOUT);
        assertNotNull(message);
        assertThat(new String(message.getBody(), StandardCharsets.UTF_8)).isEqualTo("Some text");
    }

    @Test
    public void sendingAcyncObject_shouldReceiveAsyncObject() throws Exception {
        // given
        ModelObject sendObject = new ModelObject("joe", 25);

        // when
        rabbitManager.sendMessage(EXCHANGE_NAME, RECEIVING_ROUTING_KEY, sendObject);

        // then
        Object o = rabbitTemplate.receiveAndConvert(RECEIVING_QUEUE_NAME, TIMEOUT);
        assertNotNull(o);
        assertThat(o).isInstanceOf(ModelObject.class);
        ModelObject receivedObject = (ModelObject) o;
        assertThat(receivedObject).isEqualToComparingFieldByField(sendObject);
    }

    @Test
    public void sendingAcyncObject_shouldReceiveAsyncJSON() throws Exception {
        // given
        ModelObject sendObject = new ModelObject("joe", 25);

        // when
        rabbitManager.sendMessage(EXCHANGE_NAME, RECEIVING_ROUTING_KEY, sendObject);

        // then
        Message message = rabbitTemplate.receive(RECEIVING_QUEUE_NAME, TIMEOUT);
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(message.getMessageProperties().getHeaders().get("__TypeId__"))
            .isEqualTo("eu.h2020.symbiote.enablerlogic.messaging.RabbitManagerSendingTests$ModelObject");

        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        JSONAssert.assertEquals("{\"name\":\"joe\",\"age\":25}", json, false);
        DocumentContext ctx = JsonPath.parse(json);
        assertThat(ctx).jsonPathAsString("name").isEqualTo("joe");
        assertThat(ctx).jsonPathAsInteger("age").isEqualTo(25);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelObject {
        private String name;
        private int age;
    }

    @Test
    public void sendingRPC_shouldWaitForResponseInText() throws Exception {
        // given
        String sendMessage = "Some RPC message";
        Thread t = new Thread(() -> {
            log.info("receiving thread started");
            rabbitTemplate.setReceiveTimeout(RECEIVE_TIMEOUT);
            rabbitTemplate.receiveAndReply(RECEIVING_QUEUE_NAME, new ReceiveAndReplyMessageCallback() {
            @Override
            public Message handle(Message msg) {
                log.info("receive thread received {}", msg);
                String r = new String(msg.getBody(), StandardCharsets.UTF_8) + "!!!";
                Message rmsg = new Message(r.getBytes(StandardCharsets.UTF_8),
                    MessagePropertiesBuilder.newInstance().setContentType("plain/text").build());
                log.info("returning {}", rmsg);
                return rmsg;
            }
        });
            log.info("************** receiving thread finished");
        });
        t.start();

        // when
        String response = rabbitManager.sendRpcMessage(EXCHANGE_NAME, RECEIVING_ROUTING_KEY, sendMessage);

        assertThat(response).isEqualTo(sendMessage + "!!!");
    }

    public interface ReceiveAndReplyModelObjectCallback extends ReceiveAndReplyCallback<ModelObject, ModelObject> { }

    @Test
    public void sendingRPC_shouldWaitForResponseInJSONObject() throws Exception {
        // given
        ModelObject sendObject = new ModelObject("bob", 17);
        Thread t = new Thread(() -> {
            log.info("receiving thread started");
            rabbitTemplate.setReceiveTimeout(RECEIVE_TIMEOUT);
            rabbitTemplate.receiveAndReply(RECEIVING_QUEUE_NAME, new ReceiveAndReplyModelObjectCallback() {
                @Override
            public ModelObject handle(ModelObject payload) {
                    log.info("receive thread received {}", payload);
                    ModelObject rmsg = new ModelObject("sponge " + payload.getName(), 18);
                    log.info("returning {}", rmsg);
                    return rmsg;
                }
            });
            log.info("receiving thread finished");
        });
        t.start();

        // when
        ModelObject respObject = (ModelObject) rabbitManager.sendRpcMessage(EXCHANGE_NAME, RECEIVING_ROUTING_KEY, sendObject);

        assertThat(respObject.getName()).isEqualTo("sponge bob");
        assertThat(respObject.getAge()).isEqualTo(18);
    }
}
