package cn.hc.iot.service;

import cn.hc.iot.util.ConcurrentHashMapUtil;
import cn.hc.iot.util.TlvBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

import java.util.UUID;
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
        mqttClientOptions.setClientId(UUID.randomUUID().toString());

        MqttClient client = MqttClient.create(vertx,mqttClientOptions);
        client.connect(config().getInteger("mqtt.port"), config().getString("mqtt.host"), s -> {
            if (s.succeeded()){
                client.publishHandler(msg->{
                    String topic = msg.topicName();
                    String[] split = topic.split("/");
                    String prodKey = split[4];
                    String deviceKey = split[5];
                    ConcurrentHashMap<String,NetSocket > all = ConcurrentHashMapUtil.all();
                    all.forEach((k,v)->{
                        if (k.contains(prodKey+":"+deviceKey)){
                            TlvBox tlvBox = TlvBox.create();
                            tlvBox.put(0x70,msg.payload().getBytes());
                            v.write(Buffer.buffer(tlvBox.serialize())).onSuccess(r->{
                                System.out.println("下发成功");
                            }).onFailure(r->{
                                System.out.println("下发失败");
                            });
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
