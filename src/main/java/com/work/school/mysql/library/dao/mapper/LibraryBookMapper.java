package com.work.school.mysql.library.dao.mapper;


import com.work.school.mysql.library.dao.domain.LibraryBookDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LibraryBookMapper {

    /**
     * 查询所有图书馆图书
     *
     * @return
     */
    List<LibraryBookDO> listAll();


    /**
     * 更新图书更新信息
     *
     * @param libraryBookDOList
     * @return
     */
    int updateTimeBatch(List<LibraryBookDO> libraryBookDOList);

    /**
     * 保存图书馆图书
     *
     * @param libraryBookList
     * @return
     */
    int saveBatch(List<LibraryBookDO> libraryBookList);


}