package com.cody.blues;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cody.yi on 2018/6/8.
 * bugly 配置
 */
public class CrashUtil {

    public static void init(Context context) {
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel("debug");
        strategy.setCrashHandleCallback(new StatCrashHandleCallback());
        //tencent bugly
        CrashReport.initCrashReport(context.getApplicationContext(), "a234d4966d", true, strategy);
        CrashReport.setUserId("15088886666");
    }

    static class StatCrashHandleCallback extends CrashReport.CrashHandleCallback {
        @Override
        public synchronized Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                                   String errorMessage, String errorStack) {
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                map.put("onCrashHandleStartkey" + i, "value" + this);
            }
            return map;
        }

        @Override
        public synchronized byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                                    String errorMessage, String errorStack) {
            return super.onCrashHandleStart2GetExtraDatas(crashType, errorType, errorMessage, errorStack);
        }
    }

    public static void postCatchedException(Throwable throwable) {
        CrashReport.postCatchedException(throwable);
    }

    public static void postCatchedException(Context context, Throwable throwable) {
        for (int i = 0; i < 100; i++) {
            CrashReport.putUserData(context, "key" + i, "value" + context);
        }
        CrashReport.postCatchedException(throwable);
    }
}
