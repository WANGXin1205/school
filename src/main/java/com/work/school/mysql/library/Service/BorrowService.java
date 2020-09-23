package com.work.school.mysql.library.Service;

import com.work.school.common.CattyResult;
import com.work.school.common.config.ExcelDataConfigure;
import com.work.school.common.config.SchoolDefaultConfigure;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.common.utils.common.DateUtils;
import com.work.school.common.utils.common.POIUtils;
import com.work.school.common.utils.common.StringUtils;
import com.work.school.mysql.common.dao.domain.TeacherDO;
import com.work.school.mysql.common.service.TeacherService;
import com.work.school.mysql.library.dao.domain.BorrowDO;
import com.work.school.mysql.library.dao.domain.LibraryBookDO;
import com.work.school.mysql.library.dao.mapper.BookMapper;
import com.work.school.mysql.library.dao.mapper.BorrowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/6/3 8:32 AM
 * @Description
 */
@Service
public class BorrowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BorrowService.class);

    @Resource
    private LibraryBookService libraryBookService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private BorrowMapper borrowMapper;

    /**
     * 查询未归还的图书
     *
     * @return
     */
    public CattyResult<List<BorrowDO>> listBorrowBook() {
        CattyResult<List<BorrowDO>> cattyResult = new CattyResult<>();

        var borrowBookList = borrowMapper.listBorrowBook();

        cattyResult.setData(borrowBookList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 查询某一时间前借阅，但未归还的图书
     *
     * @param dateStr
     * @return
     */
    public CattyResult<List<BorrowDO>> listBorrowPastByTime(String dateStr) {
        CattyResult<List<BorrowDO>> cattyResult = new CattyResult<>();

        Date date = DateUtils.clearTime(DateUtils.parseDate(dateStr));

        var borrowBookList = borrowMapper.listBorrowPastByTime(date);

        cattyResult.setData(borrowBookList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 查询某一时间后借阅，但未归还的图书
     *
     * @param dateStr
     * @return
     */
    public CattyResult<List<BorrowDO>> listBorrowLaterByTime(String dateStr) {
        CattyResult<List<BorrowDO>> cattyResult = new CattyResult<>();

        Date date = DateUtils.clearTime(DateUtils.parseDate(dateStr));

        var borrowBookList = borrowMapper.listBorrowLaterByTime(date);

        cattyResult.setData(borrowBookList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 保存借阅信息
     *
     * @param multipartFile
     * @return
     */
    public CattyResult save(MultipartFile multipartFile) {
        CattyResult cattyResult = new CattyResult();

        // 从excel中获取数据
        var getDataMapFromExcelResult = POIUtils.getDataMapFromExcel(multipartFile, ExcelDataConfigure.BORROW_TITLE_NAME);
        if (!getDataMapFromExcelResult.isSuccess()) {
            cattyResult.setMessage(getDataMapFromExcelResult.getMessage());
            return cattyResult;
        }

        var dataMap = getDataMapFromExcelResult.getData();

        // 将dataMap数据 变为 mobileRedBannerDO
        var listBorrowFromDataMapResult = this.listBorrowFromDataMap(dataMap);
        if (!listBorrowFromDataMapResult.isSuccess()) {
            LOGGER.warn(listBorrowFromDataMapResult.getMessage());
            cattyResult.setMessage(listBorrowFromDataMapResult.getMessage());
            return cattyResult;
        }

        var borrowList = listBorrowFromDataMapResult.getData();

        // 保存数据
        borrowMapper.saveBatch(borrowList);

        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 将dataMap转换为borrowList
     *
     * @param dataMap
     * @return
     */
    private CattyResult<List<BorrowDO>> listBorrowFromDataMap(Map<Integer, String[]> dataMap) {
        CattyResult<List<BorrowDO>> candyResult = new CattyResult<>();

        // 查询所有图书编号
        List<LibraryBookDO> allLibraryBookList = libraryBookService.listAll();
        Map<String, LibraryBookDO> allLibraryBookMap = allLibraryBookList.stream().collect(Collectors.toMap(LibraryBookDO::getNumber, Function.identity()));

        // 查询所有教师信息
        List<TeacherDO> teacherDOList = teacherService.listAll();
        Map<Integer, String> teacherMap = teacherDOList.stream().collect(Collectors.toMap(TeacherDO::getId, TeacherDO::getName));

        CattyResult<List<BorrowDO>> packBorrowListResult = this.packBorrowList(allLibraryBookMap, teacherMap, dataMap);
        if (!packBorrowListResult.isSuccess()) {
            LOGGER.warn(packBorrowListResult.getMessage());
            candyResult.setMessage(packBorrowListResult.getMessage());
            return candyResult;
        }
        List<BorrowDO> borrowList = packBorrowListResult.getData();

        candyResult.setData(borrowList);
        candyResult.setSuccess(true);
        return candyResult;
    }


    /**
     * 组装 borrowList
     *
     * @param allLibraryBookMap
     * @param teacherMap
     * @param dataMap
     * @return
     */
    private CattyResult<List<BorrowDO>> packBorrowList(Map<String, LibraryBookDO> allLibraryBookMap,
                                                       Map<Integer, String> teacherMap,
                                                       Map<Integer, String[]> dataMap) {
        CattyResult<List<BorrowDO>> cattyResult = new CattyResult<>();

        List<BorrowDO> borrowDOList = new ArrayList<>();

        List<String> libraryBookIdList = new ArrayList<>();
        for (Integer x : dataMap.keySet()) {
            String[] data = dataMap.get(x);

            String libraryBookId;
            try {
                libraryBookId = data[ExcelDataConfigure.BORROW_LIBRARY_BOOK_ID_INDEX];
            } catch (Exception e) {
                cattyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.BORROW_LIBRARY_BOOK_ID_INDEX + 1) + "列 is not num");
                return cattyResult;
            }
            if (libraryBookIdList.contains(libraryBookId)) {
                cattyResult.setMessage("请检查图书编号，excel表中有相同的图书编号");
                return cattyResult;
            }
            if (!libraryBookIdList.contains(libraryBookId)) {
                libraryBookIdList.add(libraryBookId);
            }
            // 判断图书是否合法
            LibraryBookDO libraryBookDO = allLibraryBookMap.get(libraryBookId);
            if (libraryBookDO == null) {
                cattyResult.setMessage("第" + (x + 2) + "行中," + libraryBookDO.getNumber() + "图书编号不存在数据库中");
                return cattyResult;
            }

            // 获取教师id
            String teacherIdStr = data[ExcelDataConfigure.BORROW_TEACHER_ID_INDEX];
            Integer teacherId;
            try {
                teacherId = this.packTeacherId(teacherIdStr, x);
            } catch (Exception e) {
                cattyResult.setMessage(e.getMessage());
                return cattyResult;
            }

            String teacherName = data[ExcelDataConfigure.BORROW_TEACHER_NAME_INDEX];
            if (StringUtils.isEmpty(teacherName)) {
                cattyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.BORROW_TEACHER_NAME_INDEX + 1) + "列,教师名称为空");
                return cattyResult;
            }

            // 判断教师是否合法
            CattyResult judgeTeacherLegalResult = this.judgeTeacherLegal(teacherId, teacherName, teacherMap, x);
            if (!judgeTeacherLegalResult.isSuccess()) {
                cattyResult.setMessage(judgeTeacherLegalResult.getMessage());
                return cattyResult;
            }

            // 获取借阅时间
            String borrowStartStr = data[ExcelDataConfigure.BORROW_BORROW_TIME_INDEX];
            Date borrowStart;
            try {
                borrowStart = this.packBorrowStart(borrowStartStr, x);
            } catch (Exception e) {
                cattyResult.setMessage(e.getMessage());
                return cattyResult;
            }

            String mark = data[ExcelDataConfigure.BORROW_MARK_INDEX];

            BorrowDO borrowDO = new BorrowDO();
            borrowDO.setLibraryBookId(libraryBookId);
            borrowDO.setTeacherId(teacherId);
            borrowDO.setBorrowStart(borrowStart);
            borrowDO.setMark(mark);
            borrowDO.setCreateBy(SchoolDefaultConfigure.GROWLITHE);
            borrowDOList.add(borrowDO);
        }

        cattyResult.setData(borrowDOList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }


    /**
     * 组装teacherId
     *
     * @param teacherIdStr
     * @param defaultRow
     * @return
     */
    private Integer packTeacherId(String teacherIdStr, Integer defaultRow) {
        if (StringUtils.isEmpty(teacherIdStr)) {
            throw new TransactionException("第" + (defaultRow + 2) + "行中,第" + (ExcelDataConfigure.BORROW_TEACHER_ID_INDEX + 1) + "列，教师id为空");
        }

        Integer teacherId;
        try {
            teacherId = Integer.valueOf(teacherIdStr);
        } catch (Exception e) {
            throw new TransactionException("第" + (defaultRow + 2) + "行中,第" + (ExcelDataConfigure.BORROW_TEACHER_ID_INDEX + 1) + "列 is not num");
        }

        return teacherId;
    }

    /**
     * 组装 borrowStart
     *
     * @param borrowStartStr
     * @param defaultRow
     * @return
     */
    private Date packBorrowStart(String borrowStartStr, Integer defaultRow) {
        if (StringUtils.isEmpty(borrowStartStr)) {
            throw new TransactionException("第" + (defaultRow + 2) + "行中,第" + (ExcelDataConfigure.BORROW_BORROW_TIME_INDEX + 1) + "列，借阅时间为空");
        }
        Date borrowStart;
        try {
            borrowStart = DateUtils.clearTime(DateUtils.parseDate(borrowStartStr));
        } catch (Exception e) {
            throw new TransactionException("第" + (defaultRow + 2) + "行中,第" + (ExcelDataConfigure.BORROW_BORROW_TIME_INDEX + 1) + "列借阅时间异常");
        }

        return borrowStart;
    }

    /**
     * 判断教师id和名称是否正确
     *
     * @param teacherId
     * @param teacherName
     * @param teacherMap
     * @param defaultRow
     * @return
     */
    private CattyResult judgeTeacherLegal(Integer teacherId, String teacherName, Map<Integer, String> teacherMap, Integer defaultRow) {
        CattyResult cattyResult = new CattyResult();
        String sourceTeacherName = teacherMap.get(teacherId);
        if (StringUtils.isEmpty(sourceTeacherName)) {
            cattyResult.setMessage("第" + (defaultRow + 2) + "行中,查询出教师姓名为空");
            return cattyResult;
        }
        if (!sourceTeacherName.equals(teacherName)) {
            cattyResult.setMessage("第" + (defaultRow + 2) + "行中,填写教师姓名【" + teacherName + "】和查询出教师姓名【" + sourceTeacherName + "】不一致");
            return cattyResult;
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }

}
