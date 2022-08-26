package com.nttdata.bbva.operation.services;

import com.nttdata.bbva.operation.documents.Operation;
import reactor.core.publisher.Flux;

public interface IOperationService extends ICRUD<Operation, String> {
	Flux<Operation> findByOpenAccountId(String openAccountId);
}
