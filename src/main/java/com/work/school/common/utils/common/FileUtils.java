/*
 * 项目名:      农夫山泉小瓶水系统
 * 文件名:      FileUtils.java
 * 类名:        FileUtils
 *
 * 版权声明:
 *      本系统的所有内容，包括源码、页面设计，文字、图像以及其他任何信息，
 *      如未经特殊说明，其版权均属农夫山泉股份有限公司所有。
 *
 *      Copyright (c) 2013 农夫山泉股份有限公司
 *      版权所有
 */
package com.work.school.common.utils.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;

/**
 * 【FrameWork】文件工具类
 * 
 * <br>createDate：2013-11-14
 * <br>updateDate：2013-12-05
 * @version 1.1
 * @author shikunhuang
 */
public class FileUtils {

	/**
	 * 获取文件的扩展名
	 * @param file 文件对象
	 * @return
	 */
	public static String getExtensionName(MultipartFile file) {
		if (file.isEmpty()) {
			return null;
		}

		String suffix = null;
		String fileName = file.getOriginalFilename();
		int dot = fileName.lastIndexOf(".");
		if (dot > -1 && dot < fileName.length() - 1) {
			suffix = fileName.substring(dot + 1);
		}
		return suffix;
	}

	/**
	 * 读取一个文件的内容到byte[]数组中
	 * @param filePath			文件路径
	 * @return
	 * @throws Exception
	 */
	public static byte[] getBytesByFile(String filePath) throws Exception {
		return IOUtils.getData(new FileInputStream(filePath));
	}
	
	/**
	 * 根据文件路径名删除一个文件
	 * @param filePath			文件完整路径名
	 * @return					删除成功则返回true
	 * @throws Exception
	 */
	public static void deleteFile(String filePath) throws Exception {
		if (StringUtils.isBlank(filePath)) {
			return;
		}
	
		File file = new File(filePath);
		file.delete();
	}
	
    /**
     * 下载文件
     * @param b                 字节数组
     * @param fileName          文件名称(带上文件后缀)
     * @return
     * @throws Exception
     */
    public static ResponseEntity<byte[]> download(byte[] b, String fileName) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("gbk"), "iso-8859-1"));
        return new ResponseEntity<byte[]>(b, headers, HttpStatus.OK);
    }

    public static ResponseEntity<byte[]> downloadInIE(byte[] b, String fileName) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, "UTF-8"));
        return new ResponseEntity<byte[]>(b, headers, HttpStatus.OK);
    }
}
