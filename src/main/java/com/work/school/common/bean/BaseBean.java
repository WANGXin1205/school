/*
 * 项目名:      农夫山泉小瓶水系统
 * 文件名:      BaseBean.java
 * 类名:        BaseBean
 *
 * 版权声明:
 *      本系统的所有内容，包括源码、页面设计，文字、图像以及其他任何信息，
 *      如未经特殊说明，其版权均属农夫山泉股份有限公司所有。
 *
 *      Copyright (c) 2013 农夫山泉股份有限公司
 *      版权所有
 */
package com.work.school.common.bean;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 类名:    BaseBean
 * 描述：         前台数据基础Bean，前台FormBean必须继承BaseBean。
 * @author  Qiancheng Yao
 *
 */
public abstract class BaseBean implements Serializable {

    private static final long serialVersionUID = -2462510018255864550L;

    private Boolean success          = Boolean.TRUE;

    private String token;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date systemTime;

    private List<ErrorBean> errorList        = new ArrayList<ErrorBean>();

    //存放bean参数不合法的error code(不返回前端，统一在切面中处理)
    private List<String> errorCodeList    = new ArrayList<String>();

    public abstract void validate();


    public Date getSystemTime() {
        return systemTime;
    }


    public void setSystemTime(Date systemTime) {
        this.systemTime = systemTime;
    }


    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public List<ErrorBean> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ErrorBean> errorList) {
        this.errorList = errorList;
    }


    public List<String> getErrorCodeList() {
        return errorCodeList;
    }


    public void setErrorCodeList(List<String> errorCodeList) {
        this.errorCodeList = errorCodeList;
    }

    public void setError(ErrorBean bean) {
        this.errorList.add(bean);
    }
}
