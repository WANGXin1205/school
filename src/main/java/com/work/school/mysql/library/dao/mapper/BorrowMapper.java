package com.work.school.mysql.library.dao.mapper;


import com.work.school.mysql.library.dao.domain.BorrowDO;

import java.util.Date;
import java.util.List;

public interface BorrowMapper {

    /**
     * 批量保存
     *
     * @param borrowDOList
     * @return
     */
    int saveBatch(List<BorrowDO> borrowDOList);

    /**
     * 查询所有借阅图书
     *
     * @return
     */
    List<BorrowDO> listAll();

    /**
     * 查询未归还的图书
     *
     * @return
     */
    List<BorrowDO> listBorrowBook();

    /**
     * 查询某个时间前未归还的图书
     *
     * @param time
     * @return
     */
    List<BorrowDO> listBorrowPastByTime(Date time);

    /**
     * 查询某个时间后未归还的图书
     *
     * @param time
     * @return
     */
    List<BorrowDO> listBorrowLaterByTime(Date time);
}