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
import org.joinedminds.android.planningpoker.logic.io.ChatSession;
import org.joinedminds.android.planningpoker.logic.io.MessageProperties;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    private Round round;

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

    public List<ChatSession.Participants.Player> getParticipants() {
        if (session != null) {
            return session.getParticipants().getPlayers();
        }
        return new LinkedList<ChatSession.Participants.Player>();
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
        this.round = new Round(session.getAcceptedParticipants());
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
        acceptInvite();
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
                        this.round = new Round(session.getAcceptedParticipants());
                        listener.newRound(properties.getProperty(MessageKey.User), round);
                        break;
                    case CardSelect:
                        String user1 = properties.getProperty(MessageKey.User);
                        String card = properties.getProperty(MessageKey.Card);
                        if (round != null) {
                            round.setCard(user1,  card);
                        }
                        listener.cardSelect(user1, card, round);
                        break;
                    case InviteResponse:
                        String user = properties.getProperty(MessageKey.User);
                        boolean accepted = properties.getBooleanProperty(MessageKey.Response);
                        if (!accepted) {
                            session.participantDeclined(user);
                        } else {
                            session.participantAccepted(user);
                        }
                        listener.inviteResponse(user, accepted, session.getParticipants());
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


    public static enum MessageKey {
        Type, Participants, Card, User, Response
    }

    public static enum Type {
        Invite, CardSelect, NewRound, InviteResponse
    }



}
