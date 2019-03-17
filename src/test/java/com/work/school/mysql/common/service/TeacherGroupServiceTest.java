package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.mapper.TeachingGroupMapper;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 7:05 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeacherGroupServiceTest {

    @Resource
    private TeacherGroupService teacherGroupService;

    @Test
    public void listTeachingGroupByGroupIdList() {
        List<Integer> groupIdList = Lists.newArrayList(1,2,3,4,5,6,7);
        var teachingGroupByGroupIdList = teacherGroupService.listTeachingGroupByGroupIdList(groupIdList);
        Assert.assertNotNull(teachingGroupByGroupIdList);
    }
}