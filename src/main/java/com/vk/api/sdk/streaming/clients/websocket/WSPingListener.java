package com.vk.api.sdk.streaming.clients.websocket;

import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;

/**
 * Listener for ping messages
 */
public class WSPingListener implements WebSocketListener {

    private WebSocket webSocket;

    public void onOpen(WebSocket websocket) {
        webSocket = websocket;
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
