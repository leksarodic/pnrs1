package rodic.aleksa.miberchatapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Aleksa on 4.4.2018.
 */

public class MessageListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Message> messages;
    private SharedPreferences sharedPreferences;

    MessageListAdapter(Context context) {
        messages = new ArrayList<>();
        this.context = context;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class ViewHolder {
        TextView message;
        TextView username;

        public ViewHolder(View view) {
            message = view.findViewById(R.id.message);
            username = view.findViewById(R.id.username);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.message_list, null);

            viewHolder = new ViewHolder(view);

            view.setTag(viewHolder);  // remember initialization
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Message newMessage = messages.get(i);  // getting i message

        // Setting message
        viewHolder.message.setText(newMessage.getMessage());

        // SharedPreferences usage
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFF, Context.MODE_PRIVATE);

        // Getting current user id
        final String currentUserUsername = sharedPreferences.getString(Constants.SHARED_PREFF_LOGED_USERNAME, "none");

        // Change color on even and odd position
        if (currentUserUsername.equals(newMessage.getSenderId())) {
            viewHolder.username.setText(currentUserUsername);
            viewHolder.username.setGravity(Gravity.RIGHT);
            viewHolder.message.setBackgroundColor(context.getResources().getColor(R.color.colorOdd));
            viewHolder.message.setGravity(Gravity.RIGHT | Gravity.CENTER);
        } else {
            viewHolder.username.setText(newMessage.getSenderId());
            viewHolder.username.setGravity(Gravity.LEFT);
            viewHolder.message.setBackgroundColor(context.getResources().getColor(R.color.colorEven));
            viewHolder.message.setGravity(Gravity.LEFT | Gravity.CENTER);
        }

        return view;
    }

    // Adding new message on list
    public void addMessage(Message message) {
        messages.add(message);
    }

    // Removing message from list
    public void removeMessage(Message message) {
        messages.remove(message);
    }
}
