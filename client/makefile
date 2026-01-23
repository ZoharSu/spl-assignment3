CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

OBJECTS := bin/ConnectionHandler.o bin/StompClient.o bin/StompCommand.o \
			bin/StompProtocol.o bin/StompParser.o bin/event.o bin/utils.o

all: StompWCIClient

EchoClient: bin/ConnectionHandler.o bin/echoClient.o
	g++ -o bin/EchoClient bin/ConnectionHandler.o bin/echoClient.o $(LDFLAGS)

StompWCIClient: $(OBJECTS)
	g++ -o bin/StompWCIClient $(OBJECTS) $(LDFLAGS)

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/event.o: src/event.cpp
	g++ $(CFLAGS) -o bin/event.o src/event.cpp

bin/StompCommand.o: src/StompCommand.cpp
	g++ $(CFLAGS) -o bin/StompCommand.o src/StompCommand.cpp

bin/StompProtocol.o: src/StompProtocol.cpp
	g++ $(CFLAGS) -o bin/StompProtocol.o src/StompProtocol.cpp

bin/StompClient.o: src/StompClient.cpp
	g++ $(CFLAGS) -o bin/StompClient.o src/StompClient.cpp

bin/StompParser.o: src/StompParser.cpp
	g++ $(CFLAGS) -o bin/StompParser.o src/StompParser.cpp

bin/GameTracker.o: src/GameTracker.cpp
	g++ $(CFLAGS) -o bin/GameTracker.o src/GameTracker.cpp

bin/utils.o: src/utils.cpp
	g++ $(CFLAGS) -o bin/utils.o src/utils.cpp

.PHONY: clean
clean:
	rm -f bin/*
