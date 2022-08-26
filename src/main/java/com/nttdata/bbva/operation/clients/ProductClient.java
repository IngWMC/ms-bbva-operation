package com.nttdata.bbva.operation.clients;

import com.nttdata.bbva.operation.documents.Product;
import com.nttdata.bbva.operation.exceptions.ModelNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProductClient {
    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);
    private final WebClient webClient;

    public ProductClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("http://localhost:7072/api/1.0.0/products").build();
    }

    @CircuitBreaker(name = "product", fallbackMethod = "fallBackFindById")
    public Mono<Product> findById(String id){
        logger.info("Inicio ProductClient ::: findById ::: " + id);
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnNext(x -> logger.info("Fin ProductClient ::: findById"));
    }

    private Mono<String> fallBackFindById(String id, RuntimeException e) {
        return Mono.error(() -> new ModelNotFoundException("Microservicio Product no está repondiendo."));
    }
}
