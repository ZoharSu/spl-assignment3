#include "../include/StompCommand.h"
#include "../include/utils.h"

Command::Command(const std::string& line) :
    error{}, type{CERROR}, game_name{}, filename{},
    port{}, hostname{}, username{}, password{}
 {
    std::vector<std::string> args = split(line, ' ');

    if (args.size() == 0) {
        error = "There must be at least 1 argument";
    } else if (args[0] == "logout") {
        if (args.size() > 1)
            error = "Usage: logout";
        else
            type = LOGOUT;
    } else if (args[0] == "login") {
        parseLogin(args);
    } else if (args[0] == "join") {
        if (args.size() != 2)
            error = "Usage: join {game_name}";
        else {
            game_name = args[1];
            type = JOIN;
        }
    } else if (args[0] == "exit") {
        if (args.size() != 2)
            error = "Usage: exit {game_name}";
        else {
            game_name = args[1];
            type = EXIT;
        }
    } else if (args[0] == "report") {
        if (args.size() != 2)
            error = "Usage: report {file}";
        else {
            filename = args[1];
            type = REPORT;
        }
    } else if (args[0] == "summary") {
        if (args.size() != 4)
            error = "Usage: summary {game_name} {user} {file}";
        else {
            game_name = args[1];
            username = args[2];
            filename = args[3];
            type = SUMMARY;
        }
    } else {
        error = "Invalid command";
    }
}


void Command::parseLogin(std::vector<std::string> args) {
    if (args.size() != 4) {
        error = "Usage: login {host:port} {username} {password}";
        return;
    }

    std::vector<std::string> host = split(args[1], ':');

    if (host.size() != 2) {
        error = "Usage: login {host:port} {username} {password}";
        return;
    }

    hostname = host[0];
    port     = std::stoi(host[1]);
    username = args[2];
    password = args[3];
    type     = LOGIN;
}
