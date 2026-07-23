package com.medical.platform.utils;

/**
 * 敏感信息脱敏工具类
 * 用于对患者姓名、身份证号、手机号等敏感信息进行脱敏处理
 */
public class DesensitizationUtil {

    /**
     * 对姓名进行脱敏
     * 张三 -> 张*
     * 李四四 -> 李**
     * 王五六六 -> 王***
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        if (name.length() == 1) {
            return name;
        } else if (name.length() == 2) {
            return name.charAt(0) + "*";
        } else {
            return name.charAt(0) + "*".repeat(name.length() - 1);
        }
    }

    /**
     * 对身份证号进行脱敏
     * 123456789012345678 -> 123456********5678
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return "";
        }

        if (idCard.length() >= 15) {
            return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
        }

        return idCard;
    }

    /**
     * 对手机号进行脱敏
     * 13812345678 -> 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }

        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }

        return phone;
    }

    /**
     * 对银行卡号进行脱敏
     * 1234567890123456 -> 1234********3456
     *
     * @param cardNumber 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String desensitizeBankCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }

        if (cardNumber.length() >= 12) {
            return cardNumber.substring(0, 4) + "********" + cardNumber.substring(cardNumber.length() - 4);
        }

        return cardNumber;
    }

    /**
     * 对邮箱进行脱敏
     * example@example.com -> e****e@example.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return "";
        }

        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].isEmpty()) {
            return email;
        }

        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username.charAt(0) + "*@" + domain;
        } else {
            return username.charAt(0) + "*".repeat(username.length() - 2) +
                   username.charAt(username.length() - 1) + "@" + domain;
        }
    }

    /**
     * 对地址进行脱敏
     * 北京市朝阳区某某街道某某小区 -> 北京市朝阳区***
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String desensitizeAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }

        // 保留前3个字符，后面用***代替
        if (address.length() > 3) {
            return address.substring(0, 3) + "***";
        }

        return address;
    }

    /**
     * 通用脱敏方法
     * 保留前prefixLen位和后suffixLen位，中间用*代替
     *
     * @param str 原字符串
     * @param prefixLen 保留前几位
     * @param suffixLen 保留后几位
     * @return 脱敏后的字符串
     */
    public static String desensitize(String str, int prefixLen, int suffixLen) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        if (str.length() <= prefixLen + suffixLen) {
            return str;
        }

        String prefix = str.substring(0, prefixLen);
        String suffix = str.substring(str.length() - suffixLen);
        int maskLength = str.length() - prefixLen - suffixLen;

        return prefix + "*".repeat(maskLength) + suffix;
    }

    /**
     * 判断字符串是否为身份证号格式
     */
    public static boolean isIdCard(String str) {
        if (str == null) {
            return false;
        }
        return str.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");
    }

    /**
     * 判断字符串是否为手机号格式
     */
    public static boolean isPhone(String str) {
        if (str == null) {
            return false;
        }
        return str.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 判断字符串是否为邮箱格式
     */
    public static boolean isEmail(String str) {
        if (str == null) {
            return false;
        }
        return str.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
}