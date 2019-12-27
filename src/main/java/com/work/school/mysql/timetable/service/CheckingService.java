package com.work.school.mysql.timetable.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/5 11:44 PM
 * @Description
 */
@Service
public class CheckingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckingService.class);

    /**
     * 检查年级是否一致
     *
     * @param gradeClassCountMap
     * @param gradeSubjectMap
     * @return
     */
    public CattyResult checkGrade(HashMap<Integer, Integer> gradeClassCountMap,
                                   Map<Integer, List<SubjectDTO>> gradeSubjectMap) {
        CattyResult cattyResult = new CattyResult();

        if (!gradeClassCountMap.keySet().equals(gradeSubjectMap.keySet())) {
            cattyResult.setMessage("年级和科目对应关系不一致");
            return cattyResult;
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查排课是否有解
     *
     * @param gradeSubjectMap
     * @param allSubjectNameMap
     * @param subjectGradeClassTeacherMap
     * @return
     */
    public CattyResult checkTimeTableSolution(Map<Integer, List<SubjectDTO>> gradeSubjectMap,
                                               Map<Integer, String> allSubjectNameMap,
                                               HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        CattyResult<Map<Integer, List<SubjectDO>>> cattyResult = new CattyResult<>();

        // 检查科目ok
        var checkSubjectResult = this.checkSubject(gradeSubjectMap);
        if (!checkSubjectResult.isSuccess()) {
            cattyResult.setMessage(checkSubjectResult.getMessage());
            return cattyResult;
        }

        // 检查每个班每种科目都有教师上课
        CattyResult checkAllSubjectTeacherGradeClassResult = this.checkAllSubjectTeacherGradeClass(allSubjectNameMap, subjectGradeClassTeacherMap);
        if (!checkAllSubjectTeacherGradeClassResult.isSuccess()) {
            cattyResult.setMessage(checkAllSubjectTeacherGradeClassResult.getMessage());
            return cattyResult;
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查科目OK
     *
     * @param gradeSubjectMap
     * @return
     */
    public CattyResult checkSubject(Map<Integer, List<SubjectDTO>> gradeSubjectMap) {
        CattyResult cattyResult = new CattyResult();

        for (Integer x : gradeSubjectMap.keySet()) {
            Integer times = gradeSubjectMap.get(x).stream().map(SubjectDTO::getFrequency).reduce(Integer::sum).get();
            if (!SchoolTimeTableDefaultValueDTO.getTotalFrequency().equals(times)) {
                cattyResult.setMessage(x + "年级的排课总量与目前排课量不相符");
                return cattyResult;
            }
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 检查所有科目教师年级班级都ok
     *
     * @param allSubjectNameMap
     * @param subjectGradeClassTeacherMap
     * @return
     */
    public CattyResult checkAllSubjectTeacherGradeClass(Map<Integer, String> allSubjectNameMap,
                                                         HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherMap) {
        CattyResult cattyResult = new CattyResult();

        for (SubjectGradeClassDTO x : subjectGradeClassTeacherMap.keySet()) {
            var teacherId = subjectGradeClassTeacherMap.get(x);
            if (teacherId == null) {
                var subjectName = allSubjectNameMap.get(x.getSubjectId());
                cattyResult.setMessage(x.getGrade() + "年级" + subjectName + " 科目没有教师带课");
                return cattyResult;
            }
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

}
