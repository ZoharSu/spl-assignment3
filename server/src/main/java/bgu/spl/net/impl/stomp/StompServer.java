package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Arguments: <port> {tpc | reactor}");
            return;
        }

        int port = Integer.parseInt(args[0]);

        if (args[1].equals("tpc"))
            Server.threadPerClient(
                    port, //port
                    StompProtocol::new, //protocol factory
                    StompMessageEncoderDecoder::new //message encoder decoder factory
            ).serve();
        else if (args[1].equals("reactor"))
            Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port, //port
                    StompProtocol::new, //protocol factory
                    StompMessageEncoderDecoder::new //message encoder decoder factory
            ).serve();
        else
            System.out.println("Arguments: <port> {tpc | reactor}");
    }
}
