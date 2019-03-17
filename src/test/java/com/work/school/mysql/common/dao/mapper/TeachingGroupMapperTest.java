package com.work.school.mysql.common.dao.mapper;

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
 * @Date : 2019/3/6 9:00 AM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TeachingGroupMapperTest {

    @Resource
    private TeachingGroupMapper teachingGroupMapper;

    @Test
    public void listAllTeachingGroup() {
       var allTeachingGroupList = teachingGroupMapper.listAllTeachingGroup();
       Assert.assertNotNull(allTeachingGroupList);
    }

    @Test
    public void listTeachingGroupByGroupIdList(){
        List<Integer> groupIdList = Lists.newArrayList(1,2,3,4,5,6,7,127);
        var teachingGroupList = teachingGroupMapper.listTeachingGroupByGroupIdList(groupIdList);
        Assert.assertNotNull(teachingGroupList);
    }
}