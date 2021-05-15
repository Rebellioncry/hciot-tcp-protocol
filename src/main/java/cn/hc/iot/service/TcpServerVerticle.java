package cn.hc.iot.service;

import cn.hc.iot.util.ConcurrentHashMapUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.client.WebClient;

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
            RecordParser parser = RecordParser.newFixed(3);
            parser.setOutput(new Handler<Buffer>() {
                short size = -1;

                @Override
                public void handle(Buffer buffer) {
                    if (-1 == size) {
                        size = buffer.getShort(1);
                        parser.fixedSizeMode(size);

                    } else {
                        byte[] buf = buffer.getBytes();
                        System.out.println(new String(buf));
//                        switch (buf[0]){
//                            case 0x11:
//                                Short len =  buffer.getShort(1);
//                                byte[] bytes = null;
//                                System.arraycopy(buf,3,bytes,0,len);
//
//                                break;
//                            case 0x30:
//                                break;
//                            case 0x40:
//                                break;
//                            case 0x60:
//                                break;
//                        }
                        parser.fixedSizeMode(3);
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
                System.out.println("Tcp服务端启动成功,端口："+res.result().actualPort());
            } else {
                System.err.println("Tcp服务端启动失败");
            }
        });
    }
}
