/*
 * The MIT License
 *
 * Copyright 2011 Robert Sandell - sandell.robert@gmail.com. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.joinedminds.android.planningpoker;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.joinedminds.android.planningpoker.components.ParticipantsAcceptanceAdapter;
import org.joinedminds.android.planningpoker.components.PlayerCardsAdapter;
import org.joinedminds.android.planningpoker.logic.PlanningListener;
import org.joinedminds.android.planningpoker.logic.PlanningManager;
import org.joinedminds.android.planningpoker.logic.Round;
import org.joinedminds.android.planningpoker.logic.io.ChatSession;

import static org.joinedminds.android.planningpoker.Constants.CARD_VALUES;

/**
 * Created: 9/13/11 8:25 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlanningActivity extends Activity implements PlanningListener {

    /**
     * Data structure for the view players waiting list.
     */
    class PlayersWaitingList {
        ListView playersWaitingList;
        ParticipantsAcceptanceAdapter adapter;

        PlayersWaitingList() {
            setContentView(R.layout.playerslist);
            playersWaitingList = (ListView)findViewById(R.id.playersWaitingList);
            adapter = new ParticipantsAcceptanceAdapter(PlanningActivity.this,
                    PlanningManager.getInstance().getParticipants());
            playersWaitingList.setAdapter(adapter);
        }
    }

    /**
     * Data structure for the select card view.
     */
    class CardSelectView {
        GridView gridView;

        CardSelectView() {
            setContentView(R.layout.cardgrid);
            gridView = (GridView)findViewById(R.id.cardgrid);
            gridView.setAdapter(new ArrayAdapter<String>(PlanningActivity.this, R.layout.card, CARD_VALUES));
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (view instanceof TextView) {
                        cardSelected(((TextView)view).getText().toString());
                    }
                }
            });
        }
    }

    class CardsShowAndTellView {
        GridView gridView;
        TextView myCard;
        TextView myName;
        PlayerCardsAdapter adapter;

        CardsShowAndTellView(String myName, String myCard, Round round) {
            setContentView(R.layout.card_showandtell);
            gridView = (GridView)findViewById(R.id.cardgrid_showandtell);
            LinearLayout l = (LinearLayout)findViewById(R.id.showandtell_me);
            this.myCard = (TextView)l.findViewById(R.id.showandtell_card);
            this.myName = (TextView)l.findViewById(R.id.showandtell_playername);
            this.myCard.setText(myCard);
            this.myName.setText(myName);
            adapter = new PlayerCardsAdapter(PlanningActivity.this, round);
            gridView.setAdapter(adapter);
        }
    }


    private PlayersWaitingList playersWaitingList;
    private CardSelectView cardSelectView;
    private CardsShowAndTellView cardsShowAndTellView;
    private volatile Round currentRound;
    private static final int MENU_OPTION_NEW_ROUND = 9;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewPlayersList();
        PlanningManager.getInstance().setPlanningListener(this);
    }

    private void disableViews() {
        cardSelectView = null;
        playersWaitingList = null;
        cardsShowAndTellView = null;
    }

    private synchronized void setViewPlayersList() {
        disableViews();
        playersWaitingList = new PlayersWaitingList();
    }

    private synchronized void setViewCardGrid() {
        disableViews();
        cardSelectView = new CardSelectView();
    }

    private synchronized void setCardsShowAndTellView(String myCard, Round round) {
        disableViews();
        cardsShowAndTellView = new CardsShowAndTellView(PlanningManager.getInstance().getUsername(), myCard, round);
    }


    private void cardSelected(String s) {
        Toast.makeText(this, "Card Selected: " + s, Toast.LENGTH_SHORT).show();
        System.out.println("Card Selected: " + s);
        setCardsShowAndTellView(s, currentRound);
        try {
            PlanningManager.getInstance().sendCardSelect(s);
        } catch (Exception e) {
            communicationError(e);
            e.printStackTrace();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_OPTION_NEW_ROUND, Menu.NONE, getString(R.string.newRound));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case MENU_OPTION_NEW_ROUND: {
                callNewRound();
                return true;
            }
        }
        return false;
    }

    private void callNewRound() {
        try {
            PlanningManager.getInstance().newRound();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setViewCardGrid();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            communicationError(e);
        }
    }

    @Override
    public void communicationError(final Exception ex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PlanningActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void invite(String fromUser, String[] participants) {
        //Ignore, already happened.
    }

    @Override
    public synchronized void newRound(final String fromUser, final Round round) {
        currentRound = round;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setViewCardGrid();
                Toast.makeText(PlanningActivity.this, fromUser + " requests a new round.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public synchronized void cardSelect(final String fromUser, final String card, final Round round) {
        currentRound = round;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PlanningActivity.this, fromUser + " Selected a card", Toast.LENGTH_SHORT).show();
                if (cardsShowAndTellView != null) {
                    cardsShowAndTellView.adapter.setRound(round);
                }
            }
        });
    }

    @Override
    public synchronized void inviteResponse(final String fromUser, final boolean accepted, final ChatSession.Participants participants) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playersWaitingList != null) {
                    playersWaitingList.adapter.setParticipants(participants.getPlayers());
                }
                Resources res = getResources();
                if (accepted) {
                    Toast.makeText(PlanningActivity.this, res.getString(R.string.userAccepted, fromUser), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PlanningActivity.this, res.getString(R.string.userDeclined, fromUser), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
