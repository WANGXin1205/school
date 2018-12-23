package com.work.school.mysql.banner.service;

import com.work.school.common.CandyResult;
import com.work.school.common.config.ExcelDataConfigure;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.common.utils.business.SchoolTermUtils;
import com.work.school.common.utils.common.POIUtils;
import com.work.school.common.utils.common.StringUtils;
import com.work.school.mysql.banner.dao.domain.MobileRedBannerDO;
import com.work.school.mysql.banner.dao.mapper.MobileRedBannerMapper;
import com.work.school.mysql.banner.enums.ClassEnum;
import com.work.school.mysql.banner.enums.MobileRedBannerEnum;
import com.work.school.mysql.banner.enums.SchoolTermEnum;
import com.work.school.mysql.banner.enums.WeekEnum;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2018/5/21 22:36
 * @Description
 */
@Service
public class MobileRedBannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileRedBannerService.class);

    @Resource
    private MobileRedBannerMapper mobileRedBannerMapper;

    @Transactional(value = "mysqlTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CandyResult<Void> saveBatch(MultipartFile multipartFile) {
        CandyResult<Void> candyResult = new CandyResult<>();

        // 从excel中获取数据
        var getDataMapFromExcelResult = POIUtils.getDataMapFromExcel(multipartFile, ExcelDataConfigure.RED_BANNER_DATA_NAME);
        if (!getDataMapFromExcelResult.isSuccess()) {
            candyResult.setMessage(getDataMapFromExcelResult.getMessage());
            return candyResult;
        }

        var dataMap = getDataMapFromExcelResult.getData();

        // 将dataMap数据 变为 mobileRedBannerDO
        var listMobileRedBannerFromDataMapResult = this.listMobileRedBannerFromDataMap(dataMap);
        if (!listMobileRedBannerFromDataMapResult.isSuccess()) {
            LOGGER.warn(listMobileRedBannerFromDataMapResult.getMessage());
            candyResult.setMessage(listMobileRedBannerFromDataMapResult.getMessage());
            return candyResult;
        }

        var mobileRedBannerDOList = listMobileRedBannerFromDataMapResult.getData();

        // 先根据schoolTerm和weeks 查询是否存在原始数据，如果有原始数据，需要先将原始数据清除
        Integer schoolTerm = SchoolTermUtils.getSchoolTerm();
        List<Integer> weeks = mobileRedBannerDOList.stream().map(MobileRedBannerDO::getWeek).collect(Collectors.toList());

        var deleteIds = mobileRedBannerMapper.listIds(schoolTerm, weeks);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            mobileRedBannerMapper.updateByIds(deleteIds);
        }

        // 数据保存至数据库
        mobileRedBannerMapper.saveBatch(mobileRedBannerDOList);

        candyResult.setSuccess(true);
        return candyResult;
    }

    /**
     * 将dataMap转换为mobileRedBannerDOList
     *
     * @param dataMap
     * @return
     */
    private CandyResult<List<MobileRedBannerDO>> listMobileRedBannerFromDataMap(Map<Integer, String[]> dataMap) {
        CandyResult<List<MobileRedBannerDO>> candyResult = new CandyResult<>();
        List<MobileRedBannerDO> mobileRedBannerDOList = new ArrayList<>();

        List<Integer> weekList = new ArrayList<>();
        for (Integer x : dataMap.keySet()) {
            String[] data = dataMap.get(x);

            Integer week;
            try {
                String weekStr = data[ExcelDataConfigure.RED_BANNER_WEEK_INDEX];
                week = Integer.valueOf(weekStr);
            } catch (Exception e) {
                candyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.RED_BANNER_WEEK_INDEX + 1) + "列 is not num");
                return candyResult;
            }
            if (weekList.contains(week)) {
                candyResult.setMessage("请检查周次，excel表中有相同的周次");
                return candyResult;
            }
            if (!weekList.contains(week)) {
                weekList.add(week);
            }

            List<Integer> classList = new ArrayList<>();
            try {
                String bestClassIdStr = data[ExcelDataConfigure.RED_BANNER_BEST_INDEX];
                if (StringUtils.isNotEmpty(bestClassIdStr)) {
                    Integer bestClassId = Integer.valueOf(bestClassIdStr);
                    if (classList.contains(bestClassId)){
                        throw new TransactionException("第" + (x + 2) + "行中有重复的班级");
                    }
                    if (!classList.contains(bestClassId)){
                        classList.add(bestClassId);
                    }
                    var mobileRedBannerDO = this.packMobileRedBannerDO(week, bestClassId, MobileRedBannerEnum.BEST);
                    mobileRedBannerDOList.add(mobileRedBannerDO);
                }
            } catch (Exception e) {
                candyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.RED_BANNER_BEST_INDEX + 1) + "列 is not num");
                return candyResult;
            }

            try {
                String teamClassIdStr = data[ExcelDataConfigure.RED_BANNER_TEAM_INDEX];
                if (StringUtils.isNotEmpty(teamClassIdStr)) {
                    Integer teamClassId = Integer.valueOf(teamClassIdStr);
                    if (classList.contains(teamClassId)){
                        throw new TransactionException("第" + (x + 2) + "行中有重复的班级");
                    }
                    if (!classList.contains(teamClassId)){
                        classList.add(teamClassId);
                    }
                    var mobileRedBannerDO = this.packMobileRedBannerDO(week, teamClassId, MobileRedBannerEnum.TEAM);
                    mobileRedBannerDOList.add(mobileRedBannerDO);
                }
            } catch (Exception e) {
                candyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.RED_BANNER_TEAM_INDEX + 1) + "列 is not num");
                return candyResult;
            }

            try {
                String sportClassIdStr = data[ExcelDataConfigure.RED_BANNER_SPORT_INDEX];
                if (StringUtils.isNotEmpty(sportClassIdStr)) {
                    Integer sportClassId = Integer.valueOf(sportClassIdStr);
                    if (classList.contains(sportClassId)){
                        throw new TransactionException("第" + (x + 2) + "行中有重复的班级");
                    }
                    if (!classList.contains(sportClassId)){
                        classList.add(sportClassId);
                    }
                    var mobileRedBannerDO = this.packMobileRedBannerDO(week, sportClassId, MobileRedBannerEnum.SPORT);
                    mobileRedBannerDOList.add(mobileRedBannerDO);
                }
            } catch (Exception e) {
                candyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.RED_BANNER_SPORT_INDEX + 1) + "列 is not num");
                return candyResult;
            }

            try {
                String healthClassIdStr = data[ExcelDataConfigure.RED_BANNER_HEALTH_INDEX];
                if (StringUtils.isNotEmpty(healthClassIdStr)) {
                    Integer healthClassId = Integer.valueOf(healthClassIdStr);
                    if (classList.contains(healthClassId)){
                        throw new TransactionException("第" + (x + 2) + "行中有重复的班级");
                    }
                    if (!classList.contains(healthClassId)){
                        classList.add(healthClassId);
                    }
                    var mobileRedBannerDO = this.packMobileRedBannerDO(week, healthClassId, MobileRedBannerEnum.HEALTH);
                    mobileRedBannerDOList.add(mobileRedBannerDO);
                }

            } catch (Exception e) {
                candyResult.setMessage("第" + (x + 2) + "行中,第" + (ExcelDataConfigure.RED_BANNER_HEALTH_INDEX + 1) + "列 is not num");
                return candyResult;
            }

        }

        candyResult.setData(mobileRedBannerDOList);
        candyResult.setSuccess(true);
        return candyResult;
    }

    /**
     * 组装MobileRedBannerDO
     *
     * @param week
     * @param classId
     * @param mobileRedBannerEnum
     * @return
     */
    private MobileRedBannerDO packMobileRedBannerDO(Integer week, Integer classId, MobileRedBannerEnum mobileRedBannerEnum) {
        MobileRedBannerDO mobileRedBannerDO = new MobileRedBannerDO();

        if (WeekEnum.getDesc(week) == null) {
            throw new TransactionException("未知的周次");
        }
        String className = ClassEnum.getDesc(classId);
        if (StringUtils.isEmpty(className)) {
            throw new TransactionException("未知的班级");
        }

        Integer schoolTerm = SchoolTermUtils.getSchoolTerm();

        mobileRedBannerDO.setSchoolTerm(schoolTerm);
        mobileRedBannerDO.setWeek(week);
        mobileRedBannerDO.setClassId(classId);
        mobileRedBannerDO.setClassName(className);
        mobileRedBannerDO.setRedBannerType(mobileRedBannerEnum.getCode());
        mobileRedBannerDO.setCreateBy(ExcelDataConfigure.GROWLITHE);

        return mobileRedBannerDO;
    }


}
