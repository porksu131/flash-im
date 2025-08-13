package com.szr.flashim.file.service;

import com.szr.flashim.file.model.pojo.FileInfo;
import com.szr.flashim.file.model.vo.UploadInitResponse;
import com.szr.flashim.file.model.vo.UploadInitRequest;
import com.szr.flashim.general.model.ResponseResult;

public interface FileService {
    /**
     * 检查文件是否已存在(秒传)
     */
    FileInfo checkFileExist(String md5);
    
    /**
     * 初始化文件上传
     */
    ResponseResult<UploadInitResponse> initFileUpload(UploadInitRequest uploadInitRequest);

    /**
     * 获取文件上传URL
     */
    String getUploadUrl(Long fileId, Integer partNumber);
    
    /**
     * 完成分片上传
     */
    boolean completePartUpload(Long fileId, Integer partNumber, String partMd5);
    
    /**
     * 完成文件上传
     */
    boolean completeFileUpload(Long fileId);
    
    /**
     * 获取文件下载URL
     */
    String getDownloadUrl(Long fileId);
    
    /**
     * 获取文件信息
     */
    FileInfo getFileInfo(Long fileId);
}