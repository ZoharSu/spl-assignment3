#include <string>
#include <thread>
#include <iostream>
#include "../include/StompCommand.h"
#include "../include/StompProtocol.h"
#include "../include/event.h"

void listener_loop(StompProtocol *p) {
    while (p->is_active()) {
        std::string frame;
        if (p->handler->getFrameAscii(frame, '\0'))
            p->process(frame);
        else
            std::cout << "TODO";
    }
}

void handle_login(StompProtocol& p, Command& command) {
    std::vector<std::pair<std::string, std::string>> headers = {
        {"accept-version", "1.2"},
        {"host", "stomp.cs.bgu.ac.il"},
        {"login", command.username},
        {"passcode", command.password}
    };
    p.send("CONNECT", headers);
}

std::string to_string(Event& e) {
    std::string ret;
    ret += "team a: " + e.get_team_a_name() + '\n';
    ret += "team b: " + e.get_team_b_name() + '\n';
    ret += "event name: " + e.get_name() + '\n';
    ret += "time: " + std::to_string(e.get_time()) + '\n';

    ret += "general game updates:\n";
    for (const auto& pair : e.get_game_updates())
        ret += "    " + pair.first + ": " + pair.second + '\n';

    ret += "team a updates:\n";
    for (const auto& pair : e.get_team_a_updates())
        ret += "    " + pair.first + ": " + pair.second + '\n';

    ret += "team b updates:\n";
    for (const auto& pair : e.get_team_b_updates())
        ret += "    " + pair.first + ": " + pair.second + '\n';

    ret += "discription:\n" + e.get_discription();

    return ret;
}

void handle_report(StompProtocol& p, Command& command) {
    names_and_events events = parseEventsFile(command.filename);

    std::string body = "user:" + p.username + '\n';

    auto comp = [](Event a, Event b) { return a.get_time() < b.get_time(); };
    std::sort(events.events.begin(), events.events.end(), comp);

    for (Event& e : events.events) {
        std::string body = "user: " + p.username + '\n' + to_string(e);
        p.send("SEND", {{"destination", e.get_name()}}, body);
    }
}

void handle_logout(StompProtocol& p, Command& command) {
    p.send("LOGOUT", {{"reciept", "77"}}); // TODO: next reciept
}

int main(int argc, char *argv[]) {
    StompProtocol p;

    std::thread listener;

    while (true) {
        std::string line;
        std::getline(std::cin, line);
        Command command(line);

        if (command.type == LOGIN && !p.is_active()) {
            p.connect(command.hostname, command.port);
            // send username + password, await answer
            bool answer = true;
            if (answer)
                listener = std::thread(listener_loop, &p);
        }
        else if (!p.is_active())
            std::cout << "you must log-in before doing any actions" << std::endl;
        else if (command.type == REPORT) handle_report(p, command);

        else if (command.type == LOGOUT) {
            // p.logout()
            listener.join();
        }
    }

    return 0;
}
