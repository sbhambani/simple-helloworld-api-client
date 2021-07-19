package com.example.helloworld.api.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(properties = {"eureka.client.enabled=false", "feign.circuitbreaker.enabled=true",
        "feign.hystrix.enabled=true"},
    webEnvironment = WebEnvironment.RANDOM_PORT)
class LoadBalancedHelloWorldFeignClientTest {
    private static WireMockServer wireMockServer;

    @Autowired
    private HelloWorldFeignClient helloWorldFeignClient;

    @Autowired
    private SimpleDiscoveryProperties simpleDiscoveryProperties;

    @BeforeEach
    void addServiceInstance() {
        DefaultServiceInstance defaultServiceInstance = new DefaultServiceInstance();
        defaultServiceInstance.setServiceId("simple-helloworld-api");
        defaultServiceInstance.setHost("localhost");
        defaultServiceInstance.setPort(wireMockServer.port());
        Map<String, List<DefaultServiceInstance>> instances = new HashMap<>();
        instances.put("simple-helloworld-api", Collections.singletonList(defaultServiceInstance));
        simpleDiscoveryProperties.setInstances(instances);
    }

    @AfterEach
    void removeServiceInstance() {
        simpleDiscoveryProperties.getInstances().clear();
    }

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
        wireMockServer = null;
    }

    @Test
    void testHelloWorldFeign_Success_ReturnGreetingMessage() {
        //Arrange
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/greet/Test"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withBody("Hello!! Test")));

        //Act
        String message = helloWorldFeignClient.greeting("Test", false);

        //Assert
        assertThat(message).isNotNull();
    }

    @Test
    void testHelloWorldFeign_Fail_ReturnNull() {
        DefaultServiceInstance defaultServiceInstance = new DefaultServiceInstance();
        defaultServiceInstance.setServiceId("simple-helloworld-api");
        defaultServiceInstance.setHost("localhost");
        defaultServiceInstance.setPort(wireMockServer.port());
        Map<String, List<DefaultServiceInstance>> instances = new HashMap<>();
        instances.put("simple-helloworld-api", Collections.singletonList(defaultServiceInstance));
        simpleDiscoveryProperties.setInstances(instances);
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/greet/Test"))
                .willReturn(aResponse().withStatus(501)));

        String message = helloWorldFeignClient.greeting("Test", true);

        //Hystrix fallback implementation will return null
        assertThat(message).isNull();
    }


    /*@EnableFeignClients(clients = HelloWorldFeignClient.class)
    @Configuration
    @EnableAutoConfiguration*/
    /*static class FeignConfig {

        @Bean
        public HelloWorldFeignClientFallbackFactory helloWorldFeignClientFallbackFactory() {
            return new HelloWorldFeignClientFallbackFactory();
        }
    }*/
}
