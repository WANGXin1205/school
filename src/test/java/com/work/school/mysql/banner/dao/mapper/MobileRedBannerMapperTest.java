package com.work.school.mysql.banner.dao.mapper;

import com.work.school.mysql.banner.dao.domain.MobileRedBannerDO;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 8:33 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Rollback(value = true)
@Transactional(transactionManager = "mysqlTransactionManager")
public class MobileRedBannerMapperTest {

    @Resource
    private MobileRedBannerMapper mobileRedBannerMapper;

    @Test
    public void listAllTest() {
        var list = mobileRedBannerMapper.listAll();
        Assert.assertNotNull(list);
    }


    @Test
    public void listIdsTest() {
        Integer schoolTerm = 1;
        List<Integer> weeks = Lists.newArrayList(1,2);
        var list = mobileRedBannerMapper.listIds(schoolTerm,weeks);
        Assert.assertNotNull(list);
    }

    @Test
    public void updateByIdsTest() {
        List<Long> ids = Lists.newArrayList(1L,2L);
        var count = mobileRedBannerMapper.updateByIds(ids);
        Assert.assertNotNull(count);
    }

    @Test
    public void listMobileRedBannerBySchoolTerm(){
        var schoolTerm = 1;
        List<MobileRedBannerDO> mobileRedBannerDOList = mobileRedBannerMapper.listMobileRedBannerBySchoolTerm(schoolTerm);
        Assert.assertNotNull(mobileRedBannerDOList);
    }


}