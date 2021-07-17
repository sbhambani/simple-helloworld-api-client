package com.example.helloworld.api.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(classes = {HelloWorldFeignClientTest.FeignConfig.class},
    properties = {"eureka.client.enabled=false", "feign.circuitbreaker.enabled=true"})
class HelloWorldFeignClientTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private HelloWorldFeignClient helloWorldFeignClient;

    @BeforeAll
    static void setUp() {
        //wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(9090));
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
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/greet/Sunil"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withBody("Hello World!! Sunil")));

        //Act
        String message = helloWorldFeignClient.greeting("Sunil", false);

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

    //@TestConfiguration
    @EnableFeignClients
    @Configuration
    @EnableAutoConfiguration
    static class FeignConfig {

        @Bean
        public HelloWorldFeignClientFallbackFactory helloWorldFeignClientFallbackFactory() {
            return new HelloWorldFeignClientFallbackFactory();
        }
    }

    /*@FeignClient(name = "testHelloWorldFeignClient",
            configuration = HelloWorldFeignClientConfiguration.class,
            fallbackFactory = HelloWorldFeignClientFallbackFactory.class, url = "http://localhost:9090/")
    interface TestHelloWorldFeignClient {

        @GetMapping("${helloWorldApi.baseUrl}" + "greet/{name}")
        String greeting(@PathVariable("name") String name, @RequestParam(name = "error", required = false) Boolean error);
    }*/
}
