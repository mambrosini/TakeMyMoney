package net.yepsoftware.takemymoney.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import net.yepsoftware.takemymoney.activities.AuthenticationActivity;

import java.util.concurrent.Callable;

/**
 * Created by Maxi on 30/1/2017.
 */
public class AuthUtils {
    public static void signIn(final Context context, FirebaseAuth mAuth, final Callable callable){
        if (PreferencesHelper.isAutoLogin(context)){
            final ProgressDialog progressDialog = UIUtils.showProgressDialog(context,"Signin in...");
            firebaseSignIn(context,
                    mAuth,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (!task.isSuccessful()) {
                                Log.w("FirebaseAuth", "signInWithEmail:failed", task.getException());
                                Toast.makeText(context, "Auth Failed",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                PreferencesHelper.setAppState(context, PreferencesHelper.APP_STATE_AUTHENTICATED);
                                try {
                                    callable.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        } else {
            Intent intent = new Intent(context, AuthenticationActivity.class);
            context.startActivity(intent);
        }
    }

    private static void firebaseSignIn(Context context, FirebaseAuth mAuth, OnCompleteListener onCompleteListener) {
        mAuth.signInWithEmailAndPassword(PreferencesHelper.getMail(context), PreferencesHelper.getPassword(context))
                .addOnCompleteListener((Activity)context, onCompleteListener);
    }
}
