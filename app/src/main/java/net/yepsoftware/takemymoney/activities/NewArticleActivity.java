package net.yepsoftware.takemymoney.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.model.Article;

public class NewArticleActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private Button button;

    private DatabaseReference articlesDBRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_article);

        articlesDBRef = FirebaseDatabase.getInstance().getReference().child("articles");

        titleEditText = (EditText) findViewById(R.id.title);
        descriptionEditText = (EditText) findViewById(R.id.description);
        priceEditText = (EditText) findViewById(R.id.price);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleEditText.getText().toString().isEmpty()
                        || descriptionEditText.getText().toString().isEmpty()
                        || priceEditText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"You must fill all the fields.", Toast.LENGTH_SHORT).show();
                } else {
                    writeNewArticle(titleEditText.getText().toString(), descriptionEditText.getText().toString(), Double.valueOf(priceEditText.getText().toString()));
                    finish();
                }
            }
        });
    }

    private void writeNewArticle(String title, String description, double price) {
        Article article = new Article(title, description, price);
        String key = articlesDBRef.push().getKey();
        articlesDBRef.child(key).setValue(article);
    }
}
