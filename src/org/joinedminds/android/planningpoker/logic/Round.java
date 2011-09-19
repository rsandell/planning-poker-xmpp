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

import java.util.LinkedList;
import java.util.List;

/**
 * Created: 9/18/11 2:56 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class Round {

    private List<Player> players;

    public Round(List<String> players) {
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
