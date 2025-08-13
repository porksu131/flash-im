package com.szr.flashim.file.model.pojo;

import java.io.Serializable;

/**
 * 文件分片信息表
 */
public class FilePartInfo implements Serializable {
    /**
     * 分片ID，雪花算法生成
     */
    private Long id;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 分片序号
     */
    private Integer partNumber;
    
    /**
     * 分片大小
     */
    private Long partSize;
    
    /**
     * 分片MD5
     */
    private String partMd5;
    
    /**
     * 分片上传状态(0:未上传,1:已上传)
     */
    private Integer status;

    /**
     * 预签名url
     */
    private String preSignedUrl;
    
    /**
     * 创建时间(时间戳)
     */
    private Long createTime;
    
    /**
     * 更新时间(时间戳)
     */
    private Long updateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Integer getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(Integer partNumber) {
        this.partNumber = partNumber;
    }

    public Long getPartSize() {
        return partSize;
    }

    public void setPartSize(Long partSize) {
        this.partSize = partSize;
    }

    public String getPartMd5() {
        return partMd5;
    }

    public void setPartMd5(String partMd5) {
        this.partMd5 = partMd5;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPreSignedUrl() {
        return preSignedUrl;
    }

    public void setPreSignedUrl(String preSignedUrl) {
        this.preSignedUrl = preSignedUrl;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}