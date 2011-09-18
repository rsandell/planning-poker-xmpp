package org.joinedminds.android.planningpoker.logic;

import java.util.LinkedList;
import java.util.List;

/**
 * Created: 9/18/11 2:56 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class Round {

    private List<Player> players;

    public Round(String[] players) {
        this.players = new LinkedList<Player>();
        for(String name : players) {
            this.players.add(new Player(name));
        }
    }

    public boolean areAllPlayersDone() {
        for (Player p : players) {
            if (p.getCard() == null) {
                return false;
            }
        }
        return true;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setCard(String player, String card) {
        for (Player p : players) {
            if(p.getName().equals(player)) {
                p.setCard(card);
                return;
            }
        }
        //We have missed him?W
        players.add(new Player(player, card));
    }

    public static class Player {
        private String name;
        private String card;

        public Player(String name) {
            this.name = name;
        }

        public Player(String name, String card) {
            this.name = name;
            this.card = card;
        }

        public String getName() {
            return name;
        }

        public String getCard() {
            return card;
        }

        public void setCard(String card) {
            this.card = card;
        }
    }
}
