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
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.joinedminds.android.planningpoker.logic.PlanningListener;
import org.joinedminds.android.planningpoker.logic.PlanningManager;

import java.io.IOException;

import static org.joinedminds.android.planningpoker.Constants.*;
import static org.joinedminds.android.planningpoker.Constants.CARD_VALUES;

/**
 * Created: 9/13/11 8:25 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlanningActivity extends Activity implements PlanningListener {

    public static final int OPTION_INVITE_TO_PLAN = 3;

    private GridView gridView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        PlanningManager.getInstance().setPlanningListener(this);
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
    public void communicationError(Exception ex) {
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void invite(String fromUser, String[] participants) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void newRound(String fromUser) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void cardSelect(final String fromUser, final String card) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PlanningActivity.this, fromUser + " Card: " + card, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void inviteResponse(String fromUser, boolean accepted) {
        Resources res = getResources();
        if (accepted) {
            Toast.makeText(this, res.getString(R.string.userAccepted, fromUser), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, res.getString(R.string.userDeclined, fromUser), Toast.LENGTH_LONG).show();
        }
    }
}
