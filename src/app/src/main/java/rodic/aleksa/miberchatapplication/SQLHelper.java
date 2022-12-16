package rodic.aleksa.miberchatapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class SQLHelper extends SQLiteOpenHelper {

    // Constants for easier database tables handling
    private static final String CONTASTS_TABLE = "contacts";
    private static final String CONTASTS_ID = "contact_id";
    private static final String CONTASTS_USERNAME = "username";
    private static final String CONTASTS_FIRSTNAME = "firstname";
    private static final String CONTASTS_LASTNAME = "lastname";

    private static final String MESSAGES_TABEL = "messages";
    private static final String MESSAGES_ID = "message_id";
    private static final String MESSAGES_SENDER_ID = "sender_id";
    private static final String MESSAGES_RECEIVER_ID = "receiver_id";
    private static final String MESSAGES_MESSAGE = "message";

    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Making databases using sqlite commands
        // https://www.sqlite.org/autoinc.html

        // Tabel of contacts, list of
        sqLiteDatabase.execSQL("create table contacts(" +
                "contact_id INTEGER PRIMARY KEY, username TEXT, firstname TEXT, lastname TEXT);");

        // Table of messages per contact
        sqLiteDatabase.execSQL("create table messages(" +
                "message_id INTEGER PRIMARY KEY, sender_id INTEGER, receiver_id INTEGER, message TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long getUserIdByUsername(String username) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(CONTASTS_TABLE,
                new String[]{CONTASTS_ID},
                CONTASTS_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        // If there is no username asked for, return -1
        if (cursor.getCount() == 0)
            return -1;

        cursor.moveToFirst();

        long id = cursor.getLong(cursor.getColumnIndex(CONTASTS_ID));

        cursor.close();
        database.close();

        return id;
    }

    /**
     * Check is username in database
     * @param username
     * @return true if username is in the database, if not false
     */
    public boolean usernameExist(String username){
        return getUserIdByUsername(username) != -1;
    }

    public void insertContact(Contact contact) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTASTS_USERNAME, contact.getUsername());
        contentValues.put(CONTASTS_FIRSTNAME, contact.getFirstname());
        contentValues.put(CONTASTS_LASTNAME, contact.getLastname());

        // 15:58, Ištvan, Da li postoji kofein u spreju? To je dobra ideja za patent.

        database.insert(CONTASTS_TABLE, null, contentValues);

        database.close();
    }

    public ArrayList<Contact> getContacts(long currentUserId){
        ArrayList<Contact> contacts = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(CONTASTS_TABLE, null, null, null, null, null, null);

        // There is non or just you, as a user
        if (cursor.getCount() < 1){
            return contacts;
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            if(cursor.getInt(cursor.getColumnIndex(CONTASTS_ID)) != currentUserId){
                long id = cursor.getLong(cursor.getColumnIndex(CONTASTS_ID));
                String username = cursor.getString(cursor.getColumnIndex(CONTASTS_USERNAME));
                String firstname = cursor.getString(cursor.getColumnIndex(CONTASTS_FIRSTNAME));
                String lastname = cursor.getString(cursor.getColumnIndex(CONTASTS_LASTNAME));

                Contact contact = new Contact(id, username, firstname, lastname);

                contacts.add(contact);
            }
        }

        cursor.close();
        database.close();

        return contacts;
    }

    public ArrayList<Message> getMessages(long senderId, long receiverId){
        ArrayList<Message> messages = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();

        String selectionString = "(" + MESSAGES_SENDER_ID + "=? and " + MESSAGES_RECEIVER_ID + "=?)"
                + " or (" + MESSAGES_RECEIVER_ID + "=? and " + MESSAGES_SENDER_ID + "=?)";

        Cursor cursor = database.query(MESSAGES_TABEL,
                null,
                selectionString,
                new String[]{String.valueOf(senderId), String.valueOf(receiverId), String.valueOf(senderId), String.valueOf(receiverId)},
                null, null, null);

        // There is no messages, so return empty arraylist
        if(cursor.getCount() < 1){
            return messages;
        }

        // Older messages first
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            long messageIdByCursor = cursor.getLong(cursor.getColumnIndex(MESSAGES_ID));
            long senderIdByCursor = cursor.getLong(cursor.getColumnIndex(MESSAGES_SENDER_ID));
            long receiverIdByCursor = cursor.getLong(cursor.getColumnIndex(MESSAGES_RECEIVER_ID));
            String messageByCursor = cursor.getString(cursor.getColumnIndex(MESSAGES_MESSAGE));

            Message message = new Message(messageIdByCursor, senderIdByCursor, receiverIdByCursor, messageByCursor);

            messages.add(message);
        }

        cursor.close();
        database.close();

        return messages;
    }

    public long insertMessage(Message message){
        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGES_SENDER_ID, message.getSenderId());
        contentValues.put(MESSAGES_RECEIVER_ID, message.getReceiverId());
        contentValues.put(MESSAGES_MESSAGE, message.getMessage());

        long idMessageInsertedOn = database.insert(MESSAGES_TABEL, null, contentValues);

        database.close();

        return idMessageInsertedOn;
    }

    public boolean deleteMessage(long messageId){
        SQLiteDatabase database = getWritableDatabase();

        int deleteOperationResult = database.delete(MESSAGES_TABEL, MESSAGES_ID + "=?", new String[]{String.valueOf(messageId)});

        database.close();

        return deleteOperationResult != 0;
    }
}
