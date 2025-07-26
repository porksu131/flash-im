package com.szr.flashim.core.netty.async;

import com.szr.flashim.general.model.protoc.CommonResponse;

public interface NettyClientSendCallBack {
    void onSendSuccess(CommonResponse response);
    void onSendFail(Throwable throwable);
}
