#include "../include/StompProtocol.h"
#include "../include/StompParser.h"
#include <memory>
#include <string>

StompProtocol::StompProtocol() : handler() {}

int StompProtocol::topicToId(std::string topic) const {
    return hash(username + topic);
}

bool StompProtocol::connect(std::string hostname, short port) {
    handler.reset(new ConnectionHandler(hostname, port));
    if (!handler->connect()) {
        reset();
        return false;
    }
    isActive = true;
    next_receipt = 0;
    idToTopic.clear();
    return true;
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

void StompProtocol::closeHandler() {
    isActive = false;
    if (handler) {
        handler->shutdown();
        handler->close();
    }
}

void StompProtocol::reset() {
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

    // Already subscribed
    if (idToTopic.find(topicToId(topic)) != idToTopic.end()) {
        std::cout << "Already joined channel " << topic << std::endl;
        return;
    }

    // Not subscribed
    std::string id = std::to_string(topicToId(topic));
    std::string receipt = get_receipt();

    send("SUBSCRIBE", {{"destination", topic},
                       {"receipt", receipt},
                       {"id", id}});

    await_answer(receipt);
    idToTopic.insert({topicToId(topic), topic});
    std::cout << "Joined channel " << topic << std::endl;
}

void StompProtocol::unsubscribe(const std::string& topic) {

    // Already unsubscribed
    if (idToTopic.find(topicToId(topic)) == idToTopic.end()) {
        std::cout << "Already exited channel " << topic << std::endl;
        return;
    }

    // Subscribed
    std::string id = std::to_string(topicToId(topic));
    std::string receipt = get_receipt();
    send("UNSUBSCRIBE", {{"id", id}, {"receipt", receipt}});

    await_answer(receipt);
    idToTopic.erase(topicToId(topic));
    std::cout << "Exited channel " << topic << std::endl;
}

void StompProtocol::disconnect() {
    std::string receipt = get_receipt();
    send("DISCONNECT", {{"receipt", receipt}});
    await_answer(receipt);
}

bool StompProtocol::is_active() const {
    return isActive.load();
}

StompParser StompProtocol::recv() {
    std::string frame;
    if (!handler || !handler->getFrameAscii(frame, '\0')) {
        StompParser ret;
        ret.type = UNKNOWN;
        return ret;
    }
    return StompParser{frame};
}

void StompProtocol::await_answer(std::string receipt) {
    std::unique_lock<std::mutex> lock(mtx);
    while (!receiptMap[receipt])
        cv.wait(lock);

    // recieved the receipt from the server
}

std::string StompProtocol::get_receipt() {
    return std::to_string(this->next_receipt++);
}

void StompProtocol::process(const StompParser& p) {
    {
        std::unique_lock<std::mutex> lock(mtx);
        if (!p.receipt.empty()) receiptMap[p.receipt] = true;
    }
    cv.notify_all();
}
