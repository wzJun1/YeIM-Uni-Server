package cn.wzjun1.yeimServer.utils;

import java.security.MessageDigest;

public class MD5Util {
    //加密为MD5-32位
    public static String encode(String str) {
        try {
            //确定计算方法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //加密字符串
            byte[] md5Bytes = md5.digest(str.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 16)
                    stringBuilder.append("0");
                stringBuilder.append(Integer.toHexString(val));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }
}