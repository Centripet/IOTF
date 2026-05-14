package org.iotf.collectanalyzeservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.iotf.entity.collect_analyze.TAlarmLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 告警状态变更日志表 Mapper 接口
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Mapper
public interface TAlarmLogMapper extends BaseMapper<TAlarmLog> {

}
