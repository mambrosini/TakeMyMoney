package net.yepsoftware.takemymoney.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maxi on 23/1/2017.
 */
public class User {
    public String uid;
    public String email;
    public String secondaryEmail;
    public String phone;

    public User(){}

    public User(String uid, String email, String secondaryEmail, String phone) {
        this.uid = uid;
        this.email = email;
        this.secondaryEmail = secondaryEmail;
        this.phone = phone;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("email", email);
        result.put("secondaryEmail", secondaryEmail);
        result.put("phone", phone);
        return result;
    }
}
