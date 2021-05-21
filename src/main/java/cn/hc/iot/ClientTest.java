package cn.hc.iot;

import cn.hc.iot.util.TlvBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

public class ClientTest extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new ClientTest());
    }

    @Override
    public void start() throws Exception {

        //创建连接到指定主机和端口的客户端，并绑定创建结果的处理器
        vertx.createNetClient(new NetClientOptions().setConnectTimeout(10000))
                .connect(9984, "localhost", res -> {
                    if (res.succeeded()) {
                        System.out.println("连接成功!");
                        NetSocket socket = res.result();
                        TlvBox tlvBox2 = TlvBox.create();
                        tlvBox2.put(0x40,"abcd,abcd");
                        byte[] serialize = tlvBox2.serialize();
                        System.out.println(new String(serialize));
                        //向服务器写入数据
                        socket.write(Buffer.buffer(serialize), ar -> {
                            if (ar.succeeded()) {
                                System.out.println("数据发送成功!");
                            } else {
                                System.err.println("数据发送失败!");
                            }
                        });
//                        for (int i = 0; i < 1000; i++) {
//                        }

                        //读取服务端返回的数据
                        socket.handler(buffer -> {
                            System.out.println("读取到数据:" + buffer.toString() + " 长度为: " + buffer.length());
                        });
                        socket.closeHandler(ar -> {
                            System.out.println("客户端断开连接");
                        });
                    } else {
                        System.out.println("连接失败!: " + res.cause().getMessage());
                    }
                });
    }
}
