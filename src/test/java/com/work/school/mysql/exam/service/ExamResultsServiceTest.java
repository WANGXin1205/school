package com.work.school.mysql.exam.service;

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
 * @Date : 2019/3/28 8:05 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExamResultsServiceTest {

    @Resource
    private ExamResultsService examResultsService;

    @Test
    public void computerExamTarget() throws Exception{
        var fileLocal = new File("/Users/wangxin/Downloads/语文考试成绩.xlsx");
        var fis = new FileInputStream(fileLocal);
        var contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        var multipartFile = new MockMultipartFile(fileLocal.getName(), fileLocal.getName(), contentType, fis);
        var candyResult = examResultsService.computerExamTarget(multipartFile);
        Assert.assertTrue(candyResult.isSuccess());
    }
}