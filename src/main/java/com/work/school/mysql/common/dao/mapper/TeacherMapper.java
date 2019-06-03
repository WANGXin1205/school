package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.TeacherDO;

import java.util.List;

public interface TeacherMapper {

    /**
     * 查询学校所有教师
     *
     * @return
     */
    List<TeacherDO> listAll();

    /**
     * 查询某个年级下所有教师
     *
     * @param grade
     * @return
     */
    List<TeacherDO> listAllTeacherByGrade(Integer grade);

    /**
     * 查询某个年级下所有上课教师
     *
     * @param grade
     * @return
     */
    List<TeacherDO> listAllTeachingTeacherByGrade(Integer grade);

    /**
     * 查询所有上课组合
     *
     * @return
     */
    List<Integer> listAllTeacherGroupId();


}