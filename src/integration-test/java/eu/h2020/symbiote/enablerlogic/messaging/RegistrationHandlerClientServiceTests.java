package eu.h2020.symbiote.enablerlogic.messaging;


import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import eu.h2020.symbiote.cloud.model.CloudResourceParams;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.core.model.WGS84Location;
import eu.h2020.symbiote.core.model.resources.FeatureOfInterest;
import eu.h2020.symbiote.core.model.resources.StationarySensor;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.EmbeddedRabbitFixture;
import eu.h2020.symbiote.enablerlogic.messaging.consumers.TestingRabbitConfig;

@RunWith(SpringRunner.class)
@Import({RegistrationHandlerClient.class, RegistrationHandlerClientService.class, TestingRabbitConfig.class})
@EnableFeignClients
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:integration.properties", properties = {
        "RegistrationHandler.ribbon.listOfServers=http://localhost:9001", 
        "ribbon.eureka.enabled=false"})
@AutoConfigureWireMock(port = 9001)
@DirtiesContext
public class RegistrationHandlerClientServiceTests extends EmbeddedRabbitFixture {
    
    @Autowired
    private RegistrationHandlerClientService service;
    
    @Test
    public void registerOneResource_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        CloudResource sendResource = createCloudResource1();

        CloudResource expetedResource = createCloudResource1();
        expetedResource.getResource().setId(expetedResource.getInternalId());
        
        wiremockStubPost("/resource", sendResource, expetedResource);

        // when
        List<CloudResource> registeredResources = service.registerResource(sendResource);

        //then
        assertThat(registeredResources).hasSize(1);
        assertThat(registeredResources.get(0)).isEqualToComparingOnlyGivenFields(sendResource, "internalId");
        assertThat(registeredResources.get(0).getResource().getId()).isNotNull();
    }

    @Test
    public void registerAlreadyRegisteredResource_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        CloudResource sendResource = createCloudResource1();
        
        CloudResource expetedResource = createCloudResource1();
        expetedResource.getResource().setId(expetedResource.getInternalId());
        
        wiremockStubPost("/resource", sendResource, expetedResource);
        
        // when
        List<CloudResource> registeredResources = service.registerResource(sendResource);
        
        //then
        assertThat(registeredResources).hasSize(1);
    }
    
    @Test
    public void registeringWrongResourceObject_shouldReturnEmptyList() throws Exception {
        //given
        CloudResource sendResource = createCloudResource1();
        sendResource.setInternalId(null);
        
        wiremockStubPost("/resource", sendResource, new LinkedList<>());

        // when
        List<CloudResource> registeredResources = service.registerResource(sendResource);

        //then
        assertThat(registeredResources).isEmpty();
    }
    
    @Test
    public void unregisteringResource_shouldReturnListOfunregisteredResources() throws Exception {
        //given
        CloudResource expetedResource = createCloudResource1();
        expetedResource.getResource().setId(expetedResource.getInternalId());
        
        wiremockStubDelete("/resource", "testId1", Arrays.asList(expetedResource));
        
        // when
        List<CloudResource> unregisteredResources = service.unregisterResource("testId1");
        
        //then
        assertThat(unregisteredResources).hasSize(1);
        assertThat(unregisteredResources.get(0).getInternalId()).isEqualTo("testId1");
    }
    
    @Test
    public void unregisteringNotRegisteredResource_shouldReturnEmptyList() throws Exception {
        //given
        wiremockStubDelete("/resource", "testId1", new LinkedList<>());
        
        // when
        List<CloudResource> unregisteredResources = service.unregisterResource("testId1");
        
        //then
        assertThat(unregisteredResources).isEmpty();
    }
    
    @Test
    public void registerMoreResources_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        List<CloudResource> sendResources = createResources();
        
        List<CloudResource> expetedResources = createResources();
        expetedResources.get(0).getResource().setId(expetedResources.get(0).getInternalId());
        expetedResources.get(1).getResource().setId(expetedResources.get(1).getInternalId());
        
        wiremockStubPost("/resources", sendResources, expetedResources);

        // when
        List<CloudResource> registeredResources = service.registerResources(sendResources);

        //then
        assertThat(registeredResources)
            .hasSize(2)
            .extracting("internalId", "resource.id").contains(
                    tuple("testId1", "testId1"),
                    tuple("testId2", "testId2")
            );
    }

    @Test
    public void registerAlreadyRegisteredResources_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        List<CloudResource> sendResources = createResources();
        
        List<CloudResource> expetedResources = createResources();
        expetedResources.get(0).getResource().setId(expetedResources.get(0).getInternalId());
        expetedResources.get(1).getResource().setId(expetedResources.get(1).getInternalId());
        
        wiremockStubPost("/resources", sendResources, expetedResources);
        
        // when
        List<CloudResource> registeredResources = service.registerResources(sendResources);
        
        //then
        assertThat(registeredResources)
            .hasSize(2)
            .extracting("internalId", "resource.id").contains(
                    tuple("testId1", "testId1"),
                    tuple("testId2", "testId2")
            );
    }
    
    @Test
    public void registerWrongResource0_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        List<CloudResource> sendResources = createResources();
        sendResources.get(0).setInternalId(null);
        
        List<CloudResource> expetedResources = Arrays.asList(createCloudResource2());
        expetedResources.get(0).getResource().setId(expetedResources.get(0).getInternalId());
        
        wiremockStubPost("/resources", sendResources, expetedResources);
        
        
        // when
        List<CloudResource> registeredResources = service.registerResources(sendResources);
        
        //then
        assertThat(registeredResources)
            .hasSize(1)
            .extracting("internalId", "resource.id").contains(
                    tuple("testId2", "testId2")
            );
    }
    
    @Test
    public void unregisterResources_shouldReturnListOfUnregisteredResources() throws Exception {
        //given
        List<CloudResource> expectedResources = createResources();

        List<String> resourceIds = expectedResources.stream()
                .map(r -> r.getInternalId())
                .collect(Collectors.toList());
        
        wiremockStubDelete("/resources", resourceIds, expectedResources);

        // when
        List<CloudResource> registeredResources = service.unregisterResources(resourceIds);
        
        //then
        assertThat(registeredResources)
            .hasSize(2)
            .extracting("internalId").contains("testId1", "testId2");
    }

    @Test
    public void unregisterResourcesThatAreNotRegistered_shouldReturnListOfUnregisteredResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        List<String> resourceIds = cloudResources.stream()
                .map(r -> r.getInternalId())
                .collect(Collectors.toList());
        
        wiremockStubDelete("/resources", resourceIds, new LinkedList<>());

        // when
        List<CloudResource> registeredResources = service.unregisterResources(resourceIds);
        
        //then
        assertThat(registeredResources).isEmpty();
    }
    
    @Test
    public void updateResource_shouldReturnListOfUpdatedResources() throws Exception {
        //given
        CloudResource cloudResource = createCloudResource1();
        cloudResource.setPluginId("new plugin id");
        
        wiremockStubPut("/resource", cloudResource, Arrays.asList(cloudResource));
        
        // when
        List<CloudResource> registeredResources = service.updateResource(cloudResource);
        
        //then
        assertThat(registeredResources)
            .hasSize(1)
            .extracting("internalId", "pluginId").contains(tuple("testId1", "new plugin id"));
    }
    
    @Test
    public void updateResources_shouldReturnListOfUpdatedResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        cloudResources.get(0).setPluginId("new plugin id");

        wiremockStubPut("/resources", cloudResources, cloudResources);
        
        // when
        List<CloudResource> registeredResources = service.updateResources(cloudResources);
        
        //then
        assertThat(registeredResources)
            .hasSize(2)
            .extracting("internalId", "pluginId").contains(
                tuple("testId1", "new plugin id"),
                tuple("testId2", "testPlugin")
            );
    }
    
    private void wiremockStubPost(String url, Object sendResource, Object expetedResource)
            throws IOException, JsonGenerationException, JsonMappingException {
        stubFor(post(urlEqualTo(url))
                .withRequestBody(resourceEqualToJson(sendResource))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(convertToJson(expetedResource))));
    }

    private void wiremockStubPut(String url, Object sendResource, Object expetedResource)
            throws IOException, JsonGenerationException, JsonMappingException {
        stubFor(put(urlEqualTo(url))
                .withRequestBody(resourceEqualToJson(sendResource))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(convertToJson(expetedResource))));
    }
    
    private void wiremockStubDelete(String url, String sendResource, Object expetedResource)
            throws IOException, JsonGenerationException, JsonMappingException {
        stubFor(delete(urlPathMatching(url))
                .withQueryParam("resourceInternalId", equalTo(sendResource))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(convertToJson(expetedResource))));
    }
    
    private void wiremockStubDelete(String url, List<String> sendResource, Object expetedResource)
            throws IOException, JsonGenerationException, JsonMappingException {
        stubFor(delete(urlPathMatching(url))
                //.withQueryParam("resourceInternalId", equalTo(String.join("%2C", sendResource)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(convertToJson(expetedResource))));
    }
    
    private StringValuePattern resourceEqualToJson(Object resource)
            throws IOException, JsonGenerationException, JsonMappingException {
        return equalToJson(convertToJson(resource));
    }

    private String convertToJson(Object resource)
            throws IOException, JsonGenerationException, JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, resource);
        String body = writer.toString();
        return body;
    }
    
    private List<CloudResource> createResources() {
        return Arrays.asList(createCloudResource1(), createCloudResource2());
    }
    
    private CloudResource createCloudResource2() {
        CloudResource r = createCloudResource1();
        r.setInternalId("testId2");
        return r;
    }
    
    private CloudResource createCloudResource1() {
        CloudResource resource = new CloudResource();
        resource.setInternalId("testId1");
        resource.setPluginId("testPlugin");
        resource.setCloudMonitoringHost("cloudMonitoringHostIP");
        StationarySensor sensor = new StationarySensor();
        resource.setResource(sensor);
        sensor.setLabels(Arrays.asList("lamp"));
        sensor.setComments(Arrays.asList("A comment"));
        sensor.setInterworkingServiceURL("https://symbiote-h2020.eu/example/interworkingService/");
        sensor.setLocatedAt(new WGS84Location(2.349014, 48.864716, 15, 
                Arrays.asList("Paris"), 
                Arrays.asList("This is Paris")));
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();
        sensor.setFeatureOfInterest(featureOfInterest);
        featureOfInterest.setLabels(Arrays.asList("Room1"));
        featureOfInterest.setComments(Arrays.asList("This is room 1"));
        featureOfInterest.setHasProperty(Arrays.asList("temperature"));
        sensor.setObservesProperty(Arrays.asList("temperature,humidity".split(",")));
        CloudResourceParams cloudResourceParams = new CloudResourceParams();
        resource.setParams(cloudResourceParams);
        cloudResourceParams.setType("Type of device, used in monitoring");
        return resource;
    }

}
