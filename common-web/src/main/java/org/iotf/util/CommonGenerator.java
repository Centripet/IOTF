package org.iotf.util;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class CommonGenerator {
    // 生成指定长度的数字验证码
    public static String generateNumericCaptcha(int length) {
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            captcha.append(random.nextInt(10)); // 生成0-9之间的数字
        }

        return captcha.toString();
    }

    public static String generateHexSalt(int hexLength) {
        int byteLength = hexLength / 2; // 每个字节 = 2 位 hex
        byte[] salt = new byte[byteLength];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        // 转为 Hex 字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

//    public static String generateUUID() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }

    public static Boolean isNullOrBlank(String str) { return Objects.toString(str, "").isBlank(); }

    public static boolean isAllBlank(Object... values) {
        if (values == null) {
            return true;
        }

        for (Object v : values) {
            if (v == null) {
                continue;
            }

            if (v instanceof String str) {
                if (!str.isBlank()) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public static String getInitial(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        char firstChar = str.charAt(0);

        // 如果是英文字母，直接返回大写
        if (Character.isLetter(firstChar) && firstChar < 128) {
            return String.valueOf(Character.toUpperCase(firstChar));
        }

        // 如果是中文，使用拼音库获取首字母
        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(firstChar);
        if (pinyins != null && pinyins.length > 0) {
            return String.valueOf(Character.toUpperCase(pinyins[0].charAt(0)));
        }

        // 非中文/英文字符，返回 '#'
        return "#";
    }

}
