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

package org.joinedminds.android.planningpoker.components;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.joinedminds.android.planningpoker.R;
import org.joinedminds.android.planningpoker.logic.Round;

/**
 * Created: 9/19/11 10:15 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class PlayerCardsAdapter extends BaseAdapter {
    private Activity context;
    private Round round;

    public PlayerCardsAdapter(final Activity context, Round round) {
        this.context = context;
        this.round = round;
    }

    public synchronized void setRound(Round r) {
        this.round = r;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return round.getPlayers().size();
    }

    @Override
    public Object getItem(int i) {
        return round.getPlayers().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.player_card, null);
            holder = new ViewHolder(
                    (TextView)convertView.findViewById(R.id.showandtell_card),
                    (TextView)convertView.findViewById(R.id.showandtell_playername));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        Round.Player player = round.getPlayers().get(i);
        if (player.getCard() == null) {
            holder.card.setText("...");
        } else if (round.areAllPlayersDone()) {
            holder.card.setText(player.getCard());
        } else {
            holder.card.setText("X");
        }
        holder.name.setText(player.getName());
        return convertView;
    }

    static class ViewHolder {
        TextView card;
        TextView name;

        ViewHolder(TextView card, TextView name) {
            this.card = card;
            this.name = name;
        }

        ViewHolder() {
        }
    }
}
