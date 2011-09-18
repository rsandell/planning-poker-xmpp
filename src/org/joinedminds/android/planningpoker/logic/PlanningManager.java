/*
 *  The MIT License
 *
 *  Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
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

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlanningManager implements MessageListener, ChatManagerListener {

    public static final String PARTICIPANTS_SEPARATOR = ";";
    public static final String APP_CHAT_RESOURCE = "planning-poker-xmpp";
    private String server;
    private int port;
    private String username;
    private String password;
    private PlanningListener listener;
    private static PlanningManager instance;
    private Connection connection;
    private ChatSession session;

    private PlanningManager(String server, int port, String username, String password, PlanningListener listener) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.listener = listener;
    }

    protected void connect() throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(server, port);
        connection = new XMPPConnection(config);
        connection.connect();
        connection.login(username, password, APP_CHAT_RESOURCE);
        connection.getChatManager().addChatListener(this);
    }

    public void setPlanningListener(PlanningListener listener) {
        this.listener = listener;
    }

    public static void initiate(String server, int port, String username, String password, PlanningListener listener) throws XMPPException {
        try {
            instance = new PlanningManager(server, port, username, password, listener);
            instance.connect();
        } catch (XMPPException e) {
            e.printStackTrace();
            instance = null;
            throw e;
        }
    }

    public static boolean isInitiated() {
        return instance != null;
    }

    public static PlanningManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("You need to initiate the manager first.");
        }
        return instance;
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void acceptInvite() throws IOException, XMPPException {
        MessageProperties.create(Type.InviteResponse).put(MessageKey.Response, Boolean.TRUE.toString()).send(session);
    }

    public void declineInvite() throws IOException, XMPPException {
        MessageProperties.create(Type.InviteResponse).put(MessageKey.Response, Boolean.FALSE.toString()).send(session);
    }

    public void newRound() throws IOException, XMPPException {
        MessageProperties.create(Type.NewRound).send(session);
    }

    public void sendCardSelect(String card) throws IOException, XMPPException {
        MessageProperties.create(Type.CardSelect).put(MessageKey.Card, card).send(session);
    }

    public void startPlanning(String[] users) throws IOException, XMPPException {
        List<Chat> chatList = new LinkedList<Chat>();
        for (String user : users) {
            Chat c = connection.getChatManager().createChat(user, this);
            chatList.add(c);
        }
        session = new ChatSession(chatList, username);
        session.sendInvite();
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        if (!createdLocally) {
            session = new ChatSession(Arrays.asList(chat), username);
            chat.addMessageListener(this);
            System.out.println("Chat session created!");
        } else {

        }
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        try {
            session.echoToOthers(message, chat);
        } catch (XMPPException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.communicationError(e);
            }
        }
        if (listener != null) {
            try {
                MessageProperties properties = MessageProperties.fromMessageString(message.getBody());
                switch (properties.getType()) {
                    case Invite:
                        notifyInvite(properties);
                        break;
                    case NewRound:
                        listener.newRound(properties.getProperty(MessageKey.User));
                        break;
                    case CardSelect:
                        listener.cardSelect(properties.getProperty(MessageKey.User),
                                properties.getProperty(MessageKey.Card));
                        break;
                    case InviteResponse:
                        boolean accepted = properties.getBooleanProperty(MessageKey.Response);
                        if (!accepted) {
                            session.removeChat(chat);
                        }
                        listener.inviteResponse(properties.getProperty(MessageKey.User),
                                accepted);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                listener.communicationError(e);
            }
        }
    }

    private void notifyInvite(MessageProperties properties) {
        String[] participants = properties.getProperty(MessageKey.Participants, "").split(PARTICIPANTS_SEPARATOR);
        listener.invite(properties.getProperty(MessageKey.User), participants);
    }



    static class ChatSession {
        private List<Chat> chatList;
        private String username;

        ChatSession(List<Chat> chatList, String username) {
            this.chatList = chatList;
            this.username = username;
        }

        public void sendInvite() throws IOException, XMPPException {
            StringBuilder str = new StringBuilder();
            for (Chat chat : chatList) {
                str.append(chat.getParticipant()).append(PARTICIPANTS_SEPARATOR);
            }
            MessageProperties.
                    create(Type.Invite).
                    put(MessageKey.Participants, str.toString()).
                    send(this);
        }

        public void sendMessage(MessageProperties message) throws XMPPException, IOException {
            sendMessage(message.put(MessageKey.User, username).toMessageString());
        }

        public void sendMessage(String message) throws XMPPException {
            for (Chat chat : chatList) {
                chat.sendMessage(message);
            }
        }

        public void echoToOthers(Message message, Chat origin) throws XMPPException {
            String m = message.getBody();
            for (Chat chat : chatList) {
                if (chat != origin) {
                    chat.sendMessage(m);
                }
            }
        }

        public void removeChat(Chat chat) {
            chatList.remove(chat);
        }
    }

    static enum MessageKey {
        Type, Participants, Card, User, Response
    }

    static enum Type {
        Invite, CardSelect, NewRound, InviteResponse
    }

    static class MessageProperties {
        Properties properties = new Properties();

        public static MessageProperties create(String name, String value) {
            return new MessageProperties().put(name, value);
        }

        public static MessageProperties create(MessageKey key, String value) {
            return create(key.name(), value);
        }

        public static MessageProperties create(Type type) {
            return create(MessageKey.Type, type.name());
        }

        public MessageProperties put(MessageKey key, String value) {
            return put(key.name(), value);
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

        public void send(ChatSession session) throws IOException, XMPPException {
            session.sendMessage(this);
        }

        public String getProperty(MessageKey key) {
            return properties.getProperty(key.name());
        }

        public String getProperty(MessageKey key, String defaultValue) {
            return properties.getProperty(key.name(), defaultValue);
        }

        public Type getType() {
            String str = getProperty(MessageKey.Type);
            if (str != null) {
                try {
                    return Type.valueOf(str);
                } catch (IllegalArgumentException e) {
                    System.err.println("Bad type from sender: " + str + ": " + e.getMessage());
                    return null;
                }
            } else {
                return null;
            }
        }

        public boolean getBooleanProperty(MessageKey key) {
            String val = getProperty(key);
            return Boolean.TRUE.toString().equalsIgnoreCase(val);
        }
    }
}
