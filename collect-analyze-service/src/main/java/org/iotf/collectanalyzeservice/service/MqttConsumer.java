package org.iotf.collectanalyzeservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.configuration.EmqxConfig;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.time.Instant;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttConsumer {

    private final InfluxDBService influxDBService;
    private final EmqxConfig emqxConfig;
    private final MqttPahoClientFactory mqttClientFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * MQTT 消息接收通道
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT 消息适配器（订阅 EMQX 主题）
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
        String[] topics = emqxConfig.getTopics().split(",");

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        emqxConfig.getClientId(),
                        mqttClientFactory,
                        topics
                );

        adapter.setCompletionTimeout(5000);
        adapter.setQos(emqxConfig.getQos());
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setOutputChannelName("mqttInputChannel");

        return adapter;
    }

    /**
     * 消息处理器（消费 MQTT 消息并写入 InfluxDB）
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return message -> {
            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
            String payload = (String) message.getPayload();
            int qos = (int) message.getHeaders().get(MqttHeaders.RECEIVED_QOS);

            log.debug("收到 MQTT 消息: topic={}, qos={}", topic, qos);

            try {
                // 解析 JSON
                JsonNode data = objectMapper.readTree(payload);

                // 从 Topic 中提取 deviceId
                // energy/device/DEV001/data → DEV001
                String deviceUUID = extractDeviceId(topic);

                // 构造 InfluxDB 数据点
                EnergyDataPoint point = EnergyDataPoint.builder()
                        .deviceUUID(deviceUUID)
                        .deviceType(getStringOrDefault(data, "deviceType", "UNKNOWN"))
                        .current(getDoubleOrDefault(data, "current"))
                        .voltage(getDoubleOrDefault(data, "voltage"))
                        .power(getDoubleOrDefault(data, "power"))
                        .energy(getDoubleOrDefault(data, "energy"))
                        .isOn(getBooleanOrDefault(data, "isOn"))
                        .timestamp(getTimestampOrDefault(data))
                        .build();

                // 写入 InfluxDB
                influxDBService.writeData(point);

                log.info("设备数据已写入 " + point.toString());

            } catch (Exception e) {
                log.error("处理 MQTT 消息失败: topic={}, payload={}",
                        topic, payload, e);
            }
        };
    }

    /**
     * 从 MQTT Topic 中提取设备 ID
     * energy/device/DEV001/data → DEV001
     */
    private String extractDeviceId(String topic) {
        String[] parts = topic.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "unknown";
    }

    private String getStringOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText() : defaultValue;
    }

    private Double getDoubleOrDefault(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asDouble() : null;
    }

    private Boolean getBooleanOrDefault(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asBoolean() : null;
    }

    private Instant getTimestampOrDefault(JsonNode node) {
        JsonNode timestamp = node.get("timestamp");
        if (timestamp != null && !timestamp.isNull()) {
            long epochSecond = timestamp.asLong();
            return Instant.ofEpochSecond(epochSecond);
        }
        return Instant.now();
    }
}
