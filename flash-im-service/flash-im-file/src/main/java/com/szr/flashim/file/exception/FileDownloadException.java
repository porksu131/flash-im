package com.szr.flashim.file.exception;

public class FileDownloadException extends StorageException {
    public FileDownloadException(String message) {
        super(message);
    }

    public FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}