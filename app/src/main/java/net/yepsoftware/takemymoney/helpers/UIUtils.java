package net.yepsoftware.takemymoney.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.activities.AuthenticationActivity;
import net.yepsoftware.takemymoney.activities.RegistrationActivity;

/**
 * Created by Maxi on 16/1/2017.
 */
public class UIUtils {

    private static AlertDialog alertDialog;

    public static void showKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        final View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    public static ProgressDialog showProgressDialog(Context context, String text){
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(text);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        return progressDialog;
    }

    public static void showAuthDialog(final Activity activity, boolean isRegVisible){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_auth_dialog, null);
        dialogBuilder.setView(dialogView);
        LinearLayout regLayout = (LinearLayout) dialogView.findViewById(R.id.regLayout);
        Button regButton = (Button) dialogView.findViewById(R.id.button);
        Button signButton = (Button) dialogView.findViewById(R.id.button2);
        TextView signMessage = (TextView) dialogView.findViewById(R.id.label2);
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(activity, AuthenticationActivity.class);
                intent.putExtra(RegistrationActivity.NAVIGATE_TO_NEW_ARTICLE, true);
                activity.startActivity(intent);
            }
        });
        if (isRegVisible){
            regButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    Intent intent = new Intent(activity, RegistrationActivity.class);
                    intent.putExtra(RegistrationActivity.NAVIGATE_TO_NEW_ARTICLE, true);
                    activity.startActivity(intent);
                }
            });
        } else {
            regLayout.setVisibility(View.GONE);
            signMessage.setText("You need to Sign in in order to be able to sell");
        }
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
