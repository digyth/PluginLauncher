package com.digyth.pluginLauncher;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @author digyth
 */

public class InterfaceLib {
    private Context ctx;
    private Date lastLoad;
    private static Class activityThread;

    static{
        try {
            activityThread = Class.forName("android.app.ActivityThread");
            setField(activityThread, getCurrentActivityThread(), "mInstrumentation", new MyInstrumentation((Instrumentation) getField(activityThread, getCurrentActivityThread(), "mInstrumentation")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InterfaceLib(Context context) {
        ctx = context;
    }

    public void loadExtActivity(String path, String proxyName) {
        loadExtActivity(path, proxyName, new Intent());
    }

    /**
     * It is similar with {@code Context.startActivity}
     *
     * @param path      the path of external APK
     * @param proxyName the name of proxy Activity
     */
    public void loadExtActivity(String path, String proxyName, Intent intent) {
        try {
            if (lastLoad != null && new Date().getTime() - lastLoad.getTime() < 1000) return;
            lastLoad = new Date();
            inject(path);
            intent.setClass(ctx,getClassLoader(getPackage()).loadClass(getPackageName(path)+"."+proxyName));
            startWithoutRegisterActivity(intent);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void loadExtActivityForResult(String path, String proxyName, int requestCode) {
        loadExtActivityForResult(path, proxyName, requestCode, new Intent());
    }

    /**
     * It is similar with {@code Activity.startActivityForResult}
     *
     * @param path        The path of external APK
     * @param proxyName   The name of proxy {@code Activity}
     * @param intent      A empty {@code Intent} or A {@code Intent} including Extra data
     * @param requestCode requestCode
     */
    public void loadExtActivityForResult(String path, String proxyName, int requestCode, Intent intent) {
        try {
            if (lastLoad != null && new Date().getTime() - lastLoad.getTime() < 1000) return;
            lastLoad = new Date();
            inject(path);
            intent.setClass(ctx,getClassLoader(getPackage()).loadClass(getPackageName(path)+"."+proxyName));
            startWithoutRegisterActivityForResult(intent,requestCode);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    protected void inject(String path) throws Exception {
        String cacheDir = ctx.getCacheDir().getAbsolutePath();
        WeakReference Package = getPackage();
        PathClassLoader newClassLoader = new PathClassLoader(ctx.getPackageResourcePath(), ctx.getClassLoader().getParent());
        Object sysDexElements = getDexElements(getPathList(newClassLoader));
        ClassLoader extClassLoader = new DexClassLoader(path, cacheDir, cacheDir, ctx.getClassLoader());
        Object extDexElements = getDexElements(getPathList(extClassLoader));
        Object newDexElements = combineDexElements(extDexElements, sysDexElements);
        setDexElements(getPathList(newClassLoader), newDexElements);
        setClassLoader(Package, newClassLoader);
        AssetManager manager = AssetManager.class.newInstance();
        addAssetPath(manager, path);
        Resources resources = ctx.getResources();
        setResources(Package, new Resources(manager, resources.getDisplayMetrics(), resources.getConfiguration()));
    }

    public void startWithoutRegisterActivity(Intent intent){
        ctx.startActivity(getWithoutRegisterIntent(intent));
    }

    public void startWithoutRegisterActivityForResult(Intent intent,int requestCode){
        ((Activity) ctx).startActivityForResult(getWithoutRegisterIntent(intent), requestCode);
    }

    protected Intent getWithoutRegisterIntent(Intent intent){
        try {
            return new Intent(intent).setClass(ctx,Class.forName(ctx.getPackageName()+"."+((Activity)ctx).getLocalClassName())).putExtra("destActivity", intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return intent;
        }
    }

    static void setField(Object object, String name, Object value) throws Exception {
        setField(object.getClass(), object, name, value);
    }

    static void setField(Class type, Object object, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    static Object getField(Object object, String name) throws Exception {
        return getField(object.getClass(), object, name);
    }

    static Object getField(Class type, Object object, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    protected WeakReference getPackage() throws Exception {
        Object currentActivityThread = getCurrentActivityThread();
        Field mPackages = activityThread.getDeclaredField("mPackages");
        mPackages.setAccessible(true);
        Map mPackagesObject = (Map) mPackages.get(currentActivityThread);
        WeakReference ref = (WeakReference) mPackagesObject.get(ctx.getPackageName());
        return ref;
    }

    protected static Object getCurrentActivityThread() throws Exception {
        return activityThread.getMethod("currentActivityThread").invoke(null);
    }

    protected ClassLoader getClassLoader(WeakReference ref) throws Exception {
        Class LoadedApk = Class.forName("android.app.LoadedApk");
        Field mClassLoader = LoadedApk.getDeclaredField("mClassLoader");
        mClassLoader.setAccessible(true);
        return (ClassLoader) mClassLoader.get(ref.get());
    }

    protected void setClassLoader(WeakReference ref, ClassLoader loader) throws Exception {
        Class LoadedApk = Class.forName("android.app.LoadedApk");
        Field mClassLoader = LoadedApk.getDeclaredField("mClassLoader");
        mClassLoader.setAccessible(true);
        mClassLoader.set(ref.get(), loader);
    }

    protected void setResources(WeakReference ref, Resources resources) throws Exception {
        Class LoadedApk = Class.forName("android.app.LoadedApk");
        Field mClassLoader = LoadedApk.getDeclaredField("mResources");
        mClassLoader.setAccessible(true);
        mClassLoader.set(ref.get(), resources);
    }

    protected Object getPathList(ClassLoader loader) throws Exception {
        Field field = BaseDexClassLoader.class.getDeclaredField("pathList");
        field.setAccessible(true);
        return field.get(loader);
    }

    protected Object getDexElements(Object pathList) throws Exception {
        Field field = pathList.getClass().getDeclaredField("dexElements");
        field.setAccessible(true);
        return field.get(pathList);
    }

    protected void setDexElements(Object pathList, Object dexElements) throws Exception {
        Field field = pathList.getClass().getDeclaredField("dexElements");
        field.setAccessible(true);
        field.set(pathList, dexElements);
    }

    protected Object combineDexElements(Object front, Object back) throws Exception {
        Object newArr = Array.newInstance(front.getClass().getComponentType(), Array.getLength(front) + Array.getLength(back));
        for (int i = 0; i < Array.getLength(front); i++) Array.set(newArr, i, Array.get(front, i));
        for (int i = 0; i < Array.getLength(back); i++)
            Array.set(newArr, Array.getLength(front) + i, Array.get(back, i));
        return newArr;
    }

    protected void addAssetPath(AssetManager manager, String path) throws Exception {
        Method method = manager.getClass().getDeclaredMethod("addAssetPath", String.class);
        method.setAccessible(true);
        method.invoke(manager, path);
    }

    protected Resources getResources(WeakReference ref) throws Exception {
        Class LoadedApk = Class.forName("android.app.LoadedApk");
        Field mClassLoader = LoadedApk.getDeclaredField("mResources");
        mClassLoader.setAccessible(true);
        return (Resources) mClassLoader.get(ref.get());
    }

    protected String getPackageName(String path) {
        return ctx.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES).packageName;
    }
}
