/*
 * 项目名:      农夫山泉小瓶水系统
 * 文件名:      ErrorBean.java
 * 类名:        ErrorBean
 *
 * 版权声明:
 *      本系统的所有内容，包括源码、页面设计，文字、图像以及其他任何信息，
 *      如未经特殊说明，其版权均属农夫山泉股份有限公司所有。
 *
 *      Copyright (c) 2013 农夫山泉股份有限公司
 *      版权所有
 */
package com.work.school.common.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 类名:		ErrorBean
 * 描述:		异常实例
 * @author 	Qiancheng Yao
 *
 */
public class ErrorBean implements Serializable {

    private static final long serialVersionUID = -5542497781531974550L;

    private String field;                                   // 前台调后台接口的对应的query字段

    private String errorCode;

    private String errorMsg;

    private String status;

    private Object[]          args;

    /*** ErrorBean构造函数 **/
    public ErrorBean() {
        super();
    }

    public String getField() {
        return field;
    }

    public ErrorBean setField(String field) {
        this.field = field;
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorBean setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public ErrorBean setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ErrorBean setStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * @return 返回变量args的值
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * @param args 设置args的值
     */
    public void setArgs(Object[] args) {
        this.args = args != null ? Arrays.copyOf(args, args.length) : null;
    }

}
