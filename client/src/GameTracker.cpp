#include "../include/GameTracker.h"
#include <algorithm>

Events::Events() :  team_a{}, team_b{}, general_updates{}, team_a_updates{},
                    team_b_updates{}, time_event_description{} {}

Tracker::Tracker() : user_game_events{} {}

void Tracker::add(const Event& e, const std::string& user) {
    std::string game = e.get_team_a_name() + '_' + e.get_team_b_name();
    Events events = user_game_events[user][game];
    if (events.team_a.empty()) {
        events.team_a = e.get_team_a_name();
        events.team_b = e.get_team_b_name();
    }
    events.time_event_description.push_back(
           {std::to_string(e.get_time()) + '-' + e.get_name(), e.get_discription()});
    for (std::pair<std::string, std::string> pair : e.get_team_a_updates())
        events.team_a_updates.push_back(pair);

    for (std::pair<std::string, std::string> pair : e.get_team_b_updates())
        events.team_b_updates.push_back(pair);
}

std::string Tracker::summerize(const std::string& user, const std::string& game) {
    Events events = user_game_events[user][game];
    std::string ret = events.team_a + " vs " + events.team_b + '\n';
    ret += "Game stats:\n";
    ret += "General stats:\n";

    std::sort(events.general_updates.begin(), events.general_updates.end());
    for (std::pair<std::string, std::string> pair : events.general_updates)
        ret += pair.first + ':' + pair.second + '\n';

    ret += events.team_a + "stats\n";

    std::sort(events.general_updates.begin(), events.general_updates.end());
    for (std::pair<std::string, std::string> pair : events.team_a_updates)
        ret += pair.first + ':' + pair.second + '\n';

    ret += events.team_b + "stats\n";

    std::sort(events.general_updates.begin(), events.general_updates.end());
    for (std::pair<std::string, std::string> pair : events.team_b_updates)
        ret += pair.first + ':' + pair.second + '\n';

    ret += "Game event reports:";

    for (std::pair<std::string, std::string> pair : events.time_event_description)
        ret += pair.first + ':' + pair.second + '\n';

    return ret;
}
