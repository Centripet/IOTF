package org.iotf.authservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.iotf.entity.auth.dao.SUser;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Mapper
public interface SUserMapper extends BaseMapper<SUser> {

}
