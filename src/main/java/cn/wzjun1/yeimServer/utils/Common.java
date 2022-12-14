package cn.wzjun1.yeimServer.utils;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class Common {

    /**
     * 检测字符串是否是json对象
     *
     * @param str
     * @return
     */
    public static boolean isJSONObject(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 判断对象是否为空
     *
     * @param obj
     * @return
     */
    public static Boolean isNotEmptyBean(Object obj) {
        Boolean flag = false;
        try {
            if (null != obj) {
                //得到类对象
                Class<?> c = (Class<?>) obj.getClass();
                //得到属性集合
                Field[] fs = c.getDeclaredFields();
                //得到方法体集合
                Method[] methods = c.getDeclaredMethods();
                //遍历属性
                for (Field f : fs) {
                    //设置属性是可以访问的(私有的也可以)
                    f.setAccessible(true);
                    String fieldGetName = parGetName(f.getName());
                    //判断属性是否存在get方法
                    if (!checkGetMet(methods, fieldGetName)) {
                        continue;
                    }
                    //得到此属性的值
                    Object val = f.get(obj);
                    //只要有1个属性不为空,那么就不是所有的属性值都为空
                    if (!Objects.isNull(val)) {
                        flag = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 拼接某属性的 get方法
     *
     * @param fieldName
     * @return String
     */
    public static String parGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        int startIndex = 0;
        if (fieldName.charAt(0) == '_')
            startIndex = 1;
        return "get"
                + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
                + fieldName.substring(startIndex + 1);
    }

    /**
     * 判断是否存在某属性的 get方法
     *
     * @param methods
     * @param fieldGetMet
     * @return boolean
     */
    public static Boolean checkGetMet(Method[] methods, String fieldGetMet) {

        for (Method met : methods) {
            if (fieldGetMet.equals(met.getName())) {
                return true;
            }
        }
        return false;
    }
}
