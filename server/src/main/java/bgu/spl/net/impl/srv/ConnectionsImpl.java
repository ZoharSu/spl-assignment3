package bgu.spl.net.impl.srv; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

// OPTIMIZE: Maybe add a hashtable from channels to lists of connections
//           Only really matters should this run poorly
public class ConnectionsImpl<T> implements Connections<T> {
    ArrayList<Connection<T>> connections;
    ConcurrentHashMap<String, LinkedList<ConnectionHandler<T>>> channels;

    private static class Connection<T> {
        public ConnectionHandler<T> handler;
        public LinkedList<String>   channels;
    }

    public boolean send(int connectionId, T msg) {
        if (connectionId >= connections.size() || connections.get(connectionId) == null)
            return false;

        ConnectionHandler<T> handler = connections.get(connectionId).handler;
        handler.send(msg);

        return true;
    }

    public void send(String channel, T msg) {
        LinkedList<ConnectionHandler<T>> handlers = channels.get(channel);

        if (handlers != null)
            for (ConnectionHandler<T> handler : handlers)
                handler.send(msg);
    }

    public void disconnect(int connectionId) {
        Connection<T> conn = connections.get(connectionId);

        if (conn == null) return;

        connections.set(connectionId, null);

        for (String channel : conn.channels)
            channels.get(channel).remove(conn.handler);

        try {
            conn.handler.close();
        } catch (IOException e) {}
    }
}
