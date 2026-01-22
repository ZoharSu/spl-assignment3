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
    next_reciept = 0;
    idToTopic.clear();
}

StompParser StompProtocol::login(std::string user, std::string password) {
    std::vector<std::pair<std::string, std::string>> headers = {
        {"accept-version", "1.2"},
        {"host", "stomp.cs.bgu.ac.il"},
        {"login", username},
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
    std::string reciept = get_reciept();
    send("SEND", {{"destination", topic}, {"reciept", reciept}}, msg);

    await_answer(reciept);
}

void StompProtocol::subscribe(const std::string& topic) {
    std::string id = std::to_string(topicToId(topic));
    std::string reciept = get_reciept();

    send("SUBSCRIBE", {{"destination", topic},
                       {"reciept", reciept},
                       {"id", id}});

    await_answer(reciept);
}

void StompProtocol::unsubscribe(const std::string& topic) {
    std::string id = std::to_string(topicToId(topic));
    std::string reciept = get_reciept();
    send("UNSUBSCRIBE", {{"id", id}, {"reciept", reciept}});

    await_answer(reciept);
}

void StompProtocol::disconnect() {
    std::string reciept = get_reciept();
    send("DISCONNECT", {{"reciept", reciept}});
}
bool StompProtocol::is_active() const {
    return false;
}

StompParser StompProtocol::recv() {
    std::string frame;
    bool ok = handler->getFrameAscii(frame, '\0');

    if (!ok) {
        StompParser ret;
        ret.srvErrMsg = "Socket error: connection error";
        return ret;
    }

    return StompParser{frame};
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
