package rodic.aleksa.miberchatapplication;

import android.content.Intent;
import android.graphics.Color;

import java.util.Random;

public class Contact {
    private long id;
    private String username;
    private String firstname;
    private String lastname;
    private Integer color;  // Replacement for image

    public Contact(String username, String firstname, String lastname) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;

        Random random = new Random();
        this.color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    public Contact(long id, String username, String firstname, String lastname) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;

        Random random = new Random();
        this.color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getColor() {
        return color;
    }
}
