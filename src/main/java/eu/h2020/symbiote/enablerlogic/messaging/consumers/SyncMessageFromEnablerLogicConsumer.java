package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.enablerlogic.messaging.WrongRequestException;

@Component
public class SyncMessageFromEnablerLogicConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SyncMessageFromEnablerLogicConsumer.class);

    private Map<String, Function<?, ?>> functions;

    private MessageConverter messageConverter;

    public SyncMessageFromEnablerLogicConsumer() {
        functions = new HashMap<>();
        messageConverter = new Jackson2JsonMessageConverter();
    }

    public <O> void registerReceiver(Class<O> clazz, Function<O, ?> function) {
        functions.put(clazz.getName(), function);
    }

    public void unregisterReceiver(Class<?> clazz) {
        functions.remove(clazz.getName());
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue,
        exchange = @Exchange(value = "#{enablerLogicProperties.enablerLogicExchange.name}", type = "topic"),
        key = "#{enablerLogicProperties.key.enablerLogic.syncMessageToEnablerLogic}.#{enablerLogicProperties.enablerName}"
    ))
    public Object receivedSyncMessage(Message msg) throws IOException {
        LOG.info("Consumer receivedSyncMessage: " + msg);

        Object request = messageConverter.fromMessage(msg);
        String className = request.getClass().getName();

        @SuppressWarnings({ "unchecked" })
        Function<Object, Object> function = (Function<Object, Object>) functions.get(className);

        if(function == null) {
            WrongRequestException exception = new WrongRequestException(
                "Synchronous consumer can not find function for handling this request type.",
                request,
                className);
            LOG.info("responding with exception", exception);
            return exception;
        }

        Object response = function.apply(request);
        return response;
    }
}
