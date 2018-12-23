package com.work.school.common;

import com.work.school.common.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WANGXin on 2017/5/21.
 */
public class PageResult extends Page {

    /**
     * 成功标识
     */
    private boolean isSuccess = false;
    /**
     * 返回结果code
     */
    private String code;
    /**
     * 返回信息
     */
    private String message;
    /**
     * 错误列表
     */
    private List<String> errorList = new ArrayList<String>();

    private Object otherData;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }

    public Object getOtherData() {
        return otherData;
    }

    public void setOtherData(Object otherData) {
        this.otherData = otherData;
    }

}
