#pragma once

#include "../include/ConnectionHandler.h"
#include <memory>

// TODO: implement the STOMP protocol
class StompProtocol
{
public:
    std::unique_ptr<ConnectionHandler> handler;

    StompProtocol();

    void connect(std::string hostname, short port);

    void process(std::string& msg);

    void reset();

    bool is_active();
};
