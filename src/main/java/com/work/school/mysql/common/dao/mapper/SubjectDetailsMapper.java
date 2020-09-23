package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.SubjectDetailsDO;
import com.work.school.mysql.common.service.dto.SubjectDTO;

import java.util.List;

public interface SubjectDetailsMapper {

    /**
     * 查询所有科目信息
     *
     * @return
     */
    List<SubjectDetailsDO> listAllSubjectDetails();

    /**
     * 查询所有科目和科目明细组合出来的信息
     *
     * @return
     */
    List<SubjectDTO> listAllSubjectDTO();

    /**
     * 根据年级查询科目明细
     */
    List<SubjectDTO> listSubjectDTOByGrade(Integer grade);
}