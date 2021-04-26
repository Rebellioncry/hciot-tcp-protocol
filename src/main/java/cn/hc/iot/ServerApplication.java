package cn.hc.iot;

import cn.hc.iot.util.ConcurrentHashMapUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

public class ServerApplication extends AbstractVerticle {



    public static void main(String[] args) throws InterruptedException {
        Vertx.vertx().deployVerticle(new ServerApplication());
        Thread.sleep(60000);
        ConcurrentHashMapUtil.getCache("1111").write(Buffer.buffer("睡醒了"));
    }

    @Override
    public void start() throws Exception {

        //绑定处理器，当有请求进入时触发
        NetServer netServer = vertx.createNetServer().connectHandler(netSocket -> {

            //得到NetSocket实例
            netSocket.handler(buffer -> {
                ConcurrentHashMapUtil.putCache("1111",netSocket);
                //读取数据
                System.out.println("读取到数据:" + buffer.toString() + " 长度为: " + buffer.length());
            });

            netSocket.write(Buffer.buffer("数据已接收......"), ar -> {
                if (ar.succeeded()) {
                    System.out.println("写入数据成功!");
                } else {
                    System.err.println("写入数据失败!");
                }
            });
            netSocket.closeHandler(ar -> {
                System.out.println("客户端退出连接");
            });
        }).listen(9984, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("Tcp服务端启动成功");
            } else {
                System.err.println("Tcp服务端启动失败");
            }
        });
    }
}
