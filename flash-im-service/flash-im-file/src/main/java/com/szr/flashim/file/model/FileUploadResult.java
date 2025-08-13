package com.szr.flashim.file.model;

public class FileUploadResult {
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String downloadUrl;
    private Long uploadTime;

    public FileUploadResult() {}

    public FileUploadResult(Long fileId, String fileName, Long fileSize, String fileType, String downloadUrl, Long uploadTime) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
        this.uploadTime = uploadTime;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }
}
