#include <thread>
#include <iostream>
#include "../include/StompCommand.h"

void keyboard_loop(bool *should_close) {
    while (!*should_close) {
        std::string line;
        std::getline(std::cin, line);
        Command command{line};

        if (!command.is_legal()) std::cout << command.error << std::endl;
    }
}

void mainloop() {
    std::string command;
}

int main(int argc, char *argv[]) {
    bool should_close = false;
    std::thread keyboard_thread(keyboard_loop, &should_close);

    mainloop();

    keyboard_thread.join();
    return 0;
}
