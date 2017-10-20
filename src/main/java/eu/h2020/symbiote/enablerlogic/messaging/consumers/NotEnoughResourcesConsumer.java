package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import eu.h2020.symbiote.enabler.messaging.model.NotEnoughResourcesAvailable;
import eu.h2020.symbiote.enablerlogic.ProcessingLogic;

public class NotEnoughResourcesConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(NotEnoughResourcesConsumer.class);

    private ProcessingLogic processingLogic;

    public NotEnoughResourcesConsumer(ProcessingLogic processingLogic) {
        this.processingLogic = processingLogic;
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue,
        exchange = @Exchange(value = "#{enablerLogicProperties.enablerLogicExchange.name}", type = "topic"),
        key = "#{enablerLogicProperties.key.enablerLogic.notEnoughResources}"
    ))
    public void dataAppeared(NotEnoughResourcesAvailable notEnoughResourcesAvailableMessage) throws IOException {
        LOG.info("Consumer NotEnoughResourcesAvailable message: " + notEnoughResourcesAvailableMessage);
        processingLogic.notEnoughResources(notEnoughResourcesAvailableMessage);
    }
}
