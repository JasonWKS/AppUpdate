package com.snail.appupdate.appupdate.version;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.snail.appupdate.appupdate.download.DownLoadHelper;
import com.snail.appupdate.appupdate.download.DownloadCallback;
import com.snail.appupdate.appupdate.download.DownloadMess;
import com.snail.appupdate.appupdate.update.IAppUpdate;
import com.snail.appupdate.appupdate.update.IUpdateApplication;
import com.snail.appupdate.appupdate.utils.AppVersionUtils;
import com.snail.appupdate.appupdate.utils.AppNetworkUtils;

import java.io.File;

/**
 * Created by shenjie3 on 2015/11/18.
 *
 * 需要实现和扩展：
 * IAppUpdate 请求升级接口返回的结果类需要实现
 * IUpdateApplication  Application需要实现的接口
 * BaseAppUpdateActivity 更新结果显示界面，用于提示用户有更新的弹框等
 * BaseVersionManager 总入口
 * VersionCallback  请求升级接口后返回结果的回调接口
 */
public abstract class BaseVersionManager {
    public static final String SP_APP_VERSION = "app_version";
    public static final String KEY_IGNORE_VERSION_NAME = "ignore_version_name";
    public static final String KEY_DOWNLOAD_VERSION_NAME = "download_version_name";
    public static final String KEY_DOWNLOAD_VERSION_DESC = "download_version_desc";
    public static final String KEY_DOWNLOAD_VERSION = "download_version";
    public static final String KEY_DOWNLOAD_FILE_PATH = "download_file_path";

    public static final int MSG_CHECK_UPDATE_SUCCESS = 0;
    public static final int MSG_CHECK_UPDATE_FAIL = -1;

    private Context mContext;
    private IUpdateApplication mUpdateApplication;
    private VersionCallback mVersionCallback;
    private IAppUpdate mUpdateBean;
    private DownLoadHelper mDownLoadHelper;

    private boolean isAutoDownload = true;
    private boolean mCheckWifiOr3G = true;

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_CHECK_UPDATE_SUCCESS:
                    IAppUpdate update = (IAppUpdate) msg.obj;
                    onCheckUpdateSuccess(update);
                    break;
                case MSG_CHECK_UPDATE_FAIL:
                    String result = msg.obj.toString();
                    onCheckUpdateFail(result);
                    break;
            }
        }
    };

    public void setContext(Context context) throws Exception {
        mContext = context;
        if(context instanceof IUpdateApplication){
            mUpdateApplication = (IUpdateApplication) context;
        }else{
            throw new Exception("context is not IUpdateApplication");
        }
    }

    /**
     * 检查是否有新版本
     * @param callback
     * @throws Exception
     */
    public void checkUpdate(VersionCallback callback) throws Exception {
        if(mContext == null){
            throw new Exception("context is null");
        }

        beforeCheckUpdate(callback);
        onCheckUpdate();
    }

    private void beforeCheckUpdate(VersionCallback callback){
        setVersionCallback(callback);
    }

    private void onCheckUpdate(){
        IAppUpdate updateBean = null;
        if(AppNetworkUtils.isNetworkAvailable(mContext)){
            onNetCheckUpdate();
        }else{
            updateBean = getDownloadedVersionData();

            Message msg = mMainHandler.obtainMessage(MSG_CHECK_UPDATE_SUCCESS);
            msg.obj = updateBean;
            msg.sendToTarget();
        }
    }

    /**
     * 查询更新接口
     */
    protected abstract void onNetCheckUpdate();

    /**
     * 检查新版本接口返回成功
     * @param updateBean
     */
    public void postCheckUpdateSuccess(IAppUpdate updateBean){
        Message msg = mMainHandler.obtainMessage(MSG_CHECK_UPDATE_SUCCESS);
        msg.obj = updateBean;
        msg.sendToTarget();
    }

    /**
     * 检查新版本接口有异常，如访问超时等
     * @param result
     */
    public void postCheckUpdateFail(String result){
        Message msg = mMainHandler.obtainMessage(MSG_CHECK_UPDATE_FAIL);
        msg.obj = result;
        msg.sendToTarget();
    }

    private void afterCheckUpdate(){
        setVersionCallback(null);
    }

    private void onCheckUpdateSuccess(IAppUpdate iAppUpdate){
        setUpdateBean(iAppUpdate);
        setUpdateBean(iAppUpdate);
        checkIgnoreVersion();
        performCheckUpdateSuccess(iAppUpdate);
        afterCheckUpdate();
    }

    private void onCheckUpdateFail(String result){
        performCheckUpdateFail(result);
        afterCheckUpdate();
    }

    public void setVersionCallback(VersionCallback versionCallback) {
        mVersionCallback = versionCallback;
    }

    public void performCheckUpdateSuccess(IAppUpdate iAppUpdate){
        mVersionCallback.success(iAppUpdate);
    }

    public void performCheckUpdateFail(String result){
        mVersionCallback.fail(result);
    }

    public void setUpdateBean(IAppUpdate updateBean) {
        mUpdateBean = updateBean;
    }

    /**  app版本是否需要升级，用于是否需要显示升级的弹框
     * @return
     */
    public boolean isUpdateApp(){
        return mUpdateBean != null
                && mUpdateBean.isSuccess()
                && mUpdateBean.isUpdate()
                && !isVersionIgnore();
    }

    public void checkIgnoreVersion(){
        IAppUpdate updateBean = getUpdateBean();
        if(updateBean != null){
            if(updateBean.isForceUpdate()
                    || !isVersionIgnore()){
                clearIgnoreVersion();
            }
        }
    }

    public void onUpdate(){
        if(mUpdateBean == null){
            return;
        }
        if(isUpdateApp()){
            if(isAutoDownload()){
                String downloadedVersionName = getDownloadedVersion();
                String netVersionName = mUpdateBean.getUpdateVersion();
                if(downloadedVersionName != null
                        && netVersionName != null
                        && netVersionName.equals(downloadedVersionName)){
                    mUpdateApplication.showUpdateDialog();
                }else if(AppNetworkUtils.isWifiEnabled(mContext)){
                    autoDownloadFile(mUpdateBean.getUpdateUrl());
                }
            }else{
                mUpdateApplication.showUpdateDialog();
            }
        }else{
            clearDownloadedVersion();
        }
    }

    private void autoDownloadFile(String url){
        downloadFile(url, true);
    }

    /** 下载文件
     * @param url
     */
    public void downloadFile(String url){
        downloadFile(url,false);
    }

    private void downloadFile(String url,final boolean auto){
        downloadFile(url, new DownloadCallback() {
            @Override
            public void onProgress(int progress, DownloadMess downloadMess) {

            }

            @Override
            public void onPause(DownloadMess downloadMess) {

            }

            @Override
            public void onSuccessfull(DownloadMess downloadMess) {
                if (auto) {
                    mUpdateApplication.showUpdateDialog();
                } else {
                    Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
                    installApk(downloadMess.getLocal_uri());
                }
                setDownloadedVersion(downloadMess.getLocal_uri());
                completeDownload();
            }

            @Override
            public void onFail(int code, String error, DownloadMess downloadMess) {
                if(!auto){
                    Toast.makeText(mContext, "下载失败:" + error, Toast.LENGTH_SHORT).show();
                }
                completeDownload();
            }
        }, false);
    }

    /**
     * 下载文件
     * @param url 文件url地址
     * @param callback  下载回调
     * @param databaseObserver  是否需要监听数据库，
     *                          该如果需要显示进度，就必须为true，callback.onProgress 方法才会被调用
     */
    public void downloadFile(String url,
                             DownloadCallback callback,
                             boolean databaseObserver){
        if(mDownLoadHelper == null){
            mDownLoadHelper = new DownLoadHelper();
            mDownLoadHelper.setShowNotification(!isAutoDownload);
            mDownLoadHelper.registerListener(databaseObserver);
        }
        mDownLoadHelper.downloadFile(url, callback);
    }

    /**
     * 下载完成，将会移除监听
     */
    public void completeDownload(){
        if(mDownLoadHelper != null){
            mDownLoadHelper.removeListener();
        }
    }

    /**
     * 是否忽略线上的新版本
     * @return
     */
    public boolean isVersionIgnore(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE);
        String version = sharedPreferences.getString(KEY_IGNORE_VERSION_NAME,"");
        return version.equals(mUpdateBean.getUpdateVersion());
    }

    /**
     * 清空忽略线上的新版本版本号
     */
    public void clearIgnoreVersion(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE).edit();
        editor.remove(KEY_IGNORE_VERSION_NAME);
        editor.apply();
    }

    public void setIgnoreVersion(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_IGNORE_VERSION_NAME, mUpdateBean.getUpdateVersion());
        editor.apply();
    }

    public void setDownloadedVersion(String localPath){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_DOWNLOAD_VERSION_NAME, mUpdateBean.getUpdateVersionName());
        editor.putString(KEY_DOWNLOAD_FILE_PATH, localPath);
        editor.putString(KEY_DOWNLOAD_VERSION_DESC, mUpdateBean.getUpdateDesc());
        editor.putString(KEY_DOWNLOAD_VERSION, mUpdateBean.getUpdateVersion());
        editor.apply();
    }

    /**
     * 清空保存的下载版本的相关内容
     */
    public void clearDownloadedVersion(){
        SharedPreferences sp = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(KEY_DOWNLOAD_VERSION_NAME);
        editor.remove(KEY_DOWNLOAD_VERSION_DESC);
        editor.remove(KEY_DOWNLOAD_FILE_PATH);
        editor.remove(KEY_DOWNLOAD_VERSION);
        editor.apply();
    }

    public IAppUpdate getDownloadedVersionData(){
        final String downloadVer = getDownloadedVersion();
        if(TextUtils.isEmpty(downloadVer)){
            return null;
        }
        final String desc = getDownloadedVersionDesc();
        final String versionName = getDownloadedVersionName();
        String curVersion = AppVersionUtils.getAppVersionName(mContext);
        final boolean isUpdate = compareVersion(curVersion,downloadVer);
        return new IAppUpdate() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public boolean isForceUpdate() {
                return false;
            }

            @Override
            public boolean isUpdate() {
                return isUpdate;
            }

            @Override
            public String getUpdateUrl() {
                return "";
            }

            @Override
            public String getUpdateDesc() {
                return desc;
            }

            @Override
            public String getUpdateVersionName() {
                return  versionName;
            }

            @Override
            public String getUpdateVersion() {
                return downloadVer;
            }
        };
    }

    /**
     * 取得已经下载的安装包的版本号.如：1.0.3
     * @return
     */
    public String getDownloadedVersion(){
        SharedPreferences sp = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE);
        String downloadVersion = sp.getString(KEY_DOWNLOAD_VERSION, "");
        return downloadVersion;
    }

    /**
     * 取得已经下载的安装包的版本名字，用于在弹框中显示标题.如：新版本 1.0.3
     * @return
     */
    public String getDownloadedVersionName(){
        SharedPreferences sp = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE);
        String downloadVersion = sp.getString(KEY_DOWNLOAD_VERSION_NAME, "");
        return downloadVersion;
    }

    /**
     * 取得已经下载的安装包的版本介绍，用于在弹框中显示新版本内容介绍.如：新版本 1.0.3
     * @return
     */
    public String getDownloadedVersionDesc(){
        SharedPreferences sp = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE);
        String downloadVersion = sp.getString(KEY_DOWNLOAD_VERSION_DESC,"");
        return downloadVersion;
    }

    /**
     * 取得已经下载的安装包的本地路径，用于点击安装此安装包
     * @return
     */
    public String getDownloadedVersionPath(){
        SharedPreferences sp = mContext.getSharedPreferences(SP_APP_VERSION, Context.MODE_PRIVATE);
        String downloadVersion = sp.getString(KEY_DOWNLOAD_FILE_PATH, "");
        return downloadVersion;
    }

    public boolean isCheckWifiOr3G() {
        return mCheckWifiOr3G;
    }

    /**
     * 在点击下载的时候，是否需要提示当前的网络环境
     * @param checkWifiOr3G
     */
    public void setCheckWifiOr3G(boolean checkWifiOr3G) {
        mCheckWifiOr3G = checkWifiOr3G;
    }

    /**
     * 是否自动更新
     * @return
     */
    public boolean isAutoDownload() {
        return isAutoDownload && !mUpdateBean.isForceUpdate();
    }

    /**
     * 是否开启自动下载。
     * 如果为true，在有更新的情况下，如果不是强制更新，而且wifi条件下，就会自动下载更新的apk，下载完成后就会弹出更新提醒框;
     * 如果为false，在有更新的情况下，不会自动下载，会先弹出更新提示框，点击下载后再下载更新的apk
     * @param isAutoDownload
     */
    public void setIsAutoDownload(boolean isAutoDownload) {
        this.isAutoDownload = isAutoDownload;
    }

    public IAppUpdate getUpdateBean() {
        return mUpdateBean;
    }

    /**
     * 安装apk
     * @param localFilePath
     */
    public void installApk(String localFilePath){
        if(TextUtils.isEmpty(localFilePath)){
            return;
        }
        localFilePath = fixFilePath(localFilePath);
        File file = new File(localFilePath);
        if(!file.exists() || !file.isFile()){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + localFilePath),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    private String fixFilePath(String filePath){
        if(TextUtils.isEmpty(filePath)){
            return "";
        }
        if(filePath.startsWith("file://")){
            return filePath.substring("file://".length(),filePath.length());
        }
        return filePath;
    }

    /**
     * true，表示有新版本，需要更新
     *
     * @param curVersion  目前运行的版本
     * @param netVersion  线上的版本
     * @return
     */
    public boolean compareVersion(String curVersion,String netVersion){
        return AppVersionUtils.compareVersion(curVersion, netVersion);
    }
}
