
### Hook方式实现插件化

#### 什么是Hook

1、替换 （把系统的 替换成 动态代理）

2、动态代理 （添加自己的业务逻辑）

#### ![img](/Users/lgc/Documents/ASProjects/GithubShareOpen/AndroidPluginHook/img/1.png)

#### 实现Activity不在AndroidManifest中注册也能启动 

1、在执行startActivity前Hook AMS将TragetActivity替换成有效的ProxyActivity,绕过AMS检查

2、ASM检查后，要把ProxyActivity换成TragetActivity






### 宿主APP 启动插件中的Activity

#### 方式一：APP与插件融为一体

![img](/Users/lgc/Documents/ASProjects/GithubShareOpen/AndroidPluginHook/img/2.png)

**实现步骤：**

第一步:找到宿主dexElements得到此对象 PathClassLoader代表是宿主

第二步:找到插件dexElements 得到此对象，DexClassLoader--代表插件

第三步:创建出新的newDexElements []. 类型必须是Element,必须是数组对象

第四步:宿主dexElements +  插件dexElements  →  融合新的newDexElements for遍历

第五步:把新的newDexElements.设置到宿主中去。

以上操作，就可以去加载插件里面的class



**缺点：**

不稳定，兼容性存在问题，同时当插件越来越多之后，内存中新融合的 DexElements 就会越来越大。





#### 方式二：LoadedApk方式

可以弥补方式一的缺点

思路：

PathClassLoader加载宿主的class；

自定义ClassLoader加载插件的class。

![img](/Users/lgc/Documents/ASProjects/GithubShareOpen/AndroidPluginHook/img/3.png)