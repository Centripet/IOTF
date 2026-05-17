package org.iotf.collectanalyzeservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.TAlarmLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.iotf.requestFormation.collect_analyze.alarmLogListRequest;

/**
 * <p>
 * 告警状态变更日志表 服务类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
public interface ITAlarmLogService extends IService<TAlarmLog> {

    IPage<TAlarmLog> alarmLogList(alarmLogListRequest request, JwtPayload payload);

}
