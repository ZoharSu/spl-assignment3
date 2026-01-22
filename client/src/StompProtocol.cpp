#include "../include/StompProtocol.h"
#include "../include/StompParser.h"
#include <memory>
#include <string>

StompProtocol::StompProtocol() : handler() {}

void StompProtocol::connect(std::string hostname, short port) {
    handler.reset(new ConnectionHandler(hostname, port));
    handler->connect();
    next_reciept = 0;
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

void StompProtocol::await_answer(std::string reciept) {
    bool& recieved = recieptMap[reciept];

    // TODO: busy-waiting is bad for your health, please fix
    while (!recieved);
}

std::string StompProtocol::get_reciept() {
    return std::to_string(this->next_reciept);
}

void StompProtocol::process(const StompParser& p) {
    if (p.receipt != "") recieptMap[p.receipt] = true;
}
