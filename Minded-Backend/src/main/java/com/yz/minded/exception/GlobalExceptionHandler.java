package com.yz.minded.exception;

import com.yz.minded.common.BaseResponse;
import com.yz.minded.common.ErrorCode;
import com.yz.minded.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author yz
 */
@RestControllerAdvice   // TODO 利用 spring aop（切面），调用方法前后进行额外的处理
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 全局业务异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)  // 只去捕捉 BusinessException 中的异常
    public BaseResponse<Object> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException{}", e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());

    }

    /**
     * 其他异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<Object> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(),"");
    }
}
