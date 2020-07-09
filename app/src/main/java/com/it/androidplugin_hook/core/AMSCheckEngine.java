package com.it.androidplugin_hook.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.it.androidplugin_hook.ProxyActivity;
import com.it.androidplugin_hook.utils.AndroidSdkVersion;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 绕过AMS检查引擎  专门处理绕过AMS检测，让NoActivity可以正常通过
 * （只是针对在HookApplication类下的学习内容的封装整理和版本兼容处理）
 * Created by lgc on 2020-03-07.
 */
public class AMSCheckEngine {
    public static final String TAG = AMSCheckEngine.class.getSimpleName();

    /**
     * TODO 此方法 适用于 21以下的版本 以及 21_22_23_24_25  26_27_28 等系统版本
     * @param mContext
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static void mHookAMS(final Context mContext) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        // 公共区域
        Object mIActivityManagerSingleton = null; // TODO 公共区域 适用于 21以下的版本 以及 21_22_23_24_25  26_27_28 等系统版本
        Object mIActivityManager = null; // TODO 公共区域 适用于 21以下的版本 以及 21_22_23_24_25  26_27_28 等系统版本

        if (AndroidSdkVersion.isAndroidOS_26_27_28()) {
            // 获取系统的 IActivityManager.aidl
            Class mActivityManagerClass = Class.forName("android.app.ActivityManager");
            mIActivityManager = mActivityManagerClass.getMethod("getService").invoke(null);


            // 获取IActivityManagerSingleton
            Field mIActivityManagerSingletonField = mActivityManagerClass.getDeclaredField("IActivityManagerSingleton");
            mIActivityManagerSingletonField.setAccessible(true);
            mIActivityManagerSingleton = mIActivityManagerSingletonField.get(null);

        } else if (AndroidSdkVersion.isAndroidOS_21_22_23_24_25()) {
            Class mActivityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = mActivityManagerClass.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            mIActivityManager = getDefaultMethod.invoke(null);

            //gDefault
            Field gDefaultField = mActivityManagerClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            mIActivityManagerSingleton = gDefaultField.get(null);
        }

        //获取动态代理
        Class mIActivityManagerClass = Class.forName("android.app.IActivityManager");
        final Object finalMIActivityManager = mIActivityManager;

        //Proxy.newProxyInstance(） 这里是整个流程中最耗时的操作 application启动中耗时占比达到90%
        Object mIActivityManagerProxy =  Proxy.newProxyInstance(mContext.getClassLoader(),
                new Class[]{mIActivityManagerClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("startActivity".equals(method.getName())) {
                            // 把NoActivity 换成 ProxyActivity
                            // TODO 把不能经过检测的NoActivity 替换 成能够经过检测的ProxyActivity
                            Intent proxyIntent = new Intent(mContext, ProxyActivity.class);

                            // 把目标的NoActivity 取出来 携带过去
                            Intent target = (Intent) args[2];
                            proxyIntent.putExtra(Parameter.TARGET_INTENT, target);
                            args[2] = proxyIntent;
                        }

                        return method.invoke(finalMIActivityManager, args);
                    }
                });

        if (mIActivityManagerSingleton == null || mIActivityManagerProxy == null) {
            throw new IllegalStateException("实在是没有检测到这种系统，需要对这种系统单独处理...");
        }

        Class mSingletonClass = Class.forName("android.util.Singleton");

        Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // 把系统里面的 IActivityManager 换成 我们自己写的动态代理
        mInstanceField.set(mIActivityManagerSingleton, mIActivityManagerProxy);
    }





    /**
     * 之前写在HookApplicantion中的 抽取出来了而已 （更优化后的见上面方法mHookAMS()）
     * 在系统执行AMS 之前 替换成可用的Activity（即在AndroidManifest中配置过的Activity）
     */
    private void hookAmsAction(final Context context) throws Exception {
        Log.d("TAG:" + TAG, "检测到手机Android版本：" + Build.VERSION.SDK_INT);

        //获取要监听的接口对象
        Class<?> mIActivityManagerClass = Class.forName("android.app.IActivityManager");

        //需要获取IActivityManager对象，才能让动态代理中的invoke正常执行下去
        final Object mIActivityManager;

        if (AndroidSdkVersion.isAndroidOS_26_27_28()) { //sdk>=8.0
            //通过 public static IActivityManager getService() 方法来获取
            Class<?> mActivityManagerClass = Class.forName("android.app.ActivityManager");
            Method getServiceMethod = mActivityManagerClass.getMethod("getService");
            mIActivityManager = getServiceMethod.invoke(null);
        } else {
            Class<?> mActivityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = mActivityManagerNativeClass.getMethod("getDefault");
            mIActivityManager = getDefaultMethod.invoke(null);
        }
        //动态代理
        Object mIActivityManagerProxy = Proxy.newProxyInstance(context.getClassLoader(),
                new Class[]{mIActivityManagerClass},
                new InvocationHandler() {
                    /**
                     * @param proxy
                     * @param method  IActivityManager接口中的所有方法
                     * @param args    IActivityManager接口中各方法中对应的参数
                     * @return
                     * @throws Throwable
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("startActivity".equals(method.getName())) {
                            Log.d("TAG:" + TAG, "Hook 拦截到IActivityManager接口中方法：" + method.getName());
                            //添加自己的逻辑
                            //将NoActivity 更换成 ProxyActivity  用于绕过AMS检查
                            Intent intent = new Intent(context.getApplicationContext(),
                                    ProxyActivity.class);
                            //将之前的NoActivity保存携带过去
                            // intent.putExtra("actionIntent", ((Intent) args[2]));
                            // args[2] = intent;

                            //替换前面两行代码
                            for (int i = 0; i < args.length; i++) {
                                if (args[i] instanceof Intent) {
                                    intent.putExtra("actionIntent", ((Intent) args[i]));
                                    args[i] = intent;
                                    break;
                                }
                            }
                        }

                        //让系统正常的往下执行
                        return method.invoke(mIActivityManager, args);
                        //                return null; //会报错
                    }
                });


        //获取IActivityManagerSingleton变量 （对应Singleton对象）
        Object mSingletonObj;
        if (AndroidSdkVersion.isAndroidOS_26_27_28()) { //sdk>=8.0
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            Field field = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
            //拿到 IActivityManagerSingleton 属性
            field.setAccessible(true);
            mSingletonObj = field.get(null);
        } else {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManagerNative");
            Field field = activityManagerClass.getDeclaredField("gDefault");
            field.setAccessible(true);
            //获取到是 Singleton 对象，也就是 field 对应的类型
            mSingletonObj = field.get(null);
        }


        //替换点   Singleton类中字段 mInstance
        Class<?> mSingletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        //替换需要获取对象Singleton IActivityManagerSingleton
        mInstanceField.set(mSingletonObj, mIActivityManagerProxy);

    }
}
