/*
 * 项目名:      农夫山泉小瓶水系统
 * 文件名:      IOUtils.java
 * 类名:        IOUtils
 *
 * 版权声明:
 *      本系统的所有内容，包括源码、页面设计，文字、图像以及其他任何信息，
 *      如未经特殊说明，其版权均属农夫山泉股份有限公司所有。
 *
 *      Copyright (c) 2013 农夫山泉股份有限公司
 *      版权所有
 */
package com.work.school.common.utils.common;

import java.io.*;

/**
 * 【FrameWork】I/O流工具类
 * 
 * <br>createDate：2013-11-14
 * <br>updateDate：2013-12-05
 * @version 1.0
 * @author shikunhuang
 */
public class IOUtils {

	/** 缓冲大小： */
	private static final int BUF_SIZE = 1024 * 4;
	
	/**
	 * 输出字节流信息(会关闭流)
	 * @param os				输出流
	 * @param data				数据
	 * @throws Exception 
	 */
	public static void out(OutputStream os, byte[] data) throws Exception {
		ByteArrayInputStream bais = null;
		BufferedOutputStream bos = null;
		
		try {
			bais = new ByteArrayInputStream(data);
			bos = new BufferedOutputStream(os, BUF_SIZE);
			
			byte[] b = new byte[BUF_SIZE];
			int len;
			while ((len = bais.read(b)) != -1) {
				bos.write(b, 0, len);
			}
		} finally {
			close(bais, bos);
		}
	}
	
	/**
	 * 从输入流中读取到信息，并返回一个byte[]数组(会关闭流)
	 * @param is				输入流
	 * @return
	 * @throws Exception 
	 */
	public static byte[] getData(InputStream is) throws Exception {
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = null;
		
		try {
			bis = new BufferedInputStream(is, BUF_SIZE);
			baos = new ByteArrayOutputStream(BUF_SIZE);
			
			byte[] b = new byte[BUF_SIZE];
			int len;
			while ((len = bis.read(b)) != -1) {
				baos.write(b, 0, len);
			}
			
			return baos.toByteArray();
		} finally {
			close(bis, baos);
		}
	}

    /**
     * 关闭输入输出流
     * @param is
     * @param os
     * @throws Exception
     */
	public static void close(InputStream is, OutputStream os) throws Exception {
		try {
			if (os != null) {
				os.close();
				os = null;
			}
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}
	}

}
