
Language
* [Chinese]

# 很多人曲解了这个库的用意，现特声明如下
当APP主线程抛出异常时就会导致APP crash，可能是由于view点击时抛出了异常等等，像这种异常我们更希望即使点击没反应也不要crash，用户顶多会认为是点了没反应，或者认为是本来就不可以点击，这时候就可以使用Cockroach，而且没有其他副作用，用户就跟没点一样，并且不影响其他逻辑。这样总比每次都crash要好很多。当然这个库也存在不确定因素，比如Activity初始化时等抛出了异常，就会导致Activity什么都不显示，但这并不是ANR，是由于Activity生命周期没有执行完整导致，issues中很多人认为这是ANR，进而导致微博上有人说这个库捕获到异常后会导致ANR，其实这个时候主线程并没有被阻塞，也就不存在ANR。当然这个库对于native异常和ANR也是无能为力的，只能保证java异常不会导致crash。


## Blues

> 打不死的小强,永不crash的Android。

> android 开发中最怕的就是crash，好好的APP测试时没问题，一发布就各种crash，只能通过紧急发布hotfix来解决，但准备hotfix的时间可能很长，导致这段时间用户体验非常差，android中虽然可以通过设置 Thread.setDefaultUncaughtExceptionHandler来捕获所有线程的异常，但主线程抛出异常时仍旧会导致activity闪退，app进程重启。使用Blues后就可以保证不管怎样抛异常activity都不会闪退，app进程也不会重启。


## 推荐使用姿势
* 当线上发现进入某个Activity有大量crash时，若装载Blues后不影响APP运行，不影响用户体检，就可以通过后端控制来自动开启Blues，当退出这个Activity后自动卸载Blues。这样其他用户再次进入该Activity就不会crash。

* 可以根据需要在任意地方（不一定要在主线程）装载，在任意地方卸载。可以多次装载和卸载。
例如：

```java
  

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


Blues.install(new Blues.ExceptionHandler() {

           // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException

            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
            //开发时使用Blues可能不容易发现bug，所以建议开发阶段在handlerException中用Toast谈个提示框，
            //由于handlerException可能运行在非ui线程中，Toast又需要在主线程，所以new了一个new Handler(Looper.getMainLooper())，
            //所以千万不要在下面的run方法中执行耗时操作，因为run已经运行在了ui线程中。
            //new Handler(Looper.getMainLooper())只是为了能弹出个toast，并无其他用途
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        //建议使用下面方式在控制台打印异常，这样就可以在Error级别看到红色log
                            Log.e("AndroidRuntime","--->BluesException:"+thread+"<---",throwable);
                            Toast.makeText(App.this, "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException("..."+(i++));
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });


```
卸载 Blues

```java

 Blues.uninstall();
 
```

### 测试
装载Blues后点击view抛出异常和new Handler中抛出异常

```java


        final TextView textView = (TextView) findViewById(R.id.text);
        findViewById(R.id.install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("已安装 Blues");
                install();
            }
        });

        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("已卸载 Blues");
                Blues.uninstall();
            }
        });

        findViewById(R.id.but1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("click exception...");
            }
        });

        findViewById(R.id.but2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException("handler exception...");
                    }
                });
            }
        });

        findViewById(R.id.but3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        throw new RuntimeException("new thread exception...");
                    }
                }.start();
            }
        });

        findViewById(R.id.but4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SecActivity.class));
            }
        });

    }

    private void install() {
        Blues.install(new Blues.ExceptionHandler() {
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {

                Log.d("Blues", "MainThread: " + Looper.getMainLooper().getThread() + "  curThread: " + Thread.currentThread());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Log.e("AndroidRuntime","--->BluesException:"+thread+"<---",throwable);
                            Toast.makeText(getApplicationContext(), "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException("..."+(i++));
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });
    }
````
```

捕获到的堆栈如下,可以看到都已经被 `at com.cody.blues.Blues$1.run(Blues.java:47)` 拦截，APP没有任何影响，没有闪退，也没有重启进程

```java

02-16 09:58:00.660 21199-21199/wj.com.fuck E/AndroidRuntime: --->BluesException:Thread[main,5,main]<---
                                                             java.lang.RuntimeException: click exception...
                                                                 at wj.com.fuck.MainActivity$3.onClick(MainActivity.java:53)
                                                                 at android.view.View.performClick(View.java:4909)
                                                                 at android.view.View$PerformClick.run(View.java:20390)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at com.cody.blues.Blues$1.run(Blues.java:47)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at android.app.ActivityThread.main(ActivityThread.java:5826)
                                                                 at java.lang.reflect.Method.invoke(Native Method)
                                                                 at java.lang.reflect.Method.invoke(Method.java:372)
                                                                 at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1009)
                                                                 at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:804)
02-16 09:58:12.401 21199-21199/wj.com.fuck E/AndroidRuntime: --->BluesException:Thread[main,5,main]<---
                                                             java.lang.RuntimeException: handler exception...
                                                                 at wj.com.fuck.MainActivity$4$1.run(MainActivity.java:63)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at com.cody.blues.Blues$1.run(Blues.java:47)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at android.app.ActivityThread.main(ActivityThread.java:5826)
                                                                 at java.lang.reflect.Method.invoke(Native Method)
                                                                 at java.lang.reflect.Method.invoke(Method.java:372)
                                                                 at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1009)
                                                                 at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:804)
02-16 09:58:13.241 21199-21199/wj.com.fuck E/AndroidRuntime: --->BluesException:Thread[Thread-26326,5,main]<---
                                                             java.lang.RuntimeException: new thread exception...
                                                                 at wj.com.fuck.MainActivity$5$1.run(MainActivity.java:76)


```


当卸载`Blues`后再在click中抛出异常，日志如下

```java

02-16 09:59:01.251 21199-21199/wj.com.fuck E/AndroidRuntime: FATAL EXCEPTION: main
                                                             Process: wj.com.fuck, PID: 21199
                                                             java.lang.RuntimeException: click exception...
                                                                 at wj.com.fuck.MainActivity$3.onClick(MainActivity.java:53)
                                                                 at android.view.View.performClick(View.java:4909)
                                                                 at android.view.View$PerformClick.run(View.java:20390)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at android.app.ActivityThread.main(ActivityThread.java:5826)
                                                                 at java.lang.reflect.Method.invoke(Native Method)
                                                                 at java.lang.reflect.Method.invoke(Method.java:372)
                                                                 at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1009)
                                                                 at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:804)


```             
          
 可以看到 ` at com.cody.blues.Blues$1.run(Blues.java:47)` 没有拦截，并且APP crash了。



### 注意
 
* 当主线程或子线程抛出异常时都会调用exceptionHandler.handlerException(Thread thread, Throwable throwable)
     
* exceptionHandler.handlerException可能运行在非UI线程中。
    
* handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
    
* 若设置了Thread.setDefaultUncaughtExceptionHandler则可能无法捕获子线程异常。


* 最佳拍档`android.arch.lifecycle.LiveData`+`Blues`。当使用LiveData.postValue时，Observer会在一个单独的消息中执行，这时
若Observer中发生了异常，就可以被cockroach捕获到，不会有其他影响。

虽然可以捕获到所有异常，但可能会导致一些莫名其妙的问题，比如view初始化时发生了异常，异常后面的代码得不到执行，虽然不
会导致app crash但view内部已经出现了问题，运行时就会出现很奇葩的现象。再比如activity声明周期方法中抛出了异常，则生
命周期就会不完整，从而导致各种奇葩的现象。


Blues采用android标准API编写，无依赖，足够轻量，轻量到只有不到100行代码，一般不会存在兼容性问题，也不存在性能上的问题，可以兼容所有android版本。

 
### 原理分析  

[原理分析](https://github.com/codyer/Blues/blob/master/%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90.md)


