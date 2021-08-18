package com.chinadaas.common.util;

import com.google.common.collect.Lists;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.List;

public abstract class PinYinUtils {

    private static final HanyuPinyinOutputFormat DEFAULT_FORMATTER = getFormatter(HanyuPinyinCaseType.LOWERCASE, HanyuPinyinToneType.WITHOUT_TONE, HanyuPinyinVCharType.WITH_U_AND_COLON);

    // 中文标点符号
    private static final List<Integer> PUNCTUATION = Lists.newArrayList(
            183, 215, 8212, 8216, 8217, 8220, 8221, 8230,
            12289, 12290, 12298, 12299, 12302, 12303, 12304, 12305,
            65281, 65288, 65289, 65292, 65306, 65307, 65311
    );

    /**
     * 汉字转拼音快速模式
     *
     * @param chinese 汉字
     */
    public static String cn2Pinyin(String chinese) {
        return cn2Pinyin(chinese, DEFAULT_FORMATTER, true, " ", MODE_FIRST);
    }

    /**
     * 汉字转成拼音
     *
     * @param chinese     汉字
     * @param formatter   格式化
     * @param division    是否增加分割
     * @param divisionStr 分隔符
     * @param mode        展现模式
     */
    public static String cn2Pinyin(String chinese, HanyuPinyinOutputFormat formatter, boolean division, String divisionStr, String mode) {
        StringBuffer buffer = new StringBuffer();
        char[] arr = chinese.toCharArray();
        for (char anArr : arr) {
            // 如果是ASCII 字符或者是标点符号，就忽略
            if (anArr <= 128 || PUNCTUATION.contains((int) anArr)) {
                buffer.append(anArr);
            } else {
                buffer.append(pinyin(anArr, formatter, mode));
                if (division) {
                    buffer.append(divisionStr);
                }
            }
        }
        return buffer.toString();
    }


    /**
     * 格式化
     */
    private static HanyuPinyinOutputFormat getFormatter(HanyuPinyinCaseType caseType, HanyuPinyinToneType toneType, HanyuPinyinVCharType charType) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(caseType);
        format.setToneType(toneType);
        format.setVCharType(charType);
        return format;
    }


    // 首字母大写，其余小写
    private static final String MODE_FIRST = "MODE_FIRST";
    // 首字母大写，其余忽略
    private static final String MODE_FIRST_ONLY = "MODE_FIRST_ONLY";
    // 默认，根据格式化自动适应
    private static final String MODE_DEFAULT = "MODE_DEFAULT";

    /**
     * 转拼音
     *
     * @param c      被转字符
     * @param format 格式化
     * @param mode   模式
     */
    private static String pinyin(char c, HanyuPinyinOutputFormat format, String mode) {
        try {
            String[] pinyinStr2 = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (pinyinStr2.length > 0) {
                String pinyinStr = pinyinStr2[0];

                if (MODE_FIRST_ONLY.equals(mode)) {
                    return pinyinStr.substring(0, 1).toUpperCase();
                } else if (MODE_FIRST.equals(mode)) {
                    return pinyinStr.substring(0, 1).toUpperCase() + pinyinStr.substring(1, pinyinStr.length()).toLowerCase();
                } else if (MODE_DEFAULT.equals(mode)) {
                    return pinyinStr;
                } else {
                    return pinyinStr;
                }
            }

        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

}