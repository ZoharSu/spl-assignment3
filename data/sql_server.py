#!/usr/bin/env python3
"""
Basic Python Server for STOMP Assignment – Stage 3.3

IMPORTANT:
DO NOT CHANGE the server name or the basic protocol.
Students should EXTEND this server by implementing
the methods below.
"""

import socket
import sys
import threading
import sqlite3
import atexit
import os


SERVER_NAME = "STOMP_PYTHON_SQL_SERVER"  # DO NOT CHANGE!
DB_FILE = "stomp_server.db"              # DO NOT CHANGE!
_conn = sqlite3.connect(DB_FILE, check_same_thread=False)

# NOTE:
# Deleting database as said in forum.
# Otherwise, it creates problems on the java side with login.
def delete_db_file():
    if os.path.exists(DB_FILE):
        try:
            os.remove(DB_FILE)
        except:
            print("Failed to remove previous database file")

def _close_db():
    _conn.commit()
    _conn.close()


def recv_null_terminated(sock: socket.socket) -> str:
    data = b""
    while True:
        chunk = sock.recv(1024)
        if not chunk:
            return ""
        data += chunk
        if b"\0" in data:
            msg, _ = data.split(b"\0", 1)
            return msg.decode("utf-8", errors="replace")


def init_database():
    atexit.register(_close_db)
    create_tables()

def create_tables():
    execute_sql_command("""
    CREATE TABLE users (
        username TEXT PRIMARY KEY NOT NULL,
        password TEXT NOT NULL,
        registration_date DATETIME NOT NULL
                        
    );

    CREATE INDEX idx_users_registration_date ON users(registration_date);
                        
    CREATE TABLE login_history (
        username TEXT NOT NULL,
        login_time DATETIME NOT NULL,
        logout_time DATETIME,

        PRIMARY KEY(username, login_time),
        FOREIGN KEY(username) REFERENCES users(username)
    );

    CREATE TABLE file_tracking (
        username TEXT NOT NULL,
        filename TEXT NOT NULL,
        upload_time DATETIME NOT NULL,
        game_channel TEXT NOT NULL,

        PRIMARY KEY(username, upload_time),
        FOREIGN KEY(username) REFERENCES users(username)
    );
    """)

def execute_sql_command(sql_command: str):
    try:
        _conn.executescript(sql_command)
    except sqlite3.Error as e:
        print(f"Error executing sql command: {e}")

def execute_sql_query(sql_query: str) -> str:
    cursor = _conn.cursor()
    ret = None
    try:
        cursor.execute(sql_query)
        ret = format_query_result(cursor.fetchall())
    except sqlite3.Error as e:
        print(f"Error executing sql query: {e}")
        ret = "FAILURE"

    return ret


def format_query_result(result):
    ret = []
    for row in result:
        string_row = [str(field) for field in row]
        ret.append(", ".join(string_row))
    
    return "SUCCESS|" + "|".join(ret)

def handle_client(client_socket: socket.socket, addr):
    print(f"[{SERVER_NAME}] Client connected from {addr}")

    try:
        while True:
            message = recv_null_terminated(client_socket)
            if message == "":
                break

            # TODO: REMOVE
            print(f"[{SERVER_NAME}] Received:")
            print(message)

            if message.startswith("SELECT"):
                result = execute_sql_query(message)
                if result:
                    client_socket.sendall(result.encode('utf-8') + b"\0")
            else:
                execute_sql_command(message)
                client_socket.sendall(b'\0')

    except Exception as e:
        print(f"[{SERVER_NAME}] Error handling client {addr}: {e}")
    finally:
        try:
            client_socket.close()
        except Exception:
            pass
        print(f"[{SERVER_NAME}] Client {addr} disconnected")


def start_server(host="127.0.0.1", port=7778):
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    try:
        server_socket.bind((host, port))
        server_socket.listen(5)
        print(f"[{SERVER_NAME}] Server started on {host}:{port}")
        print(f"[{SERVER_NAME}] Waiting for connections...")

        while True:
            client_socket, addr = server_socket.accept()
            t = threading.Thread(
                target=handle_client,
                args=(client_socket, addr),
                daemon=True
            )
            t.start()

    except KeyboardInterrupt:
        print(f"\n[{SERVER_NAME}] Shutting down server...")
    finally:
        try:
            server_socket.close()
        except Exception:
            pass


if __name__ == "__main__":
    init_database()
    port = 7778
    if len(sys.argv) > 1:
        raw_port = sys.argv[1].strip()
        try:
            port = int(raw_port)
        except ValueError:
            print(f"Invalid port '{raw_port}', falling back to default {port}")

    start_server(port=port)
