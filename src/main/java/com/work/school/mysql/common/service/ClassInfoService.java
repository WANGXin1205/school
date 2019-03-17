package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.common.utils.common.MathsUtils;
import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.mapper.ClassInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/7 7:18 PM
 * @Description
 */
@Service
public class ClassInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfoService.class);

    @Resource
    private ClassInfoMapper classInfoMapper;

    /**
     * 根据年级查询 班级信息
     * @param grade
     * @return
     */
    public List<ClassInfoDO> listClassByGrade(Integer grade){
        return classInfoMapper.listClassByGrade(grade);
    }



}
