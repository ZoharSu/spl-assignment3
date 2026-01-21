#include "../include/StompProtocol.h"
#include <memory>

StompProtocol::StompProtocol() : handler(nullptr) {}

void StompProtocol::connect(std::string hostname, short port) {
    handler = std::unique_ptr<ConnectionHandler>(new ConnectionHandler(hostname, port));
    handler->connect();
}


void StompProtocol::reset() {
    handler->close();
    handler.reset();
}
