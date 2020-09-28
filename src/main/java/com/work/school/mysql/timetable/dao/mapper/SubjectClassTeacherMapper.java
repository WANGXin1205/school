package com.work.school.mysql.timetable.dao.mapper;


import com.work.school.mysql.timetable.dao.domain.SubjectClassTeacherDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SubjectClassTeacherMapper {

    /**
     * 批量保存
     *
     * @param subjectClassTeacherDOList
     */
    public void saveBatchWithoutTeacher(List<SubjectClassTeacherDO> subjectClassTeacherDOList);

    /**
     * 根据年级查询科目、班级、教师信息
     *
     * @param grade
     * @return
     */
    public List<SubjectClassTeacherDO> listSubjectClassTeacherByGrade(@Param("grade") Integer grade);
}