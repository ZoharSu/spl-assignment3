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
    std::string final_receipt;

    void send(const std::string& command,
              const std::vector<std::pair<std::string, std::string>>& headers,
              const std::string& body = "");

    std::string get_receipt();

    void await_answer(const std::string& receipt);

    int topicToId(const std::string& topic) const;

public:

    std::string username;

    StompProtocol();

    bool connect(const std::string& hostname, short port);

    StompParser login(const std::string& user, const std::string& password);

    void disconnect();

    bool process(const StompParser& p);

    void send(const std::string& topic, const std::string& file, const std::string& msg);

    void send(const std::string& topic, const std::string& msg);

    void subscribe(const std::string& topic);

    void unsubscribe(const std::string& topic);

    bool isSubscribed(const std::string& topic) const;

    StompParser recv();

    void closeHandler();

    void reset();

    bool is_active() const;
};
