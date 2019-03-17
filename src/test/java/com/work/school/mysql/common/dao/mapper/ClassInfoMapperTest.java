package com.work.school.mysql.common.dao.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:58 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ClassInfoMapperTest {

    @Resource
    private ClassInfoMapper classInfoMapper;

    @Test
    public void listAllClass() {
        var allClass = classInfoMapper.listAllClass();
        Assert.assertNotNull(allClass);
    }

    @Test
    public void listClass() {
        Integer grade = 1;
        var classInfoList = classInfoMapper.listClassByGrade(grade);
        Assert.assertNotNull(classInfoList);
    }
}