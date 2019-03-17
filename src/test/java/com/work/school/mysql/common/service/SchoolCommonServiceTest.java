package com.work.school.mysql.common.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/16 1:20 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SchoolCommonServiceTest {

    @Resource
    private SchoolCommonService schoolCommonService;

    @Test
    public void getSchoolDefaultDTOTest() {
        Integer grade = 1;
        var schoolDefaultDTO = schoolCommonService.getSchoolDefaultDTO(grade);
        Assert.assertNotNull(schoolDefaultDTO);
    }
}