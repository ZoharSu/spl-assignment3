#include <string>
#include <thread>
#include <iostream>
#include "../include/StompCommand.h"
#include "../include/StompProtocol.h"

void listener_loop(StompProtocol *p) {
    while (p->is_active()) {
        std::string frame;
        p->handler->getFrameAscii(frame, '\0');
        p->process(frame);
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
            // send username + password, await answer
            bool answer = true;
            if (answer)
                listener = std::thread(listener_loop, &p);
        } if (command.type == LOGOUT && p.is_active()) {
            // p.logout()
            listener.join();
        }
    }

    return 0;
}
