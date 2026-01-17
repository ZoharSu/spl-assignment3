package bgu.spl.net.impl.srv; 

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

// PLAN: there's 2 types of id's in Connections, subscriptionID and clientID
// Channel Names ---hash--> SubscriptionIds ---hash--> ClientIds ---hash--> Handlers

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, Integer> subsIdTocId;
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> cIdtoHandlers;

    // TODO: deal with the case where an element is removed
    //       while it's connectionId is still in connections
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> channelTosubId;
    private BiFunction<T, Integer, T> appendId;
    private AtomicInteger nextClientId;

    public ConnectionsImpl(BiFunction<T, Integer, T> applyId) {
            appendId       = applyId;
            subsIdTocId    = new ConcurrentHashMap<>();
            channelTosubId = new ConcurrentHashMap<>();
            cIdtoHandlers  = new ConcurrentHashMap<>();
            nextClientId   = new AtomicInteger();
    }

    public boolean send(int connectionId, T msg) {
        Integer cId =  subsIdTocId.get(connectionId);
        if (cId == null) return false;

        ConnectionHandler<T> handler = cIdtoHandlers.get(cId);
        if (handler == null) return false;

        handler.send(appendId.apply(msg, connectionId));

        return true;
    }

    public void send(String channel, T msg) {
        ConcurrentLinkedQueue<Integer> subIds = channelTosubId.get(channel);

        if (subIds != null)
            for (int id : subIds)
                send(id, msg);
    }

    public void disconnect(int connectionId) {
        subsIdTocId.remove(connectionId);
    }

    @Override
    public int register(ConnectionHandler<T> handler) {
        int cid = nextClientId.getAndIncrement();
        cIdtoHandlers.put(cid, handler);

        return cid;
    }

}
