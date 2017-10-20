package eu.h2020.symbiote.enablerlogic;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.enabler.messaging.model.CancelTaskRequest;
import eu.h2020.symbiote.enabler.messaging.model.CancelTaskResponse;
import eu.h2020.symbiote.enabler.messaging.model.EnablerLogicDataAppearedMessage;
import eu.h2020.symbiote.enabler.messaging.model.PlatformProxyTaskInfo;
import eu.h2020.symbiote.enabler.messaging.model.ProblematicResourcesMessage;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerAcquisitionStartRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerAcquisitionStartResponse;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerTaskInfoRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerUpdateRequest;
import eu.h2020.symbiote.enabler.messaging.model.ResourceManagerUpdateResponse;
import eu.h2020.symbiote.enablerlogic.messaging.RabbitManager;
import eu.h2020.symbiote.enablerlogic.messaging.WrongResponseException;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.AsyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.SyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import lombok.Setter;

/**
 * EnblerLogic is used by ProcessingLogic to send messages to other components in enabler.
 *
 * @author Mario Kusek
 *
 */
@Service
public class EnablerLogic {

    private static final Logger LOG = LoggerFactory.getLogger(EnablerLogic.class);

    private RabbitManager rabbitManager;

    private EnablerLogicProperties props;

    @Setter
    @Autowired
    private AsyncMessageFromEnablerLogicConsumer asyncConsumer;

    @Setter
    @Autowired
    private SyncMessageFromEnablerLogicConsumer syncConsumer;
    
    /**
     * Initialization of EnablerLogic.
     *
     * @param rabbitManager it is used to send messages to RabbitMQ
     * @param props all properties are loaded in this class
     */
    public EnablerLogic(RabbitManager rabbitManager, EnablerLogicProperties props) {
        this.rabbitManager = rabbitManager;
        this.props = props;
    }

    /**
     * Registering consumer of asynchronous messages from another Enabler Logic component.
     *
     * Messages are delivered to specific Consumer depending on the class of the message that is received.
     * There should be only one Consumer for one type of message (class)
     *
     * @param clazz message class
     * @param consumer consumes message when it arrives
     * @param <O> type of message class
     */
    public <O> void registerAsyncMessageFromEnablerLogicConsumer(Class<O> clazz,
            Consumer<O> consumer) {
        asyncConsumer.registerReceiver(clazz, consumer);
    }

    /**
     * Unregistering consumer of asynchronous messages from another Enabler Logic component.
     *
     * @param clazz message class
     */
    public void unregisterAsyncMessageFromEnablerLogicConsumer(Class<?> clazz) {
        asyncConsumer.unregisterReceiver(clazz);
    }
    /**
     * Registering consumer (Function) of synchronous messages from another Enabler Logic component.
     *
     * Messages are delivered to specific Function depending on the class of the message that is received.
     * There should be only one Function for one type of message (class).
     *
     * @param clazz message class
     * @param function consumes message when it arrives and returns response
     * @param <O> type of message class
     */
    public <O> void registerSyncMessageFromEnablerLogicConsumer(Class<O> clazz, Function<O, ?> function) {
        syncConsumer.registerReceiver(clazz, function);
    }

    /**
     * Unregistering consumer (Function) of synchronous messages from another Enabler Logic component.
     *
     * @param clazz receiving message class
     */
    public void unregisterSyncMessageFromEnablerLogicConsumer(Class<?> clazz) {
        syncConsumer.unregisterReceiver(clazz);
    }

    /**
     * Queries Resource Manager component. It is blocking until response received or timeout.
     * It starts acquisition.
     *
     * In the case of timeout the null is returned.
     *
     * @param requests send to Resource Manager component
     * @return response form Resource Manager component or null in case of timeout
     */
    public ResourceManagerAcquisitionStartResponse queryResourceManager(ResourceManagerTaskInfoRequest...requests) {
    	
		if (props==null)
			throw new IllegalStateException("Props may not be null");
    	
        for(ResourceManagerTaskInfoRequest request: requests) {
            LOG.info("sending message to ResourceManager: {}", request);
        }

        ResourceManagerAcquisitionStartRequest request = new ResourceManagerAcquisitionStartRequest();
        request.setTasks(Arrays.asList(requests));

        ResourceManagerAcquisitionStartResponse response = (ResourceManagerAcquisitionStartResponse)
            rabbitManager.sendRpcMessage(props.getExchange().getResourceManager().getName(),
                props.getKey().getResourceManager().getStartDataAcquisition(),
                request);

        LOG.info("Received resourceIds from ResourceManager");
        return response;
    }
    
    /**
     * Sends to Resource Manager to cancel task for acquisition. It is blocking until response received or timeout.
     *
     * In the case of timeout the null is returned.
     *
     * @param request for canceling task
     * @return response of task cancellation
     */
    public CancelTaskResponse cancelTask(CancelTaskRequest request) {
        return (CancelTaskResponse) rabbitManager.sendRpcMessage(
                props.getExchange().getResourceManager().getName(), 
                props.getKey().getResourceManager().getCancelTask(), 
                request);
    }
    
    /**
     * Sends to Resource Manager to update acquisition task. It is blocking until response received or timeout.
     *
     * In the case of timeout the null is returned.
     * 
     * @param request updated request
     * @return response of task update
     */
    public ResourceManagerUpdateResponse updateTask(ResourceManagerUpdateRequest request) {
        return (ResourceManagerUpdateResponse) rabbitManager.sendRpcMessage(
                props.getExchange().getResourceManager().getName(),
                props.getKey().getResourceManager().getUpdateTask(),
                request);
    }

    /**
     * Sends asynchronous message to another Enabler Logic component.
     * @param enablerName the name of another Enabler Logic component
     * @param msg message send to Enabler Logic component
     */
    public void sendAsyncMessageToEnablerLogic(String enablerName, Object msg) {
        rabbitManager.sendMessage(props.getEnablerLogicExchange().getName(),
            generateAsyncEnablerLogicRoutingKey(enablerName),
            msg);
    }

    private String generateAsyncEnablerLogicRoutingKey(String enablerName) {
        return props.getKey().getEnablerLogic().getAsyncMessageToEnablerLogic() + "." +
            enablerName;
    }

    /**
     * Sends synchronous message to another Enabler Logic component.
     * @param enablerName the name of another Enabler Logic component
     * @param msg message send to Enabler Logic component
     * @param clazz class of response message
     * @param <O> type of response message
     * @return response message or null when timeout
     *
     * @throws WrongResponseException when the response message can not be casted to clazz.
     */
    @SuppressWarnings("unchecked")
    public <O> O sendSyncMessageToEnablerLogic(String enablerName, Object msg, Class<O> clazz) {
        Object response = rabbitManager.sendRpcMessage(props.getEnablerLogicExchange().getName(),
            generateSyncEnablerLogicRoutingKey(enablerName),
            msg);

        if(response == null)
            return null;

        if(clazz.isInstance(response))
            return (O) response;

        throw new WrongResponseException(response);
    }

    private String generateSyncEnablerLogicRoutingKey(String enablerName) {
        return props.getKey().getEnablerLogic().getSyncMessageToEnablerLogic() + "." +
            enablerName;
    }

    /**
     * Sends message to Resource Manager that specified broken resource is producing wrong data.
     * @param message broken resources
     */
    public void reportBrokenResource(ProblematicResourcesMessage message) {
        rabbitManager.sendMessage(
                props.getExchange().getResourceManager().getName(),
                props.getKey().getResourceManager().getWrongData(),
                message);
    }
    
    /**
     * Send request to Platform Proxy to read resource and return result.
     * 
     * @param info requested resource info
     * @return reading result
     */
    public EnablerLogicDataAppearedMessage readResource(PlatformProxyTaskInfo info) {
        return (EnablerLogicDataAppearedMessage) rabbitManager.sendRpcMessage(
                props.getExchange().getEnablerPlatformProxy().getName(),
                props.getKey().getEnablerPlatformProxy().getSingleReadRequested(),
                info);
    }
    
}
