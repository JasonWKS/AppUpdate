package com.snail.appupdate.appupdate.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.snail.appupdate.MyApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhujun on 2015/10/13.
 */
public class DownLoadHelper {
    private static final String TAG = DownLoadHelper.class.getSimpleName();
    private static DownLoadHelper instance;
    private DownloadManager downloadManager;
    private Map<String,Long> download_urls_ids = new HashMap<String,Long>();            // 下载的url和对应的下载id
    private Context context;
    private DownloadChangeObserver observer;
    private DownLoadSuccessReceiver receiver;               // 下载完成接受广播
    private Handler handler;
    private List<String> urls = new ArrayList<>(10) ;

    private boolean mShowNotification = true;

    private Map<String,DownloadCallback> mDownloadCallbackCache = new HashMap<>(3);

    public DownLoadHelper() {
        context = MyApplication.getInstance();
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        queryDownTask();
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                DownloadMess mess = (DownloadMess) msg.obj;
                Log.i(TAG,"what = " + msg.what + ", " + Integer.toBinaryString(msg.what));
                switch (msg.what){
                    case DownloadManager.STATUS_RUNNING:        // 正在下载
                        performDownloadProgress(mess);
                        break;
                    case DownloadManager.STATUS_PAUSED:         // 停止下载
                        performDownloadPause(mess);
                        break;
                    case DownloadManager.STATUS_FAILED:
                        performDownloadFail(msg.what,mess);
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void performDownloadProgress(DownloadMess mess){
        Log.i(TAG,"performDownloadProgress");
        DownloadCallback callback = getDownloadCallback(mess);
        if(callback != null){
            long curSize = 0;
            long totalSize = 0;
            String curSizeStr = mess.getSize();
            String totalSizeStr = mess.getSizeTotal();
            if(!TextUtils.isEmpty(curSizeStr)){
                curSize = Long.parseLong(curSizeStr);
            }
            if(!TextUtils.isEmpty(totalSizeStr)){
                totalSize = Long.parseLong(totalSizeStr);
            }

            if(totalSize > 0){
                int progress = (int) ((100 * curSize) / totalSize);
                callback.onProgress(progress,mess);
            }
        }
    }

    private void performDownloadPause(DownloadMess mess){
        Log.i(TAG,"performDownloadPause");
        DownloadCallback callback = getDownloadCallback(mess);
        if(callback != null){
            callback.onPause(mess);
        }
    }

    private void performDownloadSuccessfull(DownloadMess mess){
        Log.i(TAG,"performDownloadSuccessfull");
        DownloadCallback callback = getDownloadCallback(mess);
        if(callback != null){
            callback.onSuccessfull(mess);
            removeDownlaodCallback(mess);
        }
    }

    private void performDownloadFail(int what,DownloadMess mess){
        Log.i(TAG,"performDownloadFail");
        DownloadCallback callback = getDownloadCallback(mess);
        if(callback != null) {
            String des = "未知错误";
            if(what == 192){
                des = "网络错误";
            }
            callback.onFail(what, "网络错误", mess);
            removeDownlaodCallback(mess);
        }
    }

    private DownloadCallback getDownloadCallback(DownloadMess mess){
        if(mess != null){
            String url = mess.getUri();
            return  mDownloadCallbackCache.get(url);
        }
        return null;
    }

    private void removeDownlaodCallback(DownloadMess mess){
        removeDownloadCallback(mess.getUri());
    }

    private void removeDownloadCallback(String url){
        mDownloadCallbackCache.remove(url);
    }

    public static DownLoadHelper getInstance(){
        if(instance == null){
            instance = new DownLoadHelper();
        }
        return instance;
    }

    /**
     * 是否需要在通知栏显示
     * @param showNotification
     */
    public void setShowNotification(boolean showNotification) {
        mShowNotification = showNotification;
    }

    /**
     * @param url
     * @param downloadCallback
     */
    public void downloadFile(String url,DownloadCallback downloadCallback){
        if(download_urls_ids.containsKey(url)){
            Toast.makeText(context, "有相同的url下载", Toast.LENGTH_SHORT).show();
            return;
        }
        if(downloadCallback != null){
            mDownloadCallbackCache.put(url,downloadCallback);
        }

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
//         request.setShowRunningNotification(false);  // 不显示下载界面
        request.setVisibleInDownloadsUi(mShowNotification);  //在通知栏中显示
        // 设置通知栏标题
        request.setNotificationVisibility(mShowNotification ?
                DownloadManager.Request.VISIBILITY_VISIBLE : DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setTitle("下载");
        request.setDescription("正在下载" + DownLoadUtils.getFileNameFromPath(url));
        request.setAllowedOverRoaming(false);
        request.setMimeType("application/vnd.android.package-archive");
        //设置文件存放目录
        request.setDestinationInExternalPublicDir(DownLoadContacts.DOWNLOAD_SUBPATH, DownLoadUtils.getFileNameFromPath(url));
        if(downloadManager != null){
            Long id = downloadManager.enqueue(request);     // 开始下载
            download_urls_ids.put(url,id);
            if(urls != null) urls.add(url);
        }
    }

    public Map<String, Long> getDownload_urls_ids() {
        return download_urls_ids;
    }

    // 获取id对应的url
    public String getUrlFromId(Long id){
        String url = null;
        if(download_urls_ids.containsValue(id)){
            Iterator it = download_urls_ids.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String,Long> entry = (Map.Entry<String, Long>) it.next();
                if(entry.getValue().equals(id)){
                    url = entry.getKey();
                }
            }
        }
        return url;
    }

    // 获取url对应的id
    public Long getIdFromUrl(String url){
        Long id = -1L;
        if(download_urls_ids.containsKey(url)){
           id =  download_urls_ids.get(url);
        }
        return id;
    }

    // 取消id下载
    public void removeDownload(Long id){
        if(downloadManager != null){
            downloadManager.remove(id);
            Iterator it = download_urls_ids.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String,Long> entry = (Map.Entry<String, Long>) it.next();
                if(entry.getValue() == id){
                    it.remove();
                    removeDownloadCallback(entry.getKey());
                    break;
                }
            }
        }
    }

    // 取消url下载
    public void removeDownload(String url){
        if(downloadManager != null){
            if(getIdFromUrl(url) != -1L){
                downloadManager.remove(getIdFromUrl(url));
                removeDownloadCallback(url);
            }
            download_urls_ids.remove(url);
        }
    }

    // 获取下载url对应的详情
    public DownloadMess getDownloadMess(String url){
        DownloadMess mess = getDownloadMess(getIdFromUrl(url));
        if(mess == null){
            return null;
        }else if(mess.getLocal_uri() == null){
            removeDownload(mess.getId());
            return null;
        }else{
            return mess;
        }
    }

    // 获取下载id对应的详情
    public DownloadMess getDownloadMess(Long id){
        DownloadMess mess = null;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        int status1 = DownloadManager.STATUS_RUNNING
                | DownloadManager.STATUS_PAUSED
                | DownloadManager.STATUS_SUCCESSFUL
                | DownloadManager.STATUS_FAILED;
        query.setFilterByStatus(status1);
        Cursor cursor= downloadManager.query(query);
        if (cursor.moveToFirst()){
            mess = new DownloadMess();
            String downId= cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
            String des = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
            String localuri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String url = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
            long status = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String size= cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            String sizeTotal = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            mess.setId(Long.parseLong(downId));
            mess.setContent(des);
            mess.setLocal_uri(localuri);
            mess.setSize(size);
            mess.setSizeTotal(sizeTotal);
            mess.setStatus(status);
            mess.setUri(url);
            mess.setTitle(title);
            mess.setFileName(fileName);
        }
//        print(cursor);
        cursor.close();
        return mess;
    }

    private void print(Cursor c){
        Log.i(TAG, "========================");
        if(c != null){
            if (c.moveToFirst()){
                int count = c.getColumnCount();
                for (int i = 0; i < count; i++) {
                    String value = c.getString(i);
                    String column = c.getColumnName(i);
                    Log.i(TAG,"" + column + " = " + value);
                }
            }
        }
    }

    class DownLoadSuccessReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: action = " + intent.getAction());
            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if(TextUtils.isEmpty(getUrlFromId(id))) return;
                DownloadMess mess = getDownloadMess(id);
                if(mess != null){
                    performDownloadSuccessfull(mess);
                }
            }else if(intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){
                Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
                Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(dm);
            }
        }
    }

    class DownloadChangeObserver extends ContentObserver {
        public DownloadChangeObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            queryDownloadStatus();
        }
    }

    private void queryDownloadStatus(){
        if(urls == null) return;

        for (int i = 0, size = urls.size() ;i < size ;i++){
            DownloadMess downloadMess = getDownloadMess(getIdFromUrl(urls.get(i)));
            if(downloadMess == null) return;
            Message message = handler.obtainMessage((int)downloadMess.getStatus());
            message.obj = downloadMess;
            handler.sendMessage(message);
        }
    }

    private void queryDownTask() {
        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterByStatus(status);
        Cursor cursor= downloadManager.query(query);
        while(cursor.moveToNext()){
            String downId= cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            String localuri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String url = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
            if(localuri != null && localuri.contains(java.io.File.separator + DownLoadContacts.DOWNLOAD_SUBPATH)){
                File f = new File(localuri);
                if(f.isFile()){
                    download_urls_ids.clear();
                    download_urls_ids.put(url, Long.parseLong(downId));
                }else{
                    removeDownload(Long.parseLong(downId));
                }
            }else{
                removeDownload(Long.parseLong(downId));
            }
        }
        cursor.close();
    }

    public void registerListener(boolean databaseObserver){
        registerReceiver();
        if(databaseObserver){
            registerDatabaseObserver();
        }
    }

    // 注册监听器
    private void registerReceiver(){
        receiver = new DownLoadSuccessReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        context.registerReceiver(receiver, filter);
    }

    private void registerDatabaseObserver(){
        observer = new DownloadChangeObserver(handler);
        context.getContentResolver().registerContentObserver(DownLoadContacts.CONTENT_URI, true, observer);
    }

    // 取消监听器
    public void removeListener(){
        if(receiver != null){
            context.unregisterReceiver(receiver);
            receiver = null;
        }

        if(observer != null){
            context.getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        urls.clear();
        download_urls_ids.clear();
        mDownloadCallbackCache.clear();
    }
}
