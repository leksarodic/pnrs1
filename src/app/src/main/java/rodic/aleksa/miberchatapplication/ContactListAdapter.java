package rodic.aleksa.miberchatapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Aleksa on 4.4.2018.
 */

public class ContactListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Contact> contacts;

    public ContactListAdapter(Context context) {
        this.context = context;
        contacts = new ArrayList<>();
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int i) {
        return contacts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class ViewHolder {
        TextView firstLetter;
        TextView nameAndLastname;
        ImageButton enterMessages;

        public ViewHolder(View view) {
            firstLetter = view.findViewById(R.id.firstLetterOfName);
            nameAndLastname = view.findViewById(R.id.nameAndLastName);
            enterMessages = view.findViewById(R.id.enterMessages);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.contact_list, null);

            viewHolder = new ViewHolder(view);

            view.setTag(viewHolder);  // pamtimo sta smo inicijalizovali da nebi morali ponovo
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final Contact contact = contacts.get(i);

        final String nameAndLastname = contact.getFirstname() + " " + contact.getLastname();

        viewHolder.firstLetter.setBackgroundColor(contact.getColor());
        viewHolder.nameAndLastname.setText(nameAndLastname);
        viewHolder.firstLetter.setText(contact.getFirstname().substring(0, 1).toUpperCase());  // uzimamo prvi karakter imena i pravimo ga da bude veliko slovo
        viewHolder.enterMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent message = new Intent(view.getContext(), MessageActivity.class);

                Bundle transferNameAndLastname = new Bundle();
                transferNameAndLastname.putString(Constants.BUNDLE_KEY_ID_OF_MESSAGE_RECEIVER, contact.getUsername());
                transferNameAndLastname.putString(Constants.BUNDLE_KEY_NAME_AND_LASTNAME, nameAndLastname);

                message.putExtras(transferNameAndLastname);
                context.startActivity(message);
            }
        });


        return view;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }
}
