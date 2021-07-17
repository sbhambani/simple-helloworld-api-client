package com.example.helloworld.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

//Configuration for HelloWorldFeignClientTest
/*@FeignClient(name = "${feign.name.helloWorldApi}", url = "http://localhost:9090/",
		configuration = HelloWorldFeignClientConfiguration.class,
		fallbackFactory = HelloWorldFeignClientFallbackFactory.class)*/

//Configuration for LoadBalancedHelloWorldFeignClientTest
@FeignClient(name = "${feign.name.helloWorldApi}",
		configuration = HelloWorldFeignClientConfiguration.class,
		fallbackFactory = HelloWorldFeignClientFallbackFactory.class)
public interface HelloWorldFeignClient {

	@GetMapping("${helloWorldApi.baseUrl}" + "greet/{name}")
	String greeting(@PathVariable("name") String name, @RequestParam(name = "error", required = false) Boolean error);
}
