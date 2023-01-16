package cn.wzjun1.yeimServer.handler;

import cn.wzjun1.yeimServer.result.Result;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@ResponseBody
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Result<Object> allExceptionHandler(HttpServletRequest request,
                                              Exception exception) throws Exception {
        exception.printStackTrace();
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) exception;
            return Result.error(methodArgumentNotValidException.getBindingResult().getFieldError().getDefaultMessage());
        } else if (exception instanceof MissingServletRequestParameterException) {
            return Result.error("缺少参数：" + ((MissingServletRequestParameterException) exception).getParameterName());
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return Result.error(exception.getMessage());
        } else if (exception instanceof ClassCastException || exception instanceof HttpMessageNotReadableException) {
            return Result.error("request params invalid");
        } else {
            return Result.error(exception.getMessage());
        }

    }
}
