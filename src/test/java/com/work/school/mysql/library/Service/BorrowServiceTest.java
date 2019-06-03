package com.work.school.mysql.library.Service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.library.dao.domain.BorrowDO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/6/3 8:49 AM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BorrowServiceTest {

    @Resource
    private BorrowService borrowService;

    @Test
    public void saveTest()throws Exception{
        var fileLocal = new File("/Users/wangxin/Downloads/图书借阅表.xlsx");
        var fis = new FileInputStream(fileLocal);
        var contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        var multipartFile = new MockMultipartFile(fileLocal.getName(), fileLocal.getName(), contentType, fis);
        var candyResult = borrowService.save(multipartFile);
        Assert.assertTrue(candyResult.isSuccess());
    }

    @Test
    public void listBorrowBookTest(){
        var cattyResult = borrowService.listBorrowBook();
        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void listBorrowPastByTimeTest(){
        String date = "2019-5-23";
        var cattyResult = borrowService.listBorrowPastByTime(date);
        Assert.assertTrue(cattyResult.isSuccess());
    }

    @Test
    public void listBorrowLaterByTimeTest(){
        String date = "2019-5-21";
        var cattyResult = borrowService.listBorrowLaterByTime(date);
        Assert.assertTrue(cattyResult.isSuccess());
    }

}