package cn.hc.iot.service;


import cn.hc.iot.util.ConcurrentHashMapUtil;
import cn.hc.iot.util.TlvBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.ext.web.client.WebClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author lyz
 * @className TcpServerVerticle
 * @description TODO
 * @date 2021-05-06 17:22
 */
public class TcpServerVerticle extends AbstractVerticle {
    private WebClient webClient = null;

    @Override
    public void start() throws Exception {
        webClient = WebClient.create(vertx);
        NetServerOptions options = new NetServerOptions();
        // 启动TCP保活，默认不保活
        options.setTcpKeepAlive(true);
        options.setIdleTimeout(60);
        //绑定处理器，当有请求进入时触发
        vertx.createNetServer(options).connectHandler(netSocket -> {
            //得到NetSocket实例
            netSocket.handler(buffer -> {
                int type = buffer.getInt(0);
                if (type == 0x40) {
                    byte[] typeBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(0x50).array();
                    netSocket.write(Buffer.buffer(typeBytes));
                    return;
                }
                if (type == 0x60) {
                    netSocket.close();
                    return;
                }
                int size = buffer.getInt(4);
                if (size > 2048 || size <= 0) {
                    return;
                }
                String payload = buffer.getString(8, 8 + size);
                if (type == 0x11) {
                    String[] split = payload.split(",");
                    String prodKey = split[0];
                    String deviceKey = split[1];
                    String deviceSecret = split[2];
                    String sign = prodKey + ":" + deviceKey + ":" + deviceSecret;
                    // 登入
                    webClient.post(config().getString("iotos.url") + config().getString("login.path"))
                            .putHeader("sign", sign)
                            .send()
                            .onSuccess(b -> {
                                ConcurrentHashMapUtil.putCache(netSocket, sign);
                                TlvBox tlvBox1 = TlvBox.create();
                                tlvBox1.put(0x20, "SUCCESSFUL");
                                netSocket.write(Buffer.buffer(tlvBox1.serialize()));
                            })
                            .onFailure(e -> {
                                TlvBox tlvBox2 = TlvBox.create();
                                tlvBox2.put(0x21, "AUTHFAILED");
                                //回复连接失败
                                netSocket.write(Buffer.buffer(tlvBox2.serialize()), ar -> {
                                    netSocket.close();
                                });
                            });
                    return;
                }
                if (type == 0x30) {
                    String v = ConcurrentHashMapUtil.getCache(netSocket);
                    if (v.equals("")) {
                        netSocket.close();
                    } else {
                        //上报
                        webClient.post(config().getString("iotos.url") + config().getString("upload.path"))
                                .putHeader("sign", v)
                                .sendBuffer(Buffer.buffer(payload));
                    }
                    return;
                }
            });

            netSocket.closeHandler(ar -> {
                String v = ConcurrentHashMapUtil.getCache(netSocket);
                if (!v.equals("")) {
                    //登出
                    webClient.post(config().getString("iotos.url") + config().getString("logout.path"))
                            .putHeader("sign", v)
                            .send()
                            .onSuccess(b -> {
                                ConcurrentHashMapUtil.removeCache(netSocket);
                            });
                }
            });
        }).listen(config().getInteger("tcp.port"), "127.0.0.1", res -> {
            if (res.succeeded()) {
                System.out.println("Tcp服务端启动成功,端口：" + res.result().actualPort());
            } else {
                System.err.println("Tcp服务端启动失败");
            }
        });
    }
}
