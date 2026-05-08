package org.iotf.authservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.iotf.entity.auth.dao.TUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Optional;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Mapper
public interface TUserMapper extends BaseMapper<TUser> {

}
