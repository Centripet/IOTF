package batisGenerator.service.impl;

import org.iotf.entity.collect_analyze.TAlarm;
import batisGenerator.mapper.TAlarmMapper;
import org.iotf.collectanalyzeservice.service.ITAlarmService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 告警记录表 服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Service
public class TAlarmServiceImpl extends ServiceImpl<TAlarmMapper, TAlarm> implements ITAlarmService {

}
