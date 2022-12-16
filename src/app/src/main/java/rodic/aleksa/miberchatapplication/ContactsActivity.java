package rodic.aleksa.miberchatapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements ServiceConnection {

    private static final String LOG_TAG = "ContactsActivity";
    private TextView mHello;
    private ImageButton mLogout, mRefresh;

    private String username;
    private String currentUserUsername;

    private ContactListAdapter contactListAdapter;
    private ListView contactListView;

    SQLHelper sqlHelper;
    SharedPreferences sharedPreferences;
    String currentUserSessionId;

    private HTTPHelper httpHelper;
    private Handler handler;

    private ArrayList<Contact> contacts;

    // Service stuff
    private Intent intent;
    private IBinderNewMessage binder = null;
    boolean newMessageArrived;
    Context context;

    // Logout user
    protected void logOut(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    final ServerRespond serverRespond = httpHelper.postJSONObject(Constants.HTTP_LOGOUT, jsonObject, currentUserSessionId);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (serverRespond.getCode() == 200){
                                Toast.makeText(ContactsActivity.this, R.string.successfully_logout, Toast.LENGTH_SHORT).show();

                                stopService(intent);

                                Intent main = new Intent(ContactsActivity.this, MainActivity.class);
                                startActivity(main);
                                finish();  // closing contacts activity
                            } else if (serverRespond.getCode() == 404){
                                Toast.makeText(ContactsActivity.this, R.string.user_is_not_logged_in, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Getting list of contacts
    protected void refreshContacts(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerRespond serverRespond = httpHelper.getJSON(Constants.HTTP_CONTACTS, currentUserSessionId);
                    JSONArray jsonArray = serverRespond.getJsonArray();

                    if(jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String username = jsonObject.getString("username");

                            // Don't show current user in list
                            if (username.equals(currentUserUsername)) continue;

                            Contact contact = new Contact(username, username, "");
                            contacts.add(contact);

                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (serverRespond.getCode() == 200){
                                Toast.makeText(ContactsActivity.this, R.string.successfully_refreshed, Toast.LENGTH_SHORT).show();
                                contactListAdapter.setContacts(contacts);
                                contactListView.setAdapter(contactListAdapter);
                            } else if (serverRespond.getCode() == 404){
                                Toast.makeText(ContactsActivity.this, R.string.user_is_not_logged_in, Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_contacts);

        context = getApplicationContext();

        // Database usage
        sqlHelper = new SQLHelper(this, Constants.APPLICATION_DATABASE, null, 1);

        // Get username transfered by intent
        Intent resources = getIntent();
        if (resources != null) {
            username = resources.getStringExtra("username");
        }

        // SharedPreferences usage
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFF, Context.MODE_PRIVATE);

        // HTTP
        handler = new Handler();
        httpHelper = new HTTPHelper();
        contacts = new ArrayList<>();

        // Header
        mHello = findViewById(R.id.sayhello);
        String greetingsMessage = username + getString(R.string.namePlusContastListContacts);
        mHello.setText(greetingsMessage);

        // SharedPreferences getting current user session id
        currentUserSessionId = sharedPreferences.getString(Constants.SHARED_PREFF_USER_ID, "none");
        currentUserUsername = sharedPreferences.getString(Constants.SHARED_PREFF_LOGED_USERNAME, "none");

        // Setting up list view adapter
        contactListView = findViewById(R.id.listOfContacts);
        contactListAdapter = new ContactListAdapter(this);

        // Starting service
        intent = new Intent(this, BindService.class);
        if (!bindService(intent, this, Context.BIND_AUTO_CREATE)) {
            Log.d(LOG_TAG, "bind failed");
        }

        // Refresh contact list onCreate
        refreshContacts();

        mRefresh = findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contacts.clear();  // to prevent multiplying contacts
                refreshContacts();
            }
        });

        mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (binder != null) {
            unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(LOG_TAG, "onServiceConnected");
        binder = IBinderNewMessage.Stub.asInterface(iBinder);
        try{
            binder.setCallback(new Callback());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(LOG_TAG, "onServiceDisconnected");
        binder = null;
    }

    private class Callback extends ICallback.Stub {

        @Override
        public void onCallbackCall() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ServerRespond serverRespond = httpHelper.getJSON(Constants.HTTP_NEW_MESSAGE_SERVICE, currentUserSessionId);
                        String isNewMessage = serverRespond.getJsonString();

                        newMessageArrived = isNewMessage.equals("true\n");

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (serverRespond.getCode() == 200){
                                    if (newMessageArrived) {
                                        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                                                .setSmallIcon(R.drawable.ic_message_black_32dp)
                                                .setContentTitle("Notification")
                                                .setContentText(getString(R.string.new_message_arrived));

                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                        assert notificationManager != null;
                                        notificationManager.notify(0, notification.build());
                                    }
                                }

                            }
                        });

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
