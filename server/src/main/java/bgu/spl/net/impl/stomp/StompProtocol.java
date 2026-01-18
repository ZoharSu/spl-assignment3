package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompProtocol implements StompMessagingProtocol<String> {

    private int clientId;
    private Connections<String> connections;
    private boolean loggedIn = false;

    @Override
    public boolean shouldTerminate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shouldTerminate'");
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.clientId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(String message) {

        StompParser p;
        try { p = new StompParser(message); }
        catch (IllegalArgumentException e) {
            sendError(message, e.getMessage()); return;
        }

         // TODO: handle not logged in
        if (!p.isConnect() && !isLoggedIn());

        if (p.isSend() && !connections.isSubscribed(clientId, p.dest))
            sendError(message, "You cannot send a message in a topic which you're not subscribed to");

        switch (p.t) {
            case SUBSCRIBE:   connections.subscribe(clientId, p.id, p.dest); break;
            case UNSUBSCRIBE: connections.disconnect(p.id); break;
            case SEND:        connections.send(p.dest, p.body); break;
            case DISCONNECT:  handleDisconnect(p.reciept); break;
            case CONNECT: break;
        }
    }

    private boolean isLoggedIn() {
        return loggedIn;
    }

    private void handleDisconnect(int reciept) {
        if (!isLoggedIn()) {
            // Handle not logged in
            return;
        }
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

    private void sendError(String msg, String what) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented");
    }
}
