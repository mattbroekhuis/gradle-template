package io.reactivex.lab.services;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.sse.ServerSentEvent;

import java.util.List;

import rx.Observable;

public abstract class MiddleTierService {

    public HttpServer<ByteBuf, ServerSentEvent> createServer(int port) {
        System.out.println("Start " + getClass().getSimpleName() + " on port: " + port);
        return RxNetty.createHttpServer(port, (request, response) -> {
            // System.out.println("Server => Request: " + request.getPath());
            try {
                return handleRequest(request, response);
            } catch (Throwable e) {
                e.printStackTrace();
                System.err.println("Server => Error [" + request.getPath() + "] => " + e);
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
                return response.writeStringAndFlush("data: Error 500: Bad Request\n" + e.getMessage() + "\n");
            }
        }, PipelineConfigurators.<ByteBuf> serveSseConfigurator());
    }

    protected abstract Observable<Void> handleRequest(HttpServerRequest<?> request, HttpServerResponse<ServerSentEvent> response);

    protected static Observable<Void> writeError(HttpServerRequest<?> request, HttpServerResponse<?> response, String message) {
        System.err.println("Server => Error [" + request.getPath() + "] => " + message);
        response.setStatus(HttpResponseStatus.BAD_REQUEST);
        return response.writeStringAndFlush("Error 500: " + message + "\n");
    }

    protected static int getParameter(HttpServerRequest<?> request, String key, int defaultValue) {
        List<String> v = request.getQueryParameters().get(key);
        if (v == null || v.size() != 1) {
            return defaultValue;
        } else {
            return Integer.parseInt(String.valueOf(v.get(0)));
        }
    }
}
