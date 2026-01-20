#pragma once

#include <string>
#include <vector>

enum CommandType {
    LOGIN, JOIN, EXIT,
    REPORT, SUMMERY, LOGOUT,
};

class Command {
public:
    std::string error;
    
    CommandType type;
    std::string game_name;
    std::string filename;

    int port;
    std::string hostname;
    std::string username;
    std::string password;

    Command(std::string& line);

    bool is_legal() { return error.empty(); }

private:
    void parseLogin(std::vector<std::string> args);
};
