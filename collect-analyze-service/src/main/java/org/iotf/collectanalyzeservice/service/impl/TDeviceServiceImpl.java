package org.iotf.collectanalyzeservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.iotf.collectanalyzeservice.service.MqttPublisher;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.TDevice;
import org.iotf.collectanalyzeservice.mapper.TDeviceMapper;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.iotf.requestFormation.collect_analyze.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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

    private final TDeviceMapper deviceMapper;
    private final MqttPublisher mqttPublisher;

    @Override
    public TDevice deviceSubmit(JwtPayload payload, deviceSubmitRequest request) {

        TDevice device = TDevice.builder()
                .user_id(payload.getUser_id())
                .device_name(request.device_name())
                .device_type(request.device_type())
                .device_uuid(request.device_UUID())
//                .frequency(request.frequency())
//                .report_status(false)

                .overload_threshold(request.overload_threshold())
                .high_energy_threshold(request.high_energy_threshold())
                .current_threshold(request.current_threshold())

                .create_time(LocalDateTime.now())
                .deleted(0)
                .build();

        mqttPublisher.commonPush(request.device_UUID() + "/deviceSubmit", request);

        return device;
    }

    @Override
    public Boolean reportModify(JwtPayload payload, reportModifyRequest request) {

        UpdateWrapper<TDevice> wrapper = new UpdateWrapper<>();
        wrapper.eq(TDevice.DEVICE_ID, request.device_id());

        boolean updated = false;

        if (request.device_UUID() != null) {
            wrapper.set(TDevice.DEVICE_UUID, request.device_UUID());
            updated = true;
        }

        if (request.device_type() != null) {
            wrapper.set(TDevice.DEVICE_TYPE, request.device_type());
            updated = true;
        }

        if (request.device_name() != null) {
            wrapper.set(TDevice.DEVICE_NAME, request.device_name());
            updated = true;
        }

//        if (request.frequency() != null) {
//            wrapper.set(TDevice.FREQUENCY, request.frequency());
//            updated = true;
//        }

        if (request.overload_threshold() != null) {
            wrapper.set(TDevice.OVERLOAD_THRESHOLD, request.overload_threshold());
            updated = true;
        }
        if (request.high_energy_threshold() != null) {
            wrapper.set(TDevice.HIGH_ENERGY_THRESHOLD, request.high_energy_threshold());
            updated = true;
        }
        if (request.current_threshold() != null) {
            wrapper.set(TDevice.CURRENT_THRESHOLD, request.current_threshold());
            updated = true;
        }

        if (!updated) {
            return true;
        }

        wrapper.set(TDevice.UPDATE_TIME, LocalDateTime.now());

        mqttPublisher.commonPush(request.device_UUID() + "/reportModify", request);

        return deviceMapper.update(wrapper) >= 1;
    }

//    @Override
//    public Boolean reportSwitch(JwtPayload payload, reportSwitchRequest request) {
//
//        TDevice device = deviceMapper.selectById(request.device_id());
//
//        mqttPublisher.commonPush(device.getDevice_uuid() + "/reportSwitch", request);
//
//        return deviceMapper.update(
//                new UpdateWrapper<TDevice>()
//                        .eq(TDevice.USER_ID, payload.getUser_id())
//                        .eq(TDevice.DEVICE_ID, request.device_id())
//                        .set(TDevice.REPORT_STATUS, request.report_status())
//        ) >= 1;
//
//    }

    @Override
    public IPage<TDevice> deviceList(JwtPayload payload, deviceListRequest request) {

        Page<TDevice> page = new Page<>(request.page(), request.size());
        LambdaQueryWrapper<TDevice> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TDevice::getUser_id, payload.getUser_id());
        wrapper.like(StringUtils.hasText(request.device_type()), TDevice::getDevice_type, request.device_type());
        wrapper.like(StringUtils.hasText(request.device_name()), TDevice::getDevice_name, request.device_name());

        wrapper.eq(TDevice::getDeleted, 0);
        wrapper.orderByDesc(TDevice::getCreate_time);

        return deviceMapper.selectPage(page, wrapper);
    }

    @Override
    public TDevice deviceDetail(JwtPayload payload, deviceDetailRequest request) {
        return deviceMapper.selectById(request.device_id());
    }

    @Override
    public Boolean deviceDelete(JwtPayload payload, deviceDeleteRequest request) {

        TDevice device = deviceMapper.selectById(request.device_id());

        mqttPublisher.commonPush(device.getDevice_uuid() + "/deviceDelete", request);

        return deviceMapper.update(
                new UpdateWrapper<TDevice>()
                        .eq(TDevice.USER_ID, payload.getUser_id())
                        .eq(TDevice.DEVICE_ID, request.device_id())
                        .set(TDevice.DELETED, 1)
        ) >= 1;

    }

    @Override
    public TDevice getDeviceByUUID(String deviceUUID) {
        LambdaQueryWrapper<TDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TDevice::getDevice_uuid, deviceUUID)
                .eq(TDevice::getDeleted, 0);
        return baseMapper.selectOne(queryWrapper);
    }


}
