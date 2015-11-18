package com.snail.appupdate;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.snail.appupdate.appupdate.update.IUpdateApplication;
import com.snail.appupdate.appupdate.version.VersionCallback;
import com.snail.appupdate.appupdate.update.IAppUpdate;

/**
 * Created by shenjie3 on 15-11-13.
 */
public class MyApplication extends Application implements IUpdateApplication{
    private final String TAG = MyApplication.class.getSimpleName();
    private final String APP_PROCESS_NAME = "com.snail.appupdate";
    private static MyApplication application;
    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
//        if(isAppProcess()){
//            checkAppVersion();
//        }
    }

    public static MyApplication getInstance() {
        return application;
    }

    /**
     * @Description: Whether current process is the app process
     * @author zhujun
     */
    private boolean isAppProcess() {
        boolean result = false;
        int curPid = android.os.Process.myPid();
        Log.i(TAG, "Current process id: " + curPid);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if(processInfo.pid == curPid && processInfo.processName.equalsIgnoreCase(APP_PROCESS_NAME)) {
                Log.i(TAG, "Current process name: " + processInfo.processName);
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void checkAppVersion(){
        try{
            VersionManager.getInstance().setContext(this);
            VersionManager.getInstance().checkUpdate(new VersionCallback() {
                @Override
                public void success(IAppUpdate updateBean) {
                    Log.i(TAG,"versionCallback.success");
                    VersionManager.getInstance().onUpdate();
                }

                @Override
                public void fail(String result) {
                    Log.i(TAG,"versionCallback.fail, result = " + result);
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void showUpdateDialog(){
        Intent intent = new Intent(MyApplication.getInstance(),AppUpdateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
