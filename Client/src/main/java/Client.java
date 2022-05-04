import handler.JsonDecoder;
import handler.JsonEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import message.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

public class Client {

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {

        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            final FileRequestMessage frm = new FileRequestMessage();
                                            frm.setPath("C:\\geekbrains\\dump.zip");
                                            ctx.writeAndFlush(frm);
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws FileNotFoundException {
                                            System.out.println("receive msg " + msg);
                                            if (msg instanceof FileContentMessage) {
                                                FileContentMessage fcm = (FileContentMessage) msg;
                                                try(final RandomAccessFile accessFile = new RandomAccessFile("C:\\geekbrains\\dump2.zip", "rw")){
                                                    accessFile.seek(fcm.getStartPosition());
                                                    accessFile.write(fcm.getContent());
                                                    if (fcm.isLast()){
                                                        ctx.close();
                                                    }

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Client started");

            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();

//            while (channel.isActive()) {
//                TextMessage textMessage = new TextMessage();
//                textMessage.setText(String.format("[%s] %s", LocalDateTime.now(), Thread.currentThread().getName()));
//                System.out.println("Try to send message: " + textMessage);
//                channel.writeAndFlush(textMessage);
//
//                DateMessage dateMessage = new DateMessage();
//                dateMessage.setDate(new Date());
//                channel.write(dateMessage);
//                System.out.println("Try to send message: " + dateMessage);
//                channel.flush();
//
//                AuthMessage authMessage = new AuthMessage();
//                authMessage.setLogin("user1");
//                authMessage.setPassword("user1");
//                channel.write(authMessage);
//                System.out.println("Try to send message: " + authMessage);
//                channel.flush();
//
//                Thread.sleep(3000);
//            }

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
