#include "../include/StompProtocol.h"
#include "../include/StompParser.h"
#include <memory>
#include <string>

StompProtocol::StompProtocol() : handler() {}

int StompProtocol::topicToId(std::string topic) const {
    return hash(username + topic);
}

void StompProtocol::connect(std::string hostname, short port) {
    handler.reset(new ConnectionHandler(hostname, port));
    handler->connect();
    next_receipt = 0;
    idToTopic.clear();
}

StompParser StompProtocol::login(std::string user, std::string password) {
    this->username = user;
    std::vector<std::pair<std::string, std::string>> headers = {
        {"accept-version", "1.2"},
        {"host", "stomp.cs.bgu.ac.il"},
        {"login", user},
        {"passcode", password}
    };
    send("CONNECT", headers);

    return recv();
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

void StompProtocol::send(const std::string& topic, const std::string& msg) {
    std::string receipt = get_receipt();
    send("SEND", {{"destination", topic}, {"receipt", receipt}}, msg);

    await_answer(receipt);
}

void StompProtocol::subscribe(const std::string& topic) {
    std::string id = std::to_string(topicToId(topic));
    std::string receipt = get_receipt();

    send("SUBSCRIBE", {{"destination", topic},
                       {"receipt", receipt},
                       {"id", id}});

    await_answer(receipt);
}

void StompProtocol::unsubscribe(const std::string& topic) {
    std::string id = std::to_string(topicToId(topic));
    std::string receipt = get_receipt();
    send("UNSUBSCRIBE", {{"id", id}, {"receipt", receipt}});

    await_answer(receipt);
}

void StompProtocol::disconnect() {
    std::string receipt = get_receipt();
    send("DISCONNECT", {{"receipt", receipt}});
    await_answer(receipt);
    reset();
}
bool StompProtocol::is_active() const {
    return bool(handler);
}

StompParser StompProtocol::recv() {
    std::string frame;
    bool ok = handler->getFrameAscii(frame, '\0');

    if (!ok) {
        std::cout << frame << std::endl;
        StompParser ret;
        ret.srvErrMsg = "Socket error: connection error";
        return ret;
    }

    return StompParser{frame};
}

void StompProtocol::await_answer(std::string receipt) {
    bool& recieved = receiptMap[receipt];

    // TODO: busy-waiting is bad for your health, please fix
    while (!recieved);
}

std::string StompProtocol::get_receipt() {
    return std::to_string(this->next_receipt++);
}

void StompProtocol::process(const StompParser& p) {
    if (p.receipt != "") receiptMap[p.receipt] = true;
}
