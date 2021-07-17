package com.example.helloworld.api.client;

import org.springframework.context.annotation.Bean;

import feign.Logger;
import feign.Logger.Level;

public class HelloWorldFeignClientConfiguration {

	@Bean
	public Logger.Level feignLoggerLevel() {
		return Level.FULL; 
	}
}
