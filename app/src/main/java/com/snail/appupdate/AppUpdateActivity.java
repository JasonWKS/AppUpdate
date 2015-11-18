package com.snail.appupdate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.snail.appupdate.appupdate.download.DownloadMess;
import com.snail.appupdate.appupdate.update.IAppUpdate;
import com.snail.appupdate.appupdate.update.BaseAppUpdateActivity;


/**
 * Created by shenjie3 on 15-11-16.
 */
public class AppUpdateActivity extends BaseAppUpdateActivity {
    private TextView mTextView_Title,mTextView_Content,mTextView_Progress;
    private Button mButton_Sure,mButton_Cancel,mButton_Update;
    private ProgressBar mProgressBar_DownloadProgress;
    private CheckBox mCheckBox_Ignore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_app_update);
        initViews();
        showUpdateDialog();
    }

    private void initViews(){
        mTextView_Title = (TextView) findViewById(R.id.tv_title);
        mTextView_Content = (TextView) findViewById(R.id.tv_update_content);
        mButton_Sure = (Button) findViewById(R.id.btn_set);
        mButton_Cancel = (Button) findViewById(R.id.btn_cancel);
        mButton_Update = (Button) findViewById(R.id.btn_must_set);
        mProgressBar_DownloadProgress = (ProgressBar) findViewById(R.id.progress);
        mCheckBox_Ignore = (CheckBox) findViewById(R.id.chk_ignore);
        mTextView_Progress = (TextView) findViewById(R.id.tv_progress);
    }

    protected void showForceUpdateDialog(){
        mTextView_Title.setVisibility(View.VISIBLE);
        mTextView_Content.setVisibility(View.VISIBLE);
        mButton_Update.setVisibility(View.VISIBLE);
        mCheckBox_Ignore.setVisibility(View.GONE);
        mButton_Cancel.setVisibility(View.GONE);
        mButton_Sure.setVisibility(View.GONE);
        mTextView_Progress.setVisibility(View.GONE);
        mProgressBar_DownloadProgress.setVisibility(View.GONE);
        IAppUpdate updateBean = VersionManager.getInstance().getUpdateBean();
        mTextView_Title.setText(updateBean.getUpdateVersionName());
        mTextView_Content.setText(updateBean.getUpdateDesc());
        mButton_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndDownload();
            }
        });
        mButton_Update.setText("立即更新");
    }

    protected void showCancelableUpdateDialog(){
        mTextView_Title.setVisibility(View.VISIBLE);
        mTextView_Content.setVisibility(View.VISIBLE);
        mButton_Update.setVisibility(View.GONE);
        mCheckBox_Ignore.setVisibility(View.VISIBLE);
        mButton_Cancel.setVisibility(View.VISIBLE);
        mButton_Sure.setVisibility(View.VISIBLE);
        mTextView_Progress.setVisibility(View.GONE);
        mProgressBar_DownloadProgress.setVisibility(View.GONE);
        IAppUpdate updateBean = VersionManager.getInstance().getUpdateBean();
        mTextView_Title.setText(updateBean.getUpdateVersionName());
        mTextView_Content.setText(updateBean.getUpdateDesc());
        mButton_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIgnore();
                finish();
            }
        });
        mButton_Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndDownload();
            }
        });
    }

    private void checkIgnore(){
        if(isIgnoreVersion()){
            onIgnore();
        }
    }

    protected void showNoNetworkDialog(){
        mTextView_Title.setVisibility(View.VISIBLE);
        mTextView_Content.setVisibility(View.VISIBLE);
        mCheckBox_Ignore.setVisibility(View.GONE);
        mButton_Cancel.setVisibility(View.GONE);
        mButton_Sure.setVisibility(View.GONE);
        mButton_Update.setVisibility(View.VISIBLE);
        mTextView_Progress.setVisibility(View.GONE);
        mProgressBar_DownloadProgress.setVisibility(View.GONE);
        mTextView_Title.setText("没有网络");
        mTextView_Content.setText("当前没有网络连接,请检查网络设置");
        mButton_Update.setText("网络设置");
        mButton_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goWifiSetting();
            }
        });
    }

    public void showNetworkTypeDialog(){
        mTextView_Title.setVisibility(View.VISIBLE);
        mTextView_Content.setVisibility(View.VISIBLE);
        mButton_Update.setVisibility(View.GONE);
        mCheckBox_Ignore.setVisibility(View.GONE);
        mButton_Cancel.setVisibility(View.VISIBLE);
        mButton_Sure.setVisibility(View.VISIBLE);
        mProgressBar_DownloadProgress.setVisibility(View.GONE);
        mTextView_Title.setText("没有连接WIFI");
        mTextView_Content.setText("当前网络连接的是运营商网络，将会收取流量费用");
        mButton_Cancel.setText("打开wifi");
        mButton_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goWifiSetting();
            }
        });
        mButton_Sure.setText("立即下载");
        mButton_Sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndDownload();
            }
        });
    }

    @Override
    protected void showDownloadProgressDialog() {
        mTextView_Content.setVisibility(View.GONE);
        mButton_Update.setEnabled(false);
        mTextView_Progress.setVisibility(View.VISIBLE);
        mProgressBar_DownloadProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected boolean isIgnoreVersion() {
        return mCheckBox_Ignore.getVisibility() == View.VISIBLE
                && mCheckBox_Ignore.isChecked();
    }

    @Override
    public void onProgress(int progress, DownloadMess downloadMess) {
        mProgressBar_DownloadProgress.setProgress(progress);
        mTextView_Progress.setText("下载进度：" + progress + "%" );
    }
}
