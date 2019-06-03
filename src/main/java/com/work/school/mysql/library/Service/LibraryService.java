package com.work.school.mysql.library.Service;

import com.sun.mail.util.BEncoderStream;
import com.work.school.common.CattyResult;
import com.work.school.common.config.SchoolDefaultConfigure;
import com.work.school.common.excepetion.TransactionException;
import com.work.school.mysql.library.Service.dto.BookDTO;
import com.work.school.mysql.library.dao.domain.BookDO;
import com.work.school.mysql.library.dao.domain.LibraryBookDO;
import com.work.school.mysql.library.dao.mapper.LibraryBookMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author : Growlithe
 * @Date : 2019/4/25 8:27 AM
 * @Description
 */
@Service
public class LibraryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryService.class);

    /**
     * 默认的0
     */
    private static final Integer QUANTITY_ZERO = 0;

    @Resource
    private BookService bookService;
    @Resource
    private LibraryBookService libraryBookService;
    @Resource
    private BorrowService borrowService;

    /**
     * 更新图书馆图书信息
     *
     * @return
     */
    @Transactional(value = "mysqlTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CattyResult updateLibraryBook() {
        CattyResult cattyResult = new CattyResult();

        // 查询图书原始信息 和 图书馆图书信息
        var allBookList = bookService.listAll();
        var allLibraryBookList = libraryBookService.listAll();

        // 复制所有图书原始信息
        List<BookDTO> bookDTOList = new ArrayList<>();
        for (BookDO x : allBookList) {
            BookDTO bookDTO = new BookDTO();
            BeanUtils.copyProperties(x, bookDTO);
            bookDTOList.add(bookDTO);
        }

        // 转换为要储存的数据类型和数量
        List<LibraryBookDO> libraryBookList = new ArrayList<>();
        for (BookDTO x : bookDTOList) {

            while (!QUANTITY_ZERO.equals(x.getQuantity())) {
                LibraryBookDO libraryBookDO = new LibraryBookDO();
                libraryBookDO.setBookId(x.getId());
                libraryBookDO.setNumber(x.getNumber() + SchoolDefaultConfigure.HYPHEN + x.getQuantity());
                libraryBookDO.setCreateBy(SchoolDefaultConfigure.GROWLITHE);
                libraryBookList.add(libraryBookDO);
                x.setQuantity(x.getQuantity() - SchoolDefaultConfigure.SUBTRACT_STEP_ONE);
            }
        }

        var algorithmInUpdateLibraryBookResult = this.algorithmInUpdateLibraryBook(libraryBookList, allLibraryBookList);
        if (!algorithmInUpdateLibraryBookResult.isSuccess()){
            LOGGER.warn(algorithmInUpdateLibraryBookResult.getMessage());
            throw new TransactionException(algorithmInUpdateLibraryBookResult.getMessage());
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }


    /**
     * 算法 更新图书馆图书信息
     * @param libraryBookList
     * @param allLibraryBookList
     * @return
     */
    private CattyResult algorithmInUpdateLibraryBook(List<LibraryBookDO> libraryBookList,
                                                     List<LibraryBookDO> allLibraryBookList) {
        CattyResult cattyResult = new CattyResult();
        // 如果没有数据，则直接插入
        if (CollectionUtils.isEmpty(allLibraryBookList)) {

            CattyResult saveBatchResult = libraryBookService.saveBatch(libraryBookList);
            if (!saveBatchResult.isSuccess()) {
                LOGGER.warn(saveBatchResult.getMessage());
                throw new TransactionException(saveBatchResult.getMessage());
            }

        }

        // 如果有数据，比对数据，更新已经插入的数据，同时插入未插入的数据
        if (CollectionUtils.isNotEmpty(allLibraryBookList)) {

            // 将更新图书 和 保存图书分类
            List<LibraryBookDO> updateLibraryBookList = new ArrayList<>();
            List<LibraryBookDO> saveLibraryBookList = new ArrayList<>();

            var allLibraryBookMap = allLibraryBookList.stream().collect(Collectors.toMap(LibraryBookDO::getNumber, Function.identity()));
            for (LibraryBookDO x : libraryBookList) {
                // 默认更新人
                x.setUpdateBy(SchoolDefaultConfigure.GROWLITHE);

                var libraryBookDO = allLibraryBookMap.get(x.getNumber());
                var flag = libraryBookDO != null && libraryBookDO.getId() != null;
                if (flag) {
                    x.setId(libraryBookDO.getId());
                    updateLibraryBookList.add(x);
                }
                if (!flag) {
                    saveLibraryBookList.add(x);
                }
            }

            // 更新图书信息
            if (CollectionUtils.isNotEmpty(updateLibraryBookList)) {
                CattyResult updateTimeBatchResult = libraryBookService.updateTimeBatch(updateLibraryBookList);
                if (!updateTimeBatchResult.isSuccess()) {
                    LOGGER.warn(updateTimeBatchResult.getMessage());
                    throw new TransactionException(updateTimeBatchResult.getMessage());
                }
            }

            // 保存新的图书
            if (CollectionUtils.isNotEmpty(saveLibraryBookList)) {
                CattyResult saveBatchResult = libraryBookService.saveBatch(saveLibraryBookList);
                if (!saveBatchResult.isSuccess()) {
                    LOGGER.warn(saveBatchResult.getMessage());
                    throw new TransactionException(saveBatchResult.getMessage());
                }
            }
        }

        cattyResult.setSuccess(true);
        return cattyResult;
    }
}
