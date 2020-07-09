package com.it.plugin_package;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * 目标Activity
 */
public class PluginHookActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_hook);

        //如果是占位式  使用this 会报错
//        Toast.makeText(this,"我是插件", Toast.LENGTH_SHORT).show();


        //hook式 不会报错  因为插件和宿主dexElements 相互融合了 this可以使用到宿主中的了
        Toast.makeText(getApplicationContext(),"我是插件", Toast.LENGTH_SHORT).show();//这行代码 在andriod9.0机型上toast 出现异常

        Log.d("TAG: PluginHookActivity","恭喜你，成功启动我，我是插件包中的Activity" );

    }
}
