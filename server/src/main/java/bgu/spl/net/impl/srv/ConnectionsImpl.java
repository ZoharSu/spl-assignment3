package bgu.spl.net.impl.srv; 

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> subscriptions;

    // TODO: deal with the case where an element is removed
    //       while it's connectionId is still in connections
    private ConcurrentHashMap<String, LinkedList<Integer>> connections;
    private BiFunction<T, Integer, T> appendId;

    public ConnectionsImpl(BiFunction<T, Integer, T> appendId) {
            this.appendId = appendId;
            subscriptions = new ConcurrentHashMap<>();
            connections   = new ConcurrentHashMap<>();
    }

    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = subscriptions.get(connectionId);
        if (handler == null) return false;

        handler.send(appendId.apply(msg, connectionId));

        return true;
    }

    public void send(String channel, T msg) {
        LinkedList<Integer> handlers = connections.get(channel);

        if (handlers != null)
            for (int id : handlers)
                send(id, msg);
    }

    public void disconnect(int connectionId) {
        subscriptions.remove(connectionId);
    }

    @Override
    public boolean connect(int connectionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }
}
