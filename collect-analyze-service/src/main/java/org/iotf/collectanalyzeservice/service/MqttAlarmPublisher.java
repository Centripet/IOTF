package org.iotf.collectanalyzeservice.service;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotf.entity.collect_analyze.AlarmPush;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttAlarmPublisher {

    private final MqttPahoClientFactory clientFactory;
    private MqttClient client;

    @PostConstruct
    public void init() throws MqttException {
        client = new MqttClient("tcp://localhost:41883", "alarm-publisher-" + System.currentTimeMillis());
        client.connect(clientFactory.getConnectionOptions());
    }

    /**
     * 推送告警到指定设备对应的用户
     */
    public void push(Long device_id, AlarmPush alarm) {
        String topic = "energy/" + device_id + "/alarm";
        String payload = JSON.toJSONString(alarm);

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        message.setRetained(true);  // 保留消息，离线用户上线后补推

        try {
            client.publish(topic, message);
            log.info("告警推送成功: device_id={}, alarmId={}", device_id, alarm.getAlarm_id());
        } catch (MqttException e) {
            log.error("告警推送失败: device_id={}", device_id, e);
        }
    }
}
