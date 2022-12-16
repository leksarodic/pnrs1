package rodic.aleksa.miberchatapplication;

public class EncryptMessage {

    static {
        System.loadLibrary("SimpleKeyCrypt");
    }

    public native String encryptDecrypt(String message, int message_len);

}
