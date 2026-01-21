/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.net.srv;

import java.io.Closeable;

/**
 * The ConnectionHandler interface for Message of type T
 */
public interface ConnectionHandler<T> extends Closeable {

    /**
     * Send {@code msg} to the client associated with this ConnectionHandler
     * @param msg the message to send
     */
    void send(T msg);

    /**
     * Send {@code msg} to the client associated with this ConnectionHandler
     * and then close the connection
     * @param msg the message to send
     */
    void sendAndClose(T msg);
}
