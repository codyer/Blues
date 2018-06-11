package com.cody.blues;

/**
 * Created by cody.yi on 2018/6/6.
 * blues 退出异常处理类
 */
final public class BluesQuitException extends RuntimeException {
    public BluesQuitException(String message) {
        super(message);
    }

    public BluesQuitException(Throwable cause) {
        super(cause);
    }
}
