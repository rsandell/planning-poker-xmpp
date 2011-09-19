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

package org.joinedminds.android.planningpoker.logic.io;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.joinedminds.android.planningpoker.logic.PlanningManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
* Created: 9/18/11 8:13 PM
*
* @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
*/
public class ChatSession {
    private List<Chat> chatList;
    private String username;
    private Participants participants;

    public ChatSession(List<Chat> chatList, String username) {
        this.chatList = chatList;
        this.username = username;
        participants = new Participants();
    }

    public void sendInvite() throws IOException, XMPPException {
        StringBuilder str = new StringBuilder();
        participants = new Participants();
        participants.add(username);
        participants.setAccepted(username);
        for (Chat chat : chatList) {
            String participant = chat.getParticipant();
            str.append(participant).append(PlanningManager.PARTICIPANTS_SEPARATOR);
            participants.add(participant);
        }
        MessageProperties.
                create(PlanningManager.Type.Invite).
                put(PlanningManager.MessageKey.Participants, str.toString()).
                send(this);
    }

    public void initParticipants(String fromUser, String[] others) {
        participants = new Participants();
        participants.add(fromUser);
        participants.setAccepted(fromUser);
        for (String p : others) {
            participants.add(p);
        }
    }

    public void sendMessage(MessageProperties message) throws XMPPException, IOException {
        sendMessage(message.put(PlanningManager.MessageKey.User, username).toMessageString());
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


    public Participants getParticipants() {
        return participants;
    }

    public List<String> getAcceptedParticipants() {
        List<String> ap = new LinkedList<String>();
        for(Participants.Player p : participants.players) {
            if(p.getStatus() == Participants.Player.Status.Accepted) {
                ap.add(p.getName());
            }
        }
        return ap;
    }

    public void participantDeclined(String user) {
        participants.setDeclined(user);
        for (Chat ch : chatList) {
            if(ch.getParticipant().equals(user)) {
                chatList.remove(ch);
                break;
            }
        }
    }

    public void participantAccepted(String user) {
        participants.setAccepted(user);
    }

    public void close(PlanningManager planningManager) {
        for (Chat c : chatList) {
            c.removeMessageListener(planningManager);
        }
    }

    public static class Participants {

        private List<Player> players;

        public Participants() {
            players = new LinkedList<Player>();
        }

        public void add(String participant) {
            System.out.println("Adding " + participant);
            players.add(new Player(participant));
        }

        public void setAccepted(String name) {
            setStatus(name, Player.Status.Accepted);
        }

        public void setDeclined(String name) {
            setStatus(name, Player.Status.Declined);
        }

        public void setStatus(String name, Player.Status status) {
            for (Player player : players) {
                if (player.name.equals(name)) {
                    player.setStatus(status);
                    System.out.println("Status changed for " + player.getName() + " to " + status.name());
                    return;
                }
            }
            System.err.println("Status could not be set for " + name + " (not found)");
        }

        public List<Player> getPlayers() {
            return players;
        }

        public static class Player {
            String name;
            Status status;

            Player(String name) {
                this.name = name;
                status = Status.Unknown;
            }

            public String getName() {
                return name;
            }

            public Status getStatus() {
                return status;
            }

            public void setStatus(Status status) {
                this.status = status;
            }

            public static enum Status {
                Accepted, Declined, Unknown
            }
        }
    }
}
