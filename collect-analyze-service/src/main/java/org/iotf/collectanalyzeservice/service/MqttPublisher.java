package org.iotf.collectanalyzeservice.service;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iotf.entity.collect_analyze.TAlarm;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttPublisher {

    private final MqttPahoClientFactory clientFactory;
    private MqttClient client;

    @PostConstruct
    public void init() throws MqttException {

        String brokerUrl = "tcp://localhost:41883";
        String clientId = "alarm-publisher-" + System.currentTimeMillis();

        client = new MqttClient(brokerUrl, clientId, null);
        client.connect(clientFactory.getConnectionOptions());

    }

    /**
     * backend->userApp
     */
//    public void alarmPush(AlarmPush alarm) {
//        String topic = "energy/" + alarm.getDevice_id() + "/alarm";
//        String payload = JSON.toJSONString(alarm);
//
//        MqttMessage message = new MqttMessage(payload.getBytes());
//        message.setQos(1);
//        message.setRetained(true);  // 保留消息，离线用户上线后补推
//
//        try {
//            client.publish(topic, message);
//            log.info("告警推送成功: device_id={}, alarmId={}", alarm.getDevice_id(), alarm.getAlarm_id());
//        } catch (MqttException e) {
//            log.error("告警推送失败: device_id={}", alarm.getDevice_id(), e);
//        }
//    }

    public void alarmPush(TAlarm alarm) {
        String topic = "energy/" + alarm.getDevice_id() + "/alarm";
        String payload = JSON.toJSONString(alarm);

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        message.setRetained(true);  // 保留消息，离线用户上线后补推

        try {
            client.publish(topic, message);
            log.info("告警推送成功: device_id={}, alarmId={}", alarm.getDevice_id(), alarm.getAlarm_id());
        } catch (MqttException e) {
            log.error("告警推送失败: device_id={}", alarm.getDevice_id(), e);
        }
    }

    /**
     * backend->iot
     */
    public void commonPush(String topic, Object request) {

        String payload = JSON.toJSONString(request);
        MqttMessage message = new MqttMessage(payload.getBytes());

        try {
            client.publish("energy/" + topic, message);
            log.info("推送成功:{}", request);
        } catch (MqttException e) {
            log.error("推送失败:{}", request, e);
        }

    }


}
