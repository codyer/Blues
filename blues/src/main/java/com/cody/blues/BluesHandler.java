package com.cody.blues;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by cody.yi on 2018/6/6.
 * blues 默认处理类
 */
final public class BluesHandler implements Blues.ExceptionHandler {
    final private static String BLUES_KEY = "BLUES_KEY";
    final private static String BLUES_TOAST = "程序出现了神奇的问题\n紧急修复中！\n建议重启应用。";
    private Context mContext;

    BluesHandler(Context context) {
        mContext = context;
        // 确保blues初始化在 buglly 之后
        CrashReport.initCrashReport(mContext.getApplicationContext(), "a234d4966d", true);
    }

    @Override
    public void handlerException(final Thread thread, final Throwable throwable) {
        if (mContext == null) {
            CrashReport.postCatchedException(throwable);
            return;
        }
        SharedPreferences settings = mContext.getSharedPreferences(BLUES_KEY, Context.MODE_PRIVATE);
        if (throwable.getStackTrace() != null && settings != null) {
            String blues = settings.getString(BLUES_KEY, "Blues");
            String stackTrace = (throwable.getStackTrace())[0].toString();
            if (blues.equals(stackTrace)) {
                //建议使用下面方式在控制台打印异常，这样就可以在Error级别看到红色log
                Log.e("Blues", "--->BluesException:" + thread + "<---", throwable);
                showToast(BLUES_TOAST);
            } else {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(BLUES_KEY, stackTrace);
                editor.apply();
                showToast("Exception Happend\n" + thread + "\n" + throwable.toString());
                CrashReport.postCatchedException(throwable);
            }
        }
    }

    private void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                } catch (Throwable e) {
                    CrashReport.postCatchedException(e);
                }
            }
        });
    }
}
