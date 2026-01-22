package bgu.spl.net.impl.stomp;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompProtocol implements StompMessagingProtocol<String> {

    private Connections<String> connections;
    
    private volatile boolean shouldTerminate = false;
    private volatile boolean loggedIn = false;
    private volatile int connectionId;

    private static final AtomicInteger nextMessageId = new AtomicInteger();

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(String message) {
        // protocol should refuse any work if terminated
        if (shouldTerminate()) return;

        StompParser p = new StompParser(message);

        if (!p.islegal()) {
            sendError(message, p.receipt, p.errMsg);
            return;
        }

        if (!p.isConnect() && !isLoggedIn()) {
            sendError(message, p.receipt, "You are not logged in");
            return;
        }

        if (p.isSend() && !connections.isSubscribed(connectionId, p.dest)) {
            sendError(message, p.receipt, "You cannot send a message in a topic which you're not subscribed to");
            return;
        }

        switch (p.t) {
            case SUBSCRIBE:
                handleSubscribe(p); break;
            case UNSUBSCRIBE:
                handleUnsubscribe(p); break;
            case SEND:
                handleSend(p); break;
            case DISCONNECT:
                handleDisconnect(p); break;
            case CONNECT:
                handleConnect(p); break;
        }
    }

    private void sendReceipt(String receipt) {
        if (receipt == null || receipt.isEmpty()) return;

        StringBuilder msg = new StringBuilder();
        msg.append("RECEIPT\nreceipt-id:").append(receipt).append("\n\n");
        send(msg.toString());
    }

    private void handleSubscribe(StompParser p) {
        if (connections.subscribe(connectionId, p.id, p.dest)) {
            sendReceipt(p.receipt);
            return;
        }

        // Already subscribed or subscription id already in use.
        sendError(p.message, p.receipt, "Already subscribed or subscription id already in use");
    }

    private void handleUnsubscribe(StompParser p) {
        if (connections.unsubscribe(connectionId, p.id)) {
            sendReceipt(p.receipt);
            return;
        }

        // Either not subscribed, or trying to unsubscribe another client.
        sendError(p.message, p.receipt, "Either not subscribed, or trying to unsubscribe another client");
    }

    private void handleSend(StompParser p) {
        StringBuilder msg = new StringBuilder();
        msg.append("MESSAGE\n");
        msg.append("destination:").append(p.dest).append("\n");
        msg.append("message-id:").append(nextMessageId.getAndIncrement());
        msg.append("\n\n").append(p.body).append("\n");

        connections.send(p.dest, msg.toString());
        sendReceipt(p.receipt);
    }

    private void handleDisconnect(StompParser p) {
        if (!isLoggedIn()) {
            sendError(p.message, p.receipt, "Can't disconnect while logged out");
            return;
        }

        loggedIn = false;
        sendReceipt(p.receipt);
        connections.disconnect(connectionId);
    }

    private void handleConnect(StompParser p) {
        loggedIn = connections.connect(connectionId, p.login, p.pass);

        if (!loggedIn) {
            sendError(p.message, p.receipt, "Wrong login or passcode");
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("CONNECTED\nversion:1.2\n\n");

        send(msg.toString());
        sendReceipt(p.receipt);
    }

    private void send(String msg) {
        connections.send(connectionId, msg);
    }

    private boolean isLoggedIn() {
        return loggedIn;
    }


    @Override
    public String applyId(String msg, int id) {
        int lineEnd;
        if (msg == null || (lineEnd = msg.indexOf('\n')) == -1)
            return "";

        String command = msg.substring(0, lineEnd);
        if (command.equals("MESSAGE"))
            return command + "\n" + "subscription:" + id + msg.substring(lineEnd) ;

        return msg;
    }

    private void sendError(String msg, String receipt, String what) {
        StringBuilder error = new StringBuilder();
        error.append("ERROR\nmessage:").append(what).append("\n");

        if (receipt != null && !receipt.isEmpty())
            error.append("receipt-id:").append(receipt).append("\n");

        error.append("\n");
        if (msg != null)
            error.append("The message:\n-----\n").append(msg).append("\n-----");

        connections.sendAndClose(connectionId, error.toString());
        loggedIn = false;
        shouldTerminate = true;
    }

}
