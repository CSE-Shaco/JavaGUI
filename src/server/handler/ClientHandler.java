package server.handler;

import server.core.ChatRoom;
import server.core.ChatRoomManager;
import server.core.FileReceiver;
import server.core.MatchMaker;
import server.util.ChatLogger;
import server.util.LogFileUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private static final ChatRoomManager roomManager = new ChatRoomManager();
    private static final MatchMaker matchMaker = new MatchMaker(roomManager);
    private static final Map<String, ClientHandler> userHandlers = new ConcurrentHashMap<>();
    private static final Map<String, Integer> anonCount = new ConcurrentHashMap<>();
    private static final int FILE_PORT = 23456;

    private final Socket textSocket;
    private BufferedReader in;
    private BufferedWriter out;

    private String username;
    private boolean isAnonymous = false;
    private String anonymousName = null;
    private ChatRoom chatRoom = null;
    private ClientHandler partnerHandler = null;

    public ClientHandler(Socket textSocket) {
        this.textSocket = textSocket;
        try {
            in = new BufferedReader(new InputStreamReader(textSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(textSocket.getOutputStream()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing I/O", e);
        }
    }

    @Override
    public void run() {
        try {
            sendMessage("Enter 'random' for anonymous chat or your username:");
            String input;
            do {
                input = in.readLine();
            } while (input != null && input.startsWith("FILE_PORT:"));

            if ("random".equalsIgnoreCase(input.trim())) {
                isAnonymous = true;
                anonymousName = generateAnonymousName("User");
                sendMessage("Waiting for a random partner...");

                ChatRoom tempRoom = matchMaker.requestMatch(anonymousName);
                if (tempRoom == null) return;

                for (String user : tempRoom.getParticipants()) {
                    if (!user.equals(anonymousName)) {
                        partnerHandler = userHandlers.get(user);
                    }
                }

                if (partnerHandler != null) {
                    partnerHandler.partnerHandler = this;
                    partnerHandler.isAnonymous = true;
                    partnerHandler.anonymousName = generateAnonymousName("User");

                    partnerHandler.sendMessage("You are now connected to a random partner.");
                    sendMessage("You are now connected to a random partner.");
                }

            } else {
                username = input.trim();
                userHandlers.put(username, this);

                sendMessage("Enter comma-separated usernames to join (including yourself):");
                String line = in.readLine();
                Set<String> participants = parseParticipants(line);
                participants.add(username);

                if (roomManager.hasRoom(participants)) {
                    chatRoom = roomManager.getRoom(participants);
                } else {
                    chatRoom = roomManager.createRoom(participants);
                }

                chatRoom.addParticipant(username);
                sendMessage("Joined chat room with: " + String.join(", ", chatRoom.getParticipants()));
            }

            String message;
            while ((message = in.readLine()) != null) {
                String senderName = isAnonymous ? anonymousName : username;
                String formatted = senderName + ": " + message;

                if (isAnonymous && partnerHandler != null) {
                    logAndSendToPartner("anonymous:" + getAnonymousRoomId(), formatted);
                } else if (chatRoom != null) {
                    logAndBroadcast(chatRoom.getRoomId(), formatted);
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Connection lost", e);
        } finally {
            cleanup();
        }
    }


    private void handleFileTransfer() {
        try (ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {
            while (!fileServerSocket.isClosed()) {
                Socket fileSocket = fileServerSocket.accept();
                try (DataInputStream dis = new DataInputStream(fileSocket.getInputStream())) {
                    FileReceiver.receiveFile(dis, receivedFile -> {
                        String fileName = receivedFile.getName();
                        String notice = "[System] " + (isAnonymous ? anonymousName : username) + " uploaded a file: " + fileName;

                        if (isAnonymous && partnerHandler != null) {
                            partnerHandler.sendMessage(notice);
                        } else if (chatRoom != null) {
                            chatRoom.addMessage(notice);
                            broadcast(notice);
                        }
                    });


                } catch (IOException e) {
                    logger.log(Level.WARNING, "File transfer failed", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "File server error", e);
        }
    }

    private void logAndBroadcast(String roomId, String message) {
        String hashedId = LogFileUtil.hashRoomId(roomId);
        ChatLogger.log(hashedId, message);
        broadcast(message);
    }

    private void logAndSendToPartner(String roomId, String message) {
        String hashedId = LogFileUtil.hashRoomId(roomId);
        ChatLogger.log(hashedId, message);
        if (partnerHandler != null) {
            partnerHandler.sendMessage(message);
        }
    }

    private String getAnonymousRoomId() {
        // 기준: 두 유저 이름 정렬해서 고정 순서로 만들기
        List<String> names = new ArrayList<>();
        names.add(anonymousName);
        if (partnerHandler != null) {
            names.add(partnerHandler.anonymousName);
        }
        Collections.sort(names);
        return String.join("_", names);
    }

    private void broadcast(String message) {
        for (String participant : chatRoom.getParticipants()) {
            if (!participant.equals(username)) {
                ClientHandler handler = userHandlers.get(participant);
                if (handler != null) {
                    handler.sendMessage(message);
                }
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to send message", e);
        }
    }

    private Set<String> parseParticipants(String input) {
        Set<String> set = new HashSet<>();
        if (input != null && !input.trim().isEmpty()) {
            String[] names = input.split(",");
            for (String name : names) {
                set.add(name.trim());
            }
        }
        return set;
    }

    private static String generateAnonymousName(String base) {
        int count = anonCount.merge(base, 1, Integer::sum);
        return base + count;
    }

    private void cleanup() {
        try {
            if (isAnonymous && partnerHandler != null) {
                partnerHandler.sendMessage("[System] Your partner has left the chat.");
                partnerHandler.partnerHandler = null;
            }

            if (username != null) {
                userHandlers.remove(username);
            }

            if (chatRoom != null) {
                chatRoom.removeParticipant(username);
            }

            if (in != null) in.close();
            if (out != null) out.close();
            textSocket.close();

        } catch (IOException e) {
            logger.log(Level.WARNING, "Cleanup failed", e);
        }
    }
}
