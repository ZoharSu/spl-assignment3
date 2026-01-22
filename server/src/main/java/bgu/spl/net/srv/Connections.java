package bgu.spl.net.srv;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

    boolean sendAndClose(int connectionId, T msg);

    int register(ConnectionHandler<T> handler);

    boolean subscribe(int cId, int subId, String topic);

    boolean unsubscribe(int connectionId, int subId);

    boolean isSubscribed(int cId, String topic);

    boolean connect(int connectionId, String login, String pass);
}
