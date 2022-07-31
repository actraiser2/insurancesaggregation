package com.fpnatools.aggregation.insurances.framework.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
				//System.out.println(i.getKey() + " => " + i.blockingFirst());
				
			}, Throwable::fillInStackTrace);
		
		System.out.println(Thread.currentThread().getName() + " => Main thread finished:" + d.isDisposed());

		
		var list = new ArrayList<Integer>(List.of(1, 2, 3, 4, 5));
		list.replaceAll(i -> i +1);
		list.removeIf(i -> i % 2 == 0);
		System.out.println(list);
		
		Map<String, Integer> map = new HashMap<String, Integer>(Map.of("jose",  4, "alex", 4, "jesus", 5));
		//map.computeIfAbsent("Daniel", k -> k.length());
		map.merge("Daniel", 6, (v1, v2) -> {
			return v1 + v2;
		});
		System.out.println(map);
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
