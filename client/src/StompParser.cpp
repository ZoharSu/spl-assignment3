#include "../include/StompParser.h"
#include <vector>
#include <string>
#include <iostream>

StompParser::StompParser(const std::string& msg) :
    type(), subId(-1), msgId(-1), dest(""), receipt(""),
    srvErrMsg(""), body(""), isLegal(false) {

    std::vector<std::string> lines;
    std::string line;
    for (char c : msg) {
        if (c == '\n') {
            lines.push_back(line);
            line = "";
            continue;
        }

        line += c;
    }

    if (!line.empty())
        lines.push_back(line); // push last line

    if (lines.empty()) return; // not legal

         if (lines[0] == "CONNECTED")   type = CONNECTED;
    else if (lines[0] == "MESSAGE")     type = MESSAGE;
    else if (lines[0] == "RECEIPT")     type = RECEIPT;
    else if (lines[0] == "ERROR")       type = ERROR;
    else return; // not legal

    size_t lineNum = 1;
    while (lineNum < lines.size() && !lines[lineNum].empty()) {
        std::string header = lines[lineNum];
        size_t sep = header.find(':');

        if (sep != std::string::npos) {
            std::string key = header.substr(0, sep);
            std::string value = header.substr(sep + 1);

            // FIXME: should we check for id validity (integer)?
            if (key == "subscription") {
                subId = std::stoi(value);
            } else if (key == "message-id") {
                msgId = std::stoi(value);
            } else if (key == "destination") {
                dest = value;
            } else if (key == "receipt-id") {
                receipt = value;
            } else if (key == "message") {
                srvErrMsg = value;
            }
        } else return;

        lineNum++;
    }

    // Continue to body if exists
    if (lineNum < lines.size() && lines[lineNum].empty())
        lineNum++;

    // First body line
    if (lineNum < lines.size()) {
        body += lines[lineNum];
        lineNum++;
    }

    // Rest of the body
    while (lineNum < lines.size()) {
        body += "\n" + lines[lineNum];
        lineNum++;
    }

    isLegal = true;
}
