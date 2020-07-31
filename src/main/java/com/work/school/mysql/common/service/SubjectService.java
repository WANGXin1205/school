package com.work.school.mysql.common.service;

import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.mapper.SubjectDetailsMapper;
import com.work.school.mysql.common.dao.mapper.SubjectMapper;
import com.work.school.mysql.common.service.dto.*;
import com.work.school.mysql.common.service.dto.GradeClassWorkDayTimeDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.javassist.runtime.Inner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/3/6 9:18 PM
 * @Description
 */
@Service
public class SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    @Resource
    private SubjectMapper subjectMapper;
    @Resource
    private SubjectDetailsMapper subjectDetailsMapper;

    private static final double MAX_EXCITATION = 1;
    private static final double SUP_EXCITATION = 0.2;

    /**
     * 查询所有科目
     *
     * @return
     */
    public List<SubjectDO> listAllSubject() {
        return subjectMapper.listAllSubject();
    }

    /**
     * 查询所有科目明细和科目的信息
     *
     * @return
     */
    public List<SubjectDTO> listAllSubjectDTO() {
        return subjectDetailsMapper.listAllSubjectDTO();
    }

    /**
     * 所有科目对应名称map
     *
     * @return
     */
    public HashMap<Integer, String> getAllSubjectNameMap(List<SubjectDO> allSubject) {
        return (HashMap<Integer, String>) allSubject.stream().collect(Collectors.toMap(SubjectDO::getId, SubjectDO::getName));
    }

    /**
     * 所有科目根据年级转为map
     *
     * @param allSubjectDTO
     * @return
     */
    public HashMap<Integer, List<SubjectDTO>> getGradeSubjectMap(List<SubjectDTO> allSubjectDTO) {
        return (HashMap<Integer, List<SubjectDTO>>) allSubjectDTO.stream().collect(Collectors.groupingBy(SubjectDTO::getGrade));
    }

    /**
     * 将科目变为带权重的科目
     *
     * @param subjectDTOList
     * @return
     */
    public List<SubjectWeightDTO> copyAllSubjectToAllSubjectWeight(List<SubjectDTO> subjectDTOList) {
        List<SubjectWeightDTO> subjectWeightDTOList = new ArrayList<>();

        for (SubjectDTO x : subjectDTOList) {
            SubjectWeightDTO subjectWeightDTO = new SubjectWeightDTO();
            BeanUtils.copyProperties(x, subjectWeightDTO);
            subjectWeightDTOList.add(subjectWeightDTO);
        }

        return subjectWeightDTOList;
    }

    /**
     * 计算某个班级科目的最大权重，已经给出了初始权重
     *
     * @param order
     * @param maxOrder
     * @param computerSubjectWeightDTO
     */
    public void computerSubjectWeightDTO(Integer order, Integer maxOrder, ComputerSubjectWeightDTO computerSubjectWeightDTO) {

        Integer grade = computerSubjectWeightDTO.getGrade();
        Integer classNum = computerSubjectWeightDTO.getClassNum();
        Integer workDay = computerSubjectWeightDTO.getWorkDay();
        Integer time = computerSubjectWeightDTO.getTime();
        List<SubjectWeightDTO> subjectWeightDTOList = computerSubjectWeightDTO.getSubjectWeightDTOList();
        var gradeClassWorkDaySubjectCountMap = computerSubjectWeightDTO.getGradeClassNumWorDaySubjectCountMap();
        var timeTableMap = computerSubjectWeightDTO.getTimeTableMap();
        var classWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        HashMap<Integer, Integer> maxClassroomMap = computerSubjectWeightDTO.getClassroomMaxCapacityMap();

        //首先判断是周几的第几节课，如果不是特殊课程，走普通课程的流程，如果是特殊课程，走特殊课程的流程。
        boolean classMeetingFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(workDay) && SchoolTimeTableDefaultValueDTO.getClassMeetingTime().equals(time);
        boolean writingFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(workDay) && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(time);
        boolean schoolBasedFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(workDay) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
        boolean specialFlag = classMeetingFlag || writingFlag || schoolBasedFlag;
        if (specialFlag) {
            if (classMeetingFlag) {
                this.computerSpecialWeight(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId(), subjectWeightDTOList);
            }
            if (writingFlag) {
                this.computerSpecialWeight(SchoolTimeTableDefaultValueDTO.getWritingId(), subjectWeightDTOList);
            }
            if (schoolBasedFlag) {
                this.computerSpecialWeight(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId(), subjectWeightDTOList);
            }
        }
        if (!specialFlag) {
            // 早上必须有主课
            boolean firFlag = SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time);
            if (firFlag) {
                this.makeChineseOrMathWeight(subjectWeightDTOList);
            }

            boolean secTime = SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time);
            if (secTime) {
                var firstClassSubjectId = timeSubjectIdMap.get(SchoolTimeTableDefaultValueDTO.getMorningFirTime());
                this.makeChineseOrMathWeight(subjectWeightDTOList, firstClassSubjectId);
            }

            // 早上要有语文数学课
            boolean thirdTime = SchoolTimeTableDefaultValueDTO.getMorningLastTime().equals(time);
            if (thirdTime) {
                var firstClassSubjectId = timeSubjectIdMap.get(SchoolTimeTableDefaultValueDTO.getMorningFirTime());
                var secClassSubjectId = timeSubjectIdMap.get(SchoolTimeTableDefaultValueDTO.getMorningSecTime());
                this.makeChineseOrMathMaxWeight(subjectWeightDTOList, firstClassSubjectId, secClassSubjectId);
            }

            var random = (SUP_EXCITATION + (MAX_EXCITATION - SUP_EXCITATION)  * (maxOrder - order) / maxOrder) * SubjectWeightDefaultValueDTO.getExtendWeight();
            // 如果下午，围棋权重增加
            if (time > SchoolTimeTableDefaultValueDTO.getMorningLastTime()) {
                for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                    if (subjectWeightDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectGoId())) {
                        int weight = (int) (random  * subjectWeightDTO.getFrequency());
                        subjectWeightDTO.setWeight(weight + subjectWeightDTO.getWeight());
                    }
                }
            }

            // 如果上午最后一节，下午最后三节课，体育课权重增加
            if (time >= SchoolTimeTableDefaultValueDTO.getAfternoonSecTime() || time.equals(SchoolTimeTableDefaultValueDTO.getMorningLastTime())) {
                for (SubjectWeightDTO subjectWeightDTO : subjectWeightDTOList) {
                    if (subjectWeightDTO.getSubjectId().equals(SchoolTimeTableDefaultValueDTO.getSubjectSportId())) {
                        int weight = (int) (random * subjectWeightDTO.getFrequency());
                        subjectWeightDTO.setWeight(weight + subjectWeightDTO.getWeight());
                    }
                }
            }

            var subjectGradeClassTeacherCountMap = computerSubjectWeightDTO.getSubjectGradeClassTeacherCountMap();
            // 小课要优先排课
            this.makeSmallSubjectWeight(order, maxOrder, subjectWeightDTOList);
            // 如果一个老师带多个班级的小课，要优先排课
            this.makeTeacherHaveManyClassWeight(order, maxOrder, grade, classNum, subjectWeightDTOList, subjectGradeClassTeacherCountMap);
            // 如果这节课程的数目很多，要优先排课
            this.makeSubjectNumWeight(order, maxOrder, subjectWeightDTOList);
            // 如果这节课是需要教室的课程，需要优先排课
            this.makeOtherNeedClassroomSubjectWeight(order, maxOrder, subjectWeightDTOList, maxClassroomMap);

            // 保证每天有主课上
            this.makeSureChineseOrMathWeight(workDay, subjectWeightDTOList);

            // 判断学生的连堂课(上限为3)，如果连堂课则清零
            var gradeClassWorkDayTimeDTO = this.packGradeClassWorkDayTimeDTO(grade, classNum, workDay, time);
            this.clearStudentContinueSubject(gradeClassWorkDayTimeDTO, subjectWeightDTOList, timeTableMap);

            // 判断每门课程的上限(上限为2)，如果一天上同一门主课2次则权重清零
            this.clearRepeatSubject(grade, classNum, workDay, time, subjectWeightDTOList, gradeClassWorkDaySubjectCountMap);

            // 判断教师的连堂课(上限为4)，如果当天该老师连堂课，那么连堂的那节课所带的所有课程权重为0
            // 一个教师一天最多上4节课，若果超过4节课，权重清零
            var orderTeacherWorkDayTimeMap = computerSubjectWeightDTO.getOrderTeacherWorkDayTimeMap();
            var teacherTeachingMap = this.getTeacherTeachingMap(orderTeacherWorkDayTimeMap);
            var teacherSubjectListMap = computerSubjectWeightDTO.getTeacherSubjectListMap();
            this.clearTeacherContinueAndMaxTimeSubject(gradeClassWorkDayTimeDTO, subjectWeightDTOList, teacherTeachingMap, teacherSubjectListMap);

            // 如果当天上过这个小课，则不能再排课，设置权重为0
            this.clearSmallSubjectWeight(subjectWeightDTOList, timeSubjectIdMap);

            // 非特殊时间，特殊课程权重设置为0
            this.clearSpecialWeight(subjectWeightDTOList);
        }

    }

    /**
     * 计算某个班级科目的最大权重，已经给出了初始权重
     *
     * @param computerSubjectWeightDTO
     * @return
     */
    public void computerSubjectWeightDTO(ComputerSubjectWeightDTO computerSubjectWeightDTO) {

        Integer grade = computerSubjectWeightDTO.getGrade();
        Integer classNum = computerSubjectWeightDTO.getClassNum();
        Integer workDay = computerSubjectWeightDTO.getWorkDay();
        Integer time = computerSubjectWeightDTO.getTime();
        List<SubjectWeightDTO> subjectWeightDTOList = computerSubjectWeightDTO.getSubjectWeightDTOList();
        var gradeClassWorkDaySubjectCountMap = computerSubjectWeightDTO.getGradeClassNumWorDaySubjectCountMap();
        var timeTableMap = computerSubjectWeightDTO.getTimeTableMap();
        var classWorkDayTimeSubjectIdMap = timeTableMap.get(grade);
        var workDayTimeSubjectIdMap = classWorkDayTimeSubjectIdMap.get(classNum);
        var timeSubjectIdMap = workDayTimeSubjectIdMap.get(workDay);
        HashMap<Integer, Integer> maxClassroomMap = computerSubjectWeightDTO.getClassroomMaxCapacityMap();

        //首先判断是周几的第几节课，如果不是特殊课程，走普通课程的流程，如果是特殊课程，走特殊课程的流程。
        boolean classMeetingFlag = SchoolTimeTableDefaultValueDTO.getMondayNum().equals(workDay) && SchoolTimeTableDefaultValueDTO.getClassMeetingTime().equals(time);
        boolean writingFlag = SchoolTimeTableDefaultValueDTO.getWednesdayNum().equals(workDay) && SchoolTimeTableDefaultValueDTO.getWritingTime().equals(time);
        boolean schoolBasedFlag = SchoolTimeTableDefaultValueDTO.getFridayNum().equals(workDay) && Arrays.asList(SchoolTimeTableDefaultValueDTO.getSchoolBasedTime()).contains(time);
        boolean specialFlag = classMeetingFlag || writingFlag || schoolBasedFlag;
        if (specialFlag) {
            if (classMeetingFlag) {
                this.computerSpecialWeight(SchoolTimeTableDefaultValueDTO.getSubjectClassMeetingId(), subjectWeightDTOList);
            }
            if (writingFlag) {
                this.computerSpecialWeight(SchoolTimeTableDefaultValueDTO.getWritingId(), subjectWeightDTOList);
            }
            if (schoolBasedFlag) {
                this.computerSpecialWeight(SchoolTimeTableDefaultValueDTO.getSubjectSchoolBasedId(), subjectWeightDTOList);
            }
        }
        if (!specialFlag) {
            // 早上必须有主课
            boolean firFlag = SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time);
            if (firFlag) {
                this.makeChineseOrMathWeight(subjectWeightDTOList);
            }

            boolean secTime = SchoolTimeTableDefaultValueDTO.getMorningSecTime().equals(time);
            if (secTime) {
                var firstClassSubjectId = timeSubjectIdMap.get(SchoolTimeTableDefaultValueDTO.getMorningFirTime());
                this.makeChineseOrMathWeight(subjectWeightDTOList, firstClassSubjectId);
            }

            // 早上要有语文数学课
            boolean thirdTime = SchoolTimeTableDefaultValueDTO.getMorningLastTime().equals(time);
            if (thirdTime) {
                var firstClassSubjectId = timeSubjectIdMap.get(SchoolTimeTableDefaultValueDTO.getMorningFirTime());
                var secClassSubjectId = timeSubjectIdMap.get(SchoolTimeTableDefaultValueDTO.getMorningSecTime());
                this.makeChineseOrMathMaxWeight(subjectWeightDTOList, firstClassSubjectId, secClassSubjectId);
            }

            var subjectGradeClassTeacherCountMap = computerSubjectWeightDTO.getSubjectGradeClassTeacherCountMap();
            // 小课要优先排课
            this.makeSmallSubjectWeight(subjectWeightDTOList);
            // 如果一个老师带多个班级的小课，要优先排课
            this.makeTeacherHaveManyClassWeight(grade, classNum, subjectWeightDTOList, subjectGradeClassTeacherCountMap);
            // 如果这节课程的数目很多，要优先排课
            this.makeSubjectNumWeight(subjectWeightDTOList);
            // 如果这节课是需要教室的课程，需要优先排课
            this.makeOtherNeedClassroomSubjectWeight(subjectWeightDTOList, maxClassroomMap);

            // 保证每天有主课上
            this.makeSureChineseOrMathWeight(workDay, subjectWeightDTOList);

            // 判断学生的连堂课(上限为3)，如果连堂课则清零
            var gradeClassWorkDayTimeDTO = this.packGradeClassWorkDayTimeDTO(grade, classNum, workDay, time);
            this.clearStudentContinueSubject(gradeClassWorkDayTimeDTO, subjectWeightDTOList, timeTableMap);

            // 判断每门课程的上限(上限为2)，如果一天上同一门主课2次则权重清零
            this.clearRepeatSubject(grade, classNum, workDay, time, subjectWeightDTOList, gradeClassWorkDaySubjectCountMap);

            // 判断教师的连堂课(上限为4)，如果当天该老师连堂课，那么连堂的那节课所带的所有课程权重为0
            // 一个教师一天最多上4节课，若果超过4节课，权重清零
            var orderTeacherWorkDayTimeMap = computerSubjectWeightDTO.getOrderTeacherWorkDayTimeMap();
            var teacherTeachingMap = this.getTeacherTeachingMap(orderTeacherWorkDayTimeMap);
            var teacherSubjectListMap = computerSubjectWeightDTO.getTeacherSubjectListMap();
            this.clearTeacherContinueAndMaxTimeSubject(gradeClassWorkDayTimeDTO, subjectWeightDTOList, teacherTeachingMap, teacherSubjectListMap);

            // 如果当天上过这个小课，则不能再排课，设置权重为0
            this.clearSmallSubjectWeight(subjectWeightDTOList, timeSubjectIdMap);

            // 非特殊时间，特殊课程权重设置为0
            this.clearSpecialWeight(subjectWeightDTOList);
        }

    }

    /**
     * 获取教师每天的工作时间
     *
     * @param orderTeacherWorkDayTimeMap
     * @return
     */
    private HashMap<Integer, HashMap<Integer, List<Integer>>> getTeacherTeachingMap(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> orderTeacherWorkDayTimeMap) {
        HashMap<Integer, HashMap<Integer, List<Integer>>> teacherWorkDayTimesMap = new HashMap<>();
        var teacherWorkDayTimeList = orderTeacherWorkDayTimeMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
        for (HashMap<Integer, HashMap<Integer, Integer>> teacherWorkDayTimeMap : teacherWorkDayTimeList) {
            for (Integer teacherId : teacherWorkDayTimeMap.keySet()) {
                var workDayTimeMap = teacherWorkDayTimeMap.get(teacherId);
                for (Integer workDay : workDayTimeMap.keySet()) {
                    var time = workDayTimeMap.get(workDay);
                    var workDayTimesMap = teacherWorkDayTimesMap.get(teacherId);
                    if (workDayTimesMap == null) {
                        List<Integer> times = new ArrayList<>();
                        times.add(time);
                        workDayTimesMap = new HashMap<>();
                        workDayTimesMap.put(workDay, times);
                        teacherWorkDayTimesMap.put(teacherId, workDayTimesMap);
                    } else {
                        var times = workDayTimesMap.get(workDay);
                        if (CollectionUtils.isEmpty(times)) {
                            times = new ArrayList<>();
                            times.add(time);
                            workDayTimesMap.put(workDay, times);
                            teacherWorkDayTimesMap.put(teacherId, workDayTimesMap);
                        } else {
                            if (!times.contains(time)) {
                                times.add(time);
                                workDayTimesMap.put(workDay, times);
                                teacherWorkDayTimesMap.put(teacherId, workDayTimesMap);
                            }
                        }
                    }

                }
            }
        }

        return teacherWorkDayTimesMap;
    }


    /**
     * 计算特殊课程的权重
     *
     * @param specialClassId
     * @param subjectWeightDTOList
     * @return
     */
    private void computerSpecialWeight(Integer specialClassId, List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (specialClassId.equals(x.getSubjectId())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
            if (!specialClassId.equals(x.getSubjectId())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
            }
        }
    }

    /**
     * 不在特殊时间，要清理特殊排课的权重
     *
     * @param subjectWeightDTOList
     */
    private void clearSpecialWeight(List<SubjectWeightDTO> subjectWeightDTOList) {
        subjectWeightDTOList.stream().filter(x -> SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(x.getType())).
                filter(x -> x.getFrequency() > SubjectWeightDefaultValueDTO.getZeroFrequency())
                .forEach(x -> x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight()));
    }

    /**
     * 赋予语文或者数学最大权重
     *
     * @param subjectWeightDTOList
     * @param firstSubjectId
     * @return
     */
    private void makeChineseOrMathMaxWeight(List<SubjectWeightDTO> subjectWeightDTOList, Integer firstSubjectId, Integer secSubjectId) {
        boolean chineseFlag = SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(firstSubjectId) || SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(secSubjectId);
        boolean mathsFlag = SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(firstSubjectId) || SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(secSubjectId);
        if (chineseFlag && mathsFlag) {
            return;
        }
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (chineseFlag && SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(x.getSubjectId())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
            if (mathsFlag && SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(x.getSubjectId())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
        }
    }

    /**
     * 赋予语文或者数学权重
     *
     * @param subjectWeightDTOList
     */
    private void makeChineseOrMathWeight(List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(x.getType())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
        }
    }

    /**
     * 赋予语文或者数学权重
     *
     * @param subjectWeightDTOList
     */
    private void makeChineseOrMathWeight(List<SubjectWeightDTO> subjectWeightDTOList, Integer firstSubjectId) {
        boolean chineseFlag = SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(firstSubjectId);
        boolean mathsFlag = SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(firstSubjectId);
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (chineseFlag && SchoolTimeTableDefaultValueDTO.getSubjectMathsId().equals(x.getSubjectId())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
            if (mathsFlag && SchoolTimeTableDefaultValueDTO.getSubjectChineseId().equals(x.getSubjectId())) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMaxWeight());
            }
        }
    }

    /**
     * 保证每天有主课
     *
     * @param workDay
     * @param subjectWeightDTOList
     */
    private void makeSureChineseOrMathWeight(Integer workDay, List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (SchoolTimeTableDefaultValueDTO.getMainSubjectType().equals(x.getType()) && x.getFrequency() <= (SchoolTimeTableDefaultValueDTO.getFridayNum() - workDay)) {
                x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
            }
        }
    }

    /**
     * 小课要优先排课
     *
     * @param subjectWeightDTOList
     */
    private void makeSmallSubjectWeight(Integer order, Integer maxOrder, List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            boolean otherSubjectFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(x.getType())
                    || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(x.getType());
            if (otherSubjectFlag) {
                var weight = x.getFrequency() * (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType() - x.getType()) * SubjectWeightDefaultValueDTO.getExtendWeight();
                weight = (int) (Math.random() * weight + x.getWeight());
                x.setWeight(weight);
            }
        }
    }

    /**
     * 小课要优先排课
     *
     * @param subjectWeightDTOList
     */
    private void makeSmallSubjectWeight(List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            boolean otherSubjectFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(x.getType())
                    || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(x.getType());
            if (otherSubjectFlag) {
                var weight = x.getFrequency() * (SchoolTimeTableDefaultValueDTO.getSpecialSubjectType() - x.getType()) * SubjectWeightDefaultValueDTO.getExtendWeight();
                x.setWeight(x.getWeight() + weight);
            }
        }
    }

    /**
     * 如果一个老师带多个班级的小课，要优先排课
     *
     * @param grade
     * @param classNum
     * @param subjectWeightDTOList
     * @param subjectGradeClassTeacherCountMap
     */
    public void makeTeacherHaveManyClassWeight(Integer order,
                                               Integer maxOrder,
                                               Integer grade,
                                               Integer classNum,
                                               List<SubjectWeightDTO> subjectWeightDTOList,
                                               HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(x.getType())) {
                SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
                subjectGradeClassDTO.setGrade(grade);
                subjectGradeClassDTO.setClassNum(classNum);
                subjectGradeClassDTO.setSubjectId(x.getSubjectId());
                Integer count = subjectGradeClassTeacherCountMap.get(subjectGradeClassDTO);
                Integer weight = x.getWeight() + (int) (Math.random()  * count * x.getFrequency() * SubjectWeightDefaultValueDTO.getExtendWeight());
                x.setWeight(weight);
            }
        }
    }


    /**
     * 如果一个老师带多个班级的小课，要优先排课
     *
     * @param grade
     * @param classNum
     * @param subjectWeightDTOList
     * @param subjectGradeClassTeacherCountMap
     */
    public void makeTeacherHaveManyClassWeight(Integer grade,
                                               Integer classNum,
                                               List<SubjectWeightDTO> subjectWeightDTOList,
                                               HashMap<SubjectGradeClassDTO, Integer> subjectGradeClassTeacherCountMap) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (!SchoolTimeTableDefaultValueDTO.getSpecialSubjectType().equals(x.getType())) {

                SubjectGradeClassDTO subjectGradeClassDTO = new SubjectGradeClassDTO();
                subjectGradeClassDTO.setGrade(grade);
                subjectGradeClassDTO.setClassNum(classNum);
                subjectGradeClassDTO.setSubjectId(x.getSubjectId());
                var teachingCount = subjectGradeClassTeacherCountMap.get(subjectGradeClassDTO);
                Integer weight = x.getWeight() + (int) (Math.random() * x.getFrequency() * teachingCount * SubjectWeightDefaultValueDTO.getExtendWeight());
                x.setWeight(weight);
            }
        }
    }

    /**
     * 按照课程次数排课
     *
     * @param order
     * @param maxOrder
     * @param subjectWeightDTOList
     */
    public void makeSubjectNumWeight(Integer order, Integer maxOrder, List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            var weight = x.getWeight() + (int) (Math.random()  * x.getFrequency() * SubjectWeightDefaultValueDTO.getExtendWeight());
            x.setWeight(weight);
        }
    }

    /**
     * 按照课程次数排课
     *
     * @param subjectWeightDTOList
     * @return
     */
    public void makeSubjectNumWeight(List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            var weight = x.getWeight() + (int) (Math.random() * x.getFrequency() * SubjectWeightDefaultValueDTO.getExtendWeight());
            x.setWeight(weight);
        }
    }

    /**
     * 为需要教室的班级排课
     *
     * @param order
     * @param maxOrder
     * @param subjectWeightDTOList
     * @param maxClassroomMap
     */
    private void makeOtherNeedClassroomSubjectWeight(Integer order, Integer maxOrder, List<SubjectWeightDTO> subjectWeightDTOList, HashMap<Integer, Integer> maxClassroomMap) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(x.getType())) {
                var maxCount = maxClassroomMap.get(x.getSubjectId());
                Integer weight = x.getWeight() + (int) ((SchoolTimeTableDefaultValueDTO.getClassroomMaxCount() - maxCount) * SubjectWeightDefaultValueDTO.getExtendWeight()
                        * Math.random());
                x.setWeight(weight);
            }
        }
    }

    /**
     * 为需要教室的班级排课
     *
     * @param subjectWeightDTOList
     * @param maxClassroomMap
     */
    private void makeOtherNeedClassroomSubjectWeight(List<SubjectWeightDTO> subjectWeightDTOList, HashMap<Integer, Integer> maxClassroomMap) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(x.getType())) {
                var roomCount = maxClassroomMap.get(x.getSubjectId());
                Integer weight = x.getWeight() + (int) ((SchoolTimeTableDefaultValueDTO.getClassroomMaxCount()) * SubjectWeightDefaultValueDTO.getExtendWeight()
                        * (Math.random() * roomCount));
                x.setWeight(weight);
            }
        }
    }

    /**
     * 组装GradeClassWorkDayTimeDTO
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @return
     */
    private GradeClassWorkDayTimeDTO packGradeClassWorkDayTimeDTO(Integer grade, Integer classNum, Integer workDay, Integer time) {
        GradeClassWorkDayTimeDTO gradeClassWorkDayTimeDTO = new GradeClassWorkDayTimeDTO();

        gradeClassWorkDayTimeDTO.setGrade(grade);
        gradeClassWorkDayTimeDTO.setClassNum(classNum);
        gradeClassWorkDayTimeDTO.setWorkDay(workDay);
        gradeClassWorkDayTimeDTO.setTime(time);

        return gradeClassWorkDayTimeDTO;
    }

    /**
     * 清除重复排课的小课权重
     *
     * @param subjectWeightDTOList
     * @param timeSubjectIdMap
     */
    public void clearSmallSubjectWeight(List<SubjectWeightDTO> subjectWeightDTOList,
                                        HashMap<Integer, Integer> timeSubjectIdMap) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            boolean otherSubjectFlag = SchoolTimeTableDefaultValueDTO.getOtherSubjectType().equals(x.getType())
                    || SchoolTimeTableDefaultValueDTO.getOtherNeedAreaSubjectType().equals(x.getType());
            if (otherSubjectFlag) {
                var subjectIdS = timeSubjectIdMap.values().stream().distinct().collect(Collectors.toList());
                if (subjectIdS.contains(x.getSubjectId())) {
                    x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
                }
            }
        }

    }

    /**
     * 清理每天重复的课程
     *
     * @param grade
     * @param classNum
     * @param workDay
     * @param time
     * @param subjectWeightDTOList
     * @param gradeClassNumWorDaySubjectCountMap
     */
    public void clearRepeatSubject(Integer grade, Integer classNum, Integer workDay, Integer time, List<SubjectWeightDTO> subjectWeightDTOList,
                                   HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> gradeClassNumWorDaySubjectCountMap) {
        if (SchoolTimeTableDefaultValueDTO.getMorningFirTime().equals(time)) {
            return;
        }
        var classNumWorDaySubjectCountMap = gradeClassNumWorDaySubjectCountMap.get(grade);
        var worDaySubjectCountMap = classNumWorDaySubjectCountMap.get(classNum);
        var repeatSubjectMap = worDaySubjectCountMap.get(workDay);

        for (SubjectWeightDTO subject : subjectWeightDTOList) {
            var count = repeatSubjectMap.get(subject.getSubjectId());
            if (count >= SchoolTimeTableDefaultValueDTO.getSubjectContinueTime()) {
                subject.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
            }
        }
    }

    /**
     * 清理学生上的连堂课
     *
     * @param gradeClassWorkDayTimeDTO
     * @param subjectWeightDTOList
     * @param timeTableMap
     */
    public void clearStudentContinueSubject(GradeClassWorkDayTimeDTO gradeClassWorkDayTimeDTO,
                                            List<SubjectWeightDTO> subjectWeightDTOList,
                                            HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        var subjectId = this.filterStudentContinueSubjectId(gradeClassWorkDayTimeDTO, timeTableMap);
        if (subjectId != null) {
            subjectWeightDTOList.stream().filter(x -> subjectId.equals(x.getSubjectId())).forEach(x -> x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight()));
        }

    }

    /**
     * 清理教师的连堂课
     *
     * @param gradeClassWorkDayTimeDTO
     * @param subjectWeightDTOList
     * @param teacherTeachingMap
     * @param teacherSubjectListMap
     */
    public void clearTeacherContinueAndMaxTimeSubject(GradeClassWorkDayTimeDTO gradeClassWorkDayTimeDTO,
                                                      List<SubjectWeightDTO> subjectWeightDTOList,
                                                      HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap,
                                                      Map<Integer, List<SubjectTeacherGradeClassDTO>> teacherSubjectListMap) {
        Integer teacherId = this.filterTeacherContinueAndMaxTimeTeacherId(gradeClassWorkDayTimeDTO, teacherTeachingMap);
        if (teacherId != null) {
            var subjectTeacherGradeClassDTOList = teacherSubjectListMap.get(teacherId);
            var subjectIdList = subjectTeacherGradeClassDTOList.stream().map(SubjectTeacherGradeClassDTO::getSubjectId).collect(Collectors.toList());
            subjectWeightDTOList.forEach(x -> {
                if (subjectIdList.contains(x.getSubjectId())) {
                    x.setWeight(SubjectWeightDefaultValueDTO.getMinWeight());
                }
            });
        }

    }

    /**
     * 筛选出教师上连堂课和最大课程的教师id
     *
     * @param gradeClassWorkDayTimeDTO
     * @param teacherTeachingMap
     * @return
     */
    private Integer filterTeacherContinueAndMaxTimeTeacherId(GradeClassWorkDayTimeDTO gradeClassWorkDayTimeDTO,
                                                             HashMap<Integer, HashMap<Integer, List<Integer>>> teacherTeachingMap) {
        Integer teacherId = null;
        Integer pastTime = this.getPastTime(gradeClassWorkDayTimeDTO.getTime());
        for (Integer x : teacherTeachingMap.keySet()) {
            var teacherWorkDayMap = teacherTeachingMap.get(x);
            var teachingTimeList = teacherWorkDayMap.get(gradeClassWorkDayTimeDTO.getWorkDay());
            if (CollectionUtils.isEmpty(teachingTimeList)) {
                continue;
            }
            for (Integer[] y : SchoolTimeTableDefaultValueDTO.getTeacherContinueTime()) {
                if (y[SchoolTimeTableDefaultValueDTO.getTeacherContinueTimeLastIndex()].equals(pastTime)) {
                    boolean flag = (SchoolTimeTableDefaultValueDTO.getTeacherContinueTimeMaxSize().equals(teachingTimeList.size()) && teachingTimeList.contains(pastTime))
                            || SchoolTimeTableDefaultValueDTO.getTeacherTimeMinOverSize().equals(teachingTimeList.size());
                    if (flag) {
                        teacherId = x;
                    }

                }
            }

        }

        return teacherId;
    }

    /**
     * 筛选出学生的连堂课
     *
     * @param gradeClassWorkDayTimeDTO
     * @param timeTableMap
     * @return
     */
    private Integer filterStudentContinueSubjectId(GradeClassWorkDayTimeDTO gradeClassWorkDayTimeDTO,
                                                   HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> timeTableMap) {
        Integer subjectId = null;
        Integer pastTime = this.getPastTime(gradeClassWorkDayTimeDTO.getTime());

        var classWorkDayTimeMap = timeTableMap.get(gradeClassWorkDayTimeDTO.getGrade());
        var workDayTimeMap = classWorkDayTimeMap.get(gradeClassWorkDayTimeDTO.getClassNum());
        var timeMap = workDayTimeMap.get(gradeClassWorkDayTimeDTO.getWorkDay());
        for (Integer[] x : SchoolTimeTableDefaultValueDTO.getStudentContinueTime()) {
            if (x[SchoolTimeTableDefaultValueDTO.getStudentContinueTimeLastIndex()].equals(pastTime)) {
                List<Integer> subjectIdList = new ArrayList<>();
                for (Integer y : x) {
                    subjectId = timeMap.get(y);
                    if (subjectIdList.contains(subjectId)) {
                        return subjectId;
                    }
                    if (!subjectIdList.contains(subjectId)) {
                        subjectIdList.add(subjectId);
                    }
                }
            }
        }

        return subjectId;
    }

    /**
     * 获取上一节课的时间
     *
     * @param time
     * @return
     */
    private Integer getPastTime(Integer time) {
        Integer pastTime = null;
        if (SchoolTimeTableDefaultValueDTO.getStartClassTimeIndex().equals(time)) {
            return null;
        }
        if (time > SchoolTimeTableDefaultValueDTO.getStartClassTimeIndex()) {
            pastTime = time - SchoolTimeTableDefaultValueDTO.getPastTime();
        }
        return pastTime;
    }


}
