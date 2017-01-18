package net.yepsoftware.takemymoney.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maxi on 14/1/2017.
 */
@IgnoreExtraProperties
public class Article {
    public String title;
    public String description;
    public double price;

    public Article(){}

    public Article(String title, String description, double price) {
        this.title = title;
        this.description = description;
        this.price = price;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        result.put("price", price);
        return result;
    }
}
