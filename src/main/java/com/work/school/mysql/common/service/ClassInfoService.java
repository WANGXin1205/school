package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.common.utils.common.MathsUtils;
import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.mapper.ClassInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 查询所有年级下的班级
     *
     * @return
     */
    public List<ClassInfoDO> listAllClass() {
        return classInfoMapper.listAllClass();
    }

    /**
     * 查询年级下的班级
     * @param grade
     * @return
     */
    public List<ClassInfoDO> listClassByGrade(Integer grade) {
        return classInfoMapper.listClassByGrade(grade);
    }

    /**
     * 获取所有年级下属班级数目
     *
     * @param allClass
     * @return
     */
    public HashMap<Integer, Integer> getGradeClassCountMap(List<ClassInfoDO> allClass) {
        HashMap<Integer, Integer> gradeClassMap = new HashMap<>();

        var gradeClassCountLongMap = allClass.stream().collect(Collectors.groupingBy(ClassInfoDO::getGrade, Collectors.counting()));
        for (Integer x : gradeClassCountLongMap.keySet()) {
            gradeClassMap.put(x, gradeClassCountLongMap.get(x).intValue());
        }

        return gradeClassMap;
    }


}
