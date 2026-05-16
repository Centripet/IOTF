package org.iotf.collectanalyzeservice.mqtt;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

/**
 * MQTT通道配置类
 * 配置MQTT消息接收通道和消息驱动适配器
 */
@Configuration
@RequiredArgsConstructor
public class MqttChannelConfig {

    private final MqttPahoClientFactory mqttClientFactory;

    /**
     * 配置MQTT输入通道
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 配置MQTT消息驱动适配器
     * 订阅设备数据上报主题
     */
    @Bean
    public MessageProducer inbound() {
        // 订阅多个主题：设备数据上报和设备状态变更
        String[] topics = {
                "energy/device/+/data",        // 设备能耗数据上报
                "energy/device/+/status",      // 设备状态变更
                "energy/device/+/event"        // 设备事件上报
        };

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "collect-analyze-consumer",
                mqttClientFactory,
                topics
        );

        // 设置消息转换器
        adapter.setConverter(new DefaultPahoMessageConverter());
        // 设置QoS级别
        adapter.setQos(1);
        // 设置消息通道
        adapter.setOutputChannel(mqttInputChannel());

        return adapter;
    }
}