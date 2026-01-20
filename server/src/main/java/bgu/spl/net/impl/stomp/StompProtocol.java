package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class StompProtocol implements StompMessagingProtocol<String> {

    private Connections<String> connections;
    private ConnectionHandler<String> handler;
    private boolean shouldTerminate = false;
    private int clientId;
    private boolean loggedIn = false;

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.clientId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(String message) {

        // TODO: change p to not throw exception on invalid message, but have
        // an isLegal method
        StompParser p;

        try { p = new StompParser(message); }
        catch (IllegalArgumentException e) {
            sendError(message, null, e.getMessage());
            return;
        }

        if (!p.isConnect() && !isLoggedIn()) {
            connections.disconnect(clientId);
            sendError(message, p.receipt, "You are not logged in");
            return;
        }

        if (p.isSend() && !connections.isSubscribed(clientId, p.dest))
            sendError(message, p.receipt, "You cannot send a message in a topic which you're not subscribed to");

        switch (p.t) {
            case SUBSCRIBE:
                connections.subscribe(clientId, p.id, p.dest); break;
            case UNSUBSCRIBE:
                connections.unsubscribe(p.id); break;
            case SEND:
                connections.send(p.dest, p.body); break;
            case DISCONNECT:
                handleDisconnect(p.receipt); break;
            case CONNECT:
                handleConnect(p.login, p.pass, p.receipt); break;
        }
    }

    private void handleConnect(String login, String pass, String receipt) {
        // TODO: check username and password with database
        loggedIn = connections.connect(login, pass);

        if (!loggedIn) {
            sendError(null, receipt, "Wrong username or password");
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("CONNECTED\nversion:1.2\n");

        if (receipt != null && !receipt.isEmpty())
            msg.append("receipt-id:").append(receipt).append("\n");

        msg.append("\n");
        send(msg.toString());
    }

    private void send(String msg) {
        handler.send(msg);
    }

    private boolean isLoggedIn() {
        return loggedIn;
    }

    private void handleDisconnect(String receipt) {
        if (!isLoggedIn()) {
            sendError(null, receipt, "Can't disconnect while logged out");
            return;
        }

        connections.disconnect(clientId);
        StringBuilder msg = new StringBuilder();
        msg.append("RECEIPT\nreceipt-id:").append(receipt).append("\n\n");
        send(msg.toString());
    }

    @Override
    public String applyId(String msg, int id) {
        int lineEnd;
        if (msg == null || (lineEnd = msg.indexOf('\n')) == -1)
            return "";

        String command = msg.substring(0, lineEnd);
        if (command == "ERROR" || command == "RECIEPT")
            return msg;

        int null_i = msg.indexOf('\u0000');
        if (null_i != -1)
            msg = msg.substring(0, null_i);

        return msg + "\nsubscription:" + id + '\u0000';
    }

    private void sendError(String msg, String receipt, String what) {
        StringBuilder error = new StringBuilder();
        error.append("ERROR\nmessage:").append(what).append("\n");

        if (receipt != null && !receipt.isEmpty())
            error.append("receipt-id:").append(receipt).append("\n");

        error.append("\n");
        if (msg != null && !msg.isEmpty())
            error.append("The message:\n-----\n").append(msg).append("\n-----");

        send(error.toString());
        shouldTerminate = true;
    }

}
