package org.iotf.authservice.service;

import org.iotf.entity.auth.dao.TFile;
import com.baomidou.mybatisplus.extension.service.IService;
import org.iotf.requestFormation.auth.fileSubmitRequest;
import org.iotf.requestFormation.auth.fileUrlsGenRequest;
import org.iotf.requestFormation.auth.res.uploadSubmit;

import java.util.List;

/**
 * <p>
 * 文件 服务类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
public interface ITFileService extends IService<TFile> {
    // 原始文件提交
    List<TFile> uploadSubmit(Long userId, fileSubmitRequest request, boolean b);

    // netty文件上传
    TFile uploadSubmit(Long userId, uploadSubmit upload, boolean is_public_read);

    // 文件url
    List<TFile> fileUrlsGen(fileUrlsGenRequest request);
    
}
