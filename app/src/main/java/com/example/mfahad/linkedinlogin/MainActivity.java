package com.example.mfahad.linkedinlogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
ImageView imageview;
    TextView name;
    TextView email;
    ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageview =  (ImageView)findViewById(R.id.imageView);
        profile=  (ImageView)findViewById(R.id.image);
        name=  (TextView)findViewById(R.id.name);
        email=  (TextView)findViewById(R.id.email);
        //This method is to generate the hash key
//        generateHashkey();
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

    }
    String packageName;
    String hashKey;
    public void generateHashkey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.mfahad.linkedinlogin",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                packageName = info.packageName;
                hashKey = Base64.encodeToString(md.digest(),
                        Base64.NO_WRAP);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();

        }
    }

    public void handleLogin(){
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
               PersonData();
            }

            @Override
            public void onAuthError(LIAuthError error) {
                Toast.makeText(MainActivity.this, "Error " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }, true);
    }
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_BASICPROFILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Add this line to your existing onActivityResult() method
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }

    public void PersonData(){
        String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,picture-urls::(original))";
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
                JSONObject objj = apiResponse.getResponseDataAsJson();
                name.setText(objj.optString("firstName"));
                email.setText(objj.optString("lastName"));
                String url = objj.optString("pictureUrls");
                Glide.with(MainActivity.this).load(url).into(profile);
            }
            @Override
            public void onApiError(LIApiError liApiError) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
