package rodic.aleksa.miberchatapplication;

import android.util.SparseArray;

public class Constants {
    protected static final int USERNAME_LENGTH = 3;
    protected static final int PASSWORD_LENGTH = 6;
    protected static final int FIRSTNAME_LENGTH = 3;
    protected static final int LASTNAME_LENGTH = 3;
    protected static final int MINIMUM_MESSAGE_LENGTH = 1;
    protected static final String MESSAGE_LIST_SAVED = "MESSAGE_LIST_SAVED";

    // Database strings
    protected static final String APPLICATION_DATABASE = "applicationDatabase";

    // Bundle transfer
    protected static final String BUNDLE_KEY_USERNAME = "username";
    protected static final String BUNDLE_KEY_ID_OF_MESSAGE_RECEIVER = "idToSendMessageTo";
    protected static final String BUNDLE_KEY_NAME_AND_LASTNAME = "nameAndLastname";

    // Shared preferences
    protected static final String SHARED_PREFF = "sharedPreff_unique_id";
    protected static final String SHARED_PREFF_USER_ID = "sharedPreff_USER_ID";
    protected static final String SHARED_PREFF_LOGED_USERNAME = "sharedPreff_LOGED_USERNAME";

    // HTTP strings
    protected static String HTTP_SERVER_URL = "http://18.205.194.168:80";
    protected static String HTTP_REGISTER = HTTP_SERVER_URL + "/register/";
    protected static String HTTP_LOGIN = HTTP_SERVER_URL + "/login/";
    protected static String HTTP_CONTACTS = HTTP_SERVER_URL + "/contacts/";
    protected static String HTTP_MESSAGE = HTTP_SERVER_URL + "/message/";
    protected static String HTTP_MESSAGE_DELETE = HTTP_SERVER_URL + "/message";
    protected static String HTTP_LOGOUT = HTTP_SERVER_URL + "/logout/";
    protected static String HTTP_NEW_MESSAGE_SERVICE = HTTP_SERVER_URL + "/getfromservice";
}
