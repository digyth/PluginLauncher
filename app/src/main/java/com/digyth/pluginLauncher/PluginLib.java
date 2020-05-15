package com.digyth.pluginLauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Date;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @author digyth
 */

public class PluginLib extends InterfaceLib {
    private Context ctx;
    private AssetManager mAsset;
    private Resources mResources;
    private ClassLoader mClassLoader;
    private Date lastLoad;
    
    public PluginLib(Context context){
        super(context);
        this.ctx=context;
    }

    public Resources getResources(){
        try {
            return getExtResources();
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

    public AssetManager getAssets(){
        try {
            return getExtAssets();
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

    public ClassLoader getClassLoader(){
        try {
            return getExtClassLoader();
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

    private AssetManager getExtAssets()throws Exception {
        return mAsset==null?mAsset=getExtResources().getAssets():mAsset;
    }

    private Resources getExtResources()throws Exception{
        return mResources==null?mResources=getResources(getPackage()):mResources;
    }

    private ClassLoader getExtClassLoader()throws Exception{
        return mClassLoader==null?mClassLoader=getClassLoader(getPackage()):mClassLoader;
    }

    public void loadHostActivity(String proxyName) {
        loadHostActivity(proxyName, new Intent());
    }

    /**
     * It is similar with {@code Context.startActivity}
     *
     * @param proxyName the name of proxy Activity
     */
    public void loadHostActivity(String proxyName, Intent intent) {
        try {
            if (lastLoad != null && new Date().getTime() - lastLoad.getTime() < 1000) return;
            lastLoad = new Date();
            inject();
            intent.setClass(ctx,getClassLoader(getPackage()).loadClass(getPackageName(ctx.getPackageResourcePath())+"."+proxyName));
            ctx.startActivity(intent);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void loadHostActivityForResult(String proxyName, int requestCode) {
        loadHostActivityForResult(proxyName, requestCode, new Intent());
    }

    /**
     * It is similar with {@code Activity.startActivityForResult}
     *
     * @param proxyName   The name of proxy {@code Activity}
     * @param intent      A empty {@code Intent} or A {@code Intent} including Extra data
     * @param requestCode requestCode
     */
    public void loadHostActivityForResult(String proxyName, int requestCode, Intent intent) {
        try {
            if (lastLoad != null && new Date().getTime() - lastLoad.getTime() < 1000) return;
            lastLoad = new Date();
            inject();
            intent.setClass(ctx,getClassLoader(getPackage()).loadClass(getPackageName(ctx.getPackageResourcePath())+"."+proxyName));
            ((Activity)ctx).startActivityForResult(intent,requestCode);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    protected void inject() throws Exception {
        WeakReference Package = getPackage();
        PathClassLoader newClassLoader = new PathClassLoader(ctx.getPackageResourcePath(), ctx.getClassLoader().getParent());
        setClassLoader(Package, newClassLoader);
        AssetManager manager = AssetManager.class.newInstance();
        addAssetPath(manager,ctx.getPackageResourcePath());
        Resources resources = ctx.getResources();
        setResources(Package, new Resources(manager, resources.getDisplayMetrics(), resources.getConfiguration()));
    }

}
