package org.iotf.collectanalyzeservice.service.impl;

import org.iotf.collectanalyzeservice.mapper.TAlarmLogMapper;
import org.iotf.entity.collect_analyze.TAlarmLog;
import org.iotf.collectanalyzeservice.service.ITAlarmLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 告警状态变更日志表 服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Service
public class TAlarmLogServiceImpl extends ServiceImpl<TAlarmLogMapper, TAlarmLog> implements ITAlarmLogService {

}
