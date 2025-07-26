/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szr.flashim.core.netty.async;

import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.core.netty.exception.SendRequestException;
import com.szr.flashim.core.netty.exception.SendTimeoutException;
import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicBoolean;

public class ResponseFuture {
    private final Channel channel;
    private final long opaque;
    private final ImMessage request;
    private final long timeoutMillis;
    private InvokeCallback invokeCallback;
    private final long beginTimestamp = System.currentTimeMillis();
    //private final CountDownLatch countDownLatch = new CountDownLatch(1); // 暂不使用同步锁阻塞请求等待响应

    private SemaphoreReleaseOnlyOnce once;

    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);
    private volatile ImMessage response;
    private volatile boolean sendRequestOK = true;
    private volatile Throwable cause;

    public ResponseFuture(Channel channel, long opaque, ImMessage request, long timeoutMillis, InvokeCallback invokeCallback,
                          SemaphoreReleaseOnlyOnce once) {
        this.channel = channel;
        this.opaque = opaque;
        this.request = request;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.once = once;
    }

    public ResponseFuture(Channel channel, long opaque, ImMessage request, long timeoutMillis) {
        this.channel = channel;
        this.opaque = opaque;
        this.request = request;
        this.timeoutMillis = timeoutMillis;
    }

    public void executeInvokeCallback() {
        if (invokeCallback == null) {
            return;
        }
        // 确保回调方法只被调用一次
        if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
            if (this.response != null) {
                invokeCallback.operationSucceed(this.response);
            } else {
                if (!isSendRequestOK()) {
                    invokeCallback.operationFail(new SendRequestException(channel.remoteAddress().toString(), getCause()));
                } else if (isTimeout()) {
                    invokeCallback.operationFail(new SendTimeoutException(channel.remoteAddress().toString(), getTimeoutMillis(), getCause()));
                } else {
                    invokeCallback.operationFail(new Exception(getRequestCommand().toString(), getCause()));
                }
            }
            invokeCallback.operationComplete(this);
        }
    }

    public void setInvokeCallback(InvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
    }

    public void setSemaphoreReleaseOnlyOnce(SemaphoreReleaseOnlyOnce once) {
        this.once = once;
    }

    public void release() {
        if (this.once != null) {
            this.once.release();
        }
    }

    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

//    public ImMessage waitResponse(final long timeoutMillis) throws InterruptedException {
//        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
//        return this.response;
//    }
//
//    public void putResponse(final ImMessage responseCommand) {
//        this.response = responseCommand;
//        this.countDownLatch.countDown();
//    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public ImMessage getResponse() {
        return response;
    }

    public void setResponse(ImMessage response) {
        this.response = response;
    }

    public ImMessage getRequestCommand() {
        return request;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getOpaque() {
        return opaque;
    }
}
