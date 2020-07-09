package com.it.androidplugin_hook.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by lgc on 2020-02-10.
 *
 * @description
 */
public class Utils {

    /**
     * 将Assets目录下的fileName文件拷贝至App缓存目录
     *
     * @param context
     * @param fileName
     */
    public static String copyAssetsAndWrite(Context context, String fileName,boolean isUpdate) {
        String defDirName = "pluginDir";
        return copyAssetsAndWrite(context, defDirName, fileName,isUpdate);
    }


    public static String copyAssetsAndWrite(Context context, String dirName, String fileName,boolean isUpdate) {
        try {
            //            File cacheDir = context.getCacheDir();
            File cacheDir = context.getDir(dirName, Context.MODE_PRIVATE);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File outFile = new File(cacheDir, fileName);
            if (isUpdate||!outFile.exists()) {
                boolean res = outFile.createNewFile();
                if (isUpdate||res) {
                    InputStream is = context.getAssets().open(fileName);
                    FileOutputStream fos = new FileOutputStream(outFile);
                    byte[] buffer = new byte[is.available()];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                    Log.d("TAG:", "文件拷贝成功"+outFile.getAbsolutePath());
                    return outFile.getAbsolutePath();
                }
            } else {
                Log.d("TAG:", "文件已经存在"+outFile.getAbsolutePath());
                return outFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


//    public static String updatePluginApk(Context context, String dirName, String fileName) {
//        try {
//            //            File cacheDir = context.getCacheDir();
//            File cacheDir = context.getDir(dirName, Context.MODE_PRIVATE);
//            if (!cacheDir.exists()) {
//                cacheDir.mkdirs();
//            }
//
//            File outFile = new File(cacheDir, fileName);
//
//            InputStream is = context.getAssets().open(fileName);
//            FileOutputStream fos = new FileOutputStream(outFile);
//            byte[] buffer = new byte[is.available()];
//            int byteCount;
//            while ((byteCount = is.read(buffer)) != -1) {
//                fos.write(buffer, 0, byteCount);
//            }
//            fos.flush();
//            is.close();
//            fos.close();
//            Log.d("TAG:", "文件更新成功" + outFile.getAbsolutePath());
//            return outFile.getAbsolutePath();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//
//    }


//    public static String updatePluginApk(Context context, String fileName) {
//        return updatePluginApk(context, "pluginDir", fileName);
//    }

    public static File getPluginApkPath(Context context) {
        //获取插件包放置路径
        File file = new File(context.getDir("pluginHookDir",Context.MODE_PRIVATE),
                "plugin_package.apk");
        if (!file.exists()) {
            Log.d("TAG:" , "插件包不存在");
            return null;
        }
        //            String pluginPath = file.getAbsolutePath();
        return file;
    }

}
