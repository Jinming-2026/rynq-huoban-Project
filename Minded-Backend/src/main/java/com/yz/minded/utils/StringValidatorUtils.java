package com.yz.minded.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验账号密码工具类
 * @author yz
 */
public class StringValidatorUtils {

    /**
     * 账号密码校验规则
     * @param isPassword 是否为密码
     * @param input 需要校验的数据
     * @return null - 校验成功；非 null - 错误信息
     */
    public static String isValid(boolean isPassword, String input) {
        if (input == null || input.isEmpty()) {
            return "输入为空";
        }

        String validPattern = isPassword
                ? "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]{6,}$"
                : "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]{4,}$";

        // 长度
        if (isPassword && input.length() < 6) {
            return "长度不能小于6";
        }
        if (!isPassword && input.length() < 4) {
            return "长度不能小于4";
        }

        // 中文
        for (char c : input.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return "存在非法字符";
            }
        }

        // 正则（两个分支都要跑）
        if (!Pattern.compile(validPattern).matcher(input).matches()) {
            return "输入非法";
        }
        return null;
    }

    // 邮箱正则
    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * 邮箱格式校验
     * @param email 需要校验的邮箱
     * @return true - 合规；false - 非法
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    /**
     * 电话号码校验（中国大陆 11 位手机号）
     * @param phoneNumber 电话号码
     * @return 校验结果
     */
    public static boolean phoneNumberValidator(String phoneNumber) {
        String regex = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}