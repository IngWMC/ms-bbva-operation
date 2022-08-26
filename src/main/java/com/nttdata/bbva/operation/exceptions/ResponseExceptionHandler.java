package com.nttdata.bbva.operation.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@ControllerAdvice
@RestController
public class ResponseExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ResponseExceptionHandler.class);

    @ExceptionHandler(ModelNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> manejarModeloException(ModelNotFoundException ex, WebRequest request){
        logger.info("ERROR NOT_FOUND ::: " + ex);
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<ExceptionResponse> manejarModelBadRequest(BadRequestException ex, WebRequest request){
    	logger.info("ERROR BAD_REQUEST ::: " + ex);
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ExceptionResponse> badRequest(MethodArgumentNotValidException ex, WebRequest request, BindingResult bindingResult){
    	StringJoiner errors = new StringJoiner(" / ");
    	BindingResult result = ex.getBindingResult();
    	result.getFieldErrors().forEach(e -> {
    		errors.add(e.getDefaultMessage());
    	});
    	logger.info("ERROR BAD_REQUEST ::: " + ex);
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), errors.toString(), request.getDescription(false));
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.BAD_REQUEST);
    }
    
    
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> manejarTodasExcepciones(Exception ex, WebRequest request){
    	logger.info("ERROR INTERNAL_SERVER_ERROR ::: " + ex);
        ExceptionResponse er = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<ExceptionResponse>(er, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
