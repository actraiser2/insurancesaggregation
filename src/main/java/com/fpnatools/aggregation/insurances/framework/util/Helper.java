package com.fpnatools.aggregation.insurances.framework.util;

import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class Helper {

	public static void  main(String... args) {
		Disposable d = Observable.range(1, 100).
			groupBy(i -> i % 2 ==0).
			subscribeOn(Schedulers.computation()).
			subscribe(i -> {
				System.out.println(i.getKey() + " => " + i.blockingFirst());
				
			}, Throwable::fillInStackTrace);
		
		System.out.println(Thread.currentThread().getName() + " => Main thread finished:" + d.isDisposed());

		
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
