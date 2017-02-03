package net.yepsoftware.takemymoney.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maxi on 14/1/2017.
 */
@IgnoreExtraProperties
public class Article {

    public String uid;
    public String title;
    public String description;
    public State state;
    public double price;
    public ArrayList<String> images;

    public enum State {
        ACTIVE,
        SOLD,
        DISABLED;
    }

    public Article(){}

    public Article(String uid, String title, String description, double price, ArrayList<String> images, State state) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.price = price;
        this.state = state;
        if (images == null){
            images = new ArrayList<>();
        }
        this.images = images;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("title", title);
        result.put("description", description);
        result.put("price", price);
        result.put("state", state.toString());
        result.put("images", images);
        return result;
    }
}
