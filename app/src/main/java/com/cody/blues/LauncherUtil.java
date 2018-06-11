package com.cody.blues;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by cody.yi on 2018/6/11.
 * 改变桌面图标，会有一定延迟
 */
public class LauncherUtil {
    public static String[] sLogos = {"com.cody.blues.default_activity", "com.cody.blues.test_activity", "com.cody.blues.preview_activity"};

    public static void changeTo(Activity context, int index) {
        PackageManager pm = context.getPackageManager();
        for (int i = 0; i < sLogos.length; i++) {
            ComponentName component = new ComponentName(context.getBaseContext(), sLogos[i]);
            if (i == index) {
                enableComponent(pm, component);
            } else {
                disableComponent(pm, component);
            }
        }
        restart(context, pm);
    }

    private static void enableComponent(PackageManager pm, ComponentName componentName) {
        if (pm.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private static void disableComponent(PackageManager pm, ComponentName componentName) {
        if (pm.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    private static void restart(Activity context, PackageManager pm) {
        //Intent 重启 Launcher 应用
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    am.killBackgroundProcesses(res.activityInfo.packageName);
                }
            }
        }
    }
}
