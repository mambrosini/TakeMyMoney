package net.yepsoftware.takemymoney.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import net.yepsoftware.takemymoney.model.User;

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

    public static ProgressDialog showProgressDialog(Context context, String text) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(text);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        return progressDialog;
    }

    public static void showAuthDialog(final Activity activity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_auth_dialog, null);
        dialogBuilder.setView(dialogView);
        LinearLayout regLayout = (LinearLayout) dialogView.findViewById(R.id.regLayout);
        Button regButton = (Button) dialogView.findViewById(R.id.button);
        Button signButton = (Button) dialogView.findViewById(R.id.button2);
        TextView regMessage = (TextView) dialogView.findViewById(R.id.label);
        TextView signMessage = (TextView) dialogView.findViewById(R.id.label2);
        regMessage.setText("If you don't have an account");
        signMessage.setText("or if you already have one");
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(activity, RegistrationActivity.class);
                activity.startActivity(intent);
            }
        });
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(activity, AuthenticationActivity.class);
                activity.startActivity(intent);
            }
        });
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public static void showPreSellAuthDialog(final Activity activity, boolean isRegVisible) {
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
        if (isRegVisible) {
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

    public static void showContactInfoDialog(final Activity activity, User user, final String articleTitle) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_user_info_dialog, null);
        dialogBuilder.setView(dialogView);
        final LinearLayout mailLayout = (LinearLayout) dialogView.findViewById(R.id.mailLayout);
        LinearLayout phoneLayout = (LinearLayout) dialogView.findViewById(R.id.phoneLayout);
        final LinearLayout secondaryMailLayout = (LinearLayout) dialogView.findViewById(R.id.secondaryMailLayout);
        final TextView phoneText = (TextView) dialogView.findViewById(R.id.text);
        final TextView mailText = (TextView) dialogView.findViewById(R.id.text2);
        final TextView secondaryMailText = (TextView) dialogView.findViewById(R.id.text3);

        mailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { mailText.getText().toString() });
                intent.putExtra(Intent.EXTRA_SUBJECT, "I'm interested in \"" + articleTitle + "\"");
                activity.startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        secondaryMailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { secondaryMailText.getText().toString() });
                intent.putExtra(Intent.EXTRA_SUBJECT, "I'm interested in \"" + articleTitle + "\"");
                activity.startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData( Uri.parse("tel:" + phoneText.getText().toString()));
                activity.startActivity(intent);
            }
        });

        Button dismissButton = (Button) dialogView.findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        mailText.setText(user.email);
        if (user.secondaryEmail == null || user.secondaryEmail.equals("") || user.secondaryEmail.equals("null")){
            secondaryMailLayout.setVisibility(View.GONE);
        } else {
            secondaryMailText.setText(user.secondaryEmail);
        }

        if (user.phone == null || user.phone.equals("")  || user.phone.equals("null")){
            phoneLayout.setVisibility(View.GONE);
        } else {
            phoneText.setText(user.phone);
        }

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }
}
