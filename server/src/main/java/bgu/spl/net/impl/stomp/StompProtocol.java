package bgu.spl.net.impl.stomp;

import java.util.stream.Stream;

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
        int lineEnd;
        // TODO: Make sure message ends with null character
        if (message == null || (lineEnd = message.indexOf('\n')) == -1) {
            // Handle invalid message
            return;
        }

        String command = message.substring(0, lineEnd);

        switch (command) {
            case "CONNECT":     handleConnect(message);     break;
            case "SEND":        handleSend(message);        break;
            case "SUBSCRIBE":   handleSubscribe(message);   break;
            case "UNSUBSCRIBE": handleUnsubscribe(message); break;
            case "DISCONNECT":  handleDisconnect(message);  break;
            default:
                // Handle invalid message
                break;
        }
        // Send RECIEPT accordingly
    }

    private boolean isLoggedIn() {
        return loggedIn;
    }

    private void handleConnect(String message) {
        if (isLoggedIn()) {
            // Handle already logged in
            return;
        }
        // TODO:
        // Confirm with database
        // Login
        // Send CONNECTED frame
    }

    private void handleSend(String message) {
        if (!isLoggedIn()) {
            // Handle not logged in
            return;
        }
    }

    private void handleSubscribe(String message) {
        if (!isLoggedIn()) {
            // Handle not logged in
            return;
        }

        Integer id  = null;
        String dest = null;

        Stream<String> s = message.lines().skip(1); // Skip the command

        while (s.findFirst().isPresent()) {
            String line = s.findFirst().get();
            s = s.skip(1);

            String[] parts = line.split(":");

            if (parts.length != 2) {
                parseError(message, "Invalid line\n");
                return;
            }

            if (parts[0].equals("destination") && dest == null)
                dest = parts[1];
            else if (parts[0].equals("id") && id == null)
                id = Integer.parseInt(parts[1]);
            else {
                parseError(message, "Invalid line\n");
                return;
            }
        }

        if (id == null || dest == null) {
            parseError(message, "Not enough headers");
            return;
        }

        connections.subscribe(clientId, id, dest);
    }

    private void handleUnsubscribe(String message) {
        if (!isLoggedIn()) {
            // Handle not logged in
            return;
        }
    }

    private void handleDisconnect(String message) {
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

        return msg + "\r\nsubscription:" + id + '\u0000';
    }

    private void parseError(String msg, String what) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented");
    }
}
