package com.work.school.mysql.banner.dao.mapper;

import com.work.school.mysql.banner.dao.domain.MobileRedBannerDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2018/12/23 21:38
 * @Description 流动红旗mapper
 */
public interface MobileRedBannerMapper {

    /**
     * 查询所有数据
     *
     * @return
     */
    List<MobileRedBannerDO> listAll();

    /**
     * 批量保存数据
     *
     * @param mobileRedBannerDOList
     * @return
     */
    Integer saveBatch(List<MobileRedBannerDO> mobileRedBannerDOList);


    /**
     * 根据学期 年级和周次查询数据主键
     *
     * @param schoolTerm
     * @param weeks
     * @return
     */
    List<Long> listIds(@Param("schoolTerm") Integer schoolTerm,
                       @Param("weeks") List<Integer> weeks);

    /**
     * 根据ids
     *
     * @param ids
     * @return
     */
    Integer updateStatusByIds(List<Long> ids);

    /**
     * 根据schoolTerm 查询数据
     *
     * @param schoolTerm
     * @return
     */
    List<MobileRedBannerDO> listMobileRedBannerBySchoolTerm(Integer schoolTerm);
}