package com.work.school.mysql.common.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/4/4 9:19 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentServiceTest {

    @Resource
    private StudentService studentService;

    @Test
    public void saveBatchTest() throws Exception{
        var fileLocal = new File("/Users/wangxin/Downloads/2班.xlsx");
        var fis = new FileInputStream(fileLocal);
        var contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        var multipartFile = new MockMultipartFile(fileLocal.getName(), fileLocal.getName(), contentType, fis);
        var cattyResult = studentService.saveBatch(multipartFile);
        Assert.assertTrue(cattyResult.isSuccess());
    }
}