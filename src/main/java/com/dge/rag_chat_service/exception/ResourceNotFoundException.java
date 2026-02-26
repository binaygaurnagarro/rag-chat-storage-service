package com.dge.rag_chat_service.exception;

/**
 * Custom exception thrown when an entity is not found in the database.
 */
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
