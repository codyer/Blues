package com.cody.blues;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by cody.yi on 2018/6/6.
 *
 */
public class BluesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Blues.install(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
