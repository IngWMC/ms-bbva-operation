package com.nttdata.bbva.operation.controllers;

import com.nttdata.bbva.operation.documents.Operation;
import com.nttdata.bbva.operation.services.IOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("api/1.0.0/operations")
public class OperationController {
	private static final Logger logger = LoggerFactory.getLogger(OperationController.class);
	@Autowired
	private IOperationService service;

	@PostMapping
	public Mono<ResponseEntity<Mono<Operation>>> insert(@Valid @RequestBody Operation obj){
		logger.info("Inicio OperationController ::: insert");
		Mono<Operation> operation = service.insert(obj).doOnNext(x -> logger.info("Fin OperationController ::: insert"));
		return Mono.just(new ResponseEntity<Mono<Operation>>(operation, HttpStatus.CREATED));
	}

	@GetMapping("/openaccounts/{openAccountId}")
	public Mono<ResponseEntity<Flux<Operation>>> findByOpenAccountId(@PathVariable("openAccountId") String openAccountId){
		logger.info("Inicio OperationController ::: findByOpenAccountId ::: " + openAccountId);
		Flux<Operation> operations = service.findByOpenAccountId(openAccountId).doOnNext(x -> logger.info("Fin OperationController ::: findByOpenAccountId"));
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(operations));
	}

}
