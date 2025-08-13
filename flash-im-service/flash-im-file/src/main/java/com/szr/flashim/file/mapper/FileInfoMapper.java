package com.szr.flashim.file.mapper;

import com.szr.flashim.file.model.pojo.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileInfoMapper {
    int insert(FileInfo fileInfo);
    FileInfo selectByMd5(String md5);
    FileInfo selectById(Long id);
    int updateStatus(Long id, Integer status);
}