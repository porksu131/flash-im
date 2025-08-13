package com.szr.flashim.file.model.pojo;
import java.io.Serializable;

/**
 * 文件信息表
 */
public class FileInfo implements Serializable {
    /**
     * 文件ID，雪花算法生成
     */
    private Long id;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 存储的文件名
     */
    private String storageName;
    
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
     * 文件存储路径
     */
    private String path;
    
    /**
     * 上传用户ID
     */
    private Long userId;
    
    /**
     * 上传状态(0:上传中,1:上传完成,2:上传失败)
     */
    private Integer status;
    
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

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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