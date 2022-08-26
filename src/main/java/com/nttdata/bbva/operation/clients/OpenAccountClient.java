package com.nttdata.bbva.operation.clients;

import com.nttdata.bbva.operation.documents.OpenAccount;
import com.nttdata.bbva.operation.exceptions.ModelNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenAccountClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAccountClient.class);
    private final WebClient webClient;

    public OpenAccountClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("http://localhost:7073/api/1.0.0/openaccounts").build();
    }

    @CircuitBreaker(name = "openaccount", fallbackMethod = "fallBackFindById")
    public Mono<OpenAccount> findById(String id){
        logger.info("Inicio OpenAccountClient ::: findById ::: " + id);
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(OpenAccount.class)
                .doOnNext(x -> logger.info("Fin OpenAccountClient ::: findAll"));
    }
    @CircuitBreaker(name = "openaccount", fallbackMethod = "fallBackUpdate")
    public Mono<OpenAccount> update(OpenAccount obj){
        logger.info("Inicio OpenAccountClient ::: update ::: " + obj);
        return this.webClient.put()
                .uri("/")
                .body(Mono.just(obj), OpenAccount.class)
                .retrieve()
                .bodyToMono(OpenAccount.class)
                .doOnNext(x -> logger.info("Fin OpenAccountClient ::: update"));
    }

    private Mono<String> fallBackFindById(String id, RuntimeException e) {
        return Mono.error(() -> new ModelNotFoundException("Microservicio OpenAccount no está repondiendo."));
    }
    private Mono<String> fallBackUpdate(OpenAccount obj, RuntimeException e) {
        return Mono.error(() -> new ModelNotFoundException("Microservicio OpenAccount no está repondiendo."));
    }
}
