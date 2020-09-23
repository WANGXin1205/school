package com.work.school.mysql.banner.service;

import com.work.school.common.CattyResult;
import com.work.school.common.config.ExcelDataConfigure;
import com.work.school.common.utils.common.POIUtils;
import com.work.school.mysql.banner.enums.*;
import com.work.school.mysql.banner.service.dto.MobileRedBannerDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @Author : Growlithe
 * @Date : 2018/5/21 22:36
 * @Description
 */
@Service
public class MobileRedBannerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileRedBannerService.class);

    /**
     * 计算各班得分
     * @param mobileRedBannerDTOList
     * @return
     */
    public CattyResult<TreeMap<String,Integer>> computerClassScore(List<MobileRedBannerDTO> mobileRedBannerDTOList){
        CattyResult<TreeMap<String,Integer>> cattyResult = new CattyResult<>();

        TreeMap<String,Integer> classRedBannerCountMap = new TreeMap<>();
        for(MobileRedBannerDTO mobileRedBannerDTO:mobileRedBannerDTOList){
            HashMap<String, Integer> redBannerCountMap = mobileRedBannerDTO.getRedBannerCount();
            int bestCount = redBannerCountMap.get(MobileRedBannerEnum.BEST.getDesc());
            int singCount = redBannerCountMap.get(MobileRedBannerEnum.SING.getDesc());
            int sportCount = redBannerCountMap.get(MobileRedBannerEnum.SPORT.getDesc());
            int bookCount = redBannerCountMap.get(MobileRedBannerEnum.BOOK.getDesc());
            int totalScore = bestCount * ExcelDataConfigure.BEST_SCORE + singCount * ExcelDataConfigure.OTHER_SCORE
                    + sportCount * ExcelDataConfigure.OTHER_SCORE + bookCount * ExcelDataConfigure.OTHER_SCORE;
            classRedBannerCountMap.put(mobileRedBannerDTO.getGradeClassName(),totalScore);
        }

        cattyResult.setData(classRedBannerCountMap);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 从excel表中获取各班流动红旗获得次数
     *
     * @param multipartFile
     * @return
     */
    public CattyResult<List<MobileRedBannerDTO>> listAllClassMobileRedBanner(MultipartFile multipartFile) {
        CattyResult<List<MobileRedBannerDTO>> cattyResult = new CattyResult<>();

        int weekdaysTime = ExcelDataConfigure.WEEKDAYS_TIME;
        String[] weekdays = new String[weekdaysTime+1];
        for(int i = 0;i<=weekdaysTime;i++){
            weekdays[i] = String.valueOf(i);
        }

        // 从excel中获取数据
        var getDataMapFromExcelResult = POIUtils.getDataMapFromExcel(multipartFile, weekdays);
        if (!getDataMapFromExcelResult.isSuccess()) {
            cattyResult.setMessage(getDataMapFromExcelResult.getMessage());
            return cattyResult;
        }

        var dataMap = getDataMapFromExcelResult.getData();

        // 将dataMap数据 变为 mobileRedBannerDO
        var listMobileRedBannerFromDataMapResult = this.listMobileRedBannerFromDataMap(dataMap);
        if (!listMobileRedBannerFromDataMapResult.isSuccess()) {
            LOGGER.warn(listMobileRedBannerFromDataMapResult.getMessage());
            cattyResult.setMessage(listMobileRedBannerFromDataMapResult.getMessage());
            return cattyResult;
        }

        var mobileRedBannerDOList = listMobileRedBannerFromDataMapResult.getData();
        if (CollectionUtils.isEmpty(mobileRedBannerDOList)) {
            cattyResult.setMessage("没有流动红旗评比数据");
            return cattyResult;
        }


        cattyResult.setData(mobileRedBannerDOList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 将dataMap转换为mobileRedBannerDOList
     *
     * @param dataMap
     * @return
     */
    private CattyResult<List<MobileRedBannerDTO>> listMobileRedBannerFromDataMap(Map<Integer, String[]> dataMap) {
        CattyResult<List<MobileRedBannerDTO>> candyResult = new CattyResult<>();
        List<MobileRedBannerDTO> mobileRedBannerDTOList = new ArrayList<>();

        for (Integer x : dataMap.keySet()) {
            String[] data = dataMap.get(x);

            String gradeClassInfo;
            try {
                gradeClassInfo = data[ExcelDataConfigure.RED_BANNER_CLASS_INDEX];
            } catch (NullPointerException e){
                candyResult.setMessage("第" + (x + 2) + "行中,第" +  1 + "列 is null");
                return candyResult;
            }
            String[] gradeClass = gradeClassInfo.split("\\.");
            String grade = gradeClass[0];
            String classInfo = gradeClass[1];
            String name = grade.concat("年级").concat(classInfo).concat("班");

            HashMap<String,Integer> redBannerCountMap = new HashMap<>();
            for (MobileRedBannerEnum mobileRedBannerEnum: MobileRedBannerEnum.values()){
                redBannerCountMap.put(mobileRedBannerEnum.getDesc(),0);
            }
            for (int i =0;i< Arrays.asList(data).size();i++){
                if (MobileRedBannerEnum.BEST.getDesc().equals(data[i])){
                    int count = redBannerCountMap.get(MobileRedBannerEnum.BEST.getDesc());
                    redBannerCountMap.put(MobileRedBannerEnum.BEST.getDesc(),count+1);
                }
                if (MobileRedBannerEnum.SING.getDesc().equals(data[i])){
                    int count = redBannerCountMap.get(MobileRedBannerEnum.SING.getDesc());
                    redBannerCountMap.put(MobileRedBannerEnum.SING.getDesc(),count+1);
                }
                if (MobileRedBannerEnum.SPORT.getDesc().equals(data[i])){
                    int count = redBannerCountMap.get(MobileRedBannerEnum.SPORT.getDesc());
                    redBannerCountMap.put(MobileRedBannerEnum.SPORT.getDesc(),count+1);
                }
                if (MobileRedBannerEnum.BOOK.getDesc().equals(data[i])){
                    int count = redBannerCountMap.get(MobileRedBannerEnum.BOOK.getDesc());
                    redBannerCountMap.put(MobileRedBannerEnum.BOOK.getDesc(),count+1);
                }
            }

            MobileRedBannerDTO mobileRedBannerDTO = new MobileRedBannerDTO();
            mobileRedBannerDTO.setGradeClassName(name);
            mobileRedBannerDTO.setRedBannerCount(redBannerCountMap);

            mobileRedBannerDTOList.add(mobileRedBannerDTO);
        }

        candyResult.setData(mobileRedBannerDTOList);
        candyResult.setSuccess(true);
        return candyResult;
    }


}
