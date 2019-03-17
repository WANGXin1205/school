package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.domain.ClassInfoDO;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.dao.domain.TeachingGroupDO;
import com.work.school.mysql.common.service.dto.ClassSubjectKeyDTO;
import com.work.school.mysql.common.service.dto.SchoolGradeDefaultDTO;
import com.work.school.mysql.common.service.dto.SubjectWeightDTO;
import com.work.school.mysql.common.service.dto.TeacherFreeKeyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.core.ClassInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/16 11:46 AM
 * @Description
 */
@Service
public class SchoolCommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchoolCommonService.class);

    /**
     * 初始权重
     */
    private static final Integer INIT_WEIGHT = 0;

    @Resource
    private ClassInfoService classInfoService;
    @Resource
    private SubjectService subjectService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private TeacherGroupService teacherGroupService;

    /**
     * 获取某年级下的所有默认配置
     *
     * @param grade
     * @return
     */
    public SchoolGradeDefaultDTO getSchoolDefaultDTO(Integer grade) {

        SchoolGradeDefaultDTO schoolDefaultDTO = new SchoolGradeDefaultDTO();

        // 获取某年级下所有班级信息
        var allClassInfoList = classInfoService.listClassByGrade(grade);

        // 获取某年级下班的数量
        Integer classSize = allClassInfoList.size();

        // 获取某年级下班级id 和 班级的map
        Map<Integer, ClassInfoDO> allClassMap = allClassInfoList.stream().collect(Collectors.toMap(ClassInfoDO::getId, Function.identity()));

        // 获取某年级下所有的课程
        var allSubjectDOList = subjectService.listAllSubjectByGrade(grade);

        // 获取某年级下科目id和 科目的map
        Map<Integer, SubjectDO> allSubjectMap = new HashMap<>();
        // 获取课程权重List
        List<SubjectWeightDTO> subjectWeightList = new ArrayList<>();
        for (SubjectDO x : allSubjectDOList) {
            allSubjectMap.put(x.getId(), x);
            for (ClassInfoDO y : allClassInfoList) {
                SubjectWeightDTO subjectWeightDTO = this.packSubjectWeightDTO(x,y.getClassNum());
                subjectWeightList.add(subjectWeightDTO);
            }
        }

        // 获取某年级下所有的授课教师
        var allTeacherList = teacherService.listAllTeachingTeacherByGrade(grade);

        // 获取某年级下所有教师id 和教师map
        Map<Integer, TeacherDO> allTeacherMap = new HashMap<>();
        // 获取某年级下所有教师在每一天哪节课上课的初始map
        Map<TeacherFreeKeyDTO, List<Integer>> allTeacherFreeMap = new HashMap<>();
        // 获取某年级下所有教师所带班级的组合id
        List<Integer> groupIdList = allTeacherList.stream().map(TeacherDO::getTeacherGroupId).distinct().collect(Collectors.toList());

        // 获取所有的代课组合对应的班级
        var teachingGroupList = teacherGroupService.listTeachingGroupByGroupIdList(groupIdList);
        // 将代课组合按照groupId分组
        Map<Integer,List<TeachingGroupDO>> groupMap = teachingGroupList.stream().collect(Collectors.groupingBy(TeachingGroupDO::getGroupId));
        // 获取所有班级、课程和教师id对应的map
        Map<ClassSubjectKeyDTO,Integer> classSubjectTeacherMap = new HashMap<>();
        // 获取所有班级、课程和教师所带班级数目对应的map
        Map<ClassSubjectKeyDTO,Integer> classSubjectTeachingNumMap = new HashMap<>();

        for (TeacherDO x : allTeacherList) {
            allTeacherMap.put(x.getId(), x);

            for (int y = 1; y <= SchoolGradeDefaultDTO.getWorkDay(); y++) {
                TeacherFreeKeyDTO teacherFreeKeyDTO = this.packTeacherFreeKeyDTO(x.getId(),y);
                allTeacherFreeMap.put(teacherFreeKeyDTO,new ArrayList<>());
            }

            List<TeachingGroupDO> teachingGroupDOList = groupMap.get(x.getTeacherGroupId());
            for (TeachingGroupDO y:teachingGroupDOList){
                ClassSubjectKeyDTO classSubjectKeyDTO = new ClassSubjectKeyDTO();
                classSubjectKeyDTO.setSubjectId(x.getSubjectId());
                classSubjectKeyDTO.setClassNum(y.getStudentClassId());
                classSubjectTeacherMap.put(classSubjectKeyDTO,x.getId());
                classSubjectTeachingNumMap.put(classSubjectKeyDTO,teachingGroupDOList.size());
            }
        }

        schoolDefaultDTO.setAllClassInfoList(allClassInfoList);
        schoolDefaultDTO.setAllSubjectList(allSubjectDOList);
        schoolDefaultDTO.setAllTeacherList(allTeacherList);
        schoolDefaultDTO.setClassSize(classSize);
        schoolDefaultDTO.setAllClassMap(allClassMap);
        schoolDefaultDTO.setAllSubjectMap(allSubjectMap);
        schoolDefaultDTO.setAllTeacherMap(allTeacherMap);
        schoolDefaultDTO.setAllSubjectWeightList(subjectWeightList);
        schoolDefaultDTO.setAllTeacherFreeMap(allTeacherFreeMap);
        schoolDefaultDTO.setClassSubjectTeacherMap(classSubjectTeacherMap);
        schoolDefaultDTO.setClassSubjectTeachingNumMap(classSubjectTeachingNumMap);

        return schoolDefaultDTO;
    }

    /**
     * 组装subjectWeightDTO
     * @param subjectDO
     * @param classNum
     * @return
     */
    private SubjectWeightDTO packSubjectWeightDTO(SubjectDO subjectDO,Integer classNum){
        SubjectWeightDTO subjectWeightDTO = new SubjectWeightDTO();
        BeanUtils.copyProperties(subjectDO, subjectWeightDTO);
        subjectWeightDTO.setClassNum(classNum);
        subjectWeightDTO.setWeight(INIT_WEIGHT);
        return subjectWeightDTO;
    }

    /**
     * 组装TeacherFreeKeyDTO
     * @param teacherId
     * @param workDayNum
     * @return
     */
    private TeacherFreeKeyDTO packTeacherFreeKeyDTO(Integer teacherId,Integer workDayNum){
        TeacherFreeKeyDTO teacherFreeKeyDTO = new TeacherFreeKeyDTO();
        teacherFreeKeyDTO.setTeacherId(teacherId);
        teacherFreeKeyDTO.setWorkDay(workDayNum);

        return teacherFreeKeyDTO;
    }

}
