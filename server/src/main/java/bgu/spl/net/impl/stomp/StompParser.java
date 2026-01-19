package bgu.spl.net.impl.stomp;

class StompParser {
    public MessageType t;
    public Integer  id      = null;
    public String   reciept = null;
    public String   dest    = null;
    public String   body    = null;
    public String   host    = null;
    public String   login   = null;
    public String   pass    = null;
    public String   ver     = null;

    public enum MessageType {
        CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, DISCONNECT
    }

    // TODO: should change to "malformed frame recieved"?
    public StompParser(String msg) throws IllegalArgumentException {
        if (msg == null || msg.indexOf("\0") != msg.length() - 1)
            throw new IllegalArgumentException("Message must end with null character");

        int eol = msg.indexOf("\n");
        if (eol == -1)
            throw new IllegalArgumentException("Single line message");

        parseType(msg.substring(0, eol));

        // parse headers and body
        parseHeadersAndBody(msg.substring(eol+1));

        ensureValid();
    }

    private void parseHeadersAndBody(String msg) {
        // msg contains headers and body only
        int eol = msg.indexOf("\n");
        int loc = 0;
        while (eol != -1) {

            String line = msg.substring(loc, eol);

            // empty line => body starts
            if (line.isEmpty()) {
                body = msg.substring(eol + 1, msg.length() - 1);
                return;
            }

            String[] parts = msg.substring(loc, eol).split(":");
            if (parts.length != 2)
                throw new IllegalArgumentException("Illegal header structure");

            String key = parts[0];
            String value = parts[1];

            if (key.equals("destination") && dest == null)
                dest = value;
            else if (key.equals("id") && id == null)
                id = parseInt(value);
            else if (key.equals("receipt") && reciept == null)
                reciept = value;
            else if (key.equals("host") && host == null)
                host = value;
            else if (key.equals("login") && login == null)
                login = value;
            else if (key.equals("passcode") && pass == null)
                pass = value;
            else if (key.equals("accept-version") && ver == null)
                ver = value;
            else throw new IllegalArgumentException("Invalid Header");

            loc = eol + 1;
            eol = msg.indexOf("\n", loc);
        }

        if (body == null)
            throw new IllegalArgumentException("Illegal header structure");

    }

    private Integer parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer");
        }
    }

    private void parseType(String line) throws IllegalArgumentException {
             if (line.equals("CONNECT"))        t = MessageType.CONNECT;
        else if (line.equals("SEND"))           t = MessageType.SEND;
        else if (line.equals("SUBSCRIBE"))      t = MessageType.SUBSCRIBE;
        else if (line.equals("UNSUBSCRIBE"))    t = MessageType.UNSUBSCRIBE;
        else if (line.equals("DISCONNECT"))     t = MessageType.DISCONNECT;
        else throw new IllegalArgumentException("Invalid message type");
    }

    private void ensureValid() throws IllegalArgumentException {
        switch (t) {
            case CONNECT:
                if (host == null || !host.equals("stomp.cs.bgu.ac.il")
                    || login == null || pass == null || ver == null || ver != "1.2"
                    || dest != null || id != null)
                    throw new IllegalArgumentException("Invalid CONNECT headers");
                break;
            case SEND:
                // TODO: can body can be empty?
                if (dest == null || host != null || id != null || login != null
                    || pass != null || ver != null)
                    throw new IllegalArgumentException("Invalid SEND headers");
                break;
            case SUBSCRIBE:
                if (dest == null || id == null || host != null || login != null
                    || pass != null || ver != null)
                    throw new IllegalArgumentException("Invalid SUBSCRIBE headers");
                break;
            case UNSUBSCRIBE:
                if (id == null || dest != null || host != null || login != null
                    || pass != null || ver != null)
                    throw new IllegalArgumentException("Invalid UNSUBSCRIBE headers");
                break;
            case DISCONNECT:
                if (reciept == null || dest != null || id != null || host != null
                    || login != null || pass != null || ver != null)
                    throw new IllegalArgumentException("Invalid DISCONNECT headers");
                break;
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
