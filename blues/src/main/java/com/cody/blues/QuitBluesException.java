package com.cody.blues;

/**
 * Created by cody.yi on 2018/6/6.
 * blues 退出异常处理类
 */
final public class QuitBluesException extends RuntimeException {
    public QuitBluesException(String message) {
        super(message);
    }

    public QuitBluesException(Throwable cause) {
        super(cause);
    }
}
