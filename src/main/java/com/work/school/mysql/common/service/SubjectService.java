package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.common.dao.domain.SubjectDO;
import com.work.school.mysql.common.dao.mapper.SubjectMapper;
import com.work.school.mysql.common.service.dto.ClassSubjectKeyDTO;
import com.work.school.mysql.common.service.dto.InitSubjectWeightDTO;
import com.work.school.mysql.common.service.dto.SubjectWeightDTO;
import com.work.school.mysql.timetable.service.dto.TimeTableKeyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Integer MORNING_SEC_CLASS_NUM = 2;
    /**
     * 早上最后一节课
     */
    private static final Integer MORNING_LAST_CLASS_NUM = 3;


    private static final Integer MONDAY_NUM = 1;
    private static final Integer FRIDAY_NUM = 5;
    private static final Integer CLASS_MEETING_TIME = 7;
    private static final Integer[] SCHOOL_BASED_TIME = {4, 5, 6, 7};

    /**
     * 主课类型
     */
    private static final Integer MAIN_SUBJECT_TYPE = 1;
    /**
     * 小课类型
     */
    private static final Integer OTHER_SUBJECT_TYPE = 2;
    /**
     * 固定课程类型
     */
    private static final Integer SPECIAL_SUBJECT_TYPE = 3;
    /**
     * 班队会课 编号
     */
    private static final Integer SUBJECT_CLASS_MEETING_ID = 10;
    /**
     * 校本课程课 编号
     */
    private static final Integer SUBJECT_SCHOOL_BASED_ID = 9;
    /**
     * 初始权重
     */
    private static final Integer INIT_WEIGHT = 50;
    /**
     * 最大权重
     */
    private static final Integer MAX_WEIGHT = 100;
    /**
     * 最小权重
     */
    private static final Integer MIN_WEIGHT = 0;
    /**
     * 早上下午上课 特殊科目添加的权重
     */
    private static final Integer SPECIAL_TIME_WEIGHT = 7;
    /**
     * 每次减少权重
     */
    private static final Integer STEP_WEIGHT = 1;
    /**
     * 0次
     */
    private static final Integer ZERO_FREQUENCY = 0;
    /**
     * 第一节课
     */
    private static final Integer TIME_START = 1;
    /**
     * 语文课id
     */
    private static final Integer CHINESE_SUBJECT_ID = 1;
    /**
     * 数学课id
     */
    private static final Integer MATHS_SUBJECT_ID = 2;
    /**
     * 第二权重
     */
    private static final Integer SECOND_MAX_WEIGHT_INDEX = 2;

    @Resource
    private SubjectMapper subjectMapper;

    /**
     * 查询所有科目
     *
     * @return
     */
    public List<SubjectDO> listAllSubjectByGrade(Integer grade) {
        return subjectMapper.listAllSubjectByGrade(grade);
    }

    /**
     * 获取 科目类型 的Map
     *
     * @param subjectDOList
     * @return
     */
    public CattyResult<Map<Integer, Integer>> listSubjectTypeMap(List<SubjectDO> subjectDOList) {
        CattyResult<Map<Integer, Integer>> cattyResult = new CattyResult<>();

        Map<Integer, Integer> subjectTypeMap = subjectDOList.stream().collect(Collectors.toMap(SubjectDO::getId, SubjectDO::getType));

        cattyResult.setData(subjectTypeMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 初始化权重
     *
     * @param initSubjectWeightDTO
     * @return
     */
    public CattyResult<List<SubjectWeightDTO>> initSubjectWeight(InitSubjectWeightDTO initSubjectWeightDTO) {
        CattyResult<List<SubjectWeightDTO>> cattyResult = new CattyResult<>();

        var workDay = initSubjectWeightDTO.getWorkDay();
        var classNum = initSubjectWeightDTO.getClassNum();
        var time = initSubjectWeightDTO.getTime();
        var subjectWeightDTOList = initSubjectWeightDTO.getSubjectWeightDTOList();
        var classSubjectTeachingNumMap = initSubjectWeightDTO.getClassSubjectTeachingNumMap();
        var timeTableMap = initSubjectWeightDTO.getTimeTableMap();

        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (!SPECIAL_SUBJECT_TYPE.equals(x.getType())) {
                // 如果一个老师带多个班，要适当加上权重
                ClassSubjectKeyDTO classSubjectKeyDTO = new ClassSubjectKeyDTO();
                classSubjectKeyDTO.setClassNum(classNum);
                classSubjectKeyDTO.setSubjectId(x.getId());
                var teachingNum = classSubjectTeachingNumMap.get(classSubjectKeyDTO);

                // 权重为上课次数 + 教师所带班级
                Integer initWeight = INIT_WEIGHT + x.getFrequency() * 2 + teachingNum;
                x.setWeight(initWeight);

                // 上午和下午课程赋值
                if (time <= MORNING_LAST_CLASS_NUM) {
                    if (MAIN_SUBJECT_TYPE.equals(x.getType())) {
                        Integer weight = x.getWeight() + SPECIAL_TIME_WEIGHT;
                        x.setWeight(weight);
                    }
                }
                if (time > MORNING_LAST_CLASS_NUM) {
                    if (OTHER_SUBJECT_TYPE.equals(x.getType())) {
                        Integer weight = x.getWeight() + SPECIAL_TIME_WEIGHT;
                        x.setWeight(weight);
                    }
                }

                // 如果 已经排过课 则适当的减去权重
                List<Integer> morningClassList = new ArrayList<>();
                for (Integer y = TIME_START; y < time; y++) {
                    TimeTableKeyDTO timeTableKeyDTO = this.packTimeTableKeyDTO(workDay, classNum, y);
                    Integer subjectId = timeTableMap.get(timeTableKeyDTO);
                    boolean repeatFlag = classNum.equals(x.getClassNum()) && subjectId.equals(x.getId());
                    if (repeatFlag) {
                        x.setWeight(x.getWeight() - STEP_WEIGHT);
                    }

                    // 如果早上没有设立语文数学课，那么要提升权重，先保存一下
                    if (MORNING_SEC_CLASS_NUM.equals(time)) {
                        if (!morningClassList.contains(subjectId)) {
                            morningClassList.add(subjectId);
                        }
                    }

                }

                // 决定第二节课什么课
                if (MORNING_SEC_CLASS_NUM.equals(time)) {
                    boolean chineseFlag = morningClassList.contains(CHINESE_SUBJECT_ID);
                    boolean mathsFlag = morningClassList.contains(MATHS_SUBJECT_ID);
                    // 如果没有数学课，数学课权重最大
                    if (chineseFlag && !mathsFlag) {
                        if (MATHS_SUBJECT_ID.equals(x.getId())) {
                            x.setWeight(MAX_WEIGHT);
                        }

                    }
                    // 如果没有语文课，语文课权重最大
                    if (!chineseFlag && mathsFlag) {
                        if (CHINESE_SUBJECT_ID.equals(x.getId())) {
                            x.setWeight(MAX_WEIGHT);
                        }
                    }
                    // 如果没有语文和数学课，则要重新设定权重
                    if (!chineseFlag && !mathsFlag) {
                        throw new TransactionException("第一节和第二节课并非主课，请重新选课");
                    }
                }
            }

        }

        // 班队会
        boolean classMeetingTimeFlag = MONDAY_NUM.equals(workDay) && CLASS_MEETING_TIME.equals(time);
        if (classMeetingTimeFlag) {
            subjectWeightDTOList = this.computerClassMeetingWeight(subjectWeightDTOList);
        }
        // 校本课程
        boolean schoolBaseTimeFlag = FRIDAY_NUM.equals(workDay) && Arrays.asList(SCHOOL_BASED_TIME).contains(time);
        if (schoolBaseTimeFlag) {
            subjectWeightDTOList = this.computerSchoolBasedWeight(subjectWeightDTOList);
        }

        cattyResult.setData(subjectWeightDTOList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 计算班队会课程权重
     *
     * @param subjectWeightDTOList
     * @return
     */
    private List<SubjectWeightDTO> computerClassMeetingWeight(List<SubjectWeightDTO> subjectWeightDTOList) {

        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (SUBJECT_CLASS_MEETING_ID.equals(x.getId())) {
                x.setWeight(MAX_WEIGHT);
            }
            if (!SUBJECT_CLASS_MEETING_ID.equals(x.getId())) {
                x.setWeight(MIN_WEIGHT);
            }
        }

        return subjectWeightDTOList;
    }

    /**
     * 计算校本课程权重
     *
     * @param subjectWeightDTOList
     * @return
     */
    private List<SubjectWeightDTO> computerSchoolBasedWeight(List<SubjectWeightDTO> subjectWeightDTOList) {

        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (SUBJECT_SCHOOL_BASED_ID.equals(x.getId())) {
                x.setWeight(MAX_WEIGHT);
            }
            if (!SUBJECT_SCHOOL_BASED_ID.equals(x.getId())) {
                x.setWeight(MIN_WEIGHT);
            }
        }

        return subjectWeightDTOList;
    }

    /**
     * 计算最大权重
     *
     * @param classNum
     * @param subjectWeightDTOList
     * @param passFlag
     * @return
     */
    public CattyResult<SubjectWeightDTO> computerMaxSubjectWeight(Integer classNum,
                                                                  List<SubjectWeightDTO> subjectWeightDTOList,
                                                                  boolean passFlag) {
        CattyResult<SubjectWeightDTO> cattyResult = new CattyResult<>();

        // 先根据班级筛选出来需要的数据
        subjectWeightDTOList = subjectWeightDTOList.stream().filter(x -> x.getClassNum().equals(classNum))
                .filter(x -> !x.getFrequency().equals(ZERO_FREQUENCY)).collect(Collectors.toList());
        Optional<SubjectWeightDTO> optionalSubjectWeightDTO = subjectWeightDTOList.stream().max(Comparator.comparing(SubjectWeightDTO::getWeight));
        if (!optionalSubjectWeightDTO.isPresent()) {
            cattyResult.setMessage("未计算出最大权重课程");
            return cattyResult;
        }
        SubjectWeightDTO subjectWeightDTO = optionalSubjectWeightDTO.get();
        Integer maxSubjectWeightId = subjectWeightDTO.getId();

        if (!passFlag) {
            var weightList = subjectWeightDTOList.stream().filter(x -> classNum.equals(x.getClassNum()))
                    .map(SubjectWeightDTO::getWeight).distinct().collect(Collectors.toList());
            Collections.sort(weightList);
            Collections.reverse(weightList);
            if (weightList.size() > SECOND_MAX_WEIGHT_INDEX) {
                Integer secondWeight = weightList.get(SECOND_MAX_WEIGHT_INDEX);
                subjectWeightDTOList.stream().filter(x -> classNum.equals(x.getClassNum())).filter(x -> maxSubjectWeightId.equals(x.getId())).forEach(x -> {
                    x.setWeight(secondWeight - STEP_WEIGHT);
                });
            } else {
                subjectWeightDTOList.stream().filter(x -> classNum.equals(x.getClassNum())).filter(x -> maxSubjectWeightId.equals(x.getId())).forEach(x -> {
                    Integer weight = x.getWeight() - STEP_WEIGHT;
                    x.setWeight(weight);
                });
            }

        }

        optionalSubjectWeightDTO = subjectWeightDTOList.stream().max(Comparator.comparing(SubjectWeightDTO::getWeight));
        if (optionalSubjectWeightDTO.isPresent()) {
            subjectWeightDTO = optionalSubjectWeightDTO.get();
        }

        cattyResult.setData(subjectWeightDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 赋值后清除赋值的次数和所有的权重
     *
     * @param maxSubjectWeightDTO
     * @param subjectWeightDTOList
     * @return
     */
    public List<SubjectWeightDTO> clearSubjectWeightDTOList(SubjectWeightDTO maxSubjectWeightDTO, List<SubjectWeightDTO> subjectWeightDTOList) {
        for (SubjectWeightDTO x : subjectWeightDTOList) {
            if (x.getId().equals(maxSubjectWeightDTO.getId()) && x.getClassNum().equals(maxSubjectWeightDTO.getClassNum())) {
                x.setFrequency(x.getFrequency() - 1);
            }
            x.setWeight(0);
        }

        return subjectWeightDTOList;
    }

    /**
     * 拼装 TimeTableKey
     *
     * @param workDay
     * @param classNum
     * @param time
     * @return
     */
    private TimeTableKeyDTO packTimeTableKeyDTO(Integer workDay, Integer classNum, Integer time) {
        TimeTableKeyDTO timeTableKeyDTO = new TimeTableKeyDTO();

        timeTableKeyDTO.setWorkDay(workDay);
        timeTableKeyDTO.setClassNum(classNum);
        timeTableKeyDTO.setTime(time);

        return timeTableKeyDTO;
    }
}
