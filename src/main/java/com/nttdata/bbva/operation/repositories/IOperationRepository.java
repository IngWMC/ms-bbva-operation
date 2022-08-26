package com.nttdata.bbva.operation.repositories;

import com.nttdata.bbva.operation.documents.Operation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IOperationRepository extends ReactiveMongoRepository<Operation, String> {}
