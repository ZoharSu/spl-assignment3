#include "../include/StompProtocol.h"
#include <memory>

StompProtocol::StompProtocol() : handler() {}

void StompProtocol::connect(std::string hostname, short port) {
    handler.reset(new ConnectionHandler(hostname, port));
    handler->connect();
}


void StompProtocol::reset() {
    handler->close();
    handler.reset();
}

void StompProtocol::send(const std::string command,
                         const std::vector<std::pair<std::string, std::string>> headers,
                         const std::string body)
{
    std::string frame = command + '\n';

    for (const auto& header : headers)
        frame += header.first + ':' + header.second + '\n';

    frame += '\n' + body;

    handler->sendFrameAscii(frame, '\0');
}

bool StompProtocol::is_active() const {
    return false;
}

void StompProtocol::process(const std::string& msg) {
}
