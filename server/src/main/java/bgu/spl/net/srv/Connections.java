package bgu.spl.net.srv;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

    int register(ConnectionHandler<T> handler);

    void subscribe(int cId, int subId, String topic);

    void unsubscribe(int subId);

    boolean isSubscribed(int cId, String topic);

    boolean connect(String login, String pass);
}
