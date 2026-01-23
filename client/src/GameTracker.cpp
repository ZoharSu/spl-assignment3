#include "../include/GameTracker.h"
#include <algorithm>

Events::Events() :  team_a{}, team_b{}, general_updates{}, team_a_updates{},
                    team_b_updates{}, time_event_desc{} {}

Tracker::Tracker() : user_game_events{} {}

void Tracker::add(const Event& e, const std::string& user) {
    std::string game = e.get_team_a_name() + '_' + e.get_team_b_name();
    Events& events = user_game_events[user][game];

    if (events.team_a.empty()) {
        events.team_a = e.get_team_a_name();
        events.team_b = e.get_team_b_name();
    }

    std::string desc_title = std::to_string(e.get_time()) + " - " + e.get_name()
                              + ":\n\n" + e.get_discription() + "\n\n";

    events.time_event_desc.push_back({e.get_time(), desc_title});

    for (const auto& pair : e.get_game_updates())
        events.general_updates[pair.first] = pair.second;

    for (const auto& pair : e.get_team_a_updates())
        events.team_a_updates[pair.first] = pair.second;

    for (const auto& pair : e.get_team_b_updates())
        events.team_b_updates[pair.first] = pair.second;
}

std::string Tracker::summerize(const std::string& user, const std::string& game) {
    Events& events = user_game_events[user][game];
    std::string ret;

    ret += events.team_a + " vs " + events.team_b + "\n";
    ret += "Game stats:\n";

    ret += "General stats:\n";
    for (const auto& pair : events.general_updates) {
        ret += pair.first + ": " + pair.second + "\n";
    }

    ret += events.team_a + " stats:\n";
    for (const auto& pair : events.team_a_updates) {
        ret += pair.first + ": " + pair.second + "\n";
    }

    ret += events.team_b + " stats:\n";
    for (const auto& pair : events.team_b_updates) {
        ret += pair.first + ": " + pair.second + "\n";
    }

    ret += "Game event reports:\n";

    std::sort(events.time_event_desc.begin(), events.time_event_desc.end(),
        [](const std::pair<int, std::string>& a, const std::pair<int, std::string>& b) {
            return a.first < b.first;
        }
    );

    for (const std::pair<int, std::string>& pair : events.time_event_desc) {
        ret += pair.second;
    }

    return ret;
}
