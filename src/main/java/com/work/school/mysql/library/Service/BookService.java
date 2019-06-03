package com.work.school.mysql.library.Service;

import com.work.school.mysql.library.dao.domain.BookDO;
import com.work.school.mysql.library.dao.mapper.BookMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : Growlithe
 * @Date : 2019/5/30 4:06 PM
 * @Description
 */
@Service
public class BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookService.class);

    @Resource
    private BookMapper bookMapper;

    /**
     * 查询所有图书原始信息
     * @return
     */
    public List<BookDO> listAll(){
        return bookMapper.listAll();
    }

}
