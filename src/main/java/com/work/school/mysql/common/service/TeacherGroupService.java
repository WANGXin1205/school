package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.dao.domain.TeachingGroupDO;
import com.work.school.mysql.common.dao.mapper.TeachingGroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 6:59 PM
 * @Description
 */
@Service
public class TeacherGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherGroupService.class);

    @Resource
    private TeachingGroupMapper teachingGroupMapper;

    /**
     * 根据groupIdList查询所带班级
     * @param groupIdList
     * @return
     */
    public List<TeachingGroupDO> listTeachingGroupByGroupIdList(List<Integer> groupIdList){
        return teachingGroupMapper.listTeachingGroupByGroupIdList(groupIdList);
    }

}
