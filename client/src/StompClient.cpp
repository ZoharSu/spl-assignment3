#include <fstream>
#include <string>
#include <thread>
#include <iostream>
#include "../include/StompCommand.h"
#include "../include/StompProtocol.h"
#include "../include/event.h"
#include "../include/GameTracker.h"

void listener_loop(StompProtocol *p, Tracker *t) {
    while (p->is_active()) {
        StompParser msg = p->recv();
        if (msg.type == ERROR) {
            std::cerr << "ERROR: " << msg.srvErrMsg << std::endl;
            p->closeHandler();
            p->reset();
            return;
        }
        if (msg.type == MESSAGE) {
            std::string line = msg.body.substr(0, msg.body.find('\n')),
                        user = line.substr(line.find(": ") + 2),
                        frame = msg.body.substr(msg.body.find('\n') + 1);
            Event e{frame};
            t->add(e, user);
        } else p->process(msg);
    }
}

void handle_login_error(std::string msg) {
    // if (msg == "Socket error: connection error")
    //     std::cout << "Could not connect to server" << std::endl;

    if (msg == "User already logged in")
        std::cout << "The client is already logged in, log out before trying again" << std::endl;

    else if (msg == "Wrong password")
        std::cout << "Wrong password" << std::endl;
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

    auto comp = [](Event a, Event b) { return a.get_time() < b.get_time(); };
    std::sort(events.events.begin(), events.events.end(), comp);

    for (Event& e : events.events) {
        std::string body = "user: " + p.username + '\n' + to_string(e),
                    dest = e.get_team_a_name() + '_' + e.get_team_b_name();
        p.send(dest, body);
    }
}

void ensureClosed(StompProtocol& p, std::thread& listener) {
    p.closeHandler();

    if (listener.joinable())
        listener.join();

    p.reset();
}

int main(int argc, char *argv[]) {
    StompProtocol p;
    Tracker tracker;

    std::thread listener;

    while (true) {
        std::string line;
        std::getline(std::cin, line);
        Command command(line);

        if (command.type == CERROR)
            std::cout << command.error << std::endl;
        else if (command.type == LOGIN) {
            if (!p.is_active()) {
                StompParser msg;
                if (p.connect(command.hostname, command.port))
                    msg = p.login(command.username, command.password);
                else {
                    std::cout << "Could not connect to server" << std::endl;
                    continue;
                }

                if (msg.type == CONNECTED) {
                    std::cout << "Login successful" << std::endl;
                    listener = std::thread(listener_loop, &p, &tracker);
                } else {
                    ensureClosed(p, listener);
                    handle_login_error(msg.srvErrMsg);
                }
            } else
                std::cout << "The client is already logged in, log out before trying again" << std::endl;
        }
        else if (!p.is_active()) std::cout << "You must log in before doing any actions" << std::endl;
        else if (command.type == JOIN) p.subscribe(command.game_name);
        else if (command.type == EXIT) p.unsubscribe(command.game_name);
        else if (command.type == REPORT) handle_report(p, command);
        else if (command.type == SUMMARY) {
            std::string summary = tracker.summerize(command.username, command.game_name);
            std::ofstream(command.filename) << summary;
        }
        else if (command.type == LOGOUT) {
            p.disconnect();
            ensureClosed(p, listener);
        }
    }

    return 0;
}
