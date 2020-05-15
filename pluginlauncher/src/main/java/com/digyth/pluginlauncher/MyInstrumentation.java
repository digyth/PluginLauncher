package com.digyth.pluginlauncher;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

class MyInstrumentation extends Instrumentation {
    private Instrumentation mBase;

    public MyInstrumentation(Instrumentation mBase) {
        this.mBase = mBase;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Intent dest = intent.getParcelableExtra("destActivity");
        if (dest != null) {
            return mBase.newActivity(cl, dest.getComponent().getClassName(), dest);
        }
        return mBase.newActivity(cl, className, intent);
    }
}
