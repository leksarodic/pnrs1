package rodic.aleksa.miberchatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity {

    private Button mSend;
    private ImageButton mLogout, mRefresh;

    private TextView mNameOfUserChatWith;

    private EditText mMessage;
    private String message;

    private MessageListAdapter messageListAdapter;
    private ListView listOfMessages;

    private String nameAndLastname;
    private String idOfSelectedUser;
    private String currentUserUsername;

    SharedPreferences sharedPreferences;
    String currentUserSessionId;

    // Database
    SQLHelper sqlHelper;

    // HTTP
    private HTTPHelper httpHelper;
    private Handler handler;

    private ArrayList<Message> messages;

    // Logout user
    protected void logOut() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    final ServerRespond serverRespond = httpHelper.postJSONObject(Constants.HTTP_LOGOUT, jsonObject, currentUserSessionId);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (serverRespond.getCode() == 200) {
                                Toast.makeText(MessageActivity.this, R.string.successfully_logout, Toast.LENGTH_SHORT).show();

                                Intent main = new Intent(MessageActivity.this, MainActivity.class);
                                startActivity(main);
                                finish();  // closing contacts activity
                            } else if (serverRespond.getCode() == 404) {
                                Toast.makeText(MessageActivity.this, R.string.user_is_not_logged_in, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Getting list of messages
    protected void refreshMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerRespond serverRespond = httpHelper.getJSON(Constants.HTTP_MESSAGE + idOfSelectedUser, currentUserSessionId);
                    JSONArray jsonArray = serverRespond.getJsonArray();

                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String sender = jsonObject.getString("sender");
                            String messageData = jsonObject.getString("data");
                            int messageDataLen = messageData.length();

                            // Decrypt message after receive
                            EncryptMessage encryptMessage = new EncryptMessage();
                            messageData = encryptMessage.encryptDecrypt(messageData, messageDataLen);

                            if (sender.equals(currentUserUsername)) {
                                Message message = new Message(currentUserUsername, idOfSelectedUser, messageData);
                                messages.add(message);
                            } else {
                                Message message = new Message(idOfSelectedUser, currentUserUsername, messageData);
                                messages.add(message);
                            }
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (serverRespond.getCode() == 200) {
                                Toast.makeText(MessageActivity.this, R.string.successfully_refreshed, Toast.LENGTH_SHORT).show();
                                messageListAdapter.setMessages(messages);
                                listOfMessages.setAdapter(messageListAdapter);
                            } else if (serverRespond.getCode() == 404) {
                                Toast.makeText(MessageActivity.this, R.string.user_is_not_logged_in_or_receiver_does_not_exist, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Database usage
        sqlHelper = new SQLHelper(this, Constants.APPLICATION_DATABASE, null, 1);

        // SharedPreferences usage
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFF, Context.MODE_PRIVATE);

        // HTTP
        handler = new Handler();
        httpHelper = new HTTPHelper();
        messages = new ArrayList<>();

        // Getting current user id
        currentUserSessionId = sharedPreferences.getString(Constants.SHARED_PREFF_USER_ID, "none");
        currentUserUsername = sharedPreferences.getString(Constants.SHARED_PREFF_LOGED_USERNAME, "none");

        // Handling header
        final Intent resources = getIntent();
        if (resources != null) {
            nameAndLastname = resources.getStringExtra(Constants.BUNDLE_KEY_NAME_AND_LASTNAME);
            idOfSelectedUser = resources.getStringExtra(Constants.BUNDLE_KEY_ID_OF_MESSAGE_RECEIVER);
        }

        mNameOfUserChatWith = findViewById(R.id.nameOfUserChatWith);
        mNameOfUserChatWith.setText(nameAndLastname);

        mRefresh = findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messages.clear();  // clean previous messages
                refreshMessages();
            }
        });

        mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        // Showing messages, list
        listOfMessages = findViewById(R.id.listOfMessages);
        messageListAdapter = new MessageListAdapter(this);
        refreshMessages();  // get messages from server

        // Remove message on long click
        listOfMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final int listIdOfMessageLongPressedOn = i;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final ServerRespond serverRespond = httpHelper.deleteJSON(Constants.HTTP_MESSAGE_DELETE, messages.get(listIdOfMessageLongPressedOn), currentUserSessionId);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (serverRespond.getCode() == 200){
                                        Toast.makeText(getApplicationContext(), R.string.message_deleted, Toast.LENGTH_SHORT).show();
                                        messages.remove(listIdOfMessageLongPressedOn);
                                        messageListAdapter.notifyDataSetChanged();
                                    } else if (serverRespond.getCode() == 400){
                                        Toast.makeText(getApplicationContext(), R.string.error_deleting_message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                return true;
            }
        });

        // Send message
        mMessage = findViewById(R.id.messageToSend);
        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= Constants.MINIMUM_MESSAGE_LENGTH) {
                    mSend.setEnabled(true);
                } else {
                    mSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mSend = findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send new message
                message = mMessage.getText().toString();
                int messageLen = message.length();

                // Crypt message before send
                EncryptMessage encryptMessage = new EncryptMessage();
                message = encryptMessage.encryptDecrypt(message, messageLen);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("receiver", idOfSelectedUser);
                            jsonObject.put("data", message);

                            final ServerRespond serverRespond = httpHelper.postJSONObject(Constants.HTTP_MESSAGE, jsonObject, currentUserSessionId);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    if (serverRespond.getCode() == 200) {
                                        Toast.makeText(MessageActivity.this, R.string.message_is_sent, Toast.LENGTH_SHORT).show();

                                        // Sender to sam ja
                                        Message message = new Message(currentUserUsername, idOfSelectedUser, mMessage.getText().toString());
                                        messages.add(message);
                                        messageListAdapter.notifyDataSetChanged();

                                        mMessage.setText("");  // after sending a message remove it form EditText
                                    } else if (serverRespond.getCode() == 400) {
                                        Toast.makeText(MessageActivity.this, R.string.wrong_body_parameters, Toast.LENGTH_SHORT).show();
                                    } else if (serverRespond.getCode() == 404) {
                                        Toast.makeText(MessageActivity.this, R.string.user_is_not_logged_in_or_receiver_does_not_exist, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                // Scroll down at the bottom when message is sent
                listOfMessages.setStackFromBottom(true);
                listOfMessages.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
        });
    }
}
