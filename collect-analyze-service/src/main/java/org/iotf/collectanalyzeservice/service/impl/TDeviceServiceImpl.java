package org.iotf.collectanalyzeservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.iotf.entity.collect_analyze.TDevice;
import org.iotf.collectanalyzeservice.mapper.TDeviceMapper;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Service
@RequiredArgsConstructor
public class TDeviceServiceImpl extends ServiceImpl<TDeviceMapper, TDevice> implements ITDeviceService {

}
