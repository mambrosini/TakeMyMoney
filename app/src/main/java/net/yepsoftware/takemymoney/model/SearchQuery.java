package net.yepsoftware.takemymoney.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maxi on 17/1/2017.
 */
public class SearchQuery {
    public String q;

    public SearchQuery(){}

    public SearchQuery(String searchText) {
        String query;
        String [] strings = searchText.split("\\s+");
        query = "(title:" + strings[0] + " OR description:" + strings[0] + ")";
        for (int i = 1; i < strings.length; i++) {
            query = query + " OR (title:" + strings[i] + " OR description:" + strings[i] + ")";
        }
        this.q = query;
    }

    public SearchQuery searchByUser(String uid){
        String query;
        query = "(uid:" + uid + ")";
        this.q = query;
        return this;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("q", q);
        return result;
    }
}
