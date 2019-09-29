package com.work.school.mysql.common.dao.mapper;



import com.work.school.mysql.common.dao.domain.TeacherGradeClassDO;
import com.work.school.mysql.common.dao.domain.TeacherSubjectDO;

import java.util.List;

public interface TeacherGradeClassMapper {
    /**
     * 查询所有教师和年级班级的信息
     *
     * @return
     */
    List<TeacherGradeClassDO> listAllTeacherGradeClass();

}