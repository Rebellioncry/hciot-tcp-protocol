package cn.hc.iot.service;

import cn.hc.iot.util.ConcurrentHashMapUtil;
import cn.hc.iot.util.TlvBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyz
 * @className MqttClientVerticle
 * @description TODO
 * @date 2021-05-20 16:18
 */
public class MqttClientVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        MqttClientOptions mqttClientOptions = new MqttClientOptions();
        mqttClientOptions.setUsername(config().getString("mqtt.username"));
        mqttClientOptions.setPassword(config().getString("mqtt.password"));
        mqttClientOptions.setClientId("kkkkkkkkkkkkkkkkkkkkkk");

        MqttClient client = MqttClient.create(vertx,mqttClientOptions);
        client.connect(config().getInteger("mqtt.port"), config().getString("mqtt.host"), s -> {
            if (s.succeeded()){
                client.publishHandler(msg->{
                    String topic = msg.topicName();
                    String[] split = topic.split("/");
                    String prodKey = split[4];
                    String deviceKey = split[5];
                    ConcurrentHashMap<NetSocket, String> all = ConcurrentHashMapUtil.all();
                    all.forEach((k,v)->{
                        if (v.contains(prodKey+":"+deviceKey)){
                            TlvBox tlvBox = TlvBox.create();
                            tlvBox.put(0x70,msg.payload().getBytes());
                            k.write(Buffer.buffer(tlvBox.serialize()));
                        }
                    });
                }).subscribe(config().getString("mqtt.topic"), 0,r->{
                    if (r.succeeded()){
                        System.out.println("订阅成功");
                    }else {
                        System.out.println("订阅失败");
                    }
                });
                System.out.println("mqtt连接成功");
            }else {
                System.out.println("mqtt连接失败");
            }
        });
    }
}
