package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.ClassroomMaxCapacityDO;
import com.work.school.mysql.common.dao.mapper.ClassInfoMapper;
import com.work.school.mysql.common.dao.mapper.ClassroomMaxCapacityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 7:18 PM
 * @Description
 */
@Service
public class ClassroomMaxCapacityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassroomMaxCapacityService.class);

    @Resource
    private ClassroomMaxCapacityMapper classroomMaxCapacityMapper;

    /**
     * 查询所有需要教室的科目的教室最大容量
     *
     * @return
     */
    public List<ClassroomMaxCapacityDO> listClassroomMaxCapacityDO() {
        return classroomMaxCapacityMapper.listAllClassroomMaxCapacity();
    }

    /**
     * 组装需要教室科目和教室最大容量的map
     *
     * @param classroomMaxCapacityDOList
     * @return
     */
    public HashMap<Integer, Integer> getClassroomMaxCapacityMap(List<ClassroomMaxCapacityDO> classroomMaxCapacityDOList) {
        return (HashMap<Integer, Integer>) classroomMaxCapacityDOList.stream()
                .collect(Collectors.toMap(ClassroomMaxCapacityDO::getSubjectId, ClassroomMaxCapacityDO::getMaxCapacity));
    }

    /**
     * 组装需要教室科目和教室最大容量的map
     *
     * @return
     */
    public HashMap<Integer, Integer> getClassroomMaxCapacityMap() {
        var classroomMaxCapacityDOList = this.listClassroomMaxCapacityDO();
        return (HashMap<Integer, Integer>) classroomMaxCapacityDOList.stream()
                .collect(Collectors.toMap(ClassroomMaxCapacityDO::getSubjectId, ClassroomMaxCapacityDO::getMaxCapacity));
    }

}
