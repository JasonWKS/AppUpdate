package com.snail.appupdate.appupdate.download;

/**
 * Created by shenjie3 on 15-11-13.
 */
public interface DownloadCallback {
    void onProgress(int progress,DownloadMess downloadMess);
    void onPause(DownloadMess downloadMess);
    void onSuccessfull(DownloadMess downloadMess);
    void onFail(int error,String errorString,DownloadMess downloadMess);
}
