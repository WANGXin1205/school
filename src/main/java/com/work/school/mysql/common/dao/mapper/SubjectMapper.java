package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.SubjectDO;

import java.util.List;

public interface SubjectMapper {

    /**
     * 查询所有科目信息
     *
     * @return
     */
    List<SubjectDO> listAllSubject();

}