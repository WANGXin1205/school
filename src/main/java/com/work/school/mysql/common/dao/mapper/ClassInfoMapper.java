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
     * 根据年级（1）查询所有班级信息
     *
     * @param grade
     * @return
     */
    List<ClassInfoDO> listClassByGrade(Integer grade);

    /**
     * 根据年级（2018）查询所有班级信息
     *
     * @param gradeId
     * @return
     */
    List<ClassInfoDO> listClassByGradeId(Integer gradeId);
}