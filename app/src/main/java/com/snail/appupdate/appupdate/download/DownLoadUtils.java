package com.snail.appupdate.appupdate.download;

import java.io.File;

/**
 * Created by zhujun on 2015/10/13.
 */
public class DownLoadUtils {

    //获取路径文件名
    public static String getFileNameFromPath(String path){
        File tempFile =new File( path.trim());
        String fileName = tempFile.getName();
        return fileName;
    }
}
