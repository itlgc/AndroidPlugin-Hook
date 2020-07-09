package com.it.plugin_package;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * Created by lgc on 2020-02-13.
 *
 * @description
 */
public class BaseActivity extends Activity {

    //插件还是要求调用宿主的资源
    @Override
    public Resources getResources() {
        //当插件apk 融合到宿主APP中， 这里的getApplication 实际使用的是宿主APP当中的
        if (getApplication() != null && getApplication().getResources() != null) {
            return getApplication().getResources();
        }
        return super.getResources();
    }


    /**
     * 不重写此方法也可以
     * 融合方式的话，既然融为了一体，重写了方法getResources，得到了Resources 自然能得到AssetManager，AssetManager是单例的。
     */
    @Override
    public AssetManager getAssets() {
        if (getApplication() != null && getApplication().getAssets() != null) {
            return getApplication().getAssets();
        }
        return super.getAssets();
    }
}
