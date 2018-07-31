# Chat-Room-System
Distributed Systems Lab Assignment 1


Chat Room System
Summary:
This is the Chat Box system program written in Java language for Lab 1.
Designed and Developed by Akshay Bahuguna


Chat Room Server
Chat Room System works as client-server system. Each client makes a connection to server over the socket. Multiple clients can connect to server and can send chat messages among themselves. The server broadcasts each client’s messages to all clients. The server keeps track of login/logout information of clients and notifies other clients about any login/logout activity. Both client and server process have respective GUI interface to ease the user visibility. The messages are encoded in Http format. The clients also keep a timer to track the time difference between last two messages coming from all online clients.

The following features are covered:
1.	Client and server processes work successfully.
2.	Client and Server have GUI.
3.	Server Process works and shows the incoming message in Http Format.
4.	Http message format is valid.
5.	Server broadcasts the client’s incoming message to all the online clients.
6.	Each client maintains a timer for the other online clients and shows the time interval between two consecutive messages.
7.	Server works correctly with messages from multiple clients (multi-threading).
8.	Client and server handle Logoff correctly.
9.	Comments are mentioned in code.
Extra Credit Implementations  
1.	The server is multithreaded, thus allowing multiple client sessions at same time.
2.	Server maintains a Database (Text File in this case) of all messages of last session and reloads it when shut down or closed.

Software Requirements:

1.	Eclipse IDE.
2.	JDK 8 or above (build 1.8.0_144-b01).

Technology Used:
Java SWING, SOCKET Programming.
