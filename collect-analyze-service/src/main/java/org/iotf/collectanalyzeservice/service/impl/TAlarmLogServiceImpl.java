package org.iotf.collectanalyzeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.iotf.collectanalyzeservice.mapper.TAlarmLogMapper;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.TAlarmLog;
import org.iotf.collectanalyzeservice.service.ITAlarmLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.iotf.requestFormation.collect_analyze.alarmLogListRequest;
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

    @Override
    public IPage<TAlarmLog> alarmLogList(alarmLogListRequest request, JwtPayload payload) {

        Page<TAlarmLog> page = new Page<>(request.page(), request.size());
        LambdaQueryWrapper<TAlarmLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TAlarmLog::getUser_id, payload.getUser_id());

        wrapper.orderByDesc(TAlarmLog::getCreate_time);

        return baseMapper.selectPage(page, wrapper);
    }


}
