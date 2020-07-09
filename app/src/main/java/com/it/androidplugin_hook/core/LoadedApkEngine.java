package com.it.androidplugin_hook.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by lgc on 2020-03-07.
 */
public class LoadedApkEngine {
    /**
     * 自己创建一个 LoadedApk 添加到mPackages ,  利用这个LoadedApk.mClassLoader 专门用来加载插件中的class
     * @param pluginHookApkPath
     */
    public static void cusLoadedApkAction(Context context,String pluginHookApkPath) throws Exception{
        //@1 对象获取
        Class<?> mActivityThreadClass = Class.forName("android.app.ActivityThread");
        Object mActivityThread =
                mActivityThreadClass.getMethod("currentActivityThread").invoke(null);

        //final ArrayMap<String, WeakReference<LoadedApk>> mPackages
        //拿到mPackages对象  需要对象@1
        Field mPackagesField = mActivityThreadClass.getDeclaredField("mPackages");
        mPackagesField.setAccessible(true);
        Object mPackagesObj = mPackagesField.get(mActivityThread);
        Map mPackages = (Map) mPackagesObj;


        /**
         * 自定义LoadedApk  模拟系统创建LoadedApk
         */
        //@ApplicationInfo 对象获取
        ApplicationInfo applicationInfo = getApplicationInfoAction(pluginHookApkPath);

        //@CompatibilityInfo 对象获取
        Class<?> mCompatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
        Object mCompatibilityInfoObj = mCompatibilityInfoClass.getField(
                "DEFAULT_COMPATIBILITY_INFO").get(null);

        //public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai,CompatibilityInfo compatInfo)
        Method getPackageInfoNoCheckMethod = mActivityThreadClass.getMethod(
                "getPackageInfoNoCheck", ApplicationInfo.class,mCompatibilityInfoClass);
        //需要对象@ApplicationInfo 和 @ CompatibilityInfo
        Object mLoadedApk = getPackageInfoNoCheckMethod.invoke(mActivityThread, applicationInfo,
                mCompatibilityInfoObj);

        /**
         * 自定义 ClassLoader 插件的
         * private ClassLoader mClassLoader; 替换成自定义的 ClassLoader
         */

        File fileDir = context.getDir("pluginHookDir", Context.MODE_PRIVATE);
        ClassLoader cusClassLoader = new PluginClassLoader(pluginHookApkPath,
                fileDir.getAbsolutePath(),null, context.getClassLoader());

        Field mClassLoaderField = mLoadedApk.getClass().getDeclaredField("mClassLoader");
        mClassLoaderField.setAccessible(true);
        mClassLoaderField.set(mLoadedApk,cusClassLoader);

        WeakReference weakReference = new WeakReference(mLoadedApk);

        //最终目标  mPackages.put(插件包名, 插件的LoadedApk);
        mPackages.put(applicationInfo.packageName,weakReference);

        //后续 需要绕过PMS检查  代码在 hookLaunchAction() 方法中

    }


    /**
     * 如何得到 ApplicationInfo 对象
     * PackageParser类中
     * public static ApplicationInfo generateApplicationInfo(Package p, int flags,
     *             PackageUserState state, int userId) {
     * @throws Exception
     * @param pluginHookApkPath
     */
    private static ApplicationInfo getApplicationInfoAction(String pluginHookApkPath) throws Exception{

        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Object mPackageParser = packageParserClass.newInstance();

        //@Package Class
        Class<?> mPackageClass = Class.forName("android.content.pm.PackageParser$Package");
        //@PackageUserStated 对象
        Class<?> mPackageUserStateClass = Class.forName("android.content.pm.PackageUserState");

        //        File file = Utils.getPluginApkPath(this);
        File file = new File(pluginHookApkPath);
        // public Package parsePackage(File packageFile, int flags) 获取package信息
        Method parsePackageMethod = packageParserClass.getMethod("parsePackage", File.class,
                int.class);
        //Package 对象
        Object mPackage = parsePackageMethod.invoke(mPackageParser, file, PackageManager.GET_ACTIVITIES);

        Method generateApplicationInfoMethod = packageParserClass.getMethod(
                "generateApplicationInfo",mPackageClass,int.class,mPackageUserStateClass,int.class);

        //mApplicationInfo 本质就是插件的ApplicationInfo
        ApplicationInfo mApplicationInfo = (ApplicationInfo) generateApplicationInfoMethod.invoke(mPackage,mPackage, 0,
                mPackageUserStateClass.newInstance(), PackageManager.GET_ACTIVITIES);

        //其中以下两个变量初始化是未赋值的
        mApplicationInfo.sourceDir = pluginHookApkPath;
        mApplicationInfo.publicSourceDir = pluginHookApkPath;

        return  mApplicationInfo;

    }
}
