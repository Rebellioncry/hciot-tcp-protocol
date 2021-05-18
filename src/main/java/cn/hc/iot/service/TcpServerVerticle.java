package cn.hc.iot.service;


import cn.hc.iot.util.ConcurrentHashMapUtil;
import cn.hc.iot.util.TlvBox;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.HexUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.client.WebClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyz
 * @className TcpServerVerticle
 * @description TODO
 * @date 2021-05-06 17:22
 */
public class TcpServerVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        NetServerOptions options = new NetServerOptions();
        // 启动TCP保活，默认不保活
        options.setTcpKeepAlive(true);
        options.setIdleTimeout(60);
        options.isTcpQuickAck();
        //绑定处理器，当有请求进入时触发
        vertx.createNetServer(options).connectHandler(netSocket -> {
            // 构造parser
            RecordParser parser = RecordParser.newFixed(8);
            parser.setOutput(new Handler<Buffer>() {
                int size = -1;
                String type = "";
                String[] types = new String[]{"11","30","40","60"};
                List<String> list= Arrays.asList(types);

                @Override
                public void handle(Buffer buffer) {
                    if (-1 == size) {
                        type = HexUtil.toHex(buffer.getInt(0));
                        if (!list.contains(type)){
                            return;
                        }
                        size = buffer.getInt(4);
                        if (size>2048){
                            return;
                        }
                        parser.fixedSizeMode(size);

                    } else {
                        byte[] buf = buffer.getBytes();
                        if (buf.length > 2048) {
                            return;
                        }
                        switch (type) {
                            case "11":
                                String payload = new String(buf);
                                String[] split = payload.split(",");
                                String prodKey = split[0];
                                String deviceName = split[1];
                                // 创建WebClient，用于发送HTTP或者HTTPS请求
                                WebClient webClient = WebClient.create(vertx);
                                // 构造请求的数据
                                JsonObject data = new JsonObject()
                                        .put("prodKey", prodKey)
                                        .put("deviceName", deviceName);
                                // 以post方式请求远程地址
                                webClient.postAbs(config().getString("iotos.url")).sendJsonObject(data, handle -> {
                                    // 处理响应的结果
                                    if (handle.succeeded()) {
                                        System.out.println(handle.result().bodyAsJsonObject());
                                        ConcurrentHashMapUtil.putCache(prodKey + deviceName, netSocket);
                                        TlvBox tlvBox1 = TlvBox.create();
                                        tlvBox1.put(0x20,"SUCCESSFUL");
                                        netSocket.write(Buffer.buffer(tlvBox1.serialize()), ar -> {
                                            if (ar.succeeded()) {
                                                System.out.println("响应成功!");
                                            } else {
                                                System.err.println("响应失败!");
                                            }
                                        });
                                    } else {
                                        TlvBox tlvBox2 = TlvBox.create();
                                        tlvBox2.put(0x21,"AUTHFAILED");
                                        //回复连接失败
                                        netSocket.write(Buffer.buffer(tlvBox2.serialize()), ar -> {
                                            netSocket.close();
                                        });
                                    }
                                });
                                break;
                            case "30":
                                //上报
                                break;
                            case "40":
                                byte[] typeBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(0x50).array();
                                netSocket.write(Buffer.buffer(typeBytes));
                                break;
                            case "60":
                                netSocket.close();
                                break;
                        }

                        parser.fixedSizeMode(8);
                        size = -1;
                    }
                }
            });
            netSocket.handler(parser);

//            //得到NetSocket实例
//            netSocket.handler(buffer -> {
//                //读取数据
//                System.out.println("读取到数据:" + buffer.toString() + " 长度为: " + buffer.length());
//                //验证设备
//                if (buffer.getBytes()[0]==0x11){
//                    // 创建WebClient，用于发送HTTP或者HTTPS请求
//                    WebClient webClient = WebClient.create(vertx);
//                    // 构造请求的数据
//                    JsonObject data = new JsonObject()
//                            .put("deviceKey", "admin")
//                            .put("deviceSecret", "admin123");
//                    // 以post方式请求远程地址
//                    webClient.postAbs(config().getString("iotos.url")).sendJsonObject(data,handle -> {
//                        // 处理响应的结果
//                        if (handle.succeeded()) {
//                            System.out.println(handle.result().bodyAsJsonObject());
//                            ConcurrentHashMapUtil.putCache("1111",netSocket);
//                        }else {
//                            //回复连接失败
//                            netSocket.write(Buffer.buffer("验证失败......"),ar->{
//                                netSocket.close();
//                            });
//                        }
//                    });
//                }
//                //上报数据
//                if (buffer.getBytes(0,1).toString().equals("0x11")){
//                    //转mq,kafka,http等
//                }
//            });

//            netSocket.write(Buffer.buffer("数据已接收......"), ar -> {
//                if (ar.succeeded()) {
//                    System.out.println("写入数据成功!");
//                } else {
//                    System.err.println("写入数据失败!");
//                }
//            });
            netSocket.closeHandler(ar -> {
//                ConcurrentHashMap<String, NetSocket> all = ConcurrentHashMapUtil.all();
//                System.out.println("客户端退出连接");
//                for (Map.Entry<String, NetSocket> entry:all.entrySet()) {
//                    if (entry.getValue().equals(netSocket)){
//                        ConcurrentHashMapUtil.removeCache(entry.getKey());
//                    }
//                }
            });
        }).listen(config().getInteger("tcp.port", 9984), "127.0.0.1", res -> {
            if (res.succeeded()) {
                System.out.println("Tcp服务端启动成功,端口：" + res.result().actualPort());
            } else {
                System.err.println("Tcp服务端启动失败");
            }
        });
    }
}
