package bgu.spl.net.impl.srv; 

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

// PLAN: there's 2 types of id's in Connections, subscriptionID and clientID
// Channel Names ---hash--> SubscriptionIds ---hash--> ClientIds ---hash--> Handlers
// TODO: Should this class be involved with login/pass verification and database?

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

    // TODO: Add automatic message id generation
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

        if (subIds == null) return;

        subIds.removeIf(x -> !subsIdTocId.containsKey(x));

        for (int id : subIds)
            send(id, msg);
    }

    // FIXME: doesn't unsubscribe the client from all topics
    public void disconnect(int clientId) {
        // FIXME: Remove all subscriptions for this client
        cIdtoHandlers.remove(clientId);
    }

    @Override
    public int register(ConnectionHandler<T> handler) {
        int cid = nextClientId.getAndIncrement();
        cIdtoHandlers.put(cid, handler);

        return cid;
    }

    @Override
    public boolean subscribe(int cId, int subId, String topic) {
        if (subsIdTocId.containsKey(subId))
            return false;

        subsIdTocId.put(subId, cId);
        channelTosubId.putIfAbsent(topic, new ConcurrentLinkedQueue<>()).add(subId);
        return true;
    }

    @Override
    public boolean isSubscribed(int cId, String topic) {
        ConcurrentLinkedQueue<Integer> clients = channelTosubId.get(topic);

        return clients != null && clients.contains(cId);
    }

    @Override
    public void unsubscribe(int subId) {
        Integer cId = subsIdTocId.remove(subId);
        if (cId != null) {
            // FIXME: Should we add another map or something to optimize this?
            for (String topic : channelTosubId.keySet()) {
                ConcurrentLinkedQueue<Integer> subs = channelTosubId.get(topic);
                subs.remove(subId);
                if (subs.isEmpty()) {
                    channelTosubId.remove(topic);
                }
            }
        }
    }

    @Override
    public boolean connect(String login, String pass) {
        // TODO: verify login and pass with database?
        return true;
    }
}
