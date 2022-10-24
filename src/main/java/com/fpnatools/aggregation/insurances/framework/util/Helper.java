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
		
		var a = "\u0031\u0032";
		var c = 0x1e;
		var b = 0x12;
		log.info(a);
		log.info(c + "");
		log.info(b + "");
	}
}
