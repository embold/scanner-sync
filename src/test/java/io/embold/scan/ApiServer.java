package io.embold.scan;


import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Deque;

/**
 * Server to simulate APIs for unit tests
 */
public class ApiServer {
    private static final String SEP = File.separator;
    private static final String RESOURCE_DIR = System.getProperty("user.dir") + SEP + "src" + SEP + "test" + SEP
            + "resources";

    private Undertow server;

    public void start() {
        BlockingHandler handler = new BlockingHandler(new HttpHandler() {
            @Override
            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                Deque<String> cs = exchange.getQueryParameters().get("checksum");
                String checksum = null;
                if (cs != null && cs.size() > 0) {
                    checksum = cs.getFirst();
                }

                if (StringUtils.equals(checksum, "TESTCHECKSUM")) {
                    // Return 204
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.setStatusCode(204);
                    String response = "{\n" +
                            "                                    statusCode: 204,\n" +
                            "                                    statusMessage: 'No content changed or build properties file checksum looks same.'\n" +
                            "                                }";
                    exchange.getResponseSender().send(response);
                } else {
                    String file = "test.tar.gz";

                    if(!exchange.getRequestPath().endsWith("corona")) {
                        String module = StringUtils.substringAfterLast(exchange.getRequestPath(), "/");
                        file = module + ".tar.gz";
                    }
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
                    exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, file);
                    exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, new File(RESOURCE_DIR + SEP + file).length());


                    try (RandomAccessFile aFile = new RandomAccessFile(RESOURCE_DIR + SEP + file, "r")) {

                        try (FileChannel inChannel = aFile.getChannel()) {
                            long fileSize = inChannel.size();
                            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
                            inChannel.read(buffer);
                            buffer.flip();
                            exchange.getResponseSender().send(buffer);
                        }
                    }

                    exchange.endExchange();
                }
            }
        });

        server = Undertow.builder()
                .addHttpListener(35444, "localhost")
                .setHandler(handler).build();

        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}