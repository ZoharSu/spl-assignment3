package bgu.spl.net.impl.srv; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {
    private ArrayList<Connection<T>> connections;
    private ConcurrentHashMap<String, LinkedList<ConnectionHandler<T>>> channels;
    private BiFunction<T, Integer, T> appendId;

    private static class Connection<T> {
        public ConnectionHandler<T> handler;
        public LinkedList<String>   channels;
    }

    public boolean send(int connectionId, T msg) {
        Connection<T> conn = connectionId < connections.size()
                           ? connections.get(connectionId) : null;
                           
        if (conn == null)
            return false;

        conn.handler.send(msg);

        return true;
    }

    public void send(String channel, T msg) {
        LinkedList<ConnectionHandler<T>> handlers = channels.get(channel);

        if (handlers != null)
            for (ConnectionHandler<T> handler : handlers)
                handler.send(msg);
    }

    public void disconnect(int connectionId) {
        if (connectionId >= connections.size())
            return; // TODO:

        Connection<T> conn = connections.get(connectionId);

        if (conn == null) return;

        connections.set(connectionId, null);

        for (String channel : conn.channels)
            channels.get(channel).remove(conn.handler);

        try {
            conn.handler.close();
        } catch (IOException e) {}
    }

    @Override
    public boolean connect(int connectionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }
}
