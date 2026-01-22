#pragma once

#include "../include/ConnectionHandler.h"
#include <memory>
#include <utility>

// TODO: implement the STOMP protocol
class StompProtocol
{
public:
    std::unique_ptr<ConnectionHandler> handler;

    std::string username;

    StompProtocol();

    void connect(std::string hostname, short port);

    void process(const std::string& msg);

    void send(std::string command,
              std::vector<std::pair<std::string, std::string>> headers,
              std::string body = "");

    void reset();

    bool is_active() const;
};
