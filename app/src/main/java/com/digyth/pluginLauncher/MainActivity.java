package com.digyth.pluginLauncher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dalvik.system.PathClassLoader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

public class MainActivity extends AppCompatActivity {
    private InterfaceLib interfaceLib=new InterfaceLib(this);
    private ClassLoader classLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new File(getExternalCacheDir().getAbsolutePath()+"/plugin.apk").delete();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123&&resultCode==RESULT_OK&&data.hasExtra("ret")){
            Toast.makeText(this,data.getStringExtra("ret"),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader==null?super.getClassLoader():classLoader;
    }

    public String releasePlugin(){
        try {
            String releasePath= getExternalCacheDir().getAbsolutePath()+"/plugin.apk";
            if(!new File(releasePath).exists()) {
                InputStream in = getResources().openRawResource(R.raw.plugin);
                FileOutputStream out = new FileOutputStream(releasePath);
                int count = 0;
                byte[] buffer = new byte[1024];
                while ((count = in.read(buffer)) > -1) {
                    out.write(buffer, 0, count);
                }
                out.close();
                in.close();
            }
            return releasePath;
        }catch (Exception err){
            err.printStackTrace();
            return null;
        }
    }

    public void start(View v){
        try {
            interfaceLib.loadExtActivity(releasePlugin(), "ProxyActivity",new Intent().putExtra("plugin_path",releasePlugin()));
        }catch (Exception err){
            err.printStackTrace();
        }
    }

    public void startForResult(View v){
        interfaceLib.loadExtActivityForResult( releasePlugin(),"ProxyActivity",123,new Intent().putExtra("text","这是接收到参数的插件Activity").putExtra("plugin_path",releasePlugin()));
    }
}
