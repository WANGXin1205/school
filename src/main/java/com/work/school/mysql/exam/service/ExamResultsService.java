package com.work.school.mysql.exam.service;

import com.work.school.common.CattyResult;
import com.work.school.common.config.ExcelDataConfigure;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.common.utils.common.ExcelUtils;
import com.work.school.common.utils.common.POIUtils;
import com.work.school.common.utils.common.StringUtils;
import com.work.school.mysql.common.service.SubjectService;
import com.work.school.mysql.exam.service.dto.ExamResultDTO;
import com.work.school.mysql.exam.service.dto.ExamResultExcelDTO;
import com.work.school.mysql.exam.service.dto.ExamResultExcelMainDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Author : Growlithe
 * @Date : 2019/3/28 7:33 PM
 * @Description
 */
@Service
public class ExamResultsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamResultsService.class);


    private static final Integer DEFAULT_SCALE = 2;
    private static final BigDecimal RATE_UNIT = new BigDecimal("100");
    private static final BigDecimal MAX_SCORE = new BigDecimal("100");

    /**
     * 计算考试各类指标
     *
     * @param multipartFile
     * @return
     */
    public CattyResult<ExamResultDTO> computerExamTarget(MultipartFile multipartFile) {
        CattyResult<ExamResultDTO> cattyResult = new CattyResult<>();

        // 从excel中获取数据
        var getDataMapFromExcelResult = POIUtils.getDataMapFromExcel(multipartFile, ExcelDataConfigure.EXAM_RESULT_TITLE_NAME);
        if (!getDataMapFromExcelResult.isSuccess()) {
            cattyResult.setMessage(getDataMapFromExcelResult.getMessage());
            return cattyResult;
        }

        var dataMap = getDataMapFromExcelResult.getData();

        // 获取的数据转存为需要的格式
        var getExamResultExcelDTOFromDataMapResult = this.getExamResultExcelDTOFromDataMap(dataMap);
        if (!getExamResultExcelDTOFromDataMapResult.isSuccess()) {
            LOGGER.warn(getExamResultExcelDTOFromDataMapResult.getMessage());
            cattyResult.setMessage(getExamResultExcelDTOFromDataMapResult.getMessage());
            return cattyResult;
        }
        ExamResultExcelDTO examResultExcelDTO = getExamResultExcelDTOFromDataMapResult.getData();

        // 计算各种指标
        CattyResult<ExamResultDTO> getExamResultDTOResult = this.getExamResultDTO(examResultExcelDTO);
        if (!getExamResultDTOResult.isSuccess()) {
            LOGGER.warn(getExamResultDTOResult.getMessage());
            cattyResult.setMessage(getExamResultDTOResult.getMessage());
            return cattyResult;
        }
        ExamResultDTO examResultDTO = getExamResultDTOResult.getData();

        cattyResult.setData(examResultDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 从dataMap 中获取考试成绩信息
     *
     * @param dataMap
     * @return
     */
    private CattyResult<ExamResultExcelDTO> getExamResultExcelDTOFromDataMap(Map<Integer, String[]> dataMap) {
        CattyResult<ExamResultExcelDTO> cattyResult = new CattyResult<>();

        ExamResultExcelDTO examResultExcelDTO = new ExamResultExcelDTO();
        List<ExamResultExcelMainDTO> examResultExcelMainDTOList = new ArrayList<>();
        for (Integer x : dataMap.keySet()) {
            if (ExcelDataConfigure.EXAM_RESULT_CLASS_AND_SUBJECT_ROW.equals(x)) {
                String[] classNameAndSubjectInfo = dataMap.get(x);
                examResultExcelDTO = this.packClassNameAndSubjectName(examResultExcelDTO, classNameAndSubjectInfo);
            }

            if (ExcelDataConfigure.EXAM_RESULT_DATA_START_ROW < x) {
                String[] scoreInfo = dataMap.get(x);
                examResultExcelMainDTOList = this.packExamResultExcelMainDTOList(examResultExcelMainDTOList, scoreInfo);
            }
        }
        examResultExcelDTO.setExamResultExcelMainDTOList(examResultExcelMainDTOList);

        cattyResult.setData(examResultExcelDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 组装 班级 和 课程名称
     *
     * @param examResultExcelDTO
     * @param classNameAndSubjectInfo
     * @return
     */
    private ExamResultExcelDTO packClassNameAndSubjectName(ExamResultExcelDTO examResultExcelDTO, String[] classNameAndSubjectInfo) {
        String classNameInfo = classNameAndSubjectInfo[ExcelDataConfigure.EXAM_RESULT_CLASS_INDEX];
        String subjectNameInfo = classNameAndSubjectInfo[ExcelDataConfigure.EXAM_RESULT_SUBJECT_INDEX];

        try {
            String gradeClassName = classNameInfo.split(ExcelDataConfigure.EXAM_RESULT_SPILT_SIGN)[ExcelDataConfigure.EXAM_RESULT_CLASS_AND_SUBJECT_SPILT_INDEX];
            String[] gradeClassNameStr = gradeClassName.split(ExcelDataConfigure.GRADE_CLASS_SPILT_SIGN);
            Integer grade = Integer.valueOf(gradeClassNameStr[ExcelDataConfigure.GRADE_SPILT_INDEX]);
            String className = gradeClassNameStr[ExcelDataConfigure.GRADE_CLASS_INDEX];
            examResultExcelDTO.setGrade(grade);
            examResultExcelDTO.setClassNum(className);
        } catch (Exception e) {
            throw new TransactionException("未获取到班级信息");
        }

        try {
            String subjectName = subjectNameInfo.split(ExcelDataConfigure.EXAM_RESULT_SPILT_SIGN)[ExcelDataConfigure.EXAM_RESULT_CLASS_AND_SUBJECT_SPILT_INDEX];
            examResultExcelDTO.setSubjectName(subjectName);
        } catch (Exception e) {
            throw new TransactionException("未获取到科目信息");
        }

        return examResultExcelDTO;
    }

    /**
     * 组装成绩主要信息
     *
     * @param scoreInfo
     * @return
     */
    private List<ExamResultExcelMainDTO> packExamResultExcelMainDTOList(List<ExamResultExcelMainDTO> examResultExcelMainDTOList, String[] scoreInfo) {

        String studentClassIdStr = scoreInfo[ExcelDataConfigure.EXAM_RESULT_STUDENT_CLASS_ID_FIRST_INDEX];
        Integer studentClassId = Integer.valueOf(studentClassIdStr);
        String studentName = scoreInfo[ExcelDataConfigure.EXAM_RESULT_NAME_FIRST_INDEX];
        String scoreStr = scoreInfo[ExcelDataConfigure.EXAM_RESULT_SCORE_FIRST_INDEX];
        BigDecimal score = new BigDecimal(scoreStr);

        ExamResultExcelMainDTO examResultExcelMainDTO = new ExamResultExcelMainDTO();
        examResultExcelMainDTO.setStudentClassId(studentClassId);
        examResultExcelMainDTO.setStudentName(studentName);
        examResultExcelMainDTO.setScore(score);
        examResultExcelMainDTOList.add(examResultExcelMainDTO);

        studentClassIdStr = scoreInfo[ExcelDataConfigure.EXAM_RESULT_STUDENT_CLASS_ID_SEC_INDEX];
        if (StringUtils.isNotEmpty(studentClassIdStr)) {
            studentClassId = Integer.valueOf(studentClassIdStr);
            studentName = scoreInfo[ExcelDataConfigure.EXAM_RESULT_NAME_SEC_INDEX];
            scoreStr = scoreInfo[ExcelDataConfigure.EXAM_RESULT_SCORE_SEC_INDEX];
            score = new BigDecimal(scoreStr);

            ExamResultExcelMainDTO otherExamResultExcelMainDTO = new ExamResultExcelMainDTO();
            otherExamResultExcelMainDTO.setStudentClassId(studentClassId);
            otherExamResultExcelMainDTO.setStudentName(studentName);
            otherExamResultExcelMainDTO.setScore(score);
            examResultExcelMainDTOList.add(otherExamResultExcelMainDTO);
        }

        return examResultExcelMainDTOList;
    }

    /**
     * 计算各种指标
     *
     * @param examResultExcelDTO
     * @return
     */
    private CattyResult<ExamResultDTO> getExamResultDTO(ExamResultExcelDTO examResultExcelDTO) {
        CattyResult<ExamResultDTO> cattyResult = new CattyResult<>();
        ExamResultDTO examResultDTO = new ExamResultDTO();

        Integer grade = examResultExcelDTO.getGrade();
        String classNum = examResultExcelDTO.getClassNum();
        String subjectName = examResultExcelDTO.getSubjectName();
        var examResultExcelMainDTOList = examResultExcelDTO.getExamResultExcelMainDTOList();

        var totalScore = BigDecimal.ZERO;
        BigDecimal excellentCount = BigDecimal.ZERO;
        BigDecimal goodCount = BigDecimal.ZERO;
        BigDecimal middleCount = BigDecimal.ZERO;
        BigDecimal passCount = BigDecimal.ZERO;
        BigDecimal notPassCount = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = MAX_SCORE;
        Map<String,BigDecimal> maxScoreNameMap = new HashMap<>();
        Map<String,BigDecimal> minScoreNameMap = new HashMap<>();
        Map<String,BigDecimal> excellentExamineesNameScoreMap = new HashMap<>();
        Map<String,BigDecimal> goodExamineesNameScoreMap = new HashMap<>();
        Map<String,BigDecimal> middleExamineesNameScoreMap = new HashMap<>();
        Map<String,BigDecimal> notPassExamineesNameScoreMap = new HashMap<>();
        for (ExamResultExcelMainDTO x : examResultExcelMainDTOList) {
            var score = x.getScore();
            var studentName = x.getStudentName();

            if (score.compareTo(maxScore) == 1) {
                maxScore = score;
            }
            if (minScore.compareTo(score) == 1) {
                minScore = score;
            }

            /*
            1，2 年级90分以上为优，80-89分为良，70-79分为中，69分以下为不达标
            3-6年级，90分以上为优，75-89为良，60-74为中，60分以下为不达标
            */
            totalScore = totalScore.add(score);

            if (x.getScore().compareTo(ExcelDataConfigure.EXCELLENT_SUB_SCORE) == 1) {
                excellentCount = excellentCount.add(BigDecimal.ONE);
                excellentExamineesNameScoreMap.put(studentName,score);
            }

            // 1,2 年级
            if (grade <= ExcelDataConfigure.SPECIAL_SPILT_GRADE) {
                boolean passFlag = score.compareTo(ExcelDataConfigure.LOW_PASS_SCORE) == 1;
                boolean middleFlag = ExcelDataConfigure.LOW_MIDDLE_SUP_SCORE.compareTo(score) == 1
                        || ExcelDataConfigure.LOW_MIDDLE_SUP_SCORE.compareTo(score) == 0;
                boolean goodFlag = ExcelDataConfigure.LOW_GOOD_SUP_SCORE.compareTo(score) == 1
                        || ExcelDataConfigure.LOW_GOOD_SUP_SCORE.compareTo(score) == 0;
                if (passFlag) {
                    passCount = passCount.add(BigDecimal.ONE);
                }
                if (!passFlag) {
                    notPassCount = notPassCount.add(BigDecimal.ONE);
                    notPassExamineesNameScoreMap.put(studentName,score);
                }
                // 中等的判断
                if (passFlag && middleFlag) {
                    middleCount = middleCount.add(BigDecimal.ONE);
                    middleExamineesNameScoreMap.put(studentName,score);
                }
                // 良好的判断
                if (!middleFlag && goodFlag) {
                    goodCount = goodCount.add(BigDecimal.ONE);
                    goodExamineesNameScoreMap.put(studentName,score);
                }

            }

            // 3-6年级
            if (grade > ExcelDataConfigure.SPECIAL_SPILT_GRADE) {
                boolean passFlag = score.compareTo(ExcelDataConfigure.HIGH_PASS_SCORE) == 1;
                boolean middleFlag = ExcelDataConfigure.HIGH_MIDDLE_SUP_SCORE.compareTo(score) == 1
                        || ExcelDataConfigure.HIGH_MIDDLE_SUP_SCORE.compareTo(score) == 0;
                boolean goodFlag = ExcelDataConfigure.HIGH_GOOD_SUP_SCORE.compareTo(score) == 1
                        || ExcelDataConfigure.HIGH_GOOD_SUP_SCORE.compareTo(score) == 0;
                if (passFlag) {
                    passCount = passCount.add(BigDecimal.ONE);
                }
                if (!passFlag) {
                    notPassCount = notPassCount.add(BigDecimal.ONE);
                    notPassExamineesNameScoreMap.put(studentName,score);
                }
                // 中等的判断
                if (passFlag && middleFlag) {
                    middleCount = middleCount.add(BigDecimal.ONE);
                    middleExamineesNameScoreMap.put(studentName,score);
                }
                // 良好的判断
                if (!middleFlag && goodFlag) {
                    goodCount = goodCount.add(BigDecimal.ONE);
                    goodExamineesNameScoreMap.put(studentName,score);
                }

            }
        }

        for (ExamResultExcelMainDTO x : examResultExcelMainDTOList){
            var score = x.getScore();
            var studentName = x.getStudentName();

            if (score.compareTo(maxScore) == 0){
                maxScoreNameMap.put(studentName,score);
            }
            if (score.compareTo(minScore) == 0){
                minScoreNameMap.put(studentName,score);
            }

        }

        var examineesCount = examResultExcelMainDTOList.size();
        BigDecimal examineesCountBigDecimal = new BigDecimal(examineesCount);
        var avgScore = totalScore.divide(examineesCountBigDecimal, DEFAULT_SCALE, RoundingMode.HALF_UP);
        var excellentRate = excellentCount.multiply(RATE_UNIT).divide(examineesCountBigDecimal, DEFAULT_SCALE, RoundingMode.HALF_UP);
        var passRate = passCount.multiply(RATE_UNIT).divide(examineesCountBigDecimal, DEFAULT_SCALE, RoundingMode.HALF_UP);

        examResultDTO.setGrade(grade);
        examResultDTO.setClassName(classNum);
        examResultDTO.setSubjectName(subjectName);
        examResultDTO.setExamineesCount(examineesCount);
        examResultDTO.setTotalScore(totalScore);
        examResultDTO.setAvgScore(avgScore);
        examResultDTO.setExcellentRate(excellentRate);
        examResultDTO.setPassRate(passRate);
        examResultDTO.setMaxScore(maxScore);
        examResultDTO.setMinScore(minScore);
        examResultDTO.setExcellentExamineesCount(Integer.valueOf(excellentCount.toString()));
        examResultDTO.setNotPassExamineesCount(Integer.valueOf(notPassCount.toString()));
        examResultDTO.setGoodExamineesCount(Integer.valueOf(goodCount.toString()));
        examResultDTO.setMiddleExamineesCount(Integer.valueOf(middleCount.toString()));
        examResultDTO.setExcellentExamineesNameScoreMap(excellentExamineesNameScoreMap);
        examResultDTO.setGoodExamineesNameScoreMap(goodExamineesNameScoreMap);
        examResultDTO.setMiddleExamineesNameScoreMap(middleExamineesNameScoreMap);
        examResultDTO.setNotPassExamineesNameScoreMap(notPassExamineesNameScoreMap);
        examResultDTO.setMaxScoreNameMap(maxScoreNameMap);
        examResultDTO.setMinScoreNameMap(minScoreNameMap);

        cattyResult.setData(examResultDTO);
        cattyResult.setSuccess(true);
        return cattyResult;
    }
}
