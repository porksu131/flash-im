package com.szr.flashim.file.exception;

public class FileUploadException extends StorageException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}