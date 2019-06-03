package com.work.school.mysql.library.dao.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/5/30 3:52 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BookMapperTest {

    @Resource
    private BookMapper bookMapper;

    @Test
    public void listAll() {
        var bookList = bookMapper.listAll();
        Assert.assertNotNull(bookList);
    }
}