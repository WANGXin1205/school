package com.work.school.mysql.library.Service.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author : Growlithe
 * @Date : 2019/5/30 4:19 PM
 * @Description
 */
public class BookDTO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;
    /**
     * 编号 有字符X
     */
    private String number;
    /**
     * 图书名称
     */
    private String name;
    /**
     * 图书价格
     */
    private BigDecimal price;
    /**
     * 图书数量
     */
    private Integer quantity;
    /**
     * 出版商
     */
    private String publisher;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "BookDTO{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", publisher='" + publisher + '\'' +
                '}';
    }
}
