package bgu.spl.net.impl.stomp;

class StompParser {
    public MessageType  t       = null;
    public Integer      id      = null;
    public String       ver     = null;
    public String       dest    = null;
    public String       body    = null;
    public String       host    = null;
    public String       pass    = null;
    public String       login   = null;
    public String       errMsg  = null;
    public String       receipt = null;
    public String       message = null;

    public enum MessageType {
        CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE, DISCONNECT
    }

    // TODO: Consider file header too
    public StompParser(String msg) {
        message = msg;
        if (message == null) {
            errMsg = "Message can't be null";
            return;
        }

        int eol = message.indexOf("\n");
        if (eol == -1) {
            errMsg = "Single line message";
            return;
        }

        if (!parseType(message.substring(0, eol))) {
            illegal("Invalid message type");
            return;
        }

        // parse headers and body
        parseHeadersAndBody(message.substring(eol+1));
        if (!islegal()) return;

        ensureValid();
    }

    private void illegal(String reason) {
        errMsg = reason;
        
        if (message == null) return;

        // Extract receipt header
        String[] parts  = message.split("\nreceipt:");
        for (int i = 1; i < parts.length; i++) {
            int eol = parts[i].indexOf("\n");
            if (eol == -1 && !parts[i].isEmpty()) { receipt = parts[i]; break; }
            if (eol > 0) { receipt = parts[i].substring(0, eol); break; }
        }
    }

    public boolean islegal() {
        return errMsg == null;
    }

    private void parseHeadersAndBody(String msg) {
        // msg contains headers and body only
        int eol = msg.indexOf("\n");
        int loc = 0;
        while (eol != -1) {

            String line = msg.substring(loc, eol);

            // empty line => body starts
            if (line.isEmpty()) {
                if (eol < msg.length())
                    body = msg.substring(eol + 1, msg.length());
                else body = "";
                return;
            }

            // FIXME: what about headers with ':' in the value?
            String[] parts = msg.substring(loc, eol).split(":");
            if (parts.length != 2) {
                illegal("Illegal header structure");
                return;
            }

            String key = parts[0];
            String value = parts[1];

            if (key.equals("destination") && dest == null)
                dest = value;
            else if (key.equals("id") && id == null) {
                id = parseInt(value);
                if (id == null) {
                    illegal("Invalid header");
                    return;
                }
            }
            else if (key.equals("receipt") && receipt == null)
                receipt = value;
            else if (key.equals("host") && host == null)
                host = value;
            else if (key.equals("login") && login == null)
                login = value;
            else if (key.equals("passcode") && pass == null)
                pass = value;
            else if (key.equals("accept-version") && ver == null)
                ver = value;
            else {
                illegal("Invalid header");
                return;
            }

            loc = eol + 1;
            eol = msg.indexOf("\n", loc);
        }

        if (body == null)
            illegal("Malformed frame recieved");

    }

    private Integer parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean parseType(String line) {
             if (line.equals("CONNECT"))        t = MessageType.CONNECT;
        else if (line.equals("SEND"))           t = MessageType.SEND;
        else if (line.equals("SUBSCRIBE"))      t = MessageType.SUBSCRIBE;
        else if (line.equals("UNSUBSCRIBE"))    t = MessageType.UNSUBSCRIBE;
        else if (line.equals("DISCONNECT"))     t = MessageType.DISCONNECT;
        else return false;

        return true;
    }

    private void ensureValid() {
        switch (t) {
            case CONNECT:
                if (host == null || !host.equals("stomp.cs.bgu.ac.il")
                    || login == null || pass == null || ver == null
                    || !ver.equals("1.2") || !body.isEmpty()
                    || dest != null || id != null)
                    illegal("Invalid CONNECT headers");
                break;
            case SEND:
                if (dest == null || body.isEmpty() || host != null || id != null
                    || login != null || pass != null || ver != null)
                    illegal("Invalid SEND headers");
                break;
            case SUBSCRIBE:
                if (dest == null || id == null || !body.isEmpty() || host != null
                    || login != null || pass != null || ver != null)
                    illegal("Invalid SUBSCRIBE headers");
                break;
            case UNSUBSCRIBE:
                if (id == null || !body.isEmpty() || dest != null || host != null
                    || login != null || pass != null || ver != null)
                    illegal("Invalid UNSUBSCRIBE headers");
                break;
            case DISCONNECT:
                if (receipt == null || !body.isEmpty() || dest != null || id != null
                    || host != null || login != null || pass != null || ver != null)
                    illegal("Invalid DISCONNECT headers");
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
