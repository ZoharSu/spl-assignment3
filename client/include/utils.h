#pragma once

#include <string>
#include <vector>

std::vector<std::string> split(const std::string &s, char delim = ' ');

#ifndef _UTILS_NO_IMPL

std::vector<std::string> split(const std::string &s, char delim) {
    std::vector<std::string> ret;

    size_t start = 0, pos = s.find(delim);

    while (pos != std::string::npos) {
        ret.push_back(s.substr(start, pos - start));

        start = pos + 1;
        pos = s.find(delim, start);
    }

    ret.push_back(s.substr(start));

    return ret;
}

#endif
