package com.snail.appupdate.bean;

import android.text.TextUtils;

import com.snail.appupdate.appupdate.update.IAppUpdate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhujun on 2015/10/9.
 */
public class UpdateBean extends BaseBean implements IAppUpdate {
    private UpdateInfo data;

    public UpdateInfo getData() {
        return data;
    }

    public void setData(UpdateInfo data) {
        this.data = data;
    }

    @Override
    public boolean isForceUpdate() {
        return data != null && data.isForce();
    }

    @Override
    public boolean isUpdate() {
        return data != null && !TextUtils.isEmpty(data.getUrl());
    }

    @Override
    public String getUpdateUrl() {
        return data != null ? data.getUrl() : "";
    }

    @Override
    public String getUpdateDesc() {
        return data != null ? data.getDesc() : "";
    }

    @Override
    public String getUpdateVersionName() {
        return data != null ? data.getVersionName() : "";
    }

    @Override
    public String getUpdateVersion() {
        return data != null ? data.getVersion() : "";
    }

    public static class UpdateInfo{
        private String url;
        private boolean isForce;
        private String version;
        private String versionName;
        private String desc;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isForce() {
            return isForce;
        }

        public void setIsForce(boolean isForce) {
            this.isForce = isForce;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static UpdateBean parse(String result){
        try {
            UpdateBean updateBean = new UpdateBean();
            JSONObject json = new JSONObject(result);
            updateBean.setCode(json.optString("code"));
            updateBean.setDesc(json.optString("desc"));
            updateBean.setDeviceToken(json.optString("deviceToken"));
            updateBean.setValidateResults(json.optString("validateResults"));

            JSONObject jsonInfo = json.optJSONObject("data");
            UpdateBean.UpdateInfo info = new UpdateBean.UpdateInfo();
            info.setDesc(jsonInfo.optString("desc"));
            info.setIsForce(jsonInfo.optBoolean("isForce"));
            info.setUrl(jsonInfo.optString("url"));
            info.setVersion(jsonInfo.optString("version"));
            info.setVersionName(jsonInfo.optString("versionName"));
            updateBean.setData(info);
            return updateBean;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(UpdateBean updateBean){
        try {
            UpdateBean.UpdateInfo info = updateBean.getData();
            JSONObject jsonData = new JSONObject();
            jsonData.put("desc",info.getDesc());
            jsonData.put("isForce",info.isForce());
            jsonData.put("url",info.getUrl());
            jsonData.put("version",info.getVersion());
            jsonData.put("versionName",info.getVersion());

            JSONObject jsonBean = new JSONObject();
            jsonBean.put("code",updateBean.getCode());
            jsonBean.put("data",jsonData);
            jsonBean.put("desc",updateBean.getDesc());
            jsonBean.put("deviceToken",updateBean.getDeviceToken());
            jsonBean.put("validateResults",updateBean.getValidateResults());

            return jsonBean.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
