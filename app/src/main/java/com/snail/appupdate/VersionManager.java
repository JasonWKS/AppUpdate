package com.snail.appupdate;

import com.snail.appupdate.appupdate.update.IAppUpdate;
import com.snail.appupdate.appupdate.version.BaseVersionManager;
import com.snail.appupdate.bean.UpdateBean;

/**
 * Created by shenjie3 on 15-11-17.
 */
public class VersionManager extends BaseVersionManager {

    private static BaseVersionManager instance;
    public static BaseVersionManager getInstance(){
        if(instance == null){
            instance = new VersionManager();
        }
        return instance;
    }

    @Override
    protected void onNetCheckUpdate() {
        String result = "{\"code\":\"0\",\"desc\":\"操作成功！\",\"validateResults\":null,\"deviceToken\":null,\"data\":{\"url\":\"http://res.sandbox.usercenter.17178.tv:10004/APKDIR/fec9af69fe164d93a7c7bad6f5dec752.apk\",\"isForce\":true,\"version\":\"1.0.3\",\"versionName\":\"翡翠世界 1.0.3\",\"desc\":\"翡翠世界 1.0.3 android\"}}";
        IAppUpdate updateBean = UpdateBean.parse(result);

        postCheckUpdateSuccess(updateBean);
    }

    @Override
    public void postCheckUpdateFail(String result) {
        super.postCheckUpdateFail(result);
    }
}
