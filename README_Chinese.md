# 插件启动器
一个启动外部Activity和资源的简单框架

您可以使用此框架动态加载外部安装包内的Activity，可插拔的结构使得项目开发更加便捷，并且能减小主体安装包体积，快速更新各种模块及修复Bug，给用户更多的选择和更好的体验

## 使用说明
### 第一步
添加依赖
```gradle
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
    	implementation 'com.github.digyth:PluginLauncher:1.1.0'
    }
```
### 第二步
实例化其中的`PluginLib`类，之后在代理Activity中重写`getAssets`,`getResources`,`getClassLoader`三个方法，并且setTheme插件内的主题

```java
    private PluginLib pluginLib=new PluginLib(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);//一定要重新setTheme，不然会报未设置主题异常
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public Resources getResources() {
        try {
            return pluginLib.getResources();
        }catch (Exception err){
            err.printStackTrace();
            return super.getResources();
        }
    }

    @Override
    public AssetManager getAssets() {
        try{
            return pluginLib.getAssets();
        }catch (Exception err){
            err.printStackTrace();
            return super.getAssets();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return pluginLib.getClassLoader();
    }
```

### 第三步
在宿主APP中引入`InterfaceLib.jar`，即可调用其的`loadExtActivity`或`loadExtActivityForResult`方法启动插件中的Activity
```java
        private InterfaceLib pluginLib=new InterfaceLib(this);
        
        lib.loadExtActivity(apkPath, "ProxyActivity",new Intent().putExtra("text","这是传递的参数"));
        
        lib.loadExtActivityForResult(apkPath, "ProxyActivity", new Intent().putExtra("text","这是传递的参数"), 123);
```
### 第四步
在插件Activity中通过`PluginLib`类，调用与上一步相同的方法即可实现Activity的跳转，通过对自身Class的动态替换与启动，模拟不同Activity之间的跳转回调
```java
    setResult(RESULT_OK,getIntent().putExtra("ret","这是返回的字符串"));
    finish();
```
并可通过onActivityResult获取返回的参数
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123&&resultCode==RESULT_OK&&data.hasExtra("ret")){
            Toast.makeText(this,data.getStringExtra("ret"),Toast.LENGTH_LONG).show();
        }
    }
```
### 第五步
通过`PluginLib`类，调用loadHostActivity方法，可以反向启动宿主的Activity
```java
    private InterfaceLib pluginLib=new InterfaceLib(this);

    lib.loadExtActivity(apkPath, "ProxyActivity",new Intent().putExtra("text","这是传递的参数"));

    lib.loadExtActivityForResult(apkPath, "ProxyActivity", new Intent().putExtra("text","这是传递的参数"), 123);
```
## 优点
* 无需声明Activity即可启动
* 无包名要求，可启动不同包名apk
* 无转发生命周期，插件Activity具有上下文
* 缓存组件资源，避免加载组件过多内存泄漏

## 缺点
* 非debug模式会导致找不到资源id，暂无法解决，请使用debug版本
* `getPackageName()`与`getLocalClassName()`获取到的是宿主Activity的属性，如要获取插件Activity的包名与类名，请使用`getClass().getName()`

## 作者的话
* 对Java和Android的制作并不是非常深入了解，均为网络上总结出的干货，巨佬们不喜勿喷QAQ
* 还有疑问的话可以查看Demo源码
