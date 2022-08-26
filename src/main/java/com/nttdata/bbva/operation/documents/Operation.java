package com.nttdata.bbva.operation.documents;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "operations")
public class Operation {
	@Id
	private String id;
	@NotEmpty(message = "El campo openAccountId es requerido.")
	private String openAccountId;
	@NotEmpty(message = "El campo operationType es requerido.")
	private String operationType; // Depósito: D, Retiro: R, Pagos: P
	private OpenAccount openAccount;
	@DecimalMin(value = "0.0", message = "El campo amount debe tener un valor mínimo de '0.0'.")
	@Digits(integer = 10, fraction = 3, message = "El campo amount tiene un formato no válido (#####.000).")
	@NotNull(message = "El campo amount es requerido.")
	private BigDecimal amount;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime createdAt;
}
