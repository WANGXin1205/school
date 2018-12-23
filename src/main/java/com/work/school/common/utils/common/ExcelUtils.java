/*
 * 项目名:      农夫山泉小瓶水系统
 * 文件名:      ExcelUtils.java
 * 类名:        ExcelUtils
 *
 * 版权声明:
 *      本系统的所有内容，包括源码、页面设计，文字、图像以及其他任何信息，
 *      如未经特殊说明，其版权均属农夫山泉股份有限公司所有。
 *
 *      Copyright (c) 2013 农夫山泉股份有限公司
 *      版权所有
 */
package com.work.school.common.utils.common;



import com.work.school.common.bean.SheetBean;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 【FrameWork】操作Excel的工具类
 * 
 * createDate：2013-11-07
 * updateDate：2013-11-22
 * 
 * @version 1.2.2
 * @author shikunhuang
 */
public class ExcelUtils {

    /** 单元格默认的值： */
    public static final String CELL_DEF_VAL = "";

    // -------------------------------------------------------------------------
    // Workbook与文件的相互转换：
    // -------------------------------------------------------------------------

    /**
     * 根据文件输入流得到Workbook对象
     * @param is				InputStream
     * @return
     * @throws Exception 
     */
    public static Workbook getWorkbookByIO(InputStream is) throws Exception {
        if (!is.markSupported()) {
            is = new PushbackInputStream(is, 8);
        }
        
        if (FileMagic.valueOf(is) == FileMagic.OLE2) {
            return new HSSFWorkbook(is);
        }

        if (FileMagic.valueOf(is) == FileMagic.OOXML) {
            return new XSSFWorkbook(OPCPackage.open(is));
        }

        return null;
    }

    /**
     * 根据网络地址获取Workbook对象
     * @param url				网络文件地址
     * @return
     * @throws Exception 
     */
    public static Workbook getWorkbookByUrl(String url) throws Exception {
        return ExcelUtils.getWorkbookByIO(getFileStream(url));
    }

    /**
     * 根据EXCEL文件路径名，返回一个Workbook对象
     * @param filePath          excel文件的完整路径名
     * @return
     * @throws Exception 
     */
    public static Workbook getWorkbookByFilePath(String filePath) throws Exception {
        Workbook workbook = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(filePath.trim());
            workbook = ExcelUtils.getWorkbookByIO(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        return workbook;
    }

    /**
     * 根据workbook将内容写到filePath文件中：
     * @param workbook          
     * @param filePath          excel文件的完整路径名
     * @throws Exception 
     */
    public static void wirteWorkbookFile(Workbook workbook, String filePath) throws Exception {
        // 文件的目录不存在时，创建父级目录：
        FileOutputStream fos = null;
        try {
            File file = new File(filePath.trim());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            fos = new FileOutputStream(file);
            workbook.write(fos);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 下载Excel文件
     * @param workbook
     * @param fileName          文件名称(带上文件后缀)
     * @return
     * @throws Exception 
     */
    public static ResponseEntity<byte[]> download(Workbook workbook, String fileName) throws Exception {
        return FileUtils.download(ExcelUtils.getBytes(workbook), fileName);
    }

    // -------------------------------------------------------------------------
    // HSSFWorkbook的写入与读出：
    // -------------------------------------------------------------------------

    /** 
     * 给模块文件赋值，并返回一个Workbook：
     * @param filePath          模板文件的路径
     * @param beginRowIndex     起始的行下标(从0开始)
     * @param beginColIndex     起始的列下标(从0开始)
     * @param vals              二维数组值
     * @throws Exception 
     */
    public static Workbook getWorkbook(String filePath, int beginRowIndex, int beginColIndex, String[][] vals) throws Exception {
        Workbook workbook = ExcelUtils.getWorkbookByFilePath(filePath);
        Sheet sheet = workbook.getSheetAt(0);
        setSheetData(beginRowIndex, beginColIndex, vals, sheet);
        return workbook;
    }

    /** 
     * 创建Workbook对象并赋值，返回一个Workbook(默认Excel2007及以上格式)：
     * @param sheetName         工作薄名称
     * @param beginRowIndex     起始的行下标(从0开始)
     * @param beginColIndex     起始的列下标(从0开始)
     * @param vals              二维数组值
     */
    public static Workbook createWorkbook(String sheetName, int beginRowIndex, int beginColIndex, String[][] vals) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        setSheetData(beginRowIndex, beginColIndex, vals, sheet);
        return workbook;
    }

    /**
     * 创建Workbook对象并赋值，返回一个Workbook(默认Excel2007及以上格式)：
     * @param sheetName
     * @param sheetNames
     * @param beginRowIndex
     * @param beginColIndex
     * @param val1
     * @param val2
     * @return
     * @author yjwang22
     */
    public static Workbook createWorkbooks(String sheetName,String sheetNames, int beginRowIndex, int beginColIndex, String[][] val1, String[][] val2) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        Sheet sheet2 = workbook.createSheet(sheetNames);
        setSheetData(beginRowIndex, beginColIndex, val1, sheet);
        setSheetData(beginRowIndex, beginColIndex, val2, sheet2);
        return workbook;
    }

    /** 
     * 创建Workbook对象并赋值，返回一个Workbook(默认Excel2007及以上格式)：
     * @param sheetName         工作薄名称
     * @param beginRowIndex     起始的行下标(从0开始)
     * @param beginColIndex     起始的列下标(从0开始)
     * @param vals              二维数组值
     * 支持大数据量导出excel
     */
    public static Workbook createBigWorkbook(String sheetName, int beginRowIndex, int beginColIndex, String[][] vals) {
        Workbook workbook = new SXSSFWorkbook(200);
        Sheet sheet = workbook.createSheet(sheetName);
        setSheetData(beginRowIndex, beginColIndex, vals, sheet);
        return workbook;
    }

    /**
     * 
     * 描述:		 创建Workbook对象并赋值，返回一个Workbook(默认Excel2007及以上格式) 多个sheet
     *
     * @param sheets
     * @return
     */
    public static Workbook createWorkbook(List<SheetBean> sheets) {
        Workbook workbook = new XSSFWorkbook();
        for (SheetBean sheetBean : sheets) {
            Sheet sheet = workbook.createSheet(sheetBean.getSheetName());
            setSheetData(sheetBean.getBeginRowIndex(), sheetBean.getBeginColIndex(), sheetBean.getVals(), sheet);
        }

        return workbook;
    }

    /** 
     * 创建Workbook对象设置标题样式，并返回一个Workbook：
     * @param workbook         工作薄
     * @param beginRowIndex    起始的行下标(从0开始)(未使用)
     * @param title            标题列
     */
    public static Workbook setTitleStyle(Workbook workbook, int beginRowIndex, String[] title, boolean[] fillRed) {
        Sheet sheet = workbook.getSheetAt(0);
        sheet.setDefaultColumnWidth(15);
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(Font.COLOR_RED);
        //        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //        style.setFont(font);
        //设置列为String类型
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("TEXT"));
        //        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        //        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(font);
        Row row = sheet.createRow(0);
        if (null == fillRed) {
            fillRed = new boolean[title.length];
        }
        for (int i = 0; i < title.length; i++) {
            if(null == title[i]) {
                break;
            }
            Cell cell = row.createCell(i);
            cell.setCellValue(title[i]);
            if (fillRed[i]) {
                cell.setCellStyle(style);
            }
            sheet.setDefaultColumnStyle(i, style);
            sheet.setColumnWidth(i, 4096);
        }
        return workbook;
    }

    /** 
     * 创建Workbook对象设置标题样式，并返回一个Workbook：
     * @param workbook         工作薄
     * @param beginRowIndex    起始的行下标(从0开始)(未使用)
     * @param title            标题列
     */
    public static Workbook setTitleStyle(Workbook workbook, int beginRowIndex, String[] title) {
        Sheet sheet = workbook.getSheetAt(0);
        sheet.setDefaultColumnWidth(15);
        CellStyle style = workbook.createCellStyle();
        //        Font font = workbook.createFont();
        //        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        //        style.setFont(font);

        //设置列为String类型
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("TEXT"));
        Row row = sheet.createRow(0);
        for (int i = 0; i < title.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(title[i]);
            sheet.setDefaultColumnStyle(i, style);
            sheet.setColumnWidth(i, 4096);
        }
        return workbook;
    }

    /**
     * 创建Workbook对象设置标题样式，并返回一个Workbook：
     * @param workbook
     * @param beginRowIndex
     * @param title
     * @param title2
     * @return
     * @author yjwang
     */
    public static Workbook setTitleStyles(Workbook workbook, int beginRowIndex, String[] title, String[] title2,String status) {
        Sheet sheet = workbook.getSheetAt(0);
        Sheet sheet2 = workbook.getSheetAt(1);
        sheet.setDefaultColumnWidth(15);
        sheet2.setDefaultColumnWidth(15);

        Font font = workbook.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        //设置列为String类型
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("TEXT"));
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);// 水平居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);//垂直中对齐
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Row row = sheet.createRow(0);
        row.setHeightInPoints(36);
        for (int i = 0; i < title.length; i++) {
            Cell cell = row.createCell(i);
            if("W".equals(status)){
                if(i==9){
                    cell.setCellValue("待确认出库箱数");
                }else if(i==10){
                    cell.setCellValue("待确认出库瓶数");
                }else{
                    cell.setCellValue(title[i]);
                }
            }else{
                cell.setCellValue(title[i]);
            }
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 4096);
        }
        Row rows = sheet2.createRow(0);
        rows.setHeightInPoints(36);
        for (int j = 0; j < title2.length; j++) {
            Cell cell = rows.createCell(j);
            if("W".equals(status)){
                if(j==21){
                    cell.setCellValue("待确认出库箱数");
                }else if(j==22){
                    cell.setCellValue("待确认出库瓶数");
                }else{
                    cell.setCellValue(title2[j]);
                }
            }else{
                cell.setCellValue(title2[j]);
            }
            cell.setCellStyle(style);
            sheet2.setColumnWidth(j, 4096);
        }
        return workbook;
    }

    /** 通过Workbook得到一个byte[]： */
    public static byte[] getBytes(Workbook workbook) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.write(os);
        return os.toByteArray();
    }

    // -------------------------------------------------------------------------
    // 取值：
    // -------------------------------------------------------------------------

    /**
     * 读取指定工作薄的内容，并每一行内容读入到数组中(里面可能含有一些无效的值和空白数据)
     * @param sheet             工作薄
     * @param readNum           读取实际条数(从1开始;读取全部内容请赋值null)
     * @param beginColIndex     起始的列下标(从0开始)
     * @param colTotal          从列下标开始，要读取的实际列数(从1开始)
     * @return
     */
    public static List<String[]> getContent(Sheet sheet, Integer readNum, int beginColIndex, int colTotal) {
        // 得到值：
        Map<Integer, String[]> map = getContentMap(sheet, readNum, beginColIndex, colTotal);

        List<String[]> list = new ArrayList<String[]>();
        for (int rowIndex : map.keySet()) {
            list.add(map.get(rowIndex));
        }

        return list;
    }

    /**
     * 将指定工作簿的内容读入Map中
     * <br> 说明：
     * <li>		key：行的下标(从0开始)
     * <li>		value：每行的全部内容(里面可能含有一些无效的值和空白数据)
     * @param sheet				工作薄
     * @param readNum			读取实际条数(从1开始;读取全部内容请赋值null)
     * @param beginRowIndex		起始的行下标(从0开始)
     * @param colTotal			从列下标开始，要读取的实际列数(从1开始)
     * @return
     */
    public static Map<Integer, String[]> getContentMap(Sheet sheet, Integer readNum, int beginRowIndex, int colTotal) {
        int rowNum = sheet.getLastRowNum();
        int num = (null == readNum || readNum > rowNum) ? rowNum : readNum;
        Map<Integer, String[]> contents = new LinkedHashMap<Integer, String[]>();
        Row row = null;
        String[] cellVals = null;

        // 循环所有有效行：
        for (; beginRowIndex <= num; beginRowIndex++) {
            row = sheet.getRow(beginRowIndex);
            if (null == row) {
                continue;
            }
            cellVals = new String[colTotal];

            // 循环列并获取值：
            for (int currArrIndex = 0; currArrIndex < colTotal; currArrIndex++) {
                Cell cell = row.getCell(currArrIndex);
                cellVals[currArrIndex] = getCellVal(cell);
                cell = null;
            }

            boolean isAllBlank = true;
            for (String cellVal : cellVals) {
                if (StringUtils.isNotBlank(cellVal)) {
                    isAllBlank = false;
                    break;
                }
            }

            if (isAllBlank) {
                continue;
            }
            contents.put(row.getRowNum(), cellVals);
        }

        return contents;
    }

    /**
     * 获取指定行的所有值
     * <li> 从未输入过内容的单元格，返回null
     * <li> 输入过但后来又删了的空单元格式，返回""
     * <li> 合并的单元格第一格有值，其它的皆为""
     * @param sheet             工作薄
     * @param rowIndex          指定行的下标(从0开始)
     * @return                  此List始终不会为null，即使行不存在时也只是size为0而已
     */
    public static List<String> getRowVals(Sheet sheet, int rowIndex) {
        List<String> vals = new ArrayList<String>();

        // 取行，行不存在时直接返回：
        Row row = sheet.getRow(rowIndex);
        if (null == row) {
            return vals;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (null == cell) {
                vals.add(null);
            } else {
                vals.add(getCellVal(cell));
            }
        }

        return vals;
    }

    public static List<String> getNotEmptyRowVals(Sheet sheet, int rowIndex) {
        List<String> vals = new ArrayList<String>();

        // 取行，行不存在时直接返回：
        Row row = sheet.getRow(rowIndex);
        if (null == row) {
            return vals;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (null == cell || StringUtils.isBlank(getCellVal(cell))) {
                return vals;
            } else {
                vals.add(getCellVal(cell));
            }
        }

        return vals;
    }

    // -------------------------------------------------------------------------
    // 单元格写入与读出：
    // -------------------------------------------------------------------------

    /** 得到指定单元格的值，返回字符串型。如果单元格式为空，则返回""： */
    public static String getCellVal(Cell cell) {
        String cellVal = CELL_DEF_VAL;

        if (cell != null) {
            CellType cellType = cell.getCellTypeEnum();
            if(cellType == CellType.BLANK){

            }else if(cellType == CellType.STRING){
                cellVal = toString(cell.getStringCellValue());
            }else if(cellType == CellType.BOOLEAN){
                cellVal = toString(cell.getBooleanCellValue());
            }else if(cellType == CellType.NUMERIC){
                // 由于日期型与数字型都返回此形式，所以需判断一下：
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellVal = toString(DateUtils.formatDate(cell.getDateCellValue()));
                } else {
                    Double d = cell.getNumericCellValue();
                    // cellVal = new DecimalFormat("0").format(d); // 根据业务自定义格式
                    if (d.doubleValue() == d.longValue()) {
                        cellVal = String.valueOf(d.longValue());
                    } else {
                        cellVal = String.valueOf(d.doubleValue());
                    }
                }
            }else if(cellType == CellType.FORMULA){
                cellVal = toString(cell.getCellFormula());
            }else if(cellType == CellType.ERROR){
                cellVal = toString(cell.getErrorCellValue());
            }
        }

        return cellVal;
    }

    /** 根据网络地址获取EXCEL流： */
    public static InputStream getFileStream(String path) throws Exception {
        URL url = new URL(path);
        URLConnection urlConn = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) urlConn;
        if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("连接失败！");
        }

        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try {
            bis = new BufferedInputStream(urlConn.getInputStream());
            baos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = bis.read(b)) != -1) {
                baos.write(b, 0, len);
            }

            bais = new ByteArrayInputStream(baos.toByteArray());
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                    baos = null;
                }
            } finally {
                if (bis != null) {
                    bis.close();
                    bis = null;
                }
            }
        }

        return bais;
    }

    /** 对Sheet循环赋值 */
    public static void setSheetData(int beginRowIndex, int beginColIndex, String[][] vals, Sheet sheet) {
        Row row = null;
        Cell cell = null;

        // 循环赋值：
        for (int i = 0; i < vals.length; i++) {
            row = sheet.createRow(i + beginRowIndex);

            for (int j = 0; j < vals[i].length; j++) {
                cell = row.createCell(j + beginColIndex);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(vals[i][j]);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private Methods:
    // -------------------------------------------------------------------------

    /** 如果所得结果不为空的话，则将字符串的一些空格去掉(中间及两边)： */
    private static String toString(Object obj) {
        if (null == obj) {
            return CELL_DEF_VAL;
        }

        String str = String.valueOf(obj);

        if (StringUtils.isNotBlank(str)) {
            return str.trim();
        } else {
            return CELL_DEF_VAL;
        }
    }

    /**
     * 从文件读取创建excel
     *
     * @param filePath
     */
    public static XSSFWorkbook createXSSFWorkbook(String filePath) {
        File file = new File(filePath);
        POIFSFileSystem fs;
        XSSFWorkbook workbook = null;
        try {
            //读取excel模板
            workbook = new XSSFWorkbook(new FileInputStream(file));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

}
