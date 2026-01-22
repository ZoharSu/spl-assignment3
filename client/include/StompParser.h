#pragma once

#include<string>

enum MessageType {
    CONNECTED, MESSAGE, RECEIPT, ERROR
};

// Parser for STOMPS from the server
class StompParser {
    public:
        
        MessageType type;
        int subId;
        int msgId;
        std::string dest;
        std::string receipt;
        std::string srvErrMsg;
        std::string body;
        bool isLegal;

        StompParser(const std::string& msg);
};