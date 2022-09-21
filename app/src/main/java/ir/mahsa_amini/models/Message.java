package ir.mahsa_amini.models;

public class Message {
    private int id;
    private String phone, message;
    private boolean sent;
    private boolean unlocked;

    public Message(int id, String phone, String message, boolean sent, boolean unlocked) {
        this.id = id;
        this.phone = phone;
        this.message = message;
        this.sent = sent;
        this.unlocked = unlocked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
