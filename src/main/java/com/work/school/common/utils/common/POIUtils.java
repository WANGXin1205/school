package com.work.school.common.utils.common;



import com.work.school.common.CattyResult;
import com.work.school.common.config.ExcelDataConfigure;
import com.work.school.common.excepetion.TransactionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by linyuanjing on 15/9/16.
 */
public final class POIUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(POIUtils.class);

    /**
     * 默认字体大小 12号字体
     */
    private static final short FONT_HEIGHT_IN_POINTS = 12;
    /**
     * excel 标题行开始
     */
    public static final int EXCEL_HEADER_ROW_INDEX = 0;
    /**
     * excel 第一行
     */
    public static final int EXCEL_FIRST_ROW_INDEX = 1;
    /**
     * EXCEL最大的上传行数
     */
    public static final int EXCEL_MAX_ROW_NUMBER = 1048576;
    /**
     * 默认sheet
     */
    public static final String DEFAULT_SHEET = "sheet1";


    /**
     * 私有构造
     */
    private POIUtils(){

    }

    /**
     * 利用正则表达式 判断小数
     * @param str
     * @return
     */
    public static boolean isDouble(String str){
        return str.matches("^[-+]?\\d+\\.\\d+$");
    }

    /**
     * 利用正则表达式 判断整数
     * @param str
     * @return
     */
    public static boolean isInteger(String str){
        return str.matches("^[-+]?\\d+$");
    }

    /**
     * 拼装excel
     * @param columnNames 列中文名
     * @param columnMethods 列取值方法
     * @param list
     * @return
     */
    public static HSSFWorkbook createWorkbook(String[] columnNames, String[] columnMethods,List list) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            HSSFSheet sheet = workbook.createSheet(DEFAULT_SHEET);
            // 创建第1行，也就是输出列头
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell;
            //列头样式
            CellStyle styleHead = getHeadStyle(workbook);
            //创建列头
            for (int i = 0; i < columnNames.length; i++) {
                cell = row.createCell(i);
                //设置样式
                cell.setCellStyle(styleHead);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(new HSSFRichTextString(columnNames[i]));
                //设置默认列宽
                sheet.setColumnWidth(i, 22 * 256);
            }
            //各行数据
            Object exp = null;
            Method method = null;
            CellStyle styleNormal = getNormalStyle(workbook);
            if (list!=null&&!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    exp = list.get(i);
                    // 创建第i+1行
                    row = sheet.createRow(i + 1);
                    for (int j = 0; j < columnMethods.length; j++) {
                        // 创建列
                        cell = row.createCell(j);
                        method = exp.getClass().getMethod(columnMethods[j]);
                        //通过方法名来取得对应方法返回的结果对象
                        Object obj = method.invoke(exp);
                        //设置样式
                        cell.setCellStyle(styleNormal);
                        cell.setCellType(CellType.STRING);
                        if (obj != null) {
                            cell.setCellValue(obj.toString());
                        } else {
                            cell.setCellValue("");
                        }
                    }
                }
            }
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
            return workbook;
        }
        return workbook;
    }

    /**
     * 通过模板生成excel
     * @param filePath 模板路径
     * @param columnMethods 列取值方法
     * @param list
     * @return
     */
    public static XSSFWorkbook createXSSWorkbookByModel(String[] columnMethods, List list,String filePath) {
        XSSFWorkbook workbook = ExcelUtils.createXSSFWorkbook(filePath);
        try {
            XSSFSheet sheet = workbook.getSheetAt(0);
            // 各行数据
            Object exp = null;
            Method method = null;
            XSSFRow row = null;
            XSSFCell cell;
            if (CollectionUtils.isNotEmpty(list)) {
                for (int i = 0; i < list.size(); i++) {
                    exp = list.get(i);
                    row = sheet.createRow(i + 1);// 创建第i+1行
                    for (int j = 0; j < columnMethods.length; j++) {
                        cell = row.createCell(j);// 创建列
                        // 通过方法名来取得对应方法返回的结果对象
                        method = exp.getClass().getMethod(columnMethods[j]);
                        //通过方法名来取得对应方法返回的结果对象
                        Object obj = method.invoke(exp);
                        cell.setCellType(CellType.STRING);
                        if (obj != null) {
                            String str = obj.toString();
                            if(Date.class.isInstance(obj)){
                                str = DateFormatUtils.format((Date) obj, "yyyy-MM-dd HH:mm:ss");
                            }
                            if (isDouble(str)) {
                                cell.setCellValue(Double.valueOf(str));
                            } else if(isInteger(str)){
                                cell.setCellValue(Integer.valueOf(str));
                            }else{
                                cell.setCellValue(str);
                            }
                        } else {
                            cell.setCellValue("");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return workbook;
        }
        return workbook;
    }


    /**
     *拼装excel
     * @param columnNames 列中文名
     * @param columnMethods 列取值方法
     * @param list
     * @return
     */
    public static XSSFWorkbook createXSSWorkbook(String[] columnNames, String[] columnMethods, List list) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet(DEFAULT_SHEET);
            XSSFRow row = sheet.createRow(0); // 创建第1行，也就是输出列头
            XSSFCell cell;
            CellStyle styleHead = getXSSHeadStyle(workbook);//列头样式
            //创建列头
            for (int i = 0; i < columnNames.length; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(styleHead);//设置样式
                cell.setCellType(CellType.STRING);
                cell.setCellValue(new XSSFRichTextString(columnNames[i]));
                sheet.setColumnWidth(i, 22 * 256);//设置默认列宽
            }
            //各行数据
            Object exp = null;
            Method method = null;
            CellStyle styleNormal = getXSSNormalStyle(workbook);
            if (list!=null&&!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    exp = list.get(i);
                    row = sheet.createRow(i + 1);// 创建第i+1行
                    for (int j = 0; j < columnMethods.length; j++) {
                        cell = row.createCell(j);// 创建列
                        method = exp.getClass().getMethod(columnMethods[j]);
                        //通过方法名来取得对应方法返回的结果对象
                        Object obj = method.invoke(exp);
                        cell.setCellStyle(styleNormal);//设置样式
                        cell.setCellType(CellType.STRING);
                        if (obj != null) {
                            cell.setCellValue(obj.toString());
                        } else {
                            cell.setCellValue("");
                        }
                    }
                }
            }
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
            return workbook;
        }
        return workbook;
    }

    /**
     * 列头样式
     * @param workbook
     * @return
     */
    public static CellStyle getXSSHeadStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints(FONT_HEIGHT_IN_POINTS);//字号12号
        style.setFillPattern(FillPatternType.NO_FILL);//上下居中
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());//设置背景颜色
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN); // 设置单元格的边框为粗体
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex()); // 设置单元格的边框颜色．
        style.setFont(font);
        //style.setWrapText(true);//自动换行
        return style;
    }

    /**
     * 普通样式
     * @param workbook
     * @return
     */
    public static CellStyle getXSSNormalStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints(FONT_HEIGHT_IN_POINTS);//字号12号
        style.setFillPattern(FillPatternType.NO_FILL);//上下居中
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());//设置背景颜色
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN); // 设置单元格的边框为粗体
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex()); // 设置单元格的边框颜色．
        style.setFont(font);
        //style.setWrapText(true);//自动换行
        return style;
    }

    /**
     * 列头样式
     * @param workbook
     * @return
     */
    public static CellStyle getHeadStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints(FONT_HEIGHT_IN_POINTS);//字号12号
        style.setFillPattern(FillPatternType.NO_FILL);//上下居中
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());//设置背景颜色
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN); // 设置单元格的边框为粗体
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex()); // 设置单元格的边框颜色．
        style.setFont(font);
        //style.setWrapText(true);//自动换行
        return style;
    }
    /**
     * 普通样式
     * @param workbook
     * @return
     */
    public static CellStyle getNormalStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        font.setFontName("宋体");//字体
        font.setFontHeightInPoints(FONT_HEIGHT_IN_POINTS);//字号12号
        style.setFillPattern(FillPatternType.NO_FILL);//上下居中
        style.setAlignment(HorizontalAlignment.CENTER);// 左右居中
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());//设置背景颜色
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN); // 设置单元格的边框为粗体
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex()); // 设置单元格的边框颜色．
        style.setFont(font);
        //style.setWrapText(true);//自动换行
        return style;
    }

    /**
     * 判断4列内的数据是否是空行
     *
     * @return
     */
    public static boolean isEmpty(int cellNumber, Row row) {
        int num = 0;
        for (int i = 0; i < cellNumber; i++) {
            if (row.getCell(i) == null) {
                num++;
            } else if (StringUtils.isEmpty(row.getCell(i).toString())) {
                num++;
            }
        }
        return num == cellNumber;

    }

    /**
     * 获取文件后缀名
     * */
    public static String getExtensionName(MultipartFile file){
        String suffix = null;
        if(!file.isEmpty()){
            //文件名
            String filename = file.getOriginalFilename();
            int dot = filename.lastIndexOf(".");
            if(dot>-1 && dot<filename.length()-1){
                suffix = filename.substring(dot+1);
            }
        }
        return suffix;
    }

    /**
     * 简单检验 excel 数据
     * @param file
     * @param tableHeader
     * @return
     */
    public static CattyResult<Workbook> validateExcel(MultipartFile file, String[] tableHeader) {
        CattyResult<Workbook> result = new CattyResult<>();

        try {
            return validateExcel(file.getInputStream(), file.getOriginalFilename(), tableHeader);
        } catch (IOException e) {
            result.setMessage("读取文件内容错误");
            return result;
        }
    }

    /**
     * 简单检验 excel 数据
     * @param file
     * @param tableHeader
     * @return
     */
    public static CattyResult<Sheet> validateExcel2(MultipartFile file, String[] tableHeader) {
        CattyResult result = new CattyResult();

        try {
            return validateExcel2(file.getInputStream(), file.getOriginalFilename(), tableHeader);
        } catch (IOException e) {
            result.setMessage("读取文件内容错误");
            return result;
        }
    }

    /**
     * 简单校验excel数据(暂时强制校验只能有一个sheet)
     *
     * @param inputStream 上传文件流
     * @param filename    上传文件名
     * @return
     */
    public static CattyResult<Sheet> validateExcel2(InputStream inputStream, String filename, String[] tableHeader) {
        CattyResult<Sheet> result = new CattyResult<>();
        if (inputStream == null) {
            result.setMessage("上传的文件不能为空");
            return result;
        }

        //上传数据校验
        Workbook workbook = getWorkbook(inputStream, filename);
        if (workbook == null) {
            result.setMessage("上传的文件格式出现错误");
            return result;
        }
        int sheetNum = workbook.getNumberOfSheets();
        if (sheetNum > 1) {
            result.setMessage("sheet工作表超过一张");
            return result;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet != null) {
            //explain:1、判断上传数据的行数
            int rowNum = sheet.getLastRowNum();
            if (rowNum > EXCEL_MAX_ROW_NUMBER) {
                result.setMessage("最多上传" + EXCEL_MAX_ROW_NUMBER + "行");
                return result;
            }
            List<String> headerList = ExcelUtils.getRowVals(sheet, EXCEL_HEADER_ROW_INDEX);
            //explain:2、判断表头是否为空
            if (headerList.isEmpty()) {
                result.setMessage("表头不能为空");
                return result;
            } else {
                //表头不为空
                boolean flag = true;
                //explain:3、判断表头长度和模版长度是否一致
               /* if(tableHead.length != headerList.size()){
                    flag = false;
                }else{
                    //explain:4、判断表头内容和模版内容是否一致
                    for(int i=0; i<headerList.size(); i++){
                        if(!headerList.get(i).equals(tableHead[i])){
                            flag = false;
                        }
                    }
                }*/
                if (!flag) {
                    result.setMessage("导入的excel与模版excel不一致，导入失败");
                    return result;
                } else {
                    //explain:5、判断模版数据（第一行）是否为空
                    List<String> firstList = ExcelUtils.getRowVals(sheet, EXCEL_FIRST_ROW_INDEX);
                    if (firstList.isEmpty()) {
                        result.setMessage("模版数据不能为空");
                        return result;
                    }
                }
            }
        } else {
            result.setMessage("模版数据不能为空");
            return result;
        }
        result.setData(sheet);
        result.setSuccess(true);
        return result;
    }

    /**
     * 简单校验excel数据(暂时强制校验只能有一个sheet)
     * @param inputStream 上传文件流
     * @param filename 上传文件名
     * @return
     */
    public static CattyResult<Workbook> validateExcel(InputStream inputStream, String filename, String[] tableHeader){
        CattyResult<Workbook> result = new CattyResult<>();
        if(inputStream == null){
            result.setMessage("上传的文件不能为空");
            return result;
        }

        //上传数据校验
        Workbook workbook = getWorkbook(inputStream, filename);
        if(workbook == null){
            result.setMessage("上传的文件格式出现错误");
            return result;
        }
        int sheetNum = workbook.getNumberOfSheets();
        if(sheetNum>1){
            result.setMessage("sheet工作表超过一张");
            return result;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if(sheet != null){
            //explain:1、判断上传数据的行数
            int rowNum = sheet.getLastRowNum();
            if(rowNum > EXCEL_MAX_ROW_NUMBER){
                result.setMessage("最多上传"+EXCEL_MAX_ROW_NUMBER+"行");
                return result;
            }
            List<String> headerList = ExcelUtils.getRowVals(sheet, EXCEL_HEADER_ROW_INDEX);
            //explain:2、判断表头是否为空
            if(headerList.isEmpty()){
                result.setMessage("表头不能为空");
                return result;
            }else{
                //表头不为空
                boolean flag = true;
                //explain:3、判断表头长度和模版长度是否一致
               /* if(tableHead.length != headerList.size()){
                    flag = false;
                }else{
                    //explain:4、判断表头内容和模版内容是否一致
                    for(int i=0; i<headerList.size(); i++){
                        if(!headerList.get(i).equals(tableHead[i])){
                            flag = false;
                        }
                    }
                }*/
                if(!flag){
                    result.setMessage("导入的excel与模版excel不一致，导入失败");
                    return result;
                }else {
                    //explain:5、判断模版数据（第一行）是否为空
                    List<String> firstList = ExcelUtils.getRowVals(sheet, EXCEL_FIRST_ROW_INDEX);
                    if(firstList.isEmpty()){
                        result.setMessage("模版数据不能为空");
                        return result;
                    }
                }
            }
        }else{
            result.setMessage("模版数据不能为空");
            return result;
        }
        result.setData(workbook);
        result.setSuccess(true);
        return result;
    }

    /**
     * 根据文件流获取workbook对象
     * @param inputStream
     * @param filename
     * @return
     */
    public static Workbook getWorkbook(InputStream inputStream, String filename) {
        String _filename = filename.toLowerCase();
        try {
            if (_filename.endsWith(".xls")) {
                return new HSSFWorkbook(inputStream);
            } else if (_filename.endsWith(".xlsx")) {
                return new XSSFWorkbook(inputStream);
            }
        } catch (IOException e) {
            LOGGER.error("上传的excel格式异常,无法转换!");
            return null;
        }
        return null;
    }

    /**
     * 根据文件流获取workbook对象
     * */
    public static Workbook getWorkbook(MultipartFile file) {
        try {
            return getWorkbook(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            LOGGER.error("上传的excel格式异常,无法转换!");
            return null;
        }
    }

    /**
     * 获取excel页数据
     * @param sheet
     * @param beginRowIndex     读取的起始行
     * @param cellNum       列的个数
     * @return      Map<行，列数据>
     */
    public static Map<Integer,String[]> getDataMap(Sheet sheet, int beginRowIndex, int cellNum){
        Map<Integer,String[]> contents = new LinkedHashMap<>();
        if(sheet == null){
            return contents;
        }
        //读取内容的行数
        int maxRow =  sheet.getLastRowNum();

        for(; beginRowIndex<maxRow; beginRowIndex++){
            //定义一个同列个数相等的数组
            String[] cellArray = new String[cellNum];
            //获取到循环中对应的行
            Row row = sheet.getRow(beginRowIndex+1);
            if(row!=null){
                for(int i=0;i<cellNum;i++){
                    //获取每行中的列
                    Cell cell = row.getCell(i);

                    //日期转化
                    //如果传过来的时间time不为空，剔除所有不等与time的数据
                    /*if(time!=null){
                        if(i == 0){
                            if(!time.equals(getCellVal(cell))){
                                continue;
                            }
                        }
                    }*/
                    //列的值取出来放到声明的数组中去
                    cellArray[i] = getCellVal(cell);
                }
                //校验数组元素是否为空，为空break
                //boolean isAllBlank = true;
                int blankCount = 0;
                for(String str : cellArray){
                    if(StringUtils.isBlank(str)){
                        blankCount++;
                        //isAllBlank = false;
                        //break;
                    }
                }

                if(blankCount<cellNum){
                    contents.put(beginRowIndex,cellArray);
                    continue;
                }
                //contents.put(beginRowIndex,cellArray);
            }
        }
        return contents;
    }

    /**
     * 获取excel页数据, 并校验指定列是否含有%
     * @param sheet
     * @param beginRowIndex     读取的起始行
     * @param cellNum       列的个数
     * @param colomns 执行列
     * @return      Map<行，列数据>
     */
    public static Map<Integer,String[]> getDataMapValidPercent(Sheet sheet, int beginRowIndex, int cellNum, Integer[] colomns){
        List<Integer> colomnList = new ArrayList<>();
        if (colomns != null && colomns.length != 0) {
            colomnList = Arrays.asList(colomns);
        }

        Map<Integer,String[]> contents = new LinkedHashMap<>();
        if(sheet == null){
            return contents;
        }
        //读取内容的行数
        int maxRow =  sheet.getLastRowNum();

        for(; beginRowIndex<maxRow; beginRowIndex++){
            //定义一个同列个数相等的数组
            String[] cellArray = new String[cellNum];
            //获取到循环中对应的行
            Row row = sheet.getRow(beginRowIndex+1);
            if(row!=null){
                for(int i=0;i<cellNum;i++){

                    //获取每行中的列
                    Cell cell = row.getCell(i);
                    if (cell == null){
                        continue;
                    }

                    //设置指列格式为字符串
                    if (colomnList.contains(Integer.valueOf(i))) {
                        //获取单元格样式字符串
                        String styleStr = cell.getCellStyle().getDataFormatString();

                        //校验指定列是否含有%
                        if (colomnList.contains(Integer.valueOf(i)) && styleStr.indexOf('%') != -1) {
                            throw new TransactionException("第" + (beginRowIndex+2) + "行 第" + i + "列，不能输入%" );
                        }
                    }

                    //日期转化
                    //如果传过来的时间time不为空，剔除所有不等与time的数据
                        /*if(time!=null){
                            if(i == 0){
                                if(!time.equals(getCellVal(cell))){
                                    continue;
                                }
                            }
                        }*/
                    //列的值取出来放到声明的数组中去
                    cellArray[i] = getCellVal(cell);
                }
                //校验数组元素是否为空，为空break
                //boolean isAllBlank = true;
                int blankCount = 0;
                for(String str : cellArray){
                    if(StringUtils.isBlank(str)){
                        blankCount++;
                        //isAllBlank = false;
                        //break;
                    }
                }

                if(blankCount<cellNum){
                    contents.put(beginRowIndex,cellArray);
                    continue;
                }
                //contents.put(beginRowIndex,cellArray);
            }
        }
        return contents;
    }


    /**
     * 获取excel页数据
     * @param sheet
     * @param beginRowIndex     读取的起始行
     * @param cellNum       列的个数
     * @return      Map<行，列数据>
     */
    public static Map<Integer,String[]> getDataMapWithDate(Sheet sheet, int beginRowIndex, int cellNum){
        Map<Integer,String[]> contents = new LinkedHashMap<>();
        if(sheet == null){
            return contents;
        }
        //读取内容的行数
        int maxRow =  sheet.getLastRowNum();

        for(; beginRowIndex<maxRow; beginRowIndex++){
            //定义一个同列个数相等的数组
            String[] cellArray = new String[cellNum];
            //获取到循环中对应的行
            Row row = sheet.getRow(beginRowIndex+1);
            if(row!=null){
                for(int i=0;i<cellNum;i++){
                    //获取每行中的列
                    Cell cell = row.getCell(i);

                    //如果是时间格式则进行转化,消费者促销导入模版的时候第2，3列时间格式转化
                    if(i==3||i==2) {
                        try {
                            if (DateUtil.isCellDateFormatted(cell)) {// 判断单元格是否属于日期格式
                                SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd");
                                String date1 = dff.format(cell.getDateCellValue());
                                cell.setCellValue(date1);
                            }
                        } catch (Exception e) {
                            LOGGER.info("单元格转化成时间异常");
                        }
                    }
                    //日期转化
                    //如果传过来的时间time不为空，剔除所有不等与time的数据
                    /*if(time!=null){
                        if(i == 0){
                            if(!time.equals(getCellVal(cell))){
                                continue;
                            }
                        }
                    }*/
                    //列的值取出来放到声明的数组中去
                    cellArray[i] = getCellVal(cell);
                }
                //校验数组元素是否为空，为空break
                //boolean isAllBlank = true;
                int blankCount = 0;
                for(String str : cellArray){
                    if(StringUtils.isBlank(str)){
                        blankCount++;
                        //isAllBlank = false;
                        //break;
                    }
                }

                if(blankCount<cellNum){
                    contents.put(beginRowIndex,cellArray);
                    continue;
                }
                //contents.put(beginRowIndex,cellArray);
            }
        }
        return contents;
    }

    /**
     * 获取指定行的excel数据
     * @param sheet
     * @param rowIndex 对应的行号
     * @return
     */
    public static List<String> getRowVals(Sheet sheet,int rowIndex){
        List<String> vals = new ArrayList<String>();
        if(rowIndex<0){
            throw new RuntimeException("查询的行不能为负数");
        }
        Row row = sheet.getRow(rowIndex);
        if(row == null){
            return vals;
        }
        for(int i=0;i<row.getLastCellNum();i++){
            Cell cell = row.getCell(i);
            if(cell == null){
                vals.add(null);
            }else{
                CellType cellType = cell.getCellTypeEnum();
                if(cellType == CellType.BLANK){

                }else if(cellType == CellType.BOOLEAN){
                    vals.add(toString(cell.getBooleanCellValue()));
                }else if(cellType == CellType.ERROR){
                    vals.add(toString(cell.getErrorCellValue()));
                }else if(cellType == CellType.FORMULA){
                    vals.add(toString(cell.getCellFormula()));
                }else if(cellType == CellType.NUMERIC){
                    if(DateUtil.isCellDateFormatted(cell)){//日期格式
                        vals.add(toString(cell.getDateCellValue()));
                    }else{
                        Double d = cell.getNumericCellValue();
                            /*if(d.doubleValue() == d.longValue()){
                                vals.add(toString(d.longValue()));
                            }else{*/
                        vals.add(toString(d.doubleValue()));
                            /*}*/
                    }
                }else if(cellType == CellType.STRING){
                    vals.add(toString(cell.getStringCellValue()));
                }
            }
        }
        return vals;
    }

    /**
     * 获取cell列单元格的数据
     * @param cell
     * @return
     */
    public static String getCellVal(Cell cell){
        String val = "";
        if(cell != null) {
            CellType cellType = cell.getCellTypeEnum();
            if(cellType == CellType.BLANK){

            }else if(cellType == CellType.BOOLEAN){
                val = toString(cell.getBooleanCellValue());
            }else if(cellType == CellType.ERROR){
                val = toString(cell.getErrorCellValue());
            }else if(cellType == CellType.FORMULA){
                val = toString(cell.getCellFormula());
            }else if(cellType == CellType.NUMERIC){
                if (DateUtil.isCellDateFormatted(cell)) {//日期格式
                    val = toString(cell.getDateCellValue());
                } else {
                    Double d = cell.getNumericCellValue();
                    if (d.doubleValue() == d.longValue()) {
                        val = toString(d.longValue());
                    } else {
                        val = toString(d.doubleValue());
                    }
                }
            }else if(cellType == CellType.STRING){
                val = toString(cell.getStringCellValue());
            }
        }
        return val;
    }
    public static String toString(Object object){
        if (object != null){
            String str = String.valueOf(object);
            if(!str.isEmpty()){
                return str.trim();
            }
            else{
                return "";
            }
        }
        return "";
    }

    /**
     * 获取excel页数据
     * @param sheet
     * @param beginRowIndex     读取的起始行
     * @param cellNum       列的个数
     * @return      Map<行，列数据>
     */
    public static Map<Integer,String[]> getDataMapAndCheckIsEmpty(Sheet sheet, int beginRowIndex, int cellNum){

        Map<Integer,String[]> dataMap = getDataMap(sheet, beginRowIndex, cellNum);
        checkIsEmpty(dataMap);

        return dataMap;
    }


    public static void checkIsEmpty(Map<Integer,String[]> dataMap){

        for (int i = 0; i < dataMap.size(); i++) {
            try {
                //1 检查单元是否为空 拒绝魔法数
                String[] colData = dataMap.get(i);
                for (String str : colData) {
                    if (str == null || "".equals(str)) {
                        throw new TransactionException("第" + (i+2) + "行有数据为空");
                    }
                }
            } catch (Exception e) {
                throw new TransactionException("第" + (i+2) + "行有数据为空",e);
            }
        }

    }
    public static void checkIsEmptyExceptCols(Map<Integer,String[]> dataMap, String exceptColIndexs){

        for (int i = 0; i < dataMap.size(); i++) {
            try {
                //1 检查单元是否为空 拒绝魔法数
                String[] colData = dataMap.get(i);
                for (int j=0; j < colData.length; j++) {
                    if(!exceptColIndexs.contains("," + j + ",")) {  //
                        String str = colData[j];
                        if (str == null || "".equals(str)) {
                            throw new TransactionException("第" + (i+2) + "行有数据为空");
                        }
                    }

                }
            } catch (Exception e) {
                throw new TransactionException("第" + (i+2) + "行有数据为空",e);
            }
        }

    }

    /**
     * 获得附件大小
     *
     * @param filePath
     * @return
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 从excel数据中获取Map集合
     *
     * @param multipartFile
     * @param columnName
     */
    public static CattyResult<Map<Integer,String[]>> getDataMapFromExcel(MultipartFile multipartFile, String[] columnName){
        CattyResult<Map<Integer,String[]>> candyResult = new CattyResult<>();

        // 简单校验数据
        var validateExcelResult = POIUtils.validateExcel(multipartFile, columnName);
        if (!validateExcelResult.isSuccess()) {
            candyResult.setMessage(validateExcelResult.getMessage());
            return candyResult;
        }

        var workbook = POIUtils.getWorkbook(multipartFile);

        //拿第一页
        var sheet = workbook.getSheetAt(ExcelDataConfigure.DEFAULT_SHEET);
        var dataMap = POIUtils.getDataMap(sheet, ExcelDataConfigure.DEFAULT_BEGIN_ROW_INDEX, columnName.length);

        candyResult.setData(dataMap);
        candyResult.setSuccess(true);
        return candyResult;
    }


}
