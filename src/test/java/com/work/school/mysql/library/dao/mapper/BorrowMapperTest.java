package com.work.school.mysql.library.dao.mapper;

import com.work.school.common.utils.common.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/6/3 6:08 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BorrowMapperTest {

    @Resource
    private BorrowMapper borrowMapper;

    @Test
    public void listAll() {
        var borrowDOList = borrowMapper.listAll();
        Assert.assertNotNull(borrowDOList);
    }

    @Test
    public void listBorrowBook() {
        var borrowDOList = borrowMapper.listBorrowBook();
        Assert.assertNotNull(borrowDOList);
    }

    @Test
    public void listPastByTime() {
        var borrowDOList = borrowMapper.listBorrowPastByTime(DateUtils.getNow());
        Assert.assertNotNull(borrowDOList);
    }

    @Test
    public void listLaterByTime() {
        var borrowDOList = borrowMapper.listBorrowLaterByTime(DateUtils.getNow());
        Assert.assertNotNull(borrowDOList);
    }
}