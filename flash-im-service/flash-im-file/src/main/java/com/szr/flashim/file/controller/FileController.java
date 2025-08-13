package com.szr.flashim.file.controller;

import com.szr.flashim.file.model.pojo.FileInfo;
import com.szr.flashim.file.model.vo.UploadInitRequest;
import com.szr.flashim.file.model.vo.UploadInitResponse;
import com.szr.flashim.file.service.FileService;
import com.szr.flashim.general.model.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 检查文件是否已存在(秒传)
     */
    @GetMapping("/check")
    public ResponseResult<FileInfo> checkFileExist(@RequestParam String md5) {
        FileInfo fileInfo = fileService.checkFileExist(md5);
        return ResponseResult.ok(fileInfo);
    }

    /**
     * 初始化文件上传
     */
    @PostMapping("/init")
    public ResponseResult<UploadInitResponse> initFileUpload(
            @RequestBody UploadInitRequest uploadInitRequest) {
        return fileService.initFileUpload(uploadInitRequest);
    }

    /**
     * 完成分片上传
     */
    @PostMapping("/completePart")
    public ResponseResult<Boolean> completePartUpload(@RequestParam Long fileId,
                                                      @RequestParam Integer partNumber,
                                                      @RequestParam String partMd5) {
        boolean success = fileService.completePartUpload(fileId, partNumber, partMd5);
        return ResponseResult.ok(success);
    }

    /**
     * 完成文件上传
     */
    @PostMapping("/complete")
    public ResponseResult<Boolean> completeFileUpload(@RequestParam Long fileId) {
        boolean success = fileService.completeFileUpload(fileId);
        return ResponseResult.ok(success);
    }

    /**
     * 获取分片上传URL
     */
    @GetMapping("/uploadUrl")
    public ResponseResult<String> getUploadUrl(@RequestParam Long fileId, @RequestParam Integer partNumber) {
        String uploadUrl = fileService.getUploadUrl(fileId, partNumber);
        if (uploadUrl == null) {
            return ResponseResult.fail("获取上传URL失败");
        }
        return ResponseResult.ok(uploadUrl);
    }

    /**
     * 获取下载URL
     */
    @GetMapping("/downloadUrl")
    public ResponseResult<String> getDownloadUrl(@RequestParam Long fileId) {
        String downloadUrl = fileService.getDownloadUrl(fileId);
        if (downloadUrl == null) {
            return ResponseResult.fail("获取下载URL失败");
        }
        return ResponseResult.ok(downloadUrl);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info")
    public ResponseResult<FileInfo> getFileInfo(@RequestParam Long fileId) {
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        return ResponseResult.ok(fileInfo);
    }
}