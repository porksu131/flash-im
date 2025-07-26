package com.szr.flashim.core.netty.async;

import com.szr.flashim.general.model.ImMessage;

import java.util.concurrent.CompletableFuture;

public class DefaultCallBack implements InvokeCallback{
    private final CompletableFuture<ResponseFuture> future;
    private final ResponseFuture responseFuture;

    public DefaultCallBack(CompletableFuture<ResponseFuture> future, ResponseFuture responseFuture) {
        this.future = future;
        this.responseFuture = responseFuture;
    }

    @Override
    public void operationSucceed(ImMessage response) {
        future.complete(responseFuture);
    }

    @Override
    public void operationFail(Throwable throwable) {
        future.completeExceptionally(throwable);
    }
}
