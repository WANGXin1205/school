/*
 * 项目名:      农夫山泉小瓶水系统
 * 文件名:      SheetBean.java
 * 类名:        SheetBean
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
 * 类名:		SheetBean
 * 描述:		Excel sheet（支持excel多表显示bean）
 * @author 	shuangdai
 *
 */
public class SheetBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sheetName;

    private int               beginRowIndex;

    private int               beginColIndex;

    private String[][]        vals;

    /**
     * SheetBean构造函数
     *
     * @param sheetName
     * @param beginRowIndex
     * @param beginColIndex
     * @param vals
     */
    public SheetBean(String sheetName, int beginRowIndex, int beginColIndex, String[][] vals) {
        super();
        this.sheetName = sheetName;
        this.beginRowIndex = beginRowIndex;
        this.beginColIndex = beginColIndex;
        this.vals = vals != null ? Arrays.copyOf(vals, vals.length) : null;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getBeginRowIndex() {
        return beginRowIndex;
    }

    public void setBeginRowIndex(int beginRowIndex) {
        this.beginRowIndex = beginRowIndex;
    }

    public int getBeginColIndex() {
        return beginColIndex;
    }

    public void setBeginColIndex(int beginColIndex) {
        this.beginColIndex = beginColIndex;
    }

    public String[][] getVals() {
        return vals;
    }

    public void setVals(String[][] vals) {
        this.vals = vals != null ? Arrays.copyOf(vals, vals.length) : null;
    }

    /* (non-Javadoc)
     * @see com.yst.nfsq.xs.bean.BaseBean#validate()
     */
    @Override
    public void validate() {
        setSuccess(true);
    }

}
