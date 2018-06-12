package com.cody.demos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;

import com.cody.blues.Blues;

/**
 * Created by cody.yi on 2018/6/6.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Blues.install(getApplicationContext());
            }
        });
        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Blues.uninstall();
            }
        });
        findViewById(R.id.change2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.sActivity = 0;
                LauncherUtil.switchIcon(MainActivity.this);
            }
        });
        findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.sActivity = 1;
                LauncherUtil.switchIcon(MainActivity.this);
            }
        });
        findViewById(R.id.change3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.sActivity = 2;
                LauncherUtil.switchIcon(MainActivity.this);
            }
        });
        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("点击异常");
            }
        });
        findViewById(R.id.thread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        throw new RuntimeException("子线程异常");
                    }
                }.start();
            }
        });
        findViewById(R.id.handler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException("handler异常");
                    }
                });
            }
        });

        findViewById(R.id.act).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });

        findViewById(R.id.noact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UnknownActivity.class));
            }
        });
    }
}
