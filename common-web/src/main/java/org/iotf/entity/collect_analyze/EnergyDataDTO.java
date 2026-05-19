package org.iotf.entity.collect_analyze;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 能耗数据DTO
 * 对应C代码中的EnergyData结构体
 * 用于设备上报数据的接收和处理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyDataDTO {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备UUID（用于MQTT主题匹配）
     */
    private String deviceUUID;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

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
     * 数据完整性标志
     */
    private Boolean isComplete;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 开关状态
     */
    private Boolean isOn;

    /**
     * 故障状态
     */
    private Boolean isFault;

    /**
     * 通讯方式
     */
    private String commType;

    /**
     * 累计用电量(Wh)
     */
    private Double totalEnergy;

    /**
     * device表字段
     */
    private Long user_id;
    private Long device_id;
    private String device_type;
    private String device_name;
    private String location_pg;
}