package com.work.school.mysql.common.service;

import com.work.school.common.CattyResult;
import com.work.school.common.config.ExcelDataConfigure;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.common.utils.business.SchoolBusinessUtils;
import com.work.school.common.utils.common.POIUtils;
import com.work.school.common.utils.common.StringUtils;
import com.work.school.mysql.banner.enums.GradeEnum;
import com.work.school.mysql.banner.enums.SexEnum;
import com.work.school.mysql.common.dao.domain.StudentDO;
import com.work.school.mysql.common.dao.mapper.StudentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author : Growlithe
 * @Date : 2019/4/1 5:51 PM
 * @Description
 */
@Service
public class StudentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentService.class);

    @Resource
    private StudentMapper studentMapper;


    /**
     * 根据年级查询学生信息
     *
     * @param grade
     * @return
     */
    public List<StudentDO> listStudentByGrade(Integer grade) {
        return studentMapper.listStudentByGrade(grade);
    }

    /**
     * 上传学生信息
     *
     * @param multipartFile
     * @return
     */
    @Transactional(value = "mysqlTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CattyResult saveBatch(MultipartFile multipartFile) {
        CattyResult cattyResult = new CattyResult();

        // 从excel中获取数据
        var getDataMapFromExcelResult = POIUtils.getDataMapFromExcel(multipartFile, ExcelDataConfigure.STUDENT_DATA_NAME);
        if (!getDataMapFromExcelResult.isSuccess()) {
            cattyResult.setMessage(getDataMapFromExcelResult.getMessage());
            return cattyResult;
        }
        var dataMap = getDataMapFromExcelResult.getData();

        var studentDOList = this.listStudentFromDataMap(dataMap);

        studentMapper.saveBatch(studentDOList);

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 从数据中获取学生信息
     *
     * @param dataMap
     * @return
     */
    private List<StudentDO> listStudentFromDataMap(Map<Integer, String[]> dataMap) {
        List<StudentDO> studentDOList = new ArrayList<>();
        Integer i = 0;
        Integer grade = null;
        Integer gradeId = null;
        Integer classNum = null;
        for (Integer x : dataMap.keySet()) {
            var data = dataMap.get(x);
            if (ExcelDataConfigure.STUDENT_GRADE_CLASS_ROW.equals(i)) {
                var gradeAndClassInfo = data[ExcelDataConfigure.STUDENT_GRADE_CLASS_BEGIN_COL];
                var gradeAndClass = gradeAndClassInfo.split(ExcelDataConfigure.STUDENT_GRADE_CLASS_SPILT_SIGN);
                var gradeYear = Integer.valueOf(gradeAndClass[ExcelDataConfigure.STUDENT_GRADE_COL]);
                String[] classInfo = gradeAndClass[ExcelDataConfigure.STUDENT_CLASS_COL].split(ExcelDataConfigure.STUDENT_CLASS_SPILT_SIGN);
                classNum = Integer.valueOf(classInfo[ExcelDataConfigure.STUDENT_CLASS_INFO_COL]);
                GradeEnum gradeEnum = SchoolBusinessUtils.getGradeByYear(gradeYear);
                grade = gradeEnum.getCode();
                gradeId = gradeYear;
            }

            if (ExcelDataConfigure.STUDENT_TITLE_ROW.equals(i)) {
                var index = data[ExcelDataConfigure.STUDENT_TITLE_INDEX_COL];
                var name = data[ExcelDataConfigure.STUDENT_TITLE_NAME_COL];
                var sex = data[ExcelDataConfigure.STUDENT_TITLE_SEX_COL];
                if (!index.equals(ExcelDataConfigure.STUDENT_DATA_NAME[ExcelDataConfigure.STUDENT_TITLE_INDEX_COL])) {
                    throw new TransactionException("表格格式被改动，这一列应该为"
                            + ExcelDataConfigure.STUDENT_DATA_NAME[ExcelDataConfigure.STUDENT_TITLE_INDEX_COL]);
                }
                if (!name.equals(ExcelDataConfigure.STUDENT_DATA_NAME[ExcelDataConfigure.STUDENT_TITLE_NAME_COL])) {
                    throw new TransactionException("表格格式被改动，这一列应该为"
                            + ExcelDataConfigure.STUDENT_DATA_NAME[ExcelDataConfigure.STUDENT_TITLE_NAME_COL]);
                }
                if (!sex.equals(ExcelDataConfigure.STUDENT_DATA_NAME[ExcelDataConfigure.STUDENT_TITLE_SEX_COL])) {
                    throw new TransactionException("表格格式被改动，这一列应该为"
                            + ExcelDataConfigure.STUDENT_DATA_NAME[ExcelDataConfigure.STUDENT_TITLE_SEX_COL]);
                }
            }

            if (i >= ExcelDataConfigure.STUDENT_BEGIN_ROW) {

                Integer studentClassId = null;
                try {
                    studentClassId = Integer.valueOf(data[ExcelDataConfigure.STUDENT_CLASS_ID_COL]);
                } catch (Exception e) {
                    throw new TransactionException("学生班级id应该为数字");
                }

                var name = data[ExcelDataConfigure.STUDENT_NAME_COL];
                if (StringUtils.isEmpty(name)) {
                    throw new TransactionException("第" + i + "行，学生名称信息有误");
                }

                Integer sex = null;
                try {
                    sex = Integer.valueOf(data[ExcelDataConfigure.STUDENT_SEX_COL]);
                } catch (Exception e) {
                    throw new TransactionException("学生性别应该为数字，1代表男性，0代表女性");
                }
                SexEnum sexEnum = SexEnum.getSexEnum(sex);
                if (sexEnum == null) {
                    throw new TransactionException("第" + i + "行，学生性别信息有误");
                }

                StudentDO studentDO = new StudentDO();
                studentDO.setGrade(grade);
                studentDO.setGradeId(gradeId);
                studentDO.setClassNum(classNum);
                studentDO.setStudentClassId(studentClassId);
                studentDO.setStudentName(name);
                studentDO.setSex(sexEnum.getCode());
                studentDO.setCreateBy(ExcelDataConfigure.GROWLITHE);
                studentDOList.add(studentDO);
            }

            i++;
        }

        return studentDOList;
    }

}
