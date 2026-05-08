package org.iotf.authservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.iotf.entity.auth.dao.TFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 文件 Mapper 接口
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Mapper
public interface TFileMapper extends BaseMapper<TFile> {

}
