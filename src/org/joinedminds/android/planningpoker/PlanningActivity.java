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
import android.view.View;
import android.widget.*;
import org.joinedminds.android.planningpoker.components.ParticipantsAcceptanceAdapter;
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

    public static final int OPTION_INVITE_TO_PLAN = 3;

    private GridView gridView;
    private ListView playersWaitingList;
    private ParticipantsAcceptanceAdapter participantsAcceptanceAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewPlayersList();
        PlanningManager.getInstance().setPlanningListener(this);
    }

    private void disableViews() {
        disableViewCardGrid();
        disableViewPlayersList();
    }

    private void disableViewCardGrid() {
        gridView = null;
    }

    private void disableViewPlayersList() {
        playersWaitingList = null;
        participantsAcceptanceAdapter = null;
    }

    private void setViewPlayersList() {
        disableViews();
        setContentView(R.layout.playerslist);
        playersWaitingList = (ListView)findViewById(R.id.playersWaitingList);
        participantsAcceptanceAdapter = new ParticipantsAcceptanceAdapter(this,
                PlanningManager.getInstance().getParticipants());
        playersWaitingList.setAdapter(participantsAcceptanceAdapter);
    }

    private void setViewCardGrid() {
        disableViews();
        setContentView(R.layout.cardgrid);
        gridView = (GridView)findViewById(R.id.cardgrid);
        gridView.setAdapter(new ArrayAdapter<String>(this, R.layout.card, CARD_VALUES));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    cardSelected(((TextView)view).getText().toString());
                }
            }
        });
    }


    private void cardSelected(String s) {
        Toast.makeText(this, "Card Selected: " + s, Toast.LENGTH_SHORT).show();
        System.out.println("Card Selected: " + s);
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
    public void newRound(String fromUser, Round round) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void cardSelect(final String fromUser, final String card, final Round round) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PlanningActivity.this, fromUser + " Selected a card", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void inviteResponse(final String fromUser, final boolean accepted, final ChatSession.Participants participants) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (participantsAcceptanceAdapter != null) {
                    participantsAcceptanceAdapter.setParticipants(participants.getPlayers());
                    participantsAcceptanceAdapter.notifyDataSetChanged();
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
