package com.mall.product.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.StringUtils;

public class PinyinUtil {
    private static final HanyuPinyinOutputFormat FORMAT;

    static {
        FORMAT = new HanyuPinyinOutputFormat();
        // 不带声调
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 获取字符串第一个字符的拼音首字母大写
     * @param str 品牌名称
     * @return 首字母，特殊字符返回#
     */
    public static String getFirstLetter(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        char firstChar = str.charAt(0);
        // 英文字母/数字直接返回大写
        if (Character.isLetterOrDigit(firstChar)) {
            return String.valueOf(firstChar).toUpperCase();
        }
        // 汉字处理，捕获拼音工具异常
        try {
            String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(firstChar, FORMAT);
            if (pinyinArr != null && pinyinArr.length > 0) {
                return pinyinArr[0].substring(0, 1).toUpperCase();
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            // 异常日志打印，兜底返回#
            e.printStackTrace();
        }
        // 特殊符号、转换失败默认#
        return "#";
    }
}