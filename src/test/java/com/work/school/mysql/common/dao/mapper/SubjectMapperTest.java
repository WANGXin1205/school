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
 * @Date : 2019/3/6 12:02 AM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SubjectMapperTest {

    @Resource
    private SubjectMapper subjectMapper;

    @Test
    public void listAllSubject() {
        var allSubjectDOList = subjectMapper.listAllSubjectByGrade(1);
        Assert.assertNotNull(allSubjectDOList);
    }
}