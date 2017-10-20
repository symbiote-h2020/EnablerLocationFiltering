package eu.h2020.symbiote.enablerlogic;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
import eu.h2020.symbiote.enablerlogic.EnablerLogic;
import eu.h2020.symbiote.enablerlogic.messaging.RabbitManager;
import eu.h2020.symbiote.enablerlogic.messaging.VoidResponse;
import eu.h2020.symbiote.enablerlogic.messaging.WrongResponseException;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.AsyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.SyncMessageFromEnablerLogicConsumer;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

@RunWith(MockitoJUnitRunner.class)
public class EnablerLogicTest {
    private EnablerLogicProperties props;
    private EnablerLogic enablerLogic;

    @Mock
    private RabbitManager rabbitManager;


    @Before
    public void setup() {
        props = new EnablerLogicProperties();
        enablerLogic = new EnablerLogic(rabbitManager, props);
    }

    @Test
    public void queryResourceManagerWithOneRequest_shouldReturnResponse() throws Exception {
        // given
        ArgumentCaptor<ResourceManagerAcquisitionStartRequest> captor =
            ArgumentCaptor.forClass(ResourceManagerAcquisitionStartRequest.class);
        ResourceManagerTaskInfoRequest request = new ResourceManagerTaskInfoRequest();
        ResourceManagerAcquisitionStartResponse response = new ResourceManagerAcquisitionStartResponse();
        when(rabbitManager.sendRpcMessage(
            eq(props.getExchange().getResourceManager().getName()),
            eq(props.getKey().getResourceManager().getStartDataAcquisition()),
            any(ResourceManagerAcquisitionStartRequest.class))).thenReturn(response);

        // when
        enablerLogic.queryResourceManager(request);

        // then
        verify(rabbitManager).sendRpcMessage(eq(props.getExchange().getResourceManager().getName()),
            eq(props.getKey().getResourceManager().getStartDataAcquisition()),
            captor.capture());
        ResourceManagerAcquisitionStartRequest requests = captor.getValue();
        assertThat(requests.getTasks()).hasSize(1);
        assertThat(requests.getTasks()).contains(request);
    }

    @Test
    public void cancelTask_shouldReturnCancellationResponse() throws Exception {
        // given
        ArgumentCaptor<CancelTaskRequest> captor =
                ArgumentCaptor.forClass(CancelTaskRequest.class);
        CancelTaskRequest expectedRequest = new CancelTaskRequest();
        CancelTaskResponse response = new CancelTaskResponse();
        when(rabbitManager.sendRpcMessage(
                eq(props.getExchange().getResourceManager().getName()),
                eq(props.getKey().getResourceManager().getCancelTask()),
                any(ResourceManagerAcquisitionStartRequest.class))).thenReturn(response);
        
        // when
        enablerLogic.cancelTask(expectedRequest);
        
        // then
        verify(rabbitManager).sendRpcMessage(eq(props.getExchange().getResourceManager().getName()),
                eq(props.getKey().getResourceManager().getCancelTask()),
                captor.capture());
        CancelTaskRequest request = captor.getValue();
        assertThat(request).isSameAs(expectedRequest);
    }
    
    @Test
    public void updateTask_shouldReturnUpdateResponse() throws Exception {
        // given
        ArgumentCaptor<ResourceManagerUpdateRequest> captor =
                ArgumentCaptor.forClass(ResourceManagerUpdateRequest.class);
        ResourceManagerUpdateRequest expectedRequest = new ResourceManagerUpdateRequest();
        ResourceManagerUpdateResponse response = new ResourceManagerUpdateResponse();
        when(rabbitManager.sendRpcMessage(
                eq(props.getExchange().getResourceManager().getName()),
                eq(props.getKey().getResourceManager().getCancelTask()),
                any(ResourceManagerUpdateRequest.class))).thenReturn(response);
        
        // when
        enablerLogic.updateTask(expectedRequest);
        
        // then
        verify(rabbitManager).sendRpcMessage(eq(props.getExchange().getResourceManager().getName()),
                eq(props.getKey().getResourceManager().getUpdateTask()),
                captor.capture());
        ResourceManagerUpdateRequest request = captor.getValue();
        assertThat(request).isSameAs(expectedRequest);
    }    

    @Test
    public void reportBrokenResource_shouldCallRabbitManager() throws Exception {
        // given
        ProblematicResourcesMessage message = new ProblematicResourcesMessage();
        
        // when
        enablerLogic.reportBrokenResource(message);

        // then
        verify(rabbitManager).sendMessage(
                eq(props.getExchange().getResourceManager().getName()),
                eq(props.getKey().getResourceManager().getWrongData()),
                eq((Object) message));
    }
    
    @Test
    public void readResource_shouldReturnResponse() throws Exception {
        // given
        ArgumentCaptor<PlatformProxyTaskInfo> captor =
            ArgumentCaptor.forClass(PlatformProxyTaskInfo.class);
        PlatformProxyTaskInfo expectedRequest = new PlatformProxyTaskInfo();
        EnablerLogicDataAppearedMessage response = new EnablerLogicDataAppearedMessage();
        when(rabbitManager.sendRpcMessage(
            eq(props.getExchange().getEnablerPlatformProxy().getName()),
            eq(props.getKey().getEnablerPlatformProxy().getSingleReadRequested()),
            any(PlatformProxyTaskInfo.class))).thenReturn(response);

        // when
        EnablerLogicDataAppearedMessage readResponse = enablerLogic.readResource(expectedRequest);

        // then
        verify(rabbitManager).sendRpcMessage(eq(props.getExchange().getEnablerPlatformProxy().getName()),
            eq(props.getKey().getEnablerPlatformProxy().getSingleReadRequested()),
            captor.capture());
        PlatformProxyTaskInfo request = captor.getValue();
        assertThat(request).isSameAs(expectedRequest);
        assertThat(readResponse).isSameAs(response);
    }


    
    @Test
    public void registerAsyncMessageFromEnablerLogicConsumer_shouldDelegateItToConsumer() throws Exception {
        //given
        Consumer<String> lambda = (m) -> { };

        AsyncMessageFromEnablerLogicConsumer asyncConsumer = Mockito.mock(AsyncMessageFromEnablerLogicConsumer.class);
        enablerLogic.setAsyncConsumer(asyncConsumer);

        // when
        enablerLogic.registerAsyncMessageFromEnablerLogicConsumer(String.class, lambda);

        //then
        verify(asyncConsumer).registerReceiver(String.class, lambda);
    }

    @Test
    public void unregisterAsyncMessageFromEnablerLogicConsumer_shouldDelegateItToConsumer() throws Exception {
        //given
        AsyncMessageFromEnablerLogicConsumer asyncConsumer = Mockito.mock(AsyncMessageFromEnablerLogicConsumer.class);
        enablerLogic.setAsyncConsumer(asyncConsumer);

        // when
        enablerLogic.unregisterAsyncMessageFromEnablerLogicConsumer(String.class);

        //then
        verify(asyncConsumer).unregisterReceiver(String.class);
    }

    @Test
    public void sendingAsyncMessageToEnablerLogic_shouldCallRabbitManager() throws Exception {
        // given
        String message = "test message";
        String enablerName = "destEnablerName";

        // when
        enablerLogic.sendAsyncMessageToEnablerLogic(enablerName, message);

        // then
        verify(rabbitManager).sendMessage(eq("symbIoTe.enablerLogic"),
            eq("symbIoTe.enablerLogic.asyncMessageToEnablerLogic.destEnablerName"),
            eq((Object) message));
    }

    @AllArgsConstructor
    public static class ReceivedMessage {
        @Getter
        private String receivedText;
    }

    @Test
    public void sendingSyncMessageToEnablerLogic_shouldCallRabbitManagerAndReturnResponse() throws Exception {
        // given
        String sendMessage = "test message";
        String enablerName = "destEnablerName";
        ReceivedMessage mockReceiveMessage = new ReceivedMessage("received message");

        when(rabbitManager.sendRpcMessage("symbIoTe.enablerLogic",
            "symbIoTe.enablerLogic.syncMessageToEnablerLogic.destEnablerName",
            (Object) sendMessage)).thenReturn(mockReceiveMessage);

        // when
        ReceivedMessage receivedMessage = enablerLogic.sendSyncMessageToEnablerLogic(enablerName, sendMessage, ReceivedMessage.class);

        // then
        assertThat(receivedMessage).isSameAs(mockReceiveMessage);
    }

    @Test
    public void sendingSyncMessageToEnablerLogicAndExpectingVoid_shouldCallRabbitManagerAndReturnVoidResponse() throws Exception {
        // given
        String sendMessage = "test message";
        String enablerName = "destEnablerName";
        VoidResponse mockReceiveMessage = new VoidResponse();

        when(rabbitManager.sendRpcMessage("symbIoTe.enablerLogic",
            "symbIoTe.enablerLogic.syncMessageToEnablerLogic.destEnablerName",
            (Object) sendMessage)).thenReturn(mockReceiveMessage);

        // when
        VoidResponse receivedMessage = enablerLogic.sendSyncMessageToEnablerLogic(enablerName, sendMessage, VoidResponse.class);

        // then
        assertThat(receivedMessage).isSameAs(mockReceiveMessage);
    }

    @Test
    public void sendingSyncMessageToEnablerLogic_whenTimeout_shouldCallRabbitManagerAndReturnNull() throws Exception {
        // given
        String sendMessage = "test message";
        String enablerName = "enabler name";

        when(rabbitManager.sendRpcMessage("symbIoTe.enablerLogic",
            "symbIoTe.enablerLogic.syncMessageToEnablerLogic.DefaultEnablerName",
            (Object) sendMessage)).thenReturn(null);

        // when
        ReceivedMessage receivedMessage = enablerLogic.sendSyncMessageToEnablerLogic(enablerName, sendMessage, ReceivedMessage.class);

        // then
        assertThat(receivedMessage).isNull();
    }

    @Test
    public void sendingSyncMessageToEnablerLogic_whenExpectingVoid_shouldCallRabbitManagerAndReturnNull() throws Exception {
        // given
        String sendMessage = "test message";
        String enablerName = "enabler name";
        
        when(rabbitManager.sendRpcMessage("symbIoTe.enablerLogic",
                "symbIoTe.enablerLogic.syncMessageToEnablerLogic.DefaultEnablerName",
                (Object) sendMessage)).thenReturn(null);
        
        // when
        Void response = enablerLogic.sendSyncMessageToEnablerLogic(enablerName, sendMessage, Void.class);
        
        // then
        assertThat(response).isNull();
    }
    
    @Test
    public void sendingSyncMessageToEnablerLogic_whenReturnedWrongObjectType_shouldCallRabbitManagerAndThrowException() throws Exception {
        // given
        String sendMessage = "test message";
        String enablerName = "destEnablerName";

        when(rabbitManager.sendRpcMessage("symbIoTe.enablerLogic",
            "symbIoTe.enablerLogic.syncMessageToEnablerLogic.destEnablerName",
            (Object) sendMessage)).thenReturn(Long.valueOf(1));

        assertThatThrownBy(() -> {
            // when
            enablerLogic.sendSyncMessageToEnablerLogic(enablerName, sendMessage, ReceivedMessage.class);
        })
        
            // then
            .isInstanceOf(WrongResponseException.class)
            .hasFieldOrPropertyWithValue("response", Long.valueOf(1))
            .hasNoCause();
    }

    @Test
    public void registerSyncMessageFromEnablerLogicConsumer_shouldDelegateItToConsumer() throws Exception {
        //given
        Function<String, String> lambda = (m) -> m;

        SyncMessageFromEnablerLogicConsumer syncConsumer = Mockito.mock(SyncMessageFromEnablerLogicConsumer.class);
        enablerLogic.setSyncConsumer(syncConsumer);

        // when
        enablerLogic.registerSyncMessageFromEnablerLogicConsumer(String.class, lambda);

        //then
        verify(syncConsumer).registerReceiver(String.class, lambda);
    }

    @Test
    public void unregisterSyncMessageFromEnablerLogicConsumer_shouldDelegateItInToConsumer() throws Exception {
        //given
        SyncMessageFromEnablerLogicConsumer syncConsumer = Mockito.mock(SyncMessageFromEnablerLogicConsumer.class);
        enablerLogic.setSyncConsumer(syncConsumer);

        // when
        enablerLogic.unregisterSyncMessageFromEnablerLogicConsumer(String.class);

        //then
        verify(syncConsumer).unregisterReceiver(String.class);
    }
}
