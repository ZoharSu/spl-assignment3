#include <string>
#include <thread>
#include <iostream>
#include "../include/StompCommand.h"
#include "../include/StompProtocol.h"
#include "../include/event.h"

void listener_loop(StompProtocol *p) {
    while (p->is_active()) {
        StompParser msg = p->recv();
        if (msg.type != ERROR)
            p->process(msg);
        else
            std::cout << "TODO";
    }
}

bool handle_login(StompProtocol& p, Command& command) {
    StompParser msg = p.login(command.username, command.password);

    if (msg.type == ERROR) {
        if (msg.srvErrMsg == "Socket error: connection error") {
            std::cout << "Could not connect to server" << std::endl;
        }
        else if (msg.srvErrMsg == "Client already logged in") {
            std::cout << "The client is already logged in, log out before trying again" << std::endl;
        }
        else if (msg.srvErrMsg == "Wrong password") {
            std::cout << "Wrong password" << std::endl;
        }

        return false;
    } else std::cout << "Login successful" << std::endl;

    return true;
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
        p.send(e.get_name(), body);
    }
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
            p.login(command.username, command.password);
            // send username + password, await answer
            bool ok = handle_login(p, command);
            if (ok)
                listener = std::thread(listener_loop, &p);
        }
        else if (!p.is_active())
            std::cout << "you must log-in before doing any actions" << std::endl;
        else if (command.type == JOIN) p.subscribe(command.game_name);
        else if (command.type == EXIT) p.unsubscribe(command.game_name);
        else if (command.type == REPORT) handle_report(p, command);

        else if (command.type == LOGOUT) {
            p.disconnect();
            listener.join();
        }
    }

    return 0;
}
