package com.it.androidplugin_hook;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Created by lgc on 2020-02-13.
 *
 * @description
 * 必须要在AndroidManifest中配置
 */
public class ProxyActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(),"我是代理的Activity", Toast.LENGTH_SHORT).show();
    }
}
