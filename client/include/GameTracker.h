#pragma once
#include <unordered_map>
#include <string>
#include <vector>
#include <map>
#include "event.h"

class Events {
public:
    Events();
    std::string team_a;
    std::string team_b;

    std::map<std::string, std::string> general_updates;
    std::map<std::string, std::string> team_a_updates;
    std::map<std::string, std::string> team_b_updates;

    std::vector<std::pair<int, std::string>> time_event_desc;
};

class Tracker {
private:
    std::unordered_map<std::string, std::unordered_map<std::string, Events>> user_game_events;
public:
    Tracker();

    void add(const Event& e, const std::string& user);

    std::string summerize(const std::string& user, const std::string& game);
};
