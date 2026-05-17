package org.iotf.entity.collect_analyze.noUse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备状态信息
 * 对应C代码中的DeviceStatus结构体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusDTO {

    /**
     * 开关状态
     */
    private Boolean isOn;

    /**
     * 电流(A)
     */
    private Double current;

    /**
     * 电压(V)
     */
    private Double voltage;

    /**
     * 功率(W)
     */
    private Double power;

    /**
     * 用电量(Wh)
     */
    private Double energy;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdate;

    /**
     * 故障状态
     */
    private Boolean isFault;
}