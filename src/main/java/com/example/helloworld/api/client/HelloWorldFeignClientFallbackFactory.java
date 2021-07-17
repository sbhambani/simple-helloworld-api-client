package com.example.helloworld.api.client;

//import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class HelloWorldFeignClientFallbackFactory implements FallbackFactory<HelloWorldFeignClient> {
	
	private static final Logger logger = LoggerFactory.getLogger(HelloWorldFeignClientFallbackFactory.class);
	
	private void logFailureCause(Throwable cause) {
		String errorMessage = (cause == null) ? "No cause returned" : cause.getMessage();
        logger.debug("HelloWorldFeignClient fallback called. Cause: {}", errorMessage);
	}

	@Override
	public HelloWorldFeignClient create(Throwable cause) {
		return new HelloWorldFeignClient() {

			@Override
			public String greeting(@PathVariable("name") String name,
					@RequestParam(name = "error", required = false) Boolean error) {
				logFailureCause(cause);
				return null;
			}
			
		};
	}

}
