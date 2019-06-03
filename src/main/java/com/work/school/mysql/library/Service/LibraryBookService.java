package com.work.school.mysql.library.Service;

import com.work.school.common.CattyResult;
import com.work.school.mysql.library.dao.domain.BookDO;
import com.work.school.mysql.library.dao.domain.LibraryBookDO;
import com.work.school.mysql.library.dao.mapper.BookMapper;
import com.work.school.mysql.library.dao.mapper.LibraryBookMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/5/30 4:09 PM
 * @Description
 */
@Service
public class LibraryBookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryBookService.class);

    @Resource
    private LibraryBookMapper libraryBookMapper;

    /**
     * 查询所有图书信息
     *
     * @return
     */
    public List<LibraryBookDO> listAll() {
        return libraryBookMapper.listAll();
    }


    /**
     * 保存所有图书信息
     *
     * @param libraryBookList
     * @return
     */
    public CattyResult saveBatch(List<LibraryBookDO> libraryBookList) {
        CattyResult cattyResult = new CattyResult();
        libraryBookMapper.saveBatch(libraryBookList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }

    /**
     * 更新图书更新人，更新时间
     *
     * @param libraryBookList
     * @return
     */
    public CattyResult updateTimeBatch(List<LibraryBookDO> libraryBookList) {
        CattyResult cattyResult = new CattyResult();
        libraryBookMapper.updateTimeBatch(libraryBookList);
        cattyResult.setSuccess(true);
        return cattyResult;
    }
}
