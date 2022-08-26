package com.nttdata.bbva.operation.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICRUD<T, V> {

	Mono<T> insert(T obj);
	Mono<T> update(T obj);
	Flux<T> findAll();
	Mono<T> findById(V id);
	Mono<Void> delete(V id);
}
