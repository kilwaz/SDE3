package application.net.proxy.snoop;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketDetector extends ByteToMessageDecoder {
    private static Logger log = Logger.getLogger(WebSocketDetector.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() < 1) return;

        byte[] inputBytes;
        int offset = 0;
        int length = byteBuf.readableBytes();
        if (byteBuf.hasArray()) {
            inputBytes = byteBuf.array();
            offset = byteBuf.arrayOffset() + byteBuf.readerIndex();
        } else {
            inputBytes = new byte[length];
            byteBuf.getBytes(byteBuf.readerIndex(), inputBytes);
        }

        String data = new String(inputBytes, "UTF-8");
        Matcher get = Pattern.compile("^GET").matcher(data);

        log.info("Checking new decoder");
        if (get.find()) {
            log.info("This had a GET message");
            Matcher key = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            if (key.find()) {
                log.info("Web socket connect detected");
                enableWebSocket(ctx);
                return;
            }
        }

        // Pass the buffer onto the next handler
        byteBuf.resetReaderIndex();
        out.add(byteBuf.readBytes(byteBuf.readableBytes()));
    }

    private void enableWebSocket(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();

        // Setup the pipeline to handle the web socket handshake
        pipeline.remove("unifiedDetectorHttp");
        pipeline.addLast("wshandler", new WebSocketProxyServerHandler());
        pipeline.remove(this);
    }
}
