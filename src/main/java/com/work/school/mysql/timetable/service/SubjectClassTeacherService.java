package com.work.school.mysql.timetable.service;

import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import com.work.school.mysql.timetable.dao.mapper.SubjectClassTeacherMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class SubjectClassTeacherService {

    @Resource
    private SubjectClassTeacherMapper subjectClassTeacherMapper;

    /**
     * 查询所有科目班级教师信息
     *
     * @param grade
     * @return
     */
    public List<SubjectClassTeacherDO> listSubjectClassTeacherByGrade(Integer grade){
        return subjectClassTeacherMapper.listSubjectClassTeacherByGrade(grade);
    }

}
