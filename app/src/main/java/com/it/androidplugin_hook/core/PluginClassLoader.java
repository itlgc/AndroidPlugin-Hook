package com.it.androidplugin_hook.core;

import dalvik.system.DexClassLoader;

/**
 * Created by lgc on 2020-02-14.
 *
 * @description 专门用于加载插件中class 的ClassLoader
 */
public class PluginClassLoader extends DexClassLoader {

    public PluginClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath,
                             ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
