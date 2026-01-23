package bgu.spl.net.impl.srv; 

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import bgu.spl.net.impl.data.Database;
import bgu.spl.net.impl.data.LoginStatus;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, Integer> subsIdTocId;
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> cIdtoHandlers;

    // TODO: deal with the case where an element is removed
    //       while it's connectionId is still in connections
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> channelTosubId;
    private BiFunction<T, Integer, T> appendId;
    private AtomicInteger nextClientId;
    private final Database db;

    public ConnectionsImpl(BiFunction<T, Integer, T> applyId) {
            appendId        = applyId;
            subsIdTocId     = new ConcurrentHashMap<>();
            channelTosubId  = new ConcurrentHashMap<>();
            cIdtoHandlers   = new ConcurrentHashMap<>();
            nextClientId    = new AtomicInteger();
            db              = Database.getInstance();
    }

    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = cIdtoHandlers.get(connectionId);
        if (handler == null) return false;

        handler.send(msg);

        return true;
    }

    public boolean sendAndClose(int connectionId, T msg) {
        ConnectionHandler<T> handler = cIdtoHandlers.get(connectionId);
        if (handler == null) return false;

        handler.sendAndClose(msg);
        disconnect(connectionId);

        return true;
    }

    public void send(String channel, T msg) {
        ConcurrentLinkedQueue<Integer> subIds = channelTosubId.get(channel);

        if (subIds == null) return;

        subIds.removeIf(x -> !subsIdTocId.containsKey(x));

        for (int subId : subIds)
            sendWithId(subId, msg);
    }

    private void sendWithId(int subscriptionId, T msg) {
        Integer cId =  subsIdTocId.get(subscriptionId);
        if (cId == null) return;

        ConnectionHandler<T> handler = cIdtoHandlers.get(cId);
        if (handler == null) return;

        handler.send(appendId.apply(msg, subscriptionId));
    }

    public void disconnect(int clientId) {
        if (cIdtoHandlers.remove(clientId) != null) {
            subsIdTocId.values().removeIf(cId -> cId == clientId);
            db.logout(clientId);
        }
    }

    @Override
    public int register(ConnectionHandler<T> handler) {
        int cid = nextClientId.getAndIncrement();
        cIdtoHandlers.put(cid, handler);

        return cid;
    }

    @Override
    public boolean subscribe(int connectionId, int subId, String topic) {
        // Either already subscribed, or using an existing subId.
        if (isSubscribed(connectionId, topic) || subsIdTocId.containsKey(subId))
            return false;

        subsIdTocId.put(subId, connectionId);
        channelTosubId.putIfAbsent(topic, new ConcurrentLinkedQueue<>());
        channelTosubId.get(topic).add(subId);
        return true;
    }

    @Override
    public boolean isSubscribed(int connectionId, String topic) {
        ConcurrentLinkedQueue<Integer> subIds = channelTosubId.get(topic);

        if (subIds == null) return false;

        subIds.removeIf(x -> !subsIdTocId.containsKey(x));
        for(int subId : subIds)
            if (subsIdTocId.get(subId) == connectionId)
                return true;

        return false;
    }

    @Override
    public boolean unsubscribe(int connectionId, int subId) {
        // Either not subscribed, or trying to unsubscribe another client
        if (subsIdTocId.get(subId) != (Integer)connectionId)
            return false;

        subsIdTocId.remove(subId);
        return true;
    }

    @Override
    public LoginStatus connect(int connectionId, String login, String pass) {
        return db.login(connectionId, login, pass);
    }
}
