package com.szr.flashim.file.mapper;

import com.szr.flashim.file.model.pojo.FilePartInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FilePartInfoMapper {
    int insert(FilePartInfo filePartInfo);
    List<FilePartInfo> selectByFileId(Long fileId);
    int updateStatus(Long id, Integer status);
    int deleteByFileId(Long fileId);
}