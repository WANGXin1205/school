package com.work.school.mysql.library.dao.domain;

import java.util.Date;

public class BorrowDO {
    /**
     * id主键
     */
    private Integer id;
    /**
     * 图书馆图书编号
     */
    private String libraryBookId;
    /**
     * 教师id
     */
    private Integer teacherId;
    /**
     * 借书时间
     */
    private Date borrowStart;
    /**
     * 还书时间
     */
    private Date borrowEnd;
    /**
     * 备注
     */
    private String mark;
    /**
     * 状态 1-有效 0-失效
     */
    private Integer status;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibraryBookId() {
        return libraryBookId;
    }

    public void setLibraryBookId(String libraryBookId) {
        this.libraryBookId = libraryBookId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Date getBorrowStart() {
        return borrowStart;
    }

    public void setBorrowStart(Date borrowStart) {
        this.borrowStart = borrowStart;
    }

    public Date getBorrowEnd() {
        return borrowEnd;
    }

    public void setBorrowEnd(Date borrowEnd) {
        this.borrowEnd = borrowEnd;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "BorrowDO{" +
                "id=" + id +
                ", libraryBookId='" + libraryBookId + '\'' +
                ", teacherId=" + teacherId +
                ", borrowStart=" + borrowStart +
                ", borrowEnd=" + borrowEnd +
                ", mark='" + mark + '\'' +
                ", status=" + status +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", updateBy='" + updateBy + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }

}