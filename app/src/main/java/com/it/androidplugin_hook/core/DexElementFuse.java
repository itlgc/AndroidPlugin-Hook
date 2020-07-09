package com.it.androidplugin_hook.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 宿主和插件 dexElements融合
 * Created by lgc on 2020-03-07.
 */
public class DexElementFuse {

    /**
     * 把插件的 dexElements 和宿主的 dexElements 融为一体
     */
    public void mainPluginFuse(Context context,String pluginHookApkPath) throws Exception {
        // todo 宿主的dexElements
        //1、找到宿主APP的 dexElements 得到此对象
        //@1  获取的ClassLoader 本质就是PathClassLoader    PathClassLoader extends BaseDexClassLoader
        PathClassLoader mPathClassLoader = (PathClassLoader) context.getClassLoader();

        Class<?> mBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        //private final DexPathList pathList;  来得到DexPathList对象
        Field pathListField = mBaseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object mDexPathListObj = pathListField.get(mPathClassLoader);//需要对象 @1

        //private Element[] dexElements;
        Field dexElementsField = mDexPathListObj.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        //本质就是Element[] dexElements
        Object mDexElements = dexElementsField.get(mDexPathListObj);


        //todo 插件的dexElements
        //2、找到插件包的 dexElements 得到此对象
        /**
         * 参数1：插件包apk 路径
         * 参数2：指定一个缓存路径 用于存放apk解析后的dex文件
         * 参数3：ndk相关
         * 参数4 类加载器
         */
        File fileDir = context.getDir("pluginHookDir", Context.MODE_PRIVATE);
        DexClassLoader dexClassLoader = new DexClassLoader(pluginHookApkPath,
                fileDir.getAbsolutePath(), null, context.getClassLoader());

        Class<?> mBaseDexClassLoaderClassPlugin = Class.forName("dalvik.system.BaseDexClassLoader");
        //private final DexPathList pathList;  来得到DexPathList对象
        Field pathListFieldPlugin = mBaseDexClassLoaderClassPlugin.getDeclaredField("pathList");
        pathListFieldPlugin.setAccessible(true);
        Object mDexPathListObjPlugin = pathListFieldPlugin.get(dexClassLoader);//需要对象 @1

        //private Element[] dexElements;
        Field dexElementsFieldPlugin = mDexPathListObjPlugin.getClass().getDeclaredField(
                "dexElements");
        dexElementsFieldPlugin.setAccessible(true);
        //本质就是Element[] dexElements
        Object mDexElementsPlugin = dexElementsFieldPlugin.get(mDexPathListObjPlugin);

        //todo 创建出新的 newDexElements
        //3、创建出新的 newDexElements 本质是一个数组
        int mainDexLength = Array.getLength(mDexElements);
        int pluginDexLength = Array.getLength(mDexElementsPlugin);
        int sumDexLength = mainDexLength + pluginDexLength;
        /**
         * 创建新的数组对象
         * 参数1 数组类型 eg. String[]  int[]
         * 参数2 数组长度
         */
        //本质就是Element[] dexElements
        Object mNewDexElements = Array.newInstance(mDexElements.getClass().getComponentType(), sumDexLength);


        //4、宿主dexElements + 插件dexElements = 融合到新的 newDexElements
        for (int i = 0; i < sumDexLength; i++) {
            if (i < mainDexLength) {
                //先融合宿主
                Array.set(mNewDexElements, i, Array.get(mDexElements, i));
            }else {
                //再融合插件
                Array.set(mNewDexElements, i, Array.get(mDexElementsPlugin, i - mainDexLength));
            }
        }

        //5、把新的 newDexElements 设置到宿主中
        dexElementsField.set(mDexPathListObj, mNewDexElements);


        //==============================
        //处理插件中的布局
        loadResource(context,pluginHookApkPath);

    }

    /**
     * 处理插件中的布局
     * @param pluginHookApkPath
     */
    private void loadResource(Context context, String pluginHookApkPath) throws Exception{
        //加载资源
        assetManager = AssetManager.class.newInstance();
        Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath",
                String.class);
        addAssetPathMethod.setAccessible(true);
        // public int addAssetPath(String path) 执行此方法将插件包路径传入进而加载里面的资源
        addAssetPathMethod.invoke(assetManager, pluginHookApkPath);

        //宿主APP的Resources信息
        Resources r = context.getResources();

      /*  //sdk<=24   以上的版本源码有变化
        //final StringBlock[] ensureStringBlocks()
        Method ensureStringBlocksMethod = assetManager.getClass().getDeclaredMethod("ensureStringBlocks");
        ensureStringBlocksMethod.setAccessible(true);
        //执行此方法来初始化资源中的 string.xml  color.xml anim.xml...
        ensureStringBlocksMethod.invoke(assetManager);*/


        //这里的Resources 是加载插件包中的资源的Resources ，参数2、3 是资源配置信息，这里使用宿主的
        resources = new Resources(assetManager, r.getDisplayMetrics(),
                r.getConfiguration());
    }

    private AssetManager assetManager;
    private Resources resources;

    //提供给插件包中调用
    public Resources getResources() {
        return resources;
    }

    public AssetManager getAssets() {
        return assetManager;
    }
}
