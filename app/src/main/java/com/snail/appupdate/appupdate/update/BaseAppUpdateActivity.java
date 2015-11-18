package com.snail.appupdate.appupdate.update;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.snail.appupdate.MyApplication;
import com.snail.appupdate.VersionManager;
import com.snail.appupdate.appupdate.download.DownloadCallback;
import com.snail.appupdate.appupdate.download.DownloadMess;
import com.snail.appupdate.appupdate.utils.AppNetworkUtils;

/**
 * Created by shenjie3 on 15-11-13.
 */
public abstract class BaseAppUpdateActivity extends Activity implements DownloadCallback {
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showUpdateDialog(){
        IAppUpdate updateBean  = VersionManager.getInstance().getUpdateBean();
        if(updateBean.isForceUpdate()){
            showForceUpdateDialog();
        }else{
            showCancelableUpdateDialog();
        }
    }

    protected abstract void showForceUpdateDialog();
    protected abstract void showCancelableUpdateDialog();
    protected abstract void showNoNetworkDialog();
    protected abstract void showNetworkTypeDialog();
    protected abstract void showDownloadProgressDialog();
    protected abstract boolean isIgnoreVersion();

    protected void onIgnore(){
        VersionManager.getInstance().setIgnoreVersion();
        VersionManager.getInstance().clearDownloadedVersion();
    }

    protected void checkAndDownload(){
        //先查看新版本是否已经下载过了，下载过了，就直接安装。还没有下载过，就下载
        String path = VersionManager.getInstance().getDownloadedVersionPath();
        if(!TextUtils.isEmpty(path)){
            String versionName = VersionManager.getInstance().getDownloadedVersion();
            String netVersionName = VersionManager.getInstance().getUpdateBean().getUpdateVersion();
            if(netVersionName != null && versionName != null
                    && netVersionName.equals(versionName)){
                VersionManager.getInstance().installApk(path);
                finish();
                return;
            }
        }

        //m没有网络，就提示网络链接异常，有网络就判断3G还是wifi
        int type = AppNetworkUtils.getActiveNetworkType(MyApplication.getInstance());
        if (type == -1) {
            showNoNetworkDialog();
        } else if(VersionManager.getInstance().isCheckWifiOr3G()){
            if(type == ConnectivityManager.TYPE_WIFI){
                download();
            }else if(type == ConnectivityManager.TYPE_MOBILE){
                showNetworkTypeDialog();
            }
        }else{
            download();
        }
    }

    protected void download(){
        final IAppUpdate updateBean = VersionManager.getInstance().getUpdateBean();
        if(updateBean.isForceUpdate()){
            showDownloadProgressDialog();
            VersionManager.getInstance().downloadFile(updateBean.getUpdateUrl(), this,true);
        }else{
            VersionManager.getInstance().downloadFile(updateBean.getUpdateUrl());
            finish();
        }
    }

    @Override
    public abstract void onProgress(int progress, DownloadMess downloadMess);

    @Override
    public void onPause(DownloadMess downloadMess) {

    }

    @Override
    public void onSuccessfull(DownloadMess downloadMess) {
        Toast.makeText(BaseAppUpdateActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
        VersionManager.getInstance().setDownloadedVersion(downloadMess.getLocal_uri());
        VersionManager.getInstance().completeDownload();
        VersionManager.getInstance().installApk(downloadMess.getLocal_uri());
        finish();
    }

    @Override
    public void onFail(int code, String error, DownloadMess downloadMess) {
        Toast.makeText(BaseAppUpdateActivity.this, "下载失败:" + error, Toast.LENGTH_SHORT).show();
        VersionManager.getInstance().completeDownload();
        finish();
    }

    protected void goWifiSetting(){
        if(android.os.Build.VERSION.SDK_INT > 10) {
// 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
            startActivity(new Intent( android.provider.Settings.ACTION_SETTINGS));
        } else {
            startActivity(new Intent( android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showUpdateDialog();
            }
        },2000);
    }

    @Override
    public void onBackPressed() {
        final IAppUpdate updateBean = VersionManager.getInstance().getUpdateBean();
        if(updateBean.isForceUpdate()){
            return;
        }
        super.onBackPressed();
    }
}
