package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
        if (args.length < 2) {
            System.out.println("Arguments: <port> {tcp | reactor} [thread count]");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int nthreads = args.length < 3 ? 10 : Integer.parseInt(args[3]);

        switch (args[1]) {
            case "tpc":     tpcMain(port); break;
            case "reactor": reactorMain(port, nthreads); break;

        }
    }

    private static void tpcMain(int port) {
        Server.threadPerClient(port, protocolFactory, ObjectEncoderDecoder::new);
    }

    private static void reactorMain(int port, int nthreads) {
        // TODO: reactor
        // Server.reactor(nthreads, port, protocolFactory, ObjectEncoderDecoder::new);
        throw new IllegalArgumentException("TODO");
    }

}
