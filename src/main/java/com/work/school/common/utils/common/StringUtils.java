package com.work.school.common.utils.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by WANGXin on 2017/5/18.
 */
public class StringUtils {
    private static final String  NUMBER_FORMAT       = "^[0-9]*$";

    private static final String  NUMBERIC_FORMAT     = "^(-?\\d+)(\\.\\d+)?";

    private static final String  TWO_DECIMALS_FORMAT = "^[0-9]+(.[0-9]{2})?$";  //两位小数

    private static final Pattern PERCENT_REGEX       = Pattern.compile("[/%_]");

    // -------------------------------------------------------------------------
    // 字符串截取：
    // -------------------------------------------------------------------------

    /**
     * 截取中英文混合的字符串，保留前面共maxByteLength个"字节"
     * <p>	(默认为GBK编码，即一个汉字占2个字节)
     * @param targetStr			目标字符串
     * @param maxByteLength		截取的最大的"字节"长度
     * @return
     * @throws Exception
     */
    public static String trunString(String targetStr, int maxByteLength) throws Exception {
        return trunString(targetStr, maxByteLength, "GBK");
    }

    /**
     * 截取中英文混合的字符串，保留前面共maxByteLength个"字节"
     * @param targetStr			目标字符串
     * @param maxByteLength		截取的最大的"字节"长度
     * @param charset			编码集，只能为"GBK" 和 "UTF-8"
     * <br>						(因为对于中文，GBK与UTF-8转为字节时都是负数)
     * <br>						(GBK编码一个汉字占2个字节；UTF-8编码一个汉字占3个字节)
     * @return
     * @throws Exception
     */
    public static String trunString(String targetStr, int maxByteLength, String charset) throws Exception {
        // 1.1、检查：字符串、及长度
        if (isBlank(targetStr) || maxByteLength < 1) {
            return targetStr;
        }

        // 1.2、检查：编码
        if (!"GBK".equalsIgnoreCase(charset) && !"UTF-8".equalsIgnoreCase(charset)) {
            return null;
        }

        byte[] targetByte = getBytes(targetStr, charset);

        // 2.1、转换成字节数组后其长度仍小于等于指定的长度时，则直接返回：
        if (targetByte.length <= maxByteLength) {
            return targetStr;
        }

        // 2.2.1、如果当前字节大于等于0，则表示截取的是非汉字，这时直接截断即可：
        if (targetByte[maxByteLength - 1] >= 0) {
            return newString(targetByte, 0, maxByteLength, charset);
        }

        // 2.2.2、否则当前字节小于0，表示截断的位置有可能将中文字符给截断了：
        // 获取负数的总个数
        int negativeCount = 0;
        // 从当前字节的位置开始，往前一个个的检查是否为负数，直到为正数(即为非中文时)时才跳出循环
        int index = maxByteLength - 1;

        while (index >= 0) {
            if (targetByte[index] < 0) {
                negativeCount++;
            } else {
                break;
            }
            index--;
        }

        // GBK编码一个汉字占2个字节；UTF-8编码一个汉字占3个字节：
        int step = "GBK".equalsIgnoreCase(charset) ? 2 : 3;
        int residue = negativeCount % step;
        return newString(targetByte, 0, maxByteLength - residue, charset);
    }

    // -------------------------------------------------------------------------
    // 编码方面：
    // 		说明：对于一些官方方法重写，以避免要catch UnsupportedEncodingException异常
    //		注意：调用此方法时，一般要确保编码是正确的
    // -------------------------------------------------------------------------

    /** 重写官方的new String(...)方法，以不用catch其编码异常：*/
    public static String newString(byte[] bytes, String charset) throws UnsupportedEncodingException {
        return new String(bytes, charset);
    }

    /** 重写官方的new String(...)方法，以不用catch其编码异常：*/
    public static String newString(byte[] bytes, int offset, int length, String charset) throws UnsupportedEncodingException {
        return new String(bytes, offset, length, charset);
    }

    /** 重写官方的str.getBytes(...)方法，以不用catch其编码异常：*/
    public static byte[] getBytes(String str, String charset) throws UnsupportedEncodingException {
        return str.getBytes(charset);
    }

    // -------------------------------------------------------------------------
    // 获取随机字符：
    // -------------------------------------------------------------------------

    /**
     * 随机获取指定长度的字符串(由数字与字母组成)：
     * <p>	另：如果导入commons-lang.jar包，可用此类：
     * <br>		RandomStringUtils.randomAlphanumeric(length)
     * @param length			获取的长度
     * @return					长度小于或等于0时，则返回""
     */
    public static String getRandomStr(int length) {
        if (length <= 0) {
            return "";
        }

        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(str.charAt(random.nextInt(str.length())));
        }
        return sb.toString();
    }

    /**
     * 随机获取指定长度的字符串(仅由数字组成)：
     * @param length			获取的长度
     * @return					长度小于或等于0时，则返回""
     */
    public static String getRandomStr4Number(int length) {
        if (length <= 0) {
            return "";
        }

        String str = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(str.charAt(random.nextInt(str.length())));
        }
        return sb.toString();
    }

    /**
     * 获取随机的32位UUID字符串：(已将其中的"-"替换掉)
     * <p>	另：还可使用RandomGUID
     * <br>		地址：http://www.javaexchange.com/aboutRandomGUID.html
     * @return
     */
    public static String getRandomUUIDStr() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // -------------------------------------------------------------------------
    // 判断字符串是否为空白：
    //	(说明：下面两方法直接拷贝于"org.apache.commons.lang.StringUtils")
    // -------------------------------------------------------------------------

    /**
     * 判断字符串是否为空白，常见几种情况如下：
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (null == str || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不为空白，常见几种情况如下：
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     */
    public static boolean isNotBlank(String str) {
        return !StringUtils.isBlank(str);
    }

    // -------------------------------------------------------------------------
    // 判断字符串是否为空：
    //	(说明：下面两方法直接拷贝于"org.apache.commons.lang.StringUtils")
    // -------------------------------------------------------------------------

    /**
     * 判断字符串是否为空，常见几种情况如下：
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     */
    public static boolean isEmpty(String str) {
        return null == str || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空，常见几种情况如下：
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     */
    public static boolean isNotEmpty(String str) {
        return !StringUtils.isEmpty(str);
    }

    // -------------------------------------------------------------------------
    // 右侧填充字符串：
    //	(说明：下面两方法直接拷贝于"org.apache.commons.lang.StringUtils")
    // -------------------------------------------------------------------------

    private static final int PAD_LIMIT = 8192;

    /**
     * 右侧填充字符串
     * <pre>
     * StringUtils.rightPad(null, *)   = null
     * StringUtils.rightPad("", 3)     = "   "
     * StringUtils.rightPad("bat", 3)  = "bat"
     * StringUtils.rightPad("bat", 5)  = "bat  "
     * StringUtils.rightPad("bat", 1)  = "bat"
     * StringUtils.rightPad("bat", -1) = "bat"
     * </pre>
     */
    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    /**
     * 右侧填充字符串
     * <pre>
     * StringUtils.rightPad(null, *, *)     = null
     * StringUtils.rightPad("", 3, 'z')     = "zzz"
     * StringUtils.rightPad("bat", 3, 'z')  = "bat"
     * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
     * StringUtils.rightPad("bat", 1, 'z')  = "bat"
     * StringUtils.rightPad("bat", -1, 'z') = "bat"
     * </pre>
     */
    public static String rightPad(String str, int size, char padChar) {
        if (null == str) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(padding(pads, padChar));
    }

    /**
     * 右侧填充字符串
     * <pre>
     * StringUtils.rightPad(null, *, *)      = null
     * StringUtils.rightPad("", 3, "z")      = "zzz"
     * StringUtils.rightPad("bat", 3, "yz")  = "bat"
     * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
     * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
     * StringUtils.rightPad("bat", 1, "yz")  = "bat"
     * StringUtils.rightPad("bat", -1, "yz") = "bat"
     * StringUtils.rightPad("bat", 5, null)  = "bat  "
     * StringUtils.rightPad("bat", 5, "")    = "bat  "
     * </pre>
     */
    public static String rightPad(String str, int size, String padStr) {
        if (null == str) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    // -------------------------------------------------------------------------
    // JOIN组合：
    //	(说明：下面方法直接拷贝于"org.apache.commons.lang.StringUtils")
    //	(说明：下面方法里的原来是StrBuilder，现用StringBuilder代替)
    // -------------------------------------------------------------------------

    /**
     * join
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     * @param array				目标数组
     * @param separator			分隔符
     * @return
     */
    public static String join(Object[] array, String separator) {
        if (null == array) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * join
     * <br> 说明：下面方法里的原来是StrBuilder，现用StringBuilder代替
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     * @param array				目标数组
     * @param separator			分隔符
     * @param startIndex		起始下标
     * @param endIndex			结束下标
     * @return
     */
    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (null == array) {
            return null;
        }
        if (null == separator) {
            separator = "";
        }

        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= ((null == array[startIndex] ? 16 : array[startIndex].toString().length()) + separator.length());

        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    // -------------------------------------------------------------------------
    // 其它：
    // 		1、重复输出多个字符
    // 		2、对于指定属性的第一个字母大写
    // 		3、去掉字符串的最后一个字符
    // -------------------------------------------------------------------------

    /**
     * 重复输出多个字符
     * <br>	(说明：此方法直接拷贝于"org.apache.commons.lang.StringUtils")
     * <pre>
     * StringUtils.padding(0, 'e')  = ""
     * StringUtils.padding(3, 'e')  = "eee"
     * StringUtils.padding(-2, 'e') = IndexOutOfBoundsException
     * </pre>
     */
    public static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

    /**
     * 去掉字符串的最后一个字符(常用于去掉最后一个分号(;))
     * @param str				目标字符串
     * @return					如果为null，或长度为0，则返回本身
     */
    public static String removeLastChar(String str) {
        if (null == str || str.length() == 0) {
            return str;
        }

        return str.substring(0, str.length() - 1);
    }

    /**
     * 检测是否为空。如果为空，将返回空串""；否则返回原始值
     * @param str				指定值
     * @return
     */
    public static String checkNull(String str) {
        return isBlank(str) ? "" : str;
    }

    /**
     * 检测是否为空。如果为空，将返回null；否则返回原始值
     * @param str               指定值
     * @return
     */
    public static String checkBlank(String str) {
        return isBlank(str) ? null : str;
    }

    /**
     * 生成32位UUID
     * @return
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 描述: 对特殊字符进行转义，目的是为了在ORACLE端识别特殊字符
     * @param str       待转义字符串
     * @return
     */
    public static String getEscapeString(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            String s = String.valueOf(str.charAt(i));
            if (PERCENT_REGEX.matcher(s).find()) {
                sb.append("/");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * @Description:    把一个用',' 隔开的字符串解析成一个List<Integer>
     * @param str:      like "1,2,3"
     * @return List<Integer>
     */
    public static List<Integer> parseStringToIntegerList(String str) {
        List<Integer> ids = new ArrayList<Integer>();
        if (isBlank(str)) {
            return ids;
        }
        String[] strs = str.split(",");
        for (int i = 0; i < strs.length; i++) {
            Integer id = Integer.parseInt(strs[i].trim());
            ids.add(id);
        }
        return ids;
    }

    /**
     * 描述:  整型转字符串
     * @param i     要转换的整型
     * @return
     */
    public static String numberToString(Integer i) {
        return null == i ? "" : i.toString();
    }

    /**
     * 描述:  字符串转整型(有小数小数点前截取)
     * @param str   要转换的字符串
     * @return
     */
    public static Integer stringToNumber(String str) {
        String temp = str;
        if (isNotBlank(temp)) {
            int idx;
            if ((idx = str.indexOf(".")) != -1) {
                temp = str.substring(0, idx);
            }
            return Integer.parseInt(temp);
        }
        return null;
    }

    /**
     * 描述:   字符串是否为整数
     * @param str   验证的字符串
     * @return
     */
    public static boolean isNumber(String str) {
        if (isNotEmpty(str) && str.matches(NUMBER_FORMAT)) {
            return true;
        }
        return false;
    }

    /**
     * 描述:   字符串是否为整数(允许小数)
     * @param str   验证的字符串
     * @return
     */
    public static boolean isNumeric(String str) {
        if (StringUtils.isNotEmpty(str) && str.matches(NUMBERIC_FORMAT)) {
            return true;
        }
        return false;
    }

    /**
     * 描述:  字符串是否为两位小数
     * @param str   验证的字符串
     */
    public static boolean isTwoDecimals(String str) {
        if (isNotEmpty(str) && str.matches(TWO_DECIMALS_FORMAT)) {
            return true;
        }
        return false;
    }

    /**
     *
     * 描述:		字符编码转换utf-8格式
     *
     * @param bytes
     * @return
     */
    public static String getStringUtf8(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding failed", e);
        }
    }

    /**
     * 获取字符串的字节长度
     * @param value 字符串
     * @return 字节长度,汉字按2个字节算
     */
    public static long getByteLength(String value) {
        if (org.springframework.util.StringUtils.isEmpty(value)) {
            return 0;
        }

        int strLen = 0;
        try {
            char[] cs = value.toCharArray();
            for (char item : cs) {
                strLen += String.valueOf(item).getBytes("gbk").length;
            }
        } catch (Exception e) {
            //不处理,此处抛出的异常必然不会发生.
            strLen = 0;
        }

        return strLen;
    }

    /**
     * 将字符串以指定符号分割
     *
     * @param str 要分割的字符串
     * @param op  符号
     * @return 字符串数组
     */
    public static String[] split(String str, String op) {
        return split(str, op, false);
    }

    /**
     * 将字符串以指定符号分割
     *
     * @param str         要分割的字符串
     * @param op          符号
     * @param removeEmpty 是否移除空字符串
     * @return 字符串数组
     */
    public static String[] split(String str, String op, Boolean removeEmpty) {
        String[] r = str.split(op, -1);
        if (!removeEmpty) {
            return r;
        }

        List<String> list = new ArrayList();
        for (String item : r) {
            if (!isEmpty(item)) {
                list.add(item);
            }
        }

        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }

    /**
     * 将字符串以指定符号分割并返回长整型List集合
     *
     * @param str                    要分割的字符串
     * @param op                     符号
     * @param ignoreConvertException 是否忽略转化异常
     * @return 长整型List集合
     * @throws Exception 转化异常
     */
    public static List<Long> toLongList(String str, String op, Boolean ignoreConvertException) throws Exception {
        String[] arr = split(str, op);

        return ArrayUtils.toList(ArrayUtils.toLongArray(arr, ignoreConvertException));
    }
}
