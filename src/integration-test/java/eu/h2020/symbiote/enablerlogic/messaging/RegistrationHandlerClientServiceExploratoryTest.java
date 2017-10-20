package eu.h2020.symbiote.enablerlogic.messaging;


import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.cloud.model.CloudResourceParams;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.core.model.WGS84Location;
import eu.h2020.symbiote.core.model.resources.FeatureOfInterest;
import eu.h2020.symbiote.core.model.resources.StationarySensor;

@Ignore // this is exploratory test not intended for running as integration test
@RunWith(SpringRunner.class)
@Import({RegistrationHandlerClient.class, RegistrationHandlerClientService.class})
@EnableFeignClients
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:integration.properties")
public class RegistrationHandlerClientServiceExploratoryTest { // this tests real RH
    
    @Autowired
    private RegistrationHandlerClient client;
    
    @Autowired
    private RegistrationHandlerClientService service;
    
    @Before
    public void setup() throws Exception {
        List<CloudResource> registeredResources = client.getAllResources();
        List<String> internalIds = registeredResources.stream()
            .map((cr) -> cr.getInternalId())
            .collect(Collectors.toList());
        if(!internalIds.isEmpty())
            service.unregisterResources(internalIds);
    }
    
    @Test
    public void registerOneResource_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        CloudResource cloudResource1 = createCloudResource1();

        // when
        List<CloudResource> registeredResources = service.registerResource(cloudResource1);

        //then
        assertThat(registeredResources).hasSize(1);
        assertThat(registeredResources.get(0)).isEqualToComparingOnlyGivenFields(cloudResource1, "internalId");
        assertThat(registeredResources.get(0).getResource().getId()).isNotNull();
    }

    @Test
    public void registerAlreadyRegisteredResource_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        CloudResource cloudResource1 = createCloudResource1();
        service.registerResource(cloudResource1);
        
        // when
        List<CloudResource> registeredResources = service.registerResource(cloudResource1);
        
        //then
        assertThat(registeredResources).hasSize(1);
    }
    
    @Test
    public void registeringWrongResourceObject_shouldReturnEmptyList() throws Exception {
        //given
        CloudResource cloudResource1 = createCloudResource1();
        cloudResource1.setInternalId(null);
        
        // when
        List<CloudResource> registeredResources = service.registerResource(cloudResource1);

        //then
        assertThat(registeredResources).isEmpty();
    }
    
    @Test
    public void unregisteringResource_shouldReturnListOfunregisteredResources() throws Exception {
        //given
        CloudResource cloudResource1 = createCloudResource1();
        service.registerResource(cloudResource1);
        
        // when
        List<CloudResource> unregisteredResources = service.unregisterResource("testId1");
        
        //then
        assertThat(unregisteredResources).hasSize(1);
        assertThat(unregisteredResources.get(0).getInternalId()).isEqualTo("testId1");
    }
    
    @Test
    public void unregisteringNotRegisteredResource_shouldReturnEmptyList() throws Exception {
        //given
        CloudResource cloudResource1 = createCloudResource1();
        
        // when
        List<CloudResource> unregisteredResources = service.unregisterResource("testId1");
        
        //then
        assertThat(unregisteredResources).isEmpty();
    }
    
    @Test
    public void registerMoreResources_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();

        // when
        List<CloudResource> registeredResources = service.registerResources(cloudResources);

        //then
        assertThat(registeredResources)
            .hasSize(2)
            .extracting("internalId").contains("testId1", "testId2");
    }

    @Test
    public void registerAlreadyRegisteredResources_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        service.registerResources(cloudResources);
        
        // when
        List<CloudResource> registeredResources = service.registerResources(cloudResources);
        
        //then
        assertThat(registeredResources)
            .hasSize(2)
            .extracting("internalId").contains("testId1", "testId2");
    }
    
    @Test
    public void registerWrongResource0_shouldReturnListOfRegisteredResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        cloudResources.get(0).setInternalId(null);
        
        // when
        List<CloudResource> registeredResources = service.registerResources(cloudResources);
        
        //then
        assertThat(registeredResources)
            .hasSize(1)
            .extracting("internalId").contains("testId2");
    }
    
    @Test
    public void unregisterResources_shouldReturnListOfUnregisteredResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        service.registerResources(cloudResources);
        List<String> resourceIds = cloudResources.stream()
                .map(r -> r.getInternalId())
                .collect(Collectors.toList());
        
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
        
        // when
        List<CloudResource> registeredResources = service.unregisterResources(resourceIds);
        
        //then
        assertThat(registeredResources).isEmpty();
    }
    
    @Test
    public void updateResource_shouldReturnListOfUpdatedResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        service.registerResources(cloudResources);
        cloudResources.get(0).setPluginId("new plugin id");
        
        // when
        List<CloudResource> registeredResources = service.updateResource(cloudResources.get(0));
        
        //then
        assertThat(registeredResources)
            .hasSize(1)
            .extracting("internalId", "pluginId").contains(tuple("testId1", "new plugin id"));
    }
    
    @Test
    public void updateResources_shouldReturnListOfUpdatedResources() throws Exception {
        //given
        List<CloudResource> cloudResources = createResources();
        service.registerResources(cloudResources);
        cloudResources.get(0).setPluginId("new plugin id");
        
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
