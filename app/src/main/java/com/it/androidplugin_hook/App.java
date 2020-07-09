package com.it.androidplugin_hook;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import com.it.androidplugin_hook.core.AMSCheckEngine;
import com.it.androidplugin_hook.core.ActivityThreadRestore;
import com.it.androidplugin_hook.core.DexElementFuse;
import com.it.androidplugin_hook.core.LoadedApkEngine;
import com.it.androidplugin_hook.utils.Utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by lgc on 2020-07-08.
 *
 * @Author lgc
 * @Description
 */
public class App extends Application {


    public static final String TAG = App.class.getSimpleName();


    public static DexElementFuse getDexElementFuse() {
        return dexElementFuse;
    }

    private static DexElementFuse dexElementFuse;
    @Override
    public void onCreate() {
        super.onCreate();

//        File file = new File(Environment.getExternalStorageDirectory() + "/app1.trace");
//        String filePath = file.getAbsolutePath();
//        Log.d("TAG:" + TAG, "filePath:" + filePath);
//        Debug.startMethodTracing(filePath);

        Log.d("TAG:" + TAG, "检测到手机Android版本：" + Build.VERSION.SDK_INT);
        // todo ========跳转未在AndroidManifest中配置的Activity=========
        // TODO: 第一个Hook
        try {
            AMSCheckEngine.mHookAMS(this);
        } catch (ClassNotFoundException e) {
            Log.d("TAG:" + TAG, "hookAmsAction 失败 e：" + e.toString());
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        // TODO: 第二个Hook
        try {
            ActivityThreadRestore.mActivityThreadmHAction(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG:" + TAG, "hookLaunchAction 失败 e：" + e.toString());
        }


        // todo ========宿主APP 启动插件中的Activity===========  需要前两个Hook方法的支持
        //为了方便测试  拷贝插件apk操作    isUpdate: 当插件有更新时设为true可以覆盖
        //实际开发中是通过网络去下载插件包，在APP内部存储中生成存放插件包的文件夹路径，会有专门的插件包管理类去管理插件包，控制数量，占用大小，按需求定时清理等操作
        String pluginHookApkPath = Utils.copyAssetsAndWrite(this,
                "pluginHookDir", "plugin_package.apk", true);


        // todo 方式一 融合的方式
        dexElementFuse = new DexElementFuse();
        try {
            dexElementFuse.mainPluginFuse(this, pluginHookApkPath);
        } catch (Exception e) {
            Log.d("TAG:" + TAG, "融合的方式 失败 e：" + e.toString());
            e.printStackTrace();
        }


        // todo 方式二 LoadedApk方式 替换 融合的方式
        try {
//            LoadedApkEngine.cusLoadedApkAction(this,pluginHookApkPath);
        } catch (Exception e) {
            Log.d("TAG:" + TAG, "LoadedApk方式 失败 e：" + e.toString());
            e.printStackTrace();
        }


//        Debug.stopMethodTracing();


    }


    /** 为什么要重写资源管理器？
     * 为了提供资源的加载工作,为了给插件使用的（提供给插件包中调用，不然无法正常显示插件包中的布局等资源）
     *
     * 为什么要在application中写？ 虽然每个Activity中都有资源管理器，但是在每个Activity中重写太繁琐了，
     * 只需要在application中重写了就行，这里的getResources也是所有Activity共用的
     * @return
     */
    @Override
    public Resources getResources() {
        if (dexElementFuse != null) {
            return dexElementFuse.getResources() == null ? super.getResources() : dexElementFuse.getResources();
        }
        return super.getResources();
    }

    //这里不重写也没关系，和插件包统一，如果插件包也没重写的话，就不写了
    @Override
    public AssetManager getAssets() {
        if (dexElementFuse != null) {
            return dexElementFuse.getAssets() == null ? super.getAssets() : dexElementFuse.getAssets();
        }
        return super.getAssets();

    }
}
