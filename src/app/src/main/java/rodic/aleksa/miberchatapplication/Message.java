package rodic.aleksa.miberchatapplication;

public class Message {
    private long id;
    private String senderId;
    private String receiverId;
    private String message;

    public Message(String senderId, String receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public Message(long id, String senderId, String receiverId, String message) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public Message(long id, long senderId, long receiverId, String message) {
        // NOP
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getReceiverId() {
        return receiverId;
    }

    // TODO: Getters and setters
}
