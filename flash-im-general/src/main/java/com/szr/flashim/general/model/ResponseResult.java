package com.szr.flashim.general.model;

import com.szr.flashim.general.constant.Constants;

import java.io.Serializable;

public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = Constants.SUCCESS;

    /**
     * 失败
     */
    public static final int FAIL = Constants.FAIL;

    private int code;

    private String msg;

    private T data;

    public ResponseResult() {
    }

    public ResponseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseResult<T> ok() {
        return new ResponseResult<>(SUCCESS, "success");
    }

    public static <T> ResponseResult<T> ok(T data) {
        return new ResponseResult<>(SUCCESS, "success", data);
    }

    public static <T> ResponseResult<T> ok(int code, String msg) {
        return new ResponseResult<>(code, msg);
    }

    public static <T> ResponseResult<T> ok(String msg, T data) {
        return new ResponseResult<>(SUCCESS, msg, data);
    }

    public static <T> ResponseResult<T> fail() {
        return new ResponseResult<>(FAIL, "failure");
    }

    public static <T> ResponseResult<T> fail(String msg) {
        return new ResponseResult<>(FAIL, msg);
    }

    public static <T> ResponseResult<T> fail(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    public static <T> ResponseResult<T> fail(String msg, T data) {
        return new ResponseResult<>(FAIL, msg, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Boolean isError(ResponseResult<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(ResponseResult<T> ret) {
        return ResponseResult.SUCCESS == ret.getCode();
    }
}
