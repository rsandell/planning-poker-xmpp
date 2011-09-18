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

    public static class Participants {

        private List<Player> players;

        public Participants() {
            players = new LinkedList<Player>();
        }

        public void add(String participant) {
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
                }
            }
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
