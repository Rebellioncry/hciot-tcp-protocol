package cn.hc.iot;

import cn.hc.iot.service.MqttClientVerticle;
import cn.hc.iot.service.TcpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

/**
 * @author lyz
 * @className MainVerticle
 * @description TODO
 * @date 2021-05-21 14:57
 */
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config());
        vertx.deployVerticle(new TcpServerVerticle(),deploymentOptions);
        vertx.deployVerticle(new MqttClientVerticle(),deploymentOptions);
    }
}
