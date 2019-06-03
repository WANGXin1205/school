package com.work.school.mysql.library.dao.mapper;


import com.work.school.mysql.library.dao.domain.BookDO;

import java.util.List;

public interface BookMapper {

    /**
     * 查询所有的图书
     * @return
     */
    List<BookDO> listAll();
}