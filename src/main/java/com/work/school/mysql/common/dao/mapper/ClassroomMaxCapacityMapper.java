package com.work.school.mysql.common.dao.mapper;


import com.work.school.mysql.common.dao.domain.ClassroomMaxCapacityDO;

import java.util.List;

public interface ClassroomMaxCapacityMapper {

    /**
     * 查询所有科目信息
     *
     * @return
     */
    List<ClassroomMaxCapacityDO> listAllClassroomMaxCapacity();

}