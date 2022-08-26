package com.nttdata.bbva.operation.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
	private String id;
	private String name;
	private String shortName;
	private String productTypeId;
	private ProductType productType;
	private String isMonthlyMaintenance; // Libre de mantenimiento mensual
	private Integer maximumLimitMonthlyMovements; // Límite máximo de movimientos mensuales
}
