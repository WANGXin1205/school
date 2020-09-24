package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.ClassInfoDO;

import java.util.List;

public interface ClassInfoMapper {
    /**
     * 查询所有班级信息
     *
     * @return
     */
    List<ClassInfoDO> listAllClass();

    /**
     * 查询所有班级信息
     *
     * @param grade
     * @return
     */
    List<ClassInfoDO> listClassByGrade(Integer grade);
}