package bgu.spl.net.impl.stomp;

class StompParser {
    public MessageType t;
    public Integer id      = null;
    public Integer reciept = null;
    public String  dest    = null;
    public String  body    = null;

    public enum MessageType {
        CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, DISCONNECT
    }

    public StompParser(String msg) throws IllegalArgumentException {
        int eol = msg.indexOf("\n");
        int loc = eol;

        if (eol == -1)
            throw new IllegalArgumentException("Single line message");

        parseType(msg.substring(0, eol));

        while (eol != loc + 1) {
            eol = msg.indexOf("\n");
            if (eol == -1)
                throw new IllegalArgumentException("Invalid Message ending");

            String[] parts = msg.substring(loc, eol).split(":");
            if (parts.length != 2)
                throw new IllegalArgumentException("Illegal Header Structure");
            
            if (parts[0] == "destination" && dest == null)
                dest = parts[1];
            else if (parts[0] == "id" && id == null)
                id = Integer.parseInt(parts[1]);
            else if (parts[0] == "reciept" && reciept == null)
                reciept = Integer.parseInt(parts[1]);
            else throw new IllegalArgumentException("Invalid Header");

            loc = eol + 1;
        }

        body = msg.substring(loc, msg.length() - 1);

        ensureValid();
    }

    private void parseType(String line) throws IllegalArgumentException {
             if (line == "CONNECT")     t = MessageType.CONNECT;
        else if (line == "SEND")        t = MessageType.SEND;
        else if (line == "SUBSCRIBE")   t = MessageType.SUBSCRIBE;
        else if (line == "UNSUBSCRIBE") t = MessageType.UNSUBSCRIBE;
        else if (line == "DISCONNECT")  t = MessageType.DISCONNECT;
        else throw new IllegalArgumentException("Invalid message type");
    }

    private void ensureValid() throws IllegalArgumentException {
        switch (t) {
            // TODO: CONNECT, body checks
            case SEND:        if (dest == null || reciept != null || id != null)
                throw new IllegalArgumentException("Invalid SEND headers"); break;
            case SUBSCRIBE:   if (dest == null || id == null || reciept != null)
                throw new IllegalArgumentException("Invalid SUBSCRIBE headers"); break;
            case UNSUBSCRIBE: if (dest != null || id == null || reciept != null)
                throw new IllegalArgumentException("Invalid UNSUBSCRIBE headers"); break;
            case DISCONNECT:  if (dest != null || id != null || reciept == null)
                throw new IllegalArgumentException("Invalid UNSUBSCRIBE headers"); break;
            case CONNECT: break;
        }
    }

    public boolean isConnect() {
        return t == MessageType.CONNECT;
    }
    public boolean isSend() {
        return t == MessageType.SEND;
    }
    public boolean isSubscribe() {
        return t == MessageType.SUBSCRIBE;
    }
    public boolean isUnsubscribe() {
        return t == MessageType.UNSUBSCRIBE;
    }
    public boolean isDisconnect() {
        return t == MessageType.DISCONNECT;
    }
}
