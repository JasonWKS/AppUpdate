package com.snail.appupdate.appupdate.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by shenjie3 on 15-11-17.
 */
public class AppVersionUtils {

    public static String getAppVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return  version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 比较当前App的版本与线上App的版本。如果线上的大.
     * Version 必须符合 1.0.0这样的形式
     * @param thisVersion
     * @param netVersion
     * @return
     */
    public static boolean compareVersion(String thisVersion,String netVersion){
        String space = "\\.";
        String[] thisVersionArr = thisVersion.split(space);
        String[] netVersionArr = netVersion.split(space);
        int thisVersionInt = Integer.parseInt(thisVersionArr[0]);
        int netVersionInt = Integer.parseInt(netVersionArr[0]);
        if(netVersionInt == thisVersionInt){
            thisVersionInt = Integer.parseInt(thisVersionArr[1]);
            netVersionInt = Integer.parseInt(netVersionArr[1]);
            if(netVersionInt == thisVersionInt){
                thisVersionInt = Integer.parseInt(thisVersionArr[2]);
                netVersionInt = Integer.parseInt(netVersionArr[2]);
            }
        }
        return netVersionInt > thisVersionInt;
    }
}
