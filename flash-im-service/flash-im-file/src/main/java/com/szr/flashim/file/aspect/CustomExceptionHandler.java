package com.szr.flashim.file.aspect;

import com.szr.flashim.file.exception.StorageException;
import com.szr.flashim.general.model.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(StorageException.class)
    public ResponseResult<String> handleStorageException(StorageException e) {
        return ResponseResult.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<String> handleException(Exception e) {
        return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error: " + e.getMessage());
    }
}