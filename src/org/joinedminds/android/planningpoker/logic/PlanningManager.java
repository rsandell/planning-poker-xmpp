/*
 *  The MIT License
 *
 *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.joinedminds.android.planningpoker.logic;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class PlanningManager implements MessageListener {

    private String server;
    private int port;
    private String username;
    private String password;
    private static PlanningManager instance;
    private Connection connection;
    private ChatSession session;

    private PlanningManager(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    protected void connect() throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(server, port);
        connection = new XMPPConnection(config);
        connection.connect();
        connection.login(username, password, "planning-poker-xmpp");
    }

    public static void initiate(String server, int port, String username, String password) throws XMPPException {
        try {
            instance = new PlanningManager(server, port, username, password);
            instance.connect();
        } catch (XMPPException e) {
            e.printStackTrace();
            instance = null;
            throw e;
        }
    }

    public static PlanningManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("You need to initiate the manager first.");
        }
        return instance;
    }

    public void startPlanning(String[] users) {
        List<Chat> chatList = new LinkedList<Chat>();
        for (String user : users) {
            Chat c = connection.getChatManager().createChat(user, "planning", this);
            chatList.add(c);
        }
        session = new ChatSession(chatList);
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static class ChatSession {
        private List<Chat> chatList;

        ChatSession(List<Chat> chatList) {
            this.chatList = chatList;
        }

        public void sendInvite() throws IOException, XMPPException {
            StringBuilder str = new StringBuilder();
            for(Chat chat : chatList) {
                str.append(chat.getParticipant()).append(";");
            }
            sendMessage(MessageProperties.
                    create("type", "invite").
                    put("participants", str.toString()).
                    toMessageString());
        }

        public void sendMessage(String message) throws XMPPException {
            for(Chat chat : chatList) {
                chat.sendMessage(message);
            }
        }
    }

    static enum MessageKey {
        Type, Participants
    }

    static class MessageProperties {
        Properties properties = new Properties();

        public static MessageProperties create(String name, String value) {
            return new MessageProperties().put(name, value);
        }

        public MessageProperties put(String name, String value) {
            properties.setProperty(name, value);
            return this;
        }

        public String toMessageString() throws IOException {
            StringWriter out = new StringWriter();
            properties.store(out, "Planning-Command");
            return out.toString();
        }

        public static MessageProperties fromMessageString(String message) throws IOException {
            MessageProperties properties = new MessageProperties();
            properties.properties.load(new StringReader(message));
            return properties;
        }
    }
}
