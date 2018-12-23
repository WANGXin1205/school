package com.work.school.common;

import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

/**
 * Created by WANGXin on 2017/5/21.
 */
public class Page<T> {

    private final static Long TO_MANY_SIZE = 10L;

    private final static Long NEAR_SIZE = 3L;

    private Long totalElements;

    private Long currentPage;

    private Long totalPage;

    private Long pageSize = 10L;

    private List<T> contents;

    private Map<String, Object> params;

    public Page() {
        this.currentPage = 1L;
    }

    public Page(Long currentPage, Map<String, Object> params) {
        this();
        if (currentPage == null ) {
            this.currentPage = 1L;
        } else {
            this.currentPage = currentPage;
        }
        this.params = params;
    }

    public RowBounds getRowBounds() {
        int offset = (currentPage.intValue() - 1) * pageSize.intValue();
        return new RowBounds(offset, pageSize.intValue());
    }

    public boolean hasPrev() {
        return getCurrentPage() > 1;
    }

    public boolean hasNext() {
        return getTotalPage() > getCurrentPage();
    }

    public Long getPreNumber() {
        return getCurrentPage() - 1;
    }

    public Long getNextNumber() {
        return getCurrentPage() + 1;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
        Long p = this.totalElements / this.pageSize;
        this.totalPage = (this.pageSize * p) == this.totalElements ? p : p+1;
    }

    public void setTotalElements(Integer totalElements) {
        this.setTotalElements(Long.valueOf(totalElements));
    }

    public Long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Long currentPage) {
        this.currentPage = currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = Long.valueOf(currentPage);
    }

    public Long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = Long.valueOf(pageSize);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

}
