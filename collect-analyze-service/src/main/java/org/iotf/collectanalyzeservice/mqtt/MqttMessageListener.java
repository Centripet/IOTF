package org.iotf.collectanalyzeservice.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.service.EnergyDataService;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import org.iotf.entity.collect_analyze.EnergyDataDTO;
import org.iotf.entity.collect_analyze.TDevice;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MQTT消息监听器
 * 负责接收设备上报的能耗数据并转发给数据处理服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttMessageListener {

    private final EnergyDataService energyDataService;
    private final ObjectMapper objectMapper;
    private final ITDeviceService deviceService;

    // MQTT主题正则表达式，用于提取设备UUID
    private static final Pattern TOPIC_PATTERN = Pattern.compile("energy/device/([^/]+)/data");

    /**
     * test:
     * energy/device/device-001/data
     *{
     *   "deviceId": "111",
     *   "deviceUUID": "device-001",
     *   "timestamp": "2026-05-18T10:30:00",
     *   "current": 5.42,
     *   "voltage": 220.0,
     *   "power": 1192.4,
     *   "energy": 19.87,
     *   "isComplete": true,
     *   "deviceType": "AIRCON",
     *   "location": "living-room",
     *   "isOn": true,
     *   "isFault": false,
     *   "commType": "WIFI",
     *   "totalEnergy": 128.6
     * }
     * from(bucket: "energy")
     *   |> range(start: -10m)
     *   |> filter(fn: (r) => true)
     * 监听设备数据上报主题
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleDeviceData(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = getPayloadAsString(message.getPayload());

            log.debug("收到MQTT消息: topic={}, payload={}", topic, payload);

            // 从主题中提取设备UUID
            String deviceUUID = extractDeviceUUID(topic);
            if (deviceUUID == null) {
                log.warn("无法从主题中提取设备UUID: {}", topic);
                return;
            }

            // 解析JSON消息为EnergyDataDTO
            EnergyDataDTO dataDTO = parseMessage(payload);
            if (dataDTO == null) {
                log.warn("消息解析失败: {}", payload);
                return;
            }

            // 设置设备UUID和时间戳
            if (dataDTO.getDeviceUUID() == null) {
                dataDTO.setDeviceUUID(deviceUUID);
            }
            if (dataDTO.getDeviceId() == null) {
                dataDTO.setDeviceId(deviceUUID);
            }
            if (dataDTO.getTimestamp() == null) {
                dataDTO.setTimestamp(System.currentTimeMillis() / 1000);
            }

            TDevice device = deviceService.getDeviceByUUID(deviceUUID);
            if (device != null) {
                dataDTO.setDevice_id(device.getDevice_id());
                dataDTO.setUser_id(device.getUser_id());
                dataDTO.setDevice_type(device.getDevice_type());
                dataDTO.setDeviceType(device.getDevice_type());
                dataDTO.setDevice_name(device.getDevice_name());
                dataDTO.setLocation(device.getLocation());
                dataDTO.setLocation_pg(device.getLocation());
                // 处理上报数据
//                System.out.println(dataDTO);
                energyDataService.processReportedData(dataDTO);
            } else {
                log.warn("Device metadata not found, skip reported data: deviceUUID={}", deviceUUID);
            }

        } catch (Exception e) {
            log.error("处理MQTT消息失败: {}", e.getMessage(), e);
        }
    }

    private String getPayloadAsString(Object payload) {
        if (payload == null) {
            return null;
        }
        if (payload instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return payload.toString();
    }

    /**
     * 从MQTT主题中提取设备UUID
     * @param topic MQTT主题
     * @return 设备UUID
     */
    private String extractDeviceUUID(String topic) {
        if (topic == null) {
            return null;
        }
        Matcher matcher = TOPIC_PATTERN.matcher(topic);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析MQTT消息负载为EnergyDataDTO
     * @param payload 消息负载（JSON格式）
     * @return EnergyDataDTO对象
     */
    private EnergyDataDTO parseMessage(String payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(payload, EnergyDataDTO.class);
        } catch (Exception e) {
            log.warn("JSON解析失败: {}", e.getMessage());
            return tryParseSimpleFormat(payload);
        }
    }

    /**
     * 尝试解析简单格式的消息（兼容C代码格式）
     * 格式: ID:{deviceId},TIME:{timestamp},CURR:{current},VOLT:{voltage},POWER:{power},ENERGY:{energy}
     */
    private EnergyDataDTO tryParseSimpleFormat(String payload) {
        try {
            EnergyDataDTO dto = new EnergyDataDTO();
            String[] parts = payload.split(",");
            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key.toUpperCase()) {
                    case "ID":
                        dto.setDeviceId(value);
                        break;
                    case "TIME":
                        dto.setTimestamp(System.currentTimeMillis() / 1000);
                        break;
                    case "CURR":
                        dto.setCurrent(Double.parseDouble(value));
                        break;
                    case "VOLT":
                        dto.setVoltage(Double.parseDouble(value));
                        break;
                    case "POWER":
                        dto.setPower(Double.parseDouble(value));
                        break;
                    case "ENERGY":
                        dto.setEnergy(Double.parseDouble(value));
                        break;
                }
            }
            dto.setIsComplete(true);
            dto.setTimestamp(System.currentTimeMillis() / 1000);
            return dto;
        } catch (Exception e) {
            log.warn("简单格式解析失败: {}", e.getMessage());
            return null;
        }
    }
}
