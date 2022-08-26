package com.nttdata.bbva.operation.documents;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAccount {
	private String id;
	private String customerId;
	private String productId;
	private BigDecimal amountAvailable; // Monto disponible
	private BigDecimal creditLine; // Monto de la linea de crédito (Tarjeta de crédito)
}
