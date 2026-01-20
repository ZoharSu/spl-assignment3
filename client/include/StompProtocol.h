#pragma once

#include "../include/ConnectionHandler.h"

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
    ConnectionHandler handler;

public:
    StompProtocol(ConnectionHandler handler);
};
