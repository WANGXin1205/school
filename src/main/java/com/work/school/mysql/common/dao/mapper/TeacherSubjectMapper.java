package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.TeacherSubjectDO;
import com.work.school.mysql.common.service.dto.SubjectTeacherGradeClassDTO;

import java.util.List;

public interface TeacherSubjectMapper {
    /**
     * 查询所有教师和科目信息
     *
     * @return
     */
    List<TeacherSubjectDO> listAllTeacherSubject();

    /**
     * 查询所有科目，教师，年级，班级对应关系
     *
     * @return
     */
    List<SubjectTeacherGradeClassDTO> listAllSubjectTeacherGradeClass();
}