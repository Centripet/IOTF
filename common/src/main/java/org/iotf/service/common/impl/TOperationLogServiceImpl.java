package org.iotf.service.common.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.iotf.entity.common.TOperationLog;
import org.iotf.mapper.common.TOperationLogMapper;
import org.iotf.service.common.ITOperationLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Service
@RequiredArgsConstructor
public class TOperationLogServiceImpl extends ServiceImpl<TOperationLogMapper, TOperationLog> implements ITOperationLogService {

}
