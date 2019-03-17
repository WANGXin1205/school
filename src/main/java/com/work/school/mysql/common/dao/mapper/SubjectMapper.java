package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.SubjectDO;

import java.util.List;

public interface SubjectMapper {

    /**
     * 查询某个年级下所有科目信息
     *
     * @param grade
     * @return
     */
    List<SubjectDO> listAllSubjectByGrade(Integer grade);
}