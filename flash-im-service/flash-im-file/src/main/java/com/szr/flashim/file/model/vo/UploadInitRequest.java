package com.szr.flashim.file.model.vo;

public class UploadInitRequest {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小(字节)
     */
    private Long size;

    /**
     * 文件MD5值
     */
    private String md5;

    /**
     * 文件类型
     */
    private String contentType;


    /**
     * 分片大小（可选）
     */
    private Integer partSize;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getPartSize() {
        return partSize;
    }

    public void setPartSize(Integer partSize) {
        this.partSize = partSize;
    }
}