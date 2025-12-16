package com.ecommerce.project.exception;

import com.ecommerce.project.payload.APIResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class MyGlobalExceptionHandler {



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        Map<String,String> errors = new LinkedHashMap<>();

        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError)err).getField();
            String message = err.getDefaultMessage();
            errors.put(fieldName,message);
        });

        Map<String, Object> responses = new LinkedHashMap<>();
        responses.put("status", 400);
        responses.put("errors", errors);

        return new ResponseEntity<Map<String, Object>>(responses, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        String message = e.getMessage();
        APIResponse apiResponse = new APIResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String param = violation.getPropertyPath().toString();
                    String message = violation.getMessage();
                    return "Parametar '" + param + "': " + message;
                })
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body("Neispravni parametri: " + String.join("; ", errors));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body("Neispravni parametri: __" + ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String message = "Parametar '" + param + "' mora biti tipa " + type;

        return ResponseEntity.badRequest().body(Map.of("error", message));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // ovdje možeš parsirati exception poruku ako želiš detalje

        return ResponseEntity.badRequest().body("Greška spremanja u bazu !!!");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        Throwable root = ex.getMostSpecificCause();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");


        body.put("message", "Neispravan format JSON-a ili tip podataka.");


        if (root instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            String field = ife.getPath().isEmpty() ? null : ife.getPath().get(0).getFieldName();
            Object value = ife.getValue();
            Class<?> targetType = ife.getTargetType();

            if (field != null) body.put("field", field);


            String expected = (targetType != null) ? targetType.getSimpleName() : "ispravan tip";
            body.put("message", "Polje '" + field + "' mora biti tipa " + expected + ", ali je poslano: " + value);
        }

        return ResponseEntity.badRequest().body(body);
    }

}
