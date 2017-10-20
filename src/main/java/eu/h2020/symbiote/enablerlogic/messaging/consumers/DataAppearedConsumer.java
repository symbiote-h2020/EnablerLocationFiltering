package eu.h2020.symbiote.enablerlogic.messaging.consumers;

import eu.h2020.symbiote.enabler.messaging.model.EnablerLogicDataAppearedMessage;
import eu.h2020.symbiote.enablerlogic.ProcessingLogic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;

import org.springframework.stereotype.Component;


import java.io.IOException;

/**
 * Consumer of the Data Appeared Message.
 * Created by Petar Krivic on 04/04/2017.
 */

@Component
public class DataAppearedConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(DataAppearedConsumer.class);

    private ProcessingLogic processingLogic;

    public DataAppearedConsumer(ProcessingLogic processingLogic) {
        this.processingLogic = processingLogic;
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue,
        exchange = @Exchange(value = "#{enablerLogicProperties.enablerLogicExchange.name}", type = "topic"),
        key = "#{enablerLogicProperties.key.enablerLogic.dataAppeared}"
    ))
    public void dataAppeared(EnablerLogicDataAppearedMessage dataAppearedMessage) throws IOException {
        LOG.info("Consumer DataAppeared message: " + dataAppearedMessage);
        processingLogic.measurementReceived(dataAppearedMessage);
    }
}
