package com.example.spring.util;

//工具类，字符串转换和获取包名路径
public class StringUtils {

    public static boolean isEmpty(String str) {
        return null == str || "".equals(str.trim());
    }

    public static String getPackagePath(String classPath, String className) {
        return classPath.replace("." + className, "");
    }

    public static String toFirstByteLowerCase(String className) {
        char[] chars = className.toCharArray();
        chars[0] += 32;
        return new String(chars);
    }
}
