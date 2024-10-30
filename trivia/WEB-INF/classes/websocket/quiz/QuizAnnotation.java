/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package websocket.quiz;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.google.gson.Gson; 

// import org.apache.juli.logging.Log;
// import org.apache.juli.logging.LogFactory;

@ServerEndpoint(value = "/websocket/quiz")
public class QuizAnnotation {

    // private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<QuizAnnotation> connections = new CopyOnWriteArraySet<>();

    private final int playerId; //String nickname
    private Session session;
    /*
     * The queue of messages that may build up while another message is being sent. The thread that sends a message is
     * responsible for clearing any queue that builds up while that message is being sent.
     */
    private Queue<String> messageBacklog = new ArrayDeque<>();
    private boolean messageInProgress = false;

    public QuizAnnotation() {
        // nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
        playerId = connectionIds.getAndIncrement();
    }


    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        // String message = String.format("* %s %s", playerId, "has joined.");

        Gson gson = new Gson();

        // String test = "{\"id\":\"123\",\"dataType\":\"info\",\"message\":\"Player 3 has joined.\"}";

        // AnswerData a1 = new AnswerData("ans1 text", "ans1 id");
        // AnswerData a2 = new AnswerData("ans2 text", "ans2 id");
        // AnswerData[] aa = {a1, a2};

        QuizData qd = new QuizData(playerId, "join", "Player " + playerId + " has joined.");
        // QuizData qd = new QuizData("test id", "test type", "test message", aa);
        System.out.println(gson.toJson(qd));

        // QuizData testqd = gson.fromJson(gson.toJson(qd), QuizData.class);

        // System.out.println(testqd.stringRep());
        
        broadcast(gson.toJson(qd));
    }


    @OnClose
    public void end() {
        connections.remove(this);

        Gson gson = new Gson();

        QuizData qd = new QuizData(playerId, "disconnect", "Player " + playerId + " has disconnected.");
        System.out.println(gson.toJson(qd));
        
        broadcast(gson.toJson(qd));
    }


    @OnMessage
    public void incoming(String message) {
        // Parse message and respond appropriately
        Gson gson = new Gson();

        QuizData qd = gson.fromJson(message, QuizData.class);
        qd.setId(playerId);

        // String filteredMessage = message.toString();
        // broadcast(filteredMessage);
        broadcast(gson.toJson(qd));
    }


    @OnError
    public void onError(Throwable t) throws Throwable {
        // log.error("Chat Error: " + t.toString(), t);
        System.out.println("Webhook error");
    }


    private class QuizData {
        private int id;
        private String dataType;
        private String message;
        private AnswerData[] answers;
        private String selection;

        QuizData() {
            id = 9999;
            dataType = "NULL";
        }

        QuizData(String dataType, String message) {
            this.dataType = dataType;
            this.message = message;
        }

        QuizData(int id, String dataType, String message) {
            this.id = id;
            this.dataType = dataType;
            this.message = message;
        }

        QuizData(String dataType, String message, AnswerData[] answers) {
            // this.id = id;
            this.dataType = dataType;
            this.message = message;
            this.answers = answers;
        }

        QuizData(String dataType, String message, String selection) {
            // this.id = id;
            this.dataType = dataType;
            this.message = message;
            this.selection = selection;
        }

        void setId(int id) {
            this.id = id;
        }

        String stringRep() {
            return "id: " + id + ", type: " + dataType + ", message: " + message;
        }
    }

    private class AnswerData {
        private String text;
        private String id;

        AnswerData(String text, String id) {
            this.text = text;
            this.id = id;
        }
    }




    /*
     * synchronized blocks are limited to operations that are expected to be quick. More specifically, messages are not
     * sent from within a synchronized block.
     */
    private void sendMessage(String msg) throws IOException {

        synchronized (this) {
            if (messageInProgress) {
                messageBacklog.add(msg);
                return;
            } else {
                messageInProgress = true;
            }
        }

        boolean queueHasMessagesToBeSent = true;

        String messageToSend = msg;
        do {
            session.getBasicRemote().sendText(messageToSend);
            synchronized (this) {
                messageToSend = messageBacklog.poll();
                if (messageToSend == null) {
                    messageInProgress = false;
                    queueHasMessagesToBeSent = false;
                }
            }

        } while (queueHasMessagesToBeSent);
     }


    private static void broadcast(String msg) {
        for (QuizAnnotation client : connections) {
            try {
                client.sendMessage(msg);
            } catch (IOException e) {
                // log.debug("Chat Error: Failed to send message to client", e);
                if (connections.remove(client)) {
                    try {
                        client.session.close();
                    } catch (IOException e1) {
                        // Ignore
                    }
                    String message = String.format("* %s %s", client.playerId, "has been disconnected.");
                    broadcast(message);
                }
            }
        }
    }

    private static String filterHTML(String message) {

        if (message == null) {
            return null;
        }

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuilder result = new StringBuilder(content.length + 50);
        for (char c : content) {
            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(c);
            }
        }
        return result.toString();
    }
}
