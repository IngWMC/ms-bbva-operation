package com.nttdata.bbva.operation.services.impl;

import com.nttdata.bbva.operation.clients.OpenAccountClient;
import com.nttdata.bbva.operation.clients.ProductClient;
import com.nttdata.bbva.operation.documents.Operation;
import com.nttdata.bbva.operation.enums.OperationTypeEnum;
import com.nttdata.bbva.operation.enums.ProductEnum;
import com.nttdata.bbva.operation.exceptions.BadRequestException;
import com.nttdata.bbva.operation.exceptions.ModelNotFoundException;
import com.nttdata.bbva.operation.repositories.IOperationRepository;
import com.nttdata.bbva.operation.services.IOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class OperationServiceImpl implements IOperationService {
	private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);
	@Autowired
	private ProductClient productClient;
	@Autowired
	private OpenAccountClient openAccountClient;
	@Autowired
	private IOperationRepository repo;

	@Override
	public Mono<Operation> insert(Operation obj) {
		return openAccountClient.findById(obj.getOpenAccountId())
				.switchIfEmpty(Mono.error(() -> new BadRequestException("El campo openAccountId tiene un valor no válido.")))
				.flatMap(openAccount -> productClient.findById(openAccount.getProductId())
						.flatMap( product -> this.getTotalMovementsMadeInCurrentMonth(obj.getOpenAccountId(), obj.getOperationType(), product.getShortName())
								.flatMap( totalMovementsMadeInCurrentMonth -> {
									logger.info("El tipo operación es ::: " + obj.getOperationType());
									logger.info("El producto es ::: " + product.getShortName());

									obj.setCreatedAt(LocalDateTime.now());
									BigDecimal totalAmountAvailable;
									BigDecimal totalDebt;
									Long totalMovementsAllowed = product.getMaximumLimitMonthlyMovements().longValue();

									if (obj.getOperationType().equals(OperationTypeEnum.P.toString())) { // PAGO
										if (product.getProductType().getShortName().equals("CRE")) {
											if (product.getShortName().equals(ProductEnum.TCRE.toString())) {
												totalAmountAvailable = openAccount.getAmountAvailable().add(obj.getAmount());
												if (totalAmountAvailable.compareTo(openAccount.getCreditLine()) > 0) { // si totalAmountAvailable es MAYOR
													totalDebt = openAccount.getCreditLine().subtract(openAccount.getAmountAvailable());
													return Mono.error(() -> new BadRequestException("El monto abonado supera a la deuda de su tarjeta de crédito. Deuda total: " + totalDebt));
												} else if (openAccount.getAmountAvailable().compareTo(openAccount.getCreditLine()) == 0) {
													return Mono.error(() -> new BadRequestException("La tarjeta de crédito no tiene deuda."));
												} else
													openAccount.setAmountAvailable(totalAmountAvailable);
											} else {
												totalAmountAvailable = openAccount.getAmountAvailable().subtract(obj.getAmount());
												if (openAccount.getAmountAvailable().compareTo(totalAmountAvailable) > 0) { // si getAmountAvailable es MAYOR
													totalDebt = openAccount.getAmountAvailable();
													return Mono.error(() -> new BadRequestException("El monto abonado supera a la deuda de su línea de crédito. Deuda total: " + totalDebt));
												} else if (openAccount.getAmountAvailable().compareTo(new BigDecimal(0)) == 0) {
													return Mono.error(() -> new BadRequestException("La tarjeta de crédito no tiene deuda."));
												} else
													openAccount.setAmountAvailable(totalAmountAvailable);
											}
										}
									}
									else { // DEPÓSITO Y RETIRO
										totalAmountAvailable = obj.getOperationType().equals(OperationTypeEnum.D.toString())
												? openAccount.getAmountAvailable().add(obj.getAmount())
												: openAccount.getAmountAvailable().subtract(obj.getAmount());

										if (product.getShortName().equals(ProductEnum.CUEA.toString())) {
											if (totalMovementsAllowed.equals(totalMovementsMadeInCurrentMonth)) {
												return Mono.error(() -> new BadRequestException("El producto: '" + product.getName().toUpperCase() + "', ha superado el número de movimietos permitodos."));
											}
										} else if (product.getShortName().equals(ProductEnum.CUEPF.toString())) {
											if (totalMovementsMadeInCurrentMonth.intValue() == 1) {
												return Mono.error(() -> new BadRequestException("El producto:'" + product.getName().toUpperCase() + "', ha superado el número de movimietos permitodos."));
											}
										} else if (product.getShortName().equals(ProductEnum.TCRE.toString()) && obj.getOperationType().equals(OperationTypeEnum.D.toString())) {
											return Mono.error(() -> new BadRequestException("El producto: '" + product.getName().toUpperCase() + "', no tiene permitodo realizar 'DEPÓSITOS'."));
										}

										if (obj.getOperationType().equals(OperationTypeEnum.R.toString())) {
											if (totalAmountAvailable.compareTo(new BigDecimal(0)) < 0) // totalAmountAvailable es menor que cero
												return Mono.error(() -> new BadRequestException("El producto: '" + product.getName().toUpperCase() + "', no cuenta con el monto solicitado para realizar el 'RETIRO'."));
										}

										openAccount.setAmountAvailable(totalAmountAvailable);
									}

									return Mono.when(openAccountClient.update(openAccount), repo.save(obj)).then(Mono.just(obj));
								})
						)
				)
				.doOnNext(o -> logger.info("SE INSERTÓ LA OPERACIÓN ::: " + o.getId()));
	}

	@Override
	public Mono<Operation> update(Operation obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<Operation> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<Operation> findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Void> delete(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Flux<Operation> findByOpenAccountId(String openAccountId) {
		return repo.findAll()
				.filter(operations -> operations.getOpenAccountId().equals(openAccountId))
				.switchIfEmpty(Mono.error(() -> new ModelNotFoundException("OPERACIONES NO ENCONTRADOS ::: " + openAccountId)))
				.doOnNext(c -> logger.info("SE ENCONTRARÓN LAS OPERACIONES ::: " + openAccountId));
	}

	private Mono<Long> getTotalMovementsMadeInCurrentMonth(String openAccountId, String operationType, String productShortName) {
		logger.info("Inicio getTotalMovementsMadeInCurrentMonth");
		LocalDate currentDate = LocalDate.now();
		int currentYear = currentDate.getYear();
		int currentMonth = currentDate.getMonthValue();

		return this.findAll()
				.filter(operation -> operation.getOpenAccountId().equals(openAccountId))
				.filter(operation -> {
					if (productShortName.equals(ProductEnum.CUEPF.toString())) {
						return (operation.getCreatedAt().getYear() == currentYear
								&& operation.getCreatedAt().getMonthValue() == currentMonth) && operation.getOperationType().equals(operationType);
					} else {
						return operation.getCreatedAt().getYear() == currentYear
								&& operation.getCreatedAt().getMonthValue() == currentMonth;
					}
				})
				.count()
				.doOnNext(total -> logger.info("Total de movimientos realizados durante el mes actual: " + total));
	}
}
