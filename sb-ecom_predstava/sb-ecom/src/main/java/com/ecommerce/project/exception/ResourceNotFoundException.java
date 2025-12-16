package com.ecommerce.project.exception;

public class ResourceNotFoundException extends RuntimeException {
    String resourceName;
    String field;
    String fieldName;
    Long filedId;

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String resourceName, String field, String fieldName) {
        super(String.format("%s not found with %s: %s", resourceName, field, fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }

    public ResourceNotFoundException( String resourceName, String field, Long filedId ) {
        super(String.format("%s not found with %s: %d", resourceName, field, filedId));
        this.filedId = filedId;
        this.field = field;
        this.resourceName = resourceName;
    }
}
