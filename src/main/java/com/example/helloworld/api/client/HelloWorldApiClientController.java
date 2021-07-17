package com.example.helloworld.api.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("client/")
public class HelloWorldApiClientController {

	private final HelloWorldFeignClient feignClient;
	
	public HelloWorldApiClientController(HelloWorldFeignClient feignClient) {
		this.feignClient = feignClient;
	}

	@GetMapping("greet")
	public String greetMessage(@RequestParam(name = "name", required = false) String name, 
			@RequestParam(name = "error", required = false) Boolean error) {
		String greetingMessage = feignClient.greeting(name, error);
		return greetingMessage;
	}
}
