package org.joinedminds.android.planningpoker.components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.joinedminds.android.planningpoker.R;
import org.joinedminds.android.planningpoker.logic.io.ChatSession;

import java.util.List;

/**
 * Created: 9/18/11 9:49 PM
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class ParticipantsAcceptanceAdapter extends BaseAdapter {

    private List<ChatSession.Participants.Player> participants;
    private Activity context;

    public ParticipantsAcceptanceAdapter(Activity context, List<ChatSession.Participants.Player> participants) {
        this.context = context;
        this.participants = participants;
    }

    public synchronized List<ChatSession.Participants.Player> getParticipants() {
        return participants;
    }

    public synchronized void setParticipants(List<ChatSession.Participants.Player> participants) {
        this.participants = participants;
    }

    @Override
    public int getCount() {
        return getParticipants().size();
    }

    @Override
    public ChatSession.Participants.Player getItem(int i) {
        return getParticipants().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ChatSession.Participants.Player player = getParticipants().get(i);
        ViewHolder holder;

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.player_status, null);
            holder = new ViewHolder(
                    (TextView)convertView.findViewById(R.id.playerStatusCard),
                    (TextView)convertView.findViewById(R.id.playerStatusName));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        switch (player.getStatus()) {
            case Accepted:
                holder.card.setText("OK");
                holder.card.setTextColor(Color.GREEN);
                break;
            case Declined:
                holder.card.setText("NO");
                holder.card.setTextColor(Color.RED);
                break;
            case Unknown:
                holder.card.setText("?");
                holder.card.setTextColor(Color.GRAY);
                break;
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
