package com.work.school.mysql.banner.service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.banner.service.dto.MobileRedBannerDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 10:30 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MobileRedBannerServiceTest {

    @Autowired
    private MobileRedBannerService mobileRedBannerService;

    @Test
    public void listAllClassMobileRedBannerTest() throws Exception{
        var fileLocal = new File("/Users/wangxin/Downloads/流动红旗.xlsx");
        var fis = new FileInputStream(fileLocal);
        var contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        var multipartFile = new MockMultipartFile(fileLocal.getName(), fileLocal.getName(), contentType, fis);
        CattyResult<List<MobileRedBannerDTO>> candyResult = mobileRedBannerService.listAllClassMobileRedBanner(multipartFile);
        var classBannerCountDTOList = candyResult.getData();
        classBannerCountDTOList.forEach(x->System.out.println(x.toString()));
        Assert.assertTrue(candyResult.isSuccess());
    }

    @Test
    public void computerClassScoreTest() throws Exception{
        var fileLocal = new File("/Users/wangxin/Downloads/流动红旗.xlsx");
        var fis = new FileInputStream(fileLocal);
        var contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        var multipartFile = new MockMultipartFile(fileLocal.getName(), fileLocal.getName(), contentType, fis);
        CattyResult<List<MobileRedBannerDTO>> candyResult = mobileRedBannerService.listAllClassMobileRedBanner(multipartFile);
        var classBannerCountDTOList = candyResult.getData();

        CattyResult<TreeMap<String,Integer>> cattyResult = mobileRedBannerService.computerClassScore(classBannerCountDTOList);
        Assert.assertTrue(cattyResult.isSuccess());
    }
}