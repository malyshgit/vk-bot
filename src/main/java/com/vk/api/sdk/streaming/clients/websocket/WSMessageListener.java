package com.vk.api.sdk.streaming.clients.websocket;

import com.google.gson.Gson;
import com.vk.api.sdk.streaming.clients.StreamingEventHandler;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;

/**
 * Listener for messages
 */
public class WSMessageListener implements WebSocketListener {

    private StreamingEventHandler handler;
    private Gson gson;


    public WSMessageListener(Gson gson, StreamingEventHandler handler) {
        this.handler = handler;
        this.gson = gson;
    }

    public void onTextFrame(String message, boolean finalFragment, int rsv) {
        handler.handle(gson.fromJson(message, StreamingCallbackMessage.class));
    }

    @Override
    public void onOpen(WebSocket websocket) {
        System.out.println("Open websocket" + websocket);
    }

    public void onClose(WebSocket websocket) {
        System.out.println("Close websocket" + websocket);
    }

    public void onClose(WebSocket websocket, int code, String reason) {
        System.out.println("Close websocket" + websocket);
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Websocket error " + t);
    }
}

