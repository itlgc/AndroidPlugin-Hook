package com.it.androidplugin_hook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.it.androidplugin_hook.utils.Utils;

/**
 * Hook式插件化设计
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转未在AndroidManifest中配置的Activity
                startActivity(new Intent(MainActivity.this,NoActivity.class));
            }
        });

        findViewById(R.id.btn_hook_start_plugin_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //宿主APP 启动插件中的Activity
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.it.plugin_package",
                        "com.it.plugin_package.PluginHookActivity"));
                startActivity(intent);


            }
        });
    }
}
