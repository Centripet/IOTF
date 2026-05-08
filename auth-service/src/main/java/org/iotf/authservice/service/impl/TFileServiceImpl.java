package org.iotf.authservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.iotf.authservice.mapper.TFileMapper;
import org.iotf.authservice.service.AliOssService;
import org.iotf.authservice.service.ITFileService;
import org.iotf.entity.auth.dao.TFile;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.iotf.requestFormation.auth.fileSubmitRequest;
import org.iotf.requestFormation.auth.fileUrlsGenRequest;
import org.iotf.requestFormation.auth.res.uploadSubmit;
import org.iotf.requestFormation.auth.uploadSubmitRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * <p>
 * 文件 服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Service
@RequiredArgsConstructor
public class TFileServiceImpl extends ServiceImpl<TFileMapper, TFile> implements ITFileService {

    private final TFileMapper wFileMapper;
    private final AliOssService ossService;

    @Override
    public List<TFile> uploadSubmit(Long userId, fileSubmitRequest requests, boolean is_public_read) {
        List<TFile> files = new ArrayList<>();
        for (uploadSubmitRequest request : requests.files()) {
            TFile file = TFile.builder()
                    .uploader(userId)
                    .file_key(request.key())
                    .title(request.title())
                    .info(request.info())
                    .type(request.type())
                    .suffix(request.suffix())
                    .create_time(LocalDateTime.now())
                    .is_public_read(is_public_read)
                    .origin_name(request.origin_name())
                    .build();
            wFileMapper.insert(file);
            files.add(file);
        }
        return files;
    }

    @Override
    public TFile uploadSubmit(Long userId, uploadSubmit upload, boolean is_public_read) {
        TFile file = TFile.builder()
                .uploader(userId)
                .file_key(upload.getKey())
                .title(upload.getTitle())
                .info(upload.getInfo())
                .type(upload.getType())
                .suffix(upload.getSuffix())
                .create_time(LocalDateTime.now())
                .is_public_read(is_public_read)
                .origin_name(upload.getOrigin_name())
                .build();
        wFileMapper.insert(file);
        return file;
    }

    @Override
    public List<TFile> fileUrlsGen(fileUrlsGenRequest request) {
        LambdaQueryWrapper<TFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TFile::getFile_id, request.files_id());
        List<TFile> files = wFileMapper.selectList(wrapper);

        ossService.generateUrlForEntity(files);

        return files;
    }
    
}
