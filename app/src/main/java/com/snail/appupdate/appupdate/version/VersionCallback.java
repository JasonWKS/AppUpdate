package com.snail.appupdate.appupdate.version;

import com.snail.appupdate.appupdate.update.IAppUpdate;

/**
 * Created by shenjie3 on 15-11-13.
 */
public interface VersionCallback {
    void success(IAppUpdate updateBean);
    void fail(String result);
}
