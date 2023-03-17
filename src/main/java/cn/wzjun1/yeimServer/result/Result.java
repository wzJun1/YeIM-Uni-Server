package cn.wzjun1.yeimServer.result;

import cn.wzjun1.yeimServer.constant.StatusCode;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -3960261604608758516L;

    //通用返回成功状态码
    private static final int SUCCESS_CODE = 200;

    //通用返回失败状态码
    private static final int ERROR_CODE = 500;
    private int code;
    private String message;
    private T data;

    /**
     * 构造器,自定义状态码,返回消息,返回数据
     *
     * @param code    状态码
     * @param message 返回消息
     * @param data    返回数据
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功，无返回消息，无返回数据
     *
     * @param <T>
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "success", null);
    }

    /**
     * 成功，无返回消息，返回数据
     *
     * @param data 返回数据
     * @param <T>
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data);
    }

    /**
     * 失败，无返回消息，无返回数据
     *
     * @param <T>
     */
    public static <T> Result<T> error() {
        return new Result<>(ERROR_CODE, "", null);
    }

    /**
     * 失败，返回消息，无返回数据
     *
     * @param message 返回消息
     * @param <T>
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    /**
     * 失败，返回消息，无返回数据
     *
     * @param statusCode
     * @return
     * @param <T>
     */
    public static <T> Result<T> error(StatusCode statusCode) {
        return new Result<>(statusCode.getCode(), statusCode.getDesc(), null);
    }

    /**
     * 失败，返回消息，返回数据
     *
     * @param message 返回消息
     * @param data    返回数据
     * @param <T>
     */
    public static <T> Result<T> error(String message, T data) {
        return new Result<>(ERROR_CODE, message, data);
    }

    /**
     * 失败，返回状态码，消息
     *
     * @param code
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败，返回状态码，消息，返回数据
     *
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 信息，返回状态码，消息，返回数据
     *
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> info(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 转化为json字符串
     *
     * @return
     */
    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }
}