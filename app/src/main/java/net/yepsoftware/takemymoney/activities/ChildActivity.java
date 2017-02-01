package net.yepsoftware.takemymoney.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.yepsoftware.takemymoney.helpers.UIUtils;

/**
 * Created by mambrosini on 1/24/17.
 */
public class ChildActivity extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                UIUtils.hideKeyboard(ChildActivity.this);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
