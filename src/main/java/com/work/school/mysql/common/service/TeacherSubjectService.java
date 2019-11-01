package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.domain.TeacherGradeClassDO;
import com.work.school.mysql.common.dao.domain.TeacherSubjectDO;
import com.work.school.mysql.common.dao.mapper.TeacherGradeClassMapper;
import com.work.school.mysql.common.dao.mapper.TeacherSubjectMapper;
import com.work.school.mysql.common.service.dto.SubjectGradeClassDTO;
import com.work.school.mysql.common.service.dto.SubjectTeacherGradeClassDTO;
import org.apache.poi.ss.formula.functions.Count;
import org.apache.poi.ss.formula.functions.Function;
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
 * @Date : 2019/3/6 7:30 PM
 * @Description
 */
@Service
public class TeacherSubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherSubjectService.class);

    @Resource
    private TeacherSubjectMapper teacherSubjectMapper;

    /**
     * 查询所有科目，教师，年级，班级之间的关系
     *
     * @return
     */
    public List<SubjectTeacherGradeClassDTO> listAllSubjectTeacherGradeClassDTO() {
        return teacherSubjectMapper.listAllSubjectTeacherGradeClass();
    }

    /**
     * 获取科目年级班级对应教师的map
     *
     * @param subjectTeacherGradeClassDTOList
     * @return
     */
    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherMap(List<SubjectTeacherGradeClassDTO> subjectTeacherGradeClassDTOList) {
        HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap = new HashMap<>();
        for (SubjectTeacherGradeClassDTO x : subjectTeacherGradeClassDTOList) {
            SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
            subjectGradeClassDTO.setSubjectId(x.getSubjectId());
            subjectGradeClassDTO.setGrade(x.getGrade());
            subjectGradeClassDTO.setClassNum(x.getClassNum());
            subjectGradeClassTeacherMap.put(subjectGradeClassDTO, x.getTeacherId());
        }
        return subjectGradeClassTeacherMap;
    }

    /**
     * 获取一个老师带多少班级的map
     *
     * @param subjectTeacherGradeClassDTOList
     * @return
     */
    public HashMap<Integer, Integer> getTeacherClassNumMap(List<SubjectTeacherGradeClassDTO> subjectTeacherGradeClassDTOList) {
        var map = subjectTeacherGradeClassDTOList.stream().collect(Collectors
                .groupingBy(SubjectTeacherGradeClassDTO::getTeacherId, Collectors.counting()));

        HashMap<Integer, Integer> teacherClassNumMap = new HashMap<>();
        for (Integer x : map.keySet()) {
            teacherClassNumMap.put(x, map.get(x).intValue());
        }

        return teacherClassNumMap;
    }

    /**
     * 获取某个科目对应教师所带班级数目的map
     * @param subjectTeacherGradeClassDTOList
     * @return
     */
    public HashMap<SubjectGradeClassDTO, Integer> getSubjectGradeClassTeacherCountMap(List<SubjectTeacherGradeClassDTO> subjectTeacherGradeClassDTOList) {
        HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap = new HashMap<>();
        var subjectGradeClassTeacherMap = this.getSubjectGradeClassTeacherMap(subjectTeacherGradeClassDTOList);
        var teacherClassNumMap = this.getTeacherClassNumMap(subjectTeacherGradeClassDTOList);
        for (SubjectGradeClassDTO x : subjectGradeClassTeacherMap.keySet()) {
            var teacherId = subjectGradeClassTeacherMap.get(x);
            var count = teacherClassNumMap.get(teacherId);
            subjectGradeClassTeacherCountMap.put(x, count);
        }
        return subjectGradeClassTeacherCountMap;
    }

    /**
     * 获取教师所带年级班级和科目map
     * @param subjectTeacherGradeClassDTOList
     * @return
     */
    public HashMap<Integer,List<SubjectTeacherGradeClassDTO>> getTeacherSubjectListMap(List<SubjectTeacherGradeClassDTO> subjectTeacherGradeClassDTOList){
        return (HashMap<Integer, List<SubjectTeacherGradeClassDTO>>) subjectTeacherGradeClassDTOList.stream().collect(Collectors.groupingBy(SubjectTeacherGradeClassDTO::getTeacherId));
    }

}
