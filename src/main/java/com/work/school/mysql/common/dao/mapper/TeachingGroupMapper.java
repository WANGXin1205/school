package com.work.school.mysql.common.dao.mapper;

import com.work.school.mysql.common.dao.domain.TeachingGroupDO;

import java.util.List;

public interface TeachingGroupMapper {

    /**
     * 查询所有数据
     * @return
     */
    List<TeachingGroupDO> listAllTeachingGroup();

    /**
     * 根据groupIdList查询数据
     * @param groupIdList
     * @return
     */
    List<TeachingGroupDO> listTeachingGroupByGroupIdList(List<Integer> groupIdList);

}