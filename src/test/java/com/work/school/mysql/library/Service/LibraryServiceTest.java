package com.work.school.mysql.library.Service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @Author : Growlithe
 * @Date : 2019/5/30 5:00 PM
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LibraryServiceTest {

    @Resource
    private LibraryService libraryService;

    @Test
    public void save() {

    }

    @Test
    public void updateLibraryBook() {
        var cattyResult = libraryService.updateLibraryBook();
        Assert.assertTrue(cattyResult.isSuccess());
    }

}