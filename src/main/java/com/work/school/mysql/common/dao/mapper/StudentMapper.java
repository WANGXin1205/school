package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.StudentDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {

    /**
     * 根据年级查询学生
     *
     * @param grade
     * @return
     */
    List<StudentDO> listStudentByGrade(Integer grade);

    /**
     * 根据年级和班级查询所有学生
     *
     * @param grade
     * @param classId
     * @return
     */
    List<StudentDO> listStudentByGradeClass(@Param("grade") Integer grade,
                                            @Param("classId") Integer classId);

    /**
     * 批量保存学生信息
     *
     * @param studentDOList
     * @return
     */
    Integer saveBatch(List<StudentDO> studentDOList);
}