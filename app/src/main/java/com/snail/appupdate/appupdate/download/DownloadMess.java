package com.snail.appupdate.appupdate.download;

/**
 * Created by zhujun on 2015/10/14.
 */
public class DownloadMess {
    private String local_uri;               // 本地存放的uri
    private String uri;                     // 下载url的uri
    private Long id;                        // 下载id
    private String title;                   // 通知栏标题
    private String content;                 // 通知栏描述
    private long status;                  // 下载状态
    private String sizeTotal;               // 下载总大小
    private String size;                    // 已下载大小
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocal_uri() {
        return local_uri;
    }

    public void setLocal_uri(String local_uri) {
        this.local_uri = local_uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public String getSizeTotal() {
        return sizeTotal;
    }

    public void setSizeTotal(String sizeTotal) {
        this.sizeTotal = sizeTotal;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
