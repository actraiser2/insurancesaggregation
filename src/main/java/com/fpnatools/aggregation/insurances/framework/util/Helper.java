package com.fpnatools.aggregation.insurances.framework.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Helper {

	public static void  main(String... args) throws InterruptedException {
		Mono.just("jose").subscribeOn(Schedulers.boundedElastic()).
			subscribe(log::info);
		
		log.info("Main thread");
		Thread.sleep(10000);
	}
}
