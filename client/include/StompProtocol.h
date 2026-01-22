#pragma once

#include "ConnectionHandler.h"
#include "StompParser.h"
#include <memory>
#include <utility>
#include <unordered_map>

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
    std::unique_ptr<ConnectionHandler>   handler;
    std::unordered_map<int, std::string> idToTopic;
    std::unordered_map<std::string, bool> recieptMap;
    std::hash<std::string> hash;
    int next_reciept;

    void send(const std::string command,
              const std::vector<std::pair<std::string, std::string>> headers,
              const std::string body = "");

    std::string get_reciept();

    void await_answer(std::string reciept);

public:

    std::string username;

    StompProtocol();

    void connect(std::string hostname, short port);

    StompParser login(std::string user, std::string password);

    void disconnect();

    void process(const StompParser& p);

    void send(const std::string& topic, const std::string& msg);

    void subscribe(const std::string& topic);

    void unsubscribe(const std::string& topic);

    StompParser recv();

    void reset();

    bool is_active() const;

    int topicToId(std::string topic) const;
};
