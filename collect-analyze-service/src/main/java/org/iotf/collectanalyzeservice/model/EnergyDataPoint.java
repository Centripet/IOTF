package org.iotf.collectanalyzeservice.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Measurement(name = "device_energy_raw")
public class EnergyDataPoint {

    // ========== 标签字段（用于查询分组） ==========

    @Column(tag = true)
    private String deviceUUID;      // 设备ID (对应C代码的deviceId)

    @Column(tag = true)
    private String deviceType;      // 设备类型 (LIGHT/AIRCON/REFRIGERATOR/TV/WATER_HEATER/OTHER)

    @Column(tag = true)
    private String location;        // 新增：安装位置 (客厅/卧室/厨房等)

    @Column(tag = true)
    private String commType;        // 新增：通讯方式 (WIFI/BLUETOOTH/ZIGBEE)

    // ========== 字段（时序数据） ==========

    @Column
    private Double current;         // 电流(A) - 对应C代码current

    @Column
    private Double voltage;         // 电压(V) - 对应C代码voltage

    @Column
    private Double power;           // 功率(W) - 对应C代码power

    @Column
    private Double energy;          // 单次采样用电量(Wh) - 对应C代码energy

    @Column
    private Double totalEnergy;     // 新增：累计总用电量(Wh) - 设备累计能耗

    // ========== 状态字段 ==========

    @Column
    private Boolean isOn;           // 开关状态 - 对应C代码isOn

    @Column
    private Boolean isFault;        // 新增：故障状态 - 对应C代码isFault

    @Column
    private Boolean isComplete;     // 新增：数据完整性 - 对应C代码isComplete

    // ========== 报警字段 ==========

    @Column
    private String alarmType;       // 新增：报警类型 (OVERLOAD/HIGH_ENERGY/ELECTRIC_LEAK/DEVICE_FAULT)

    @Column
    private String alarmDescription; // 新增：报警描述

    // ========== 时间戳 ==========

    @Column(timestamp = true)
    private Instant timestamp;      // 数据时间戳 - 对应C代码timestamp

    @Column
    private Instant receivedTime;   // 新增：服务端接收时间（用于判断延迟）

    @Column
    private Instant lastUpdateTime; // 新增：设备最后更新时间 - 对应C代码lastUpdate

    /**
     * device表字段
     */
    @Column(tag = true)
    private Long user_id;

    @Column(tag = true)
    private Long device_id;

    @Column
    private String device_type;
    @Column
    private String device_name;
    @Column
    private String location_pg;


}
