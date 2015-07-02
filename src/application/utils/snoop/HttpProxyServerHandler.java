package application.utils.snoop;

import application.net.proxy.WebProxyRequestManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<Object> {
    private ChannelPipeline pipeline;
    private Boolean SSL = false;
    private WebProxyRequestManager webProxyRequestManager;

    public HttpProxyServerHandler(ChannelPipeline pipeline, WebProxyRequestManager webProxyRequestManager) {
        this.pipeline = pipeline;
        this.webProxyRequestManager = webProxyRequestManager;
    }

    private HttpRequest request;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpHeaderUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            System.out.println("Method " + request.method());

            if ("CONNECT".equals(request.method().toString())) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
                ctx.write(response);

                System.out.println("SWITCHING TO SSL");

                SSLEngine sslEngine = SSLContextProvider.get().createSSLEngine();
                sslEngine.setUseClientMode(false); // We are a server
                sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());

                pipeline.addFirst("ssl", new SslHandler(sslEngine));
                System.out.println("SSL READY ON THIS CHANNEL NOW");
                SSL = true;

                return;
            } else {
                //runRequest(request.uri(), "", request.method().toString());
                //System.out.println("RESPONSE " + responseSrc);
                //buf.setLength(0);
                //buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
                // buf.append("===================================\r\n");

                //buf.append(responseSrc);

                //buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
                //buf.append("HOSTNAME: ").append(request.headers().get(HOST, "unknown")).append("\r\n");
                //buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");


                HttpHeaders headers = request.headers();
                if (!headers.isEmpty()) {
                    for (Map.Entry<CharSequence, CharSequence> h : headers) {
                        CharSequence key = h.getKey();
                        CharSequence value = h.getValue();
                        //buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
                    }
                    //buf.append("\r\n");
                }

                try {
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);

                    for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                            Attribute attribute = (Attribute) data;
                            System.out.println(attribute.getName() + ":" + attribute.getValue());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("URI!!! " + request.uri() + " " + request);

                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
                Map<String, List<String>> params = queryStringDecoder.parameters();
                if (!params.isEmpty()) {
                    for (Map.Entry<String, List<String>> p : params.entrySet()) {
                        String key = p.getKey();
                        List<String> vals = p.getValue();
                        for (String val : vals) {
                            System.out.println("PARAMS " + key + " = " + val);


                            //buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                        }
                    }
                    //buf.append("\r\n");
                }

//                appendDecoderResult(buf, request);
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();
            if (content.isReadable()) {
                //buf.append("CONTENT: ");
                //buf.append(content.toString(CharsetUtil.UTF_8));
                //buf.append("\r\n");
//                appendDecoderResult(buf, request);
            }

            if (msg instanceof LastHttpContent) {
                //buf.append("END OF CONTENT\r\n");

                LastHttpContent trailer = (LastHttpContent) msg;
                if (!trailer.trailingHeaders().isEmpty()) {
                    //buf.append("\r\n");
                    for (CharSequence name : trailer.trailingHeaders().names()) {
                        for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
                            //buf.append("TRAILING HEADER: ");
                            //buf.append(name).append(" = ").append(value).append("\r\n");
                        }
                    }
                    //buf.append("\r\n");
                }

                if (!writeResponse(trailer, ctx)) {
                    // If keep-alive is off, close the connection once the content is fully written.
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }

        //System.out.println(buf.toString());
    }

//    private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
//        DecoderResult result = o.decoderResult();
//        if (result.isSuccess()) {
//            return;
//        }
//
//        buf.append(".. WITH DECODER FAILURE: ");
//        buf.append(result.cause());
//        buf.append("\r\n");
//    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);

        // Pass on the headers sent to the proxy
        HttpHeaders headers = request.headers();
        HashMap<String, String> requestHeaders = new HashMap<>();
        if (!headers.isEmpty()) {
            for (Map.Entry<CharSequence, CharSequence> h : headers) {
                CharSequence key = h.getKey();
                CharSequence value = h.getValue();
                System.out.println("REQUEST TO PROXY HEADER " + key + " = " + value);
                requestHeaders.put(key.toString(), value.toString());
                //buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
            }
            //buf.append("\r\n");
        }

        // Pass on the parameters sent by the proxy
        HashMap<String, String> requestParameters = new HashMap<>();
        try {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);

            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    requestParameters.put(attribute.getName(), attribute.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String uri = request.uri();
        if (SSL) {
            uri = "https://" + requestHeaders.get("Host") + request.uri();
        }

        StandaloneHTTPRequest standaloneHTTPRequest = new StandaloneHTTPRequest()
                .setUrl(uri)
                .setMethod(request.method().toString())
                .setRequestHeaders(requestHeaders)
                .setRequestParameters(requestParameters)
                .setHttps(SSL)
                .setRequestManager(webProxyRequestManager)
                .execute();


        // Create our response object
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(standaloneHTTPRequest.getResponse().array()));

        HashMap<String, String> responseHeaders = standaloneHTTPRequest.getResponseHeaders();
        for (String header : responseHeaders.keySet()) {
            response.headers().set(header, responseHeaders.get(header));
            System.out.println("RESPONSE FROM SERVER THEN TO CLIENT HEADER " + header + " - " + responseHeaders.get(header));
        }

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
