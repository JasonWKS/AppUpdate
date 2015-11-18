package com.snail.appupdate.appupdate.update;

/**
 * Created by shenjie3 on 15-11-13.
 */
public interface IAppUpdate {
    /**
     * 请求是否成功
     * @return
     */
    public boolean isSuccess();

    /**
     * 是否需要强制更新
     * @return
     */
    public boolean isForceUpdate();

    /**
     * 是否需要更新
     * @return
     */
    public boolean isUpdate();

    /**
     * 新版本下载路径
     * @return
     */
    public String getUpdateUrl();

    /**
     * 新版本描述
     * @return
     */
    public String getUpdateDesc();

    /**
     * 新版本标题
     * @return
     */
    public String getUpdateVersionName();

    /**
     * 新版本版本号，如：1.0.2
     * @return
     */
    public String getUpdateVersion();
}
