package cn.hc.iot;

import cn.hc.iot.service.TcpServerVerticle;
import cn.hc.iot.util.ConcurrentHashMapUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class ServerApplication {

    public static void main(String[] args) throws InterruptedException {
        Vertx.vertx().deployVerticle(new TcpServerVerticle());
        Thread.sleep(60000);
        ConcurrentHashMapUtil.getCache("1111").write(Buffer.buffer("睡醒了"));
    }
}
