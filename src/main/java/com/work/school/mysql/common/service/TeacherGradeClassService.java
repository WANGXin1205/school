package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.domain.TeacherGradeClassDO;
import com.work.school.mysql.common.dao.mapper.TeacherGradeClassMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 7:30 PM
 * @Description
 */
@Service
public class TeacherGradeClassService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherGradeClassService.class);

    @Resource
    private TeacherGradeClassMapper teacherGradeClassMapper;

    /**
     * 查询所有教师和年级班级的对应关系
     * @return
     */
    public List<TeacherGradeClassDO> listAllTeacherGradeClass(){
        return teacherGradeClassMapper.listAllTeacherGradeClass();
    }

}
