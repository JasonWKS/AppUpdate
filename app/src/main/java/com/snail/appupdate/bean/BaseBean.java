package com.snail.appupdate.bean;

/**
 * BaseBean 包含cod msg
 * Created by weststreet on 15/7/3.
 */
public class BaseBean {
    public String code;
    public String desc;
    public String validateResults;
    public String msg;
    public String requestTag;
    public String error;
    public String deviceToken;

    public static final String CODE_SUCCESS = "200";
    public static final String CODE_SUCCESS2 = "0";

    public boolean isSuccess(){
        return CODE_SUCCESS.equals(getCode()) || CODE_SUCCESS2.equals(getCode());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getValidateResults() {
        return validateResults;
    }

    public void setValidateResults(String validateResults) {
        this.validateResults = validateResults;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(String requestTag) {
        this.requestTag = requestTag;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
