package org.iotf.collectanalyzeservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.TDevice;
import com.baomidou.mybatisplus.extension.service.IService;
import org.iotf.requestFormation.collect_analyze.*;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
public interface ITDeviceService extends IService<TDevice> {

    TDevice deviceSubmit(JwtPayload payload, deviceSubmitRequest request);

    Boolean reportModify(JwtPayload payload, reportModifyRequest request);

    Boolean reportSwitch(JwtPayload payload, reportSwitchRequest request);

    IPage<TDevice> deviceList(JwtPayload payload, deviceListRequest request);

    TDevice deviceDetail(JwtPayload payload, deviceDetailRequest request);

    Boolean deviceDelete(JwtPayload payload, deviceDeleteRequest request);

    /**
     * 根据设备UUID查询设备信息
     * @param deviceUUID 设备UUID
     * @return 设备信息
     */
    TDevice getDeviceByUUID(String deviceUUID);

}
