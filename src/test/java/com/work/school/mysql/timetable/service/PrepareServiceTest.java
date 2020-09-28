package com.work.school.mysql.timetable.service;

import com.work.school.mysql.common.service.enums.BacktrackingTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PrepareServiceTest {

    @Resource
    private PrepareService prepareService;

    @Test
    public void prepareTimeTablingTest() {
        Integer grade = 1;
        var prepareTimeTablingDTO = prepareService.prepareTimeTabling(grade,BacktrackingTypeEnum.BA);
        Assert.assertNotNull(prepareTimeTablingDTO);
    }
}