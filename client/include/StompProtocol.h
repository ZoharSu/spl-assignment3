#pragma once

#include "ConnectionHandler.h"
#include "StompParser.h"
#include <memory>
#include <utility>
#include <unordered_map>
#include <mutex>
#include <condition_variable>

class StompProtocol
{
private:
    std::unique_ptr<ConnectionHandler>   handler;
    std::unordered_map<int, std::string> idToTopic;
    std::unordered_map<std::string, bool> receiptMap;
    std::hash<std::string> hash;
    int next_receipt;
    std::atomic<bool> isActive;
    std::mutex mtx;
    std::condition_variable cv;

    void send(const std::string command,
              const std::vector<std::pair<std::string, std::string>> headers,
              const std::string body = "");

    std::string get_receipt();

    void await_answer(std::string receipt);

    int topicToId(std::string topic) const;

public:

    std::string username;

    StompProtocol();

    bool connect(std::string hostname, short port);

    StompParser login(std::string user, std::string password);

    void disconnect();

    void process(const StompParser& p);

    void send(const std::string& topic, const std::string& msg);

    void subscribe(const std::string& topic);

    void unsubscribe(const std::string& topic);

    StompParser recv();

    void closeHandler();

    void reset();

    bool is_active() const;
};
