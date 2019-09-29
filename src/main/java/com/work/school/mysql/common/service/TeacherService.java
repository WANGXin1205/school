package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.dao.mapper.TeacherMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 7:30 PM
 * @Description
 */
@Service
public class TeacherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherService.class);

    @Resource
    private TeacherMapper teacherMapper;

    /**
     * 查询学校所有教师
     *
     * @return
     */
    public List<TeacherDO> listAll() {
        return teacherMapper.listAll();
    }

    /**
     * 查询所有上课的教师
     *
     * @return
     */
    public List<TeacherDO> listAllWorkTeacher(){
        return teacherMapper.listAllWorkTeacher();
    }



}
