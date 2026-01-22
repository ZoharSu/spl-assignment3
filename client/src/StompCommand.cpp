#include "../include/StompCommand.h"
#include "../include/utils.h"

Command::Command(std::string& line) :
    error{}, type{}, game_name{}, filename{},
    port{}, hostname{}, username{}, password{}
 {
    std::vector<std::string> args = split(line, ' ');

    if (args.size() == 0) return; // TODO: error
    if (args[0] == "logout")
        this->type = CommandType::LOGOUT;
    else if (args[0] == "login" && args.size() == 4) {
        parseLogin(args);
    } else if (args[0] == "join" && args.size() == 2) {
        game_name = args[1];
        type = JOIN;
    } else if (args[0] == "exit" && args.size() == 2) {
        game_name = args[1];
        type = EXIT;
    } else if (args[0] == "report" && args.size() == 2) {
        filename = args[1];
        type = REPORT;
    } else if (args[0] == "summery" && args.size() == 4) {
        game_name = args[1];
        username = args[2];
        filename = args[3];
        type = SUMMERY;
    } else {
        
    }
}


void Command::parseLogin(std::vector<std::string> args) {
    if (args.size() != 4) return; // TODO: error

    std::vector<std::string> host = split(args[1], ':');

    if (host.size() != 2) return; // TODO: error

    hostname = host[0];
    port     = std::stoi(host[1]);
    username = args[2];
    password = args[3];
    type     = LOGIN;
}
