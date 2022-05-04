package handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import message.Message;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
//        final String[] bytes = ByteBufUtil.getBytes(msg);
        Message message = OBJECT_MAPPER.readValue(msg, Message.class);
        out.add(message);
        System.out.println("decode: " + message);
    }
}

