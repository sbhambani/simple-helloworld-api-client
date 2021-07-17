package com.example.helloworld.api.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(classes = {HelloWorldFeignClientTest.FeignConfig.class},
        properties = {"eureka.client.enabled=false", "feign.circuitbreaker.enabled=true"},
    webEnvironment = WebEnvironment.RANDOM_PORT)
class LoadBalancedHelloWorldFeignClientTest {
    private static WireMockServer wireMockServer;

    @Autowired
    private HelloWorldFeignClient helloWorldFeignClient;

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
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/greet/.*"))
                .willReturn(aResponse().withStatus(501)));

        String message = helloWorldFeignClient.greeting("Sunil", true);

        //Hystrix fallback implementation will return null
        assertThat(message).isNull();
    }

    @EnableFeignClients(clients = HelloWorldFeignClient.class)
    @Configuration
    @EnableAutoConfiguration
    @RibbonClient(name = "simple-helloworld-api", configuration = LoadBalancedHelloWorldFeignClientTest.RibbonConfig.class)
    static class FeignConfig {

        @Bean
        public HelloWorldFeignClientFallbackFactory helloWorldFeignClientFallbackFactory() {
            return new HelloWorldFeignClientFallbackFactory();
        }
    }

    @Configuration
    static class RibbonConfig {

        @Bean
        public ServerList<Server> serverList() {
            return new StaticServerList<>(new Server("localhost", wireMockServer.port()));
        }
    }
}
