package cn.hc.iot;

import cn.hc.iot.service.TcpServerVerticle;
import cn.hc.iot.util.ConcurrentHashMapUtil;
import cn.hc.iot.util.TlvBox;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class ServerApplication {

    public static void main(String[] args) throws InterruptedException {
        Vertx.vertx().deployVerticle(new TcpServerVerticle());
        Thread.sleep(10000);
        TlvBox tlvBox2 = TlvBox.create();
        tlvBox2.put(0x11,"abcd,abcd");
        System.out.println(tlvBox2.serialize().toString());
        //ConcurrentHashMapUtil.getCache("1111").write(Buffer.buffer(tlvBox2.serialize()));
    }
}
