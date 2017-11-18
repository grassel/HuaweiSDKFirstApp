package org.grassel.huaweisdkfirstapp;

import android.support.v7.app.AppCompatActivity;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.content.Intent;

import android.util.Log;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks;
import com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.HuaweiIdStatusCodes;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;
import com.huawei.hms.support.api.hwid.SignInResult;
import com.huawei.hms.support.api.hwid.SignOutResult;



public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    public static final String TAG = "MainActivity";

    // HMS Client
    private HuaweiApiClient client;

    // Start parameter to clarify which intent (sign-in/permission) is invoked.
    private static final int REQUEST_SIGN_IN_UNLOGIN = 1002;

    private static final int REQUEST_SIGN_IN_AUTH = 1003;

    private static final int REQUEST_SIGN_IN_CHECK_PASSWORD = 1005;

    // Invoke the third parameter passed by HuaweiApiAvailability.getInstance().resolveError
    // This functions the same as requestcode in startactivityforresult method.
    private static final int REQUEST_HMS_RESOLVE_ERROR = 1000;

    // If the developer invoked resolveError interface in onConnectionFailed, the error result will be returned through onActivityResult.
    // To obtain the specific return code, use:
    public static final String EXTRA_RESULT = "intent.extra.RESULT";

    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button signInButtom = findViewById(R.id.sign_in_button_id);
        signInButtom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn();
            }
        });
        final Button signOutButton = findViewById(R.id.sign_out_button_id);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
             }
        });

        // Create sign-in parameter options with basic permissions
        // requestUid is optional. If this parameter is configured, userid will be returned after users sign in successfully
        // NOTE to the codee at http://developer.huawei.com/consumer/en/service/hms/catalog/huaweiid.html?page=hmssdk_huaweiid_api_reference_c1
        HuaweiIdSignInOptions signInOptions = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
                .requestOpenId()
                .build();

        // Create HMS client instance to sign into the Huawei ID
        // Designate the api as HuaweiId.SIGN_IN_API as HuaweiId.SIGN_IN_API
        // Scope is HuaweiId.HUAWEIID_BASE_SCOPE and can remain undesignated, HuaweiIdSignInOptions.DEFAULT_SIGN_IN uses this scope by default.
        // Connection callback and connection error listener
        // NOTE see lso http://developer.huawei.com/consumer/en/service/hms/catalog/huaweiid.html?page=hmssdk_huaweiid_api_reference_c1
        client =  client = new HuaweiApiClient.Builder(this)
                .addApi(HuaweiId.SIGN_IN_API, signInOptions)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect to HMS when invoking onCreate method
        // You can decide the time to connect and disconect the client, but make sure connect and disconnect are in pairs
       // client.connect();
      //  boolean isconn = client.isConnecting();
      //  Log.d("lcy", "iscon=" + isconn);
    }


        /* onConnected, onConnectionSuspended, onConnectionFailed ========================================== */


    @Override
    public void onConnected() {
            // Huawei HMS client connected successfully; deal with business events
            Log.i(TAG, "HuaweiApiClient connected");
    }


    @Override
    public void onConnectionSuspended(int arg0)
    {
        // HuaweiApiClient disconnected abnormally, the if condition can be modified as needed.
        if (!this.isDestroyed() && !this.isFinishing())
        {
            client.connect();
        }
        Log.i(TAG, "HuaweiApiClient disconnected");
    }


    @Override
    public void onConnectionFailed(ConnectionResult arg0)
    {
        Log.i(TAG, "HuaweiApiClient connection failed; error code:" + arg0.getErrorCode());

        if (mResolvingError)
        {
            return;
        }

        if (HuaweiApiAvailability.getInstance().isUserResolvableError(arg0.getErrorCode()))
        {
            android.util.Log.e("hmssdk", "onConnectionFailed");
            mResolvingError = true;
            HuaweiApiAvailability.getInstance().resolveError(this, arg0.getErrorCode(), REQUEST_HMS_RESOLVE_ERROR);
        }
        else
        {
            // For other code error, see the R&D guides and the API documents.
        }
    }



    /* signin ========================================== */

    /**
     *  Developers can refer to this method
     */
    private void signIn()
    {
        if (!client.isConnected())
        {
            Log.i(TAG, "Login failed, cause: HuaweiApiClient disconnected");
            client.connect();
            return;
        }

        PendingResult<SignInResult> signInResult = HuaweiId.HuaweiIdApi.signIn(client);
        signInResult.setResultCallback(new SignInResultCallback());
    }

    /**
     * Sign-in result callback
     * The result should be handled by developer.
     * Sign-in is successful. result.isSuccess() Developers can proceed based on their own needs.
     * Permission is required. Please use the intent field in the returned result to start the authorization activity.
     * Not signed in. Please use the intent field in the returned result to start sign-in activity.
     */
    private class SignInResultCallback implements ResultCallback<SignInResult>
    {

        @Override
        public void onResult(SignInResult result)
        {
            if (result.isSuccess())
            {
                // Account information such as openid, nickname, profile picture and access token can be obtained.
                SignInHuaweiId account = result.getSignInHuaweiId();
                Log.i(TAG, "Sign-in successful");
               // FIXME findViewById(R.id.user_acount_layout).setVisibility(View.VISIBLE);
               // ((TextView)findViewById(R.id.hwid_nickname)).setText("nickname:" + account.getDisplayName());
               // ((TextView)findViewById(R.id.hwid_openid)).setText("openid:" + account.getOpenId());
               // ((TextView)findViewById(R.id.hwid_at)).setText("accessToken:" + account.getAccessToken());
              //  ((TextView)findViewById(R.id.hwid_photo)).setText("profile picture url:" + account.getGender());
            }
            else
            {
                // If the user is not signed in or did not give permission, the result callback will contain an intent to deal with this problem. The developer can get this intent using getData.
                // and use startActivityForResult to start an activity to solve the problem. After the result is returned, the developer needs to process the result accordingly.
                // and more
                if (result.getStatus().getStatusCode() == HuaweiIdStatusCodes.SIGN_IN_UNLOGIN)
                {
                    Log.i(TAG, "Not signed in");
                    Intent intent = result.getData();
                    if (intent != null)
                    {
                        startActivityForResult(intent, REQUEST_SIGN_IN_UNLOGIN);
                    }
                    else
                    {
                        // Sign-in failure due to unknown error. The developer may fix the problem by employing a fault-tolerant approach.
                    }
                }
                else if (result.getStatus().getStatusCode() == HuaweiIdStatusCodes.SIGN_IN_AUTH)
                {
                    Log.i(TAG, "Signed in, user permission required");
                    Intent intent = result.getData();
                    if (intent != null)
                    {
                        startActivityForResult(intent, REQUEST_SIGN_IN_AUTH);
                    }
                    else
                    {
                        // Sign-in failure due to unknown error. The developer may fix the problem by employing a fault-tolerant approach.
                    }
                }
                else if (result.getStatus().getStatusCode() == HuaweiIdStatusCodes.SIGN_IN_CHECK_PASSWORD)
                {
                    // Password verification is required for Huawei ID.
                    Intent intent = result.getData();
                    if (intent != null)
                    {
                        startActivityForResult(intent, REQUEST_SIGN_IN_CHECK_PASSWORD);
                    }
                    else
                    {
                        // Sign-in failure due to unknown error. The developer may fix the problem by employing a fault-tolerant approach.
                    }
                }
                else if (result.getStatus().getStatusCode() == HuaweiIdStatusCodes.SIGN_IN_NETWORK_ERROR)
                {
                    // Network error; Developer will need to handle this issue.
                }
                else
                {
                    // Other errors.
                }
            }
        }
    }

    /* signout ========================================== */

    /**
     * User permission is canceled once the user is signed out. User permission is required when the user signs in next time.
     * Developers can refer to this method
     */
    private void signOut()
    {
        if (!client.isConnected())
        {
            Log.i(TAG, "Signout failed, cause: HuaweiApiClient disconnected");
            return;
        }

        PendingResult<SignOutResult> signOutResult = HuaweiId.HuaweiIdApi.signOut(client);
        signOutResult.setResultCallback(new SignOutResultCallback());
    }

    /**
     * SignOut callback
     */
    private class SignOutResultCallback implements ResultCallback<SignOutResult>
    {

        @Override
        public void onResult(SignOutResult result)
        {
            Status status = result.getStatus();
            // Sign out the Huawei ID. If the returned value is 0, the sign-out is successful.
            if (status.getStatusCode() == 0)
            {
                Log.i(TAG, "Sign-out successful");
            }
            else
            {
                Log.i(TAG, "Sign-out failed");
            }
        }
    }


    /* =============================================== */

    /**
     * If the user is not signed in or did not give permission, invoke signIn method to launch the corresponding page and return the result to the current activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGN_IN_UNLOGIN)
        {
            // If the returned value is -1, the user is signed in successfully. The developer needs to re-invoke signIn.
            if (resultCode == Activity.RESULT_OK)
            {
                Log.i(TAG, "User sign-in successful");
                signIn();
            }
            else
            {
                // If resultCode is 0, the user is not signed in. The developer may deal with this event.
                Log.i(TAG, "Sign-in failed or not signed in");
            }
        }
        else if (requestCode == REQUEST_SIGN_IN_AUTH)
        {
            // If the returned value is -1, the user has given permission.
            if (resultCode == Activity.RESULT_OK)
            {
                Log.i(TAG, "User permission granted");
                SignInResult result = HuaweiId.HuaweiIdApi.getSignInResultFromIntent(data);

                if (result.isSuccess())
                {
                    // Permission granted, use result.getSignInHuaweiId() to get Huawei ID information
                    Log.i(TAG, "User permission granted, return account information");
                   // FIXME findViewById(R.id.user_acount_layout).setVisibility(View.VISIBLE);
                    SignInHuaweiId account = result.getSignInHuaweiId();
                    // Developers handle the obtained account information.
                }
                else
                {
                    // Permission denied, use result.getStatus() to obtain error cause
                    Log.i(TAG, "Permission denied. Cause of error:" + result.getStatus().toString());
                }
            }
            else
            {
                // The user denied permission if resultCode is 0. The developer may need to deal with this event.
                Log.i(TAG, "User permission denied");
            }
        }
        else if (requestCode == REQUEST_SIGN_IN_CHECK_PASSWORD)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                // Sign-in successful.
                signIn();
            }
            else
            {
                // Sign-in failed.
            }
        }
    }

}
