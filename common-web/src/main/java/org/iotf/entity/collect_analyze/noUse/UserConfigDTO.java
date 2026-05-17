package org.iotf.entity.collect_analyze.noUse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户配置DTO
 * 对应C代码中的UserConfig结构体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfigDTO {

    /**
     * 采样间隔(秒)
     */
    private Integer samplingInterval;

    /**
     * 过载阈值(W)
     */
    private Float overloadThreshold;

    /**
     * 高能耗阈值(Wh)
     */
    private Float highEnergyThreshold;

    /**
     * APP通知开关
     */
    private Boolean notifyByApp;

    /**
     * 短信通知开关
     */
    private Boolean notifyBySms;

    /**
     * MQTT服务器地址
     */
    private String mqttServer;
}