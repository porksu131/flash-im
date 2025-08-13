package com.szr.flashim.file.exception;

public class FileDeleteException extends StorageException {
    public FileDeleteException(String message) {
        super(message);
    }

    public FileDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}