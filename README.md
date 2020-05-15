# PluginLauncher
A simple framework to load external Activity and Resources in Android. (It don't come true by forwarding lifecycle)<br/>
[中文文档](README_Chinese.md)

You can load `Activity` inside external APK via this framework. The plug-in construction make it convenient to develop project, and it can reduce the size of main APK. Updating module and fixing bugs rapidly. Giving better feeling to user!

## Usage
### Step 1
Import `PluginLib.jar` in Plugin-Application,Create a new instance of the `PluginLib` Class. Override `getAssets`,`getResources`,`getClassLoader`,`onPause`,`onResume`,`onCreate` 6 methods after,and set a theme.

    private PluginLib pluginLib=new PluginLib(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);//set a theme necessarily, or throw Exception that not set theme 
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

    @Override
    protected void onPause() {
        pluginLib.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        pluginLib.onResume();
        super.onResume();
    }

### Step 2
Import `InterfaceLib.jar` in Host-Application, and call its `loadExtActivity` or `loadExtActivityForResult` method to load external Activity. They are similar with `startActivity` or `startActivityForResult`.

        private InterfaceLib pluginLib=new InterfaceLib(this);
        
        lib.loadExtActivity(apkPath, "ProxyActivity",new Intent().putExtra("text","this is the parameter passed"));
        
        lib.loadExtActivityForResult(apkPath, "ProxyActivity", new Intent().putExtra("text","this is the parameter passed"), 123);

### Step 3
You can return a result in Plugin-Activity and receive it in Host-Activity as usual.

`Plugin-Activity`

    setResult(RESULT_OK,getIntent().putExtra("ret","this is the string returned"));
    finish();

`Host-Activity`

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123&&resultCode==RESULT_OK&&data.hasExtra("ret")){
            Toast.makeText(this,data.getStringExtra("ret"),Toast.LENGTH_LONG).show();
        }
    }
#Step 4
You can call the `loadExtActivity` or `loadExtActivityForResult` method of `PluginLib` class the same as `InterfaceLib` class if you want to load `Activity` inside external APK in Plugin Activity.

## Advantage
* You don't need to register Plugin-Activity in `Manifest.xml`
* You can load APK that package name different
* Not forwarding lifecycle, so Plugin-Activity has context.
* Singleton and Cache Mechanism make running more stable.

## Disadvantage
* Not suitable for below Android 6.0
* `getPackageName()`and`getLocalClassName()`only can get Host-Activity class info. You have to use `getClass().getName()` to get Plugin-Activty class info.
