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
     * 查询所有上课的教师
     *
     * @return
     */
    List<TeacherDO> listAllWorkTeacher();
}