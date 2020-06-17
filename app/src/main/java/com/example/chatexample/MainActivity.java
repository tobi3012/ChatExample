package com.example.chatexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicomkit.listners.AlLogoutHandler;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_RELOAD_DATA = "action_reload_data";
    Intent intent, intent2;
    private boolean receiverRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ApplozicClient.getInstance(this).enableNotification();
        if (ApplozicClient.getInstance(getApplicationContext()).isContextBasedChat()) {
            Log.d(MainActivity.class.getSimpleName(), "onCreate: isContextBasedChat: true");
        } else {
            Log.d(MainActivity.class.getSimpleName(), "onCreate: isContextBasedChat: false");
            ApplozicClient.getInstance(getApplicationContext()).setContextBasedChat(true);
        }
        Button buttonCus1 = findViewById(R.id.btn_cus1);
        Button buttonCus2 = findViewById(R.id.btn_cus2);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button chatList = findViewById(R.id.btn_chat);

        chatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chatList = new Intent(MainActivity.this, ConversationActivity.class);
                MainActivity.this.startActivity(chatList);
            }
        });

        buttonCus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logOutChat();

                if (!Applozic.isConnected(MainActivity.this)) {
                    registerUserChat("customer01", User.AuthenticationType.APPLOZIC);
                } else {
                    Toast.makeText(MainActivity.this, "Please click logout button and wait for logout !!! ", Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonCus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logOutChat();

                if (!Applozic.isConnected(MainActivity.this)) {
                    registerUserChat("customer02", User.AuthenticationType.APPLOZIC);
                } else {
                    Toast.makeText(MainActivity.this, "Please click logout button and wait for logout !! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutChat();
            }
        });


       /* User user = new User();
        user.setUserId("hieptq01"); //userId it can be any unique user identifier
        user.setDisplayName("customer hieptq01"); //displayName is the name of the user which will be shown in chat messages
//        user.setEmail(email); //optional
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //User.AuthenticationType.APPLOZIC.getValue() for password verification from Applozic server and User.AuthenticationType.CLIENT.getValue() for access Token verification from your server set access token as password
        user.setPassword(""); //optional, leave it blank for testing purpose, read this if you want to add additional security by verifying password from your server https://www.applozic.com/docs/configuration.html#access-token-url
        user.setImageLink("");//optional,pass your image link

        Applozic.connectUser(this, user, new AlLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                // After successful registration with Applozic server the callback will come here
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                // If any failure in registration the callback  will come here
            }
        });*/
    }


    private void registerUserChat(String userId, User.AuthenticationType authenticationType) {

        User user = new User();
        user.setUserId(String.valueOf(userId)); //userId it can be any unique user identifier
        user.setDisplayName(userId); //displayName is the name of the user which will be shown in chat messages
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //User.AuthenticationType.APPLOZIC.getValue() for password verification from Applozic server and User.AuthenticationType.CLIENT.getValue() for access Token verification from your server set access token as password
        user.setPassword(""); //optional, leave it blank for testing purpose, read this if you want to add additional security by verifying password from your server https://www.applozic.com/docs/configuration.html#access-token-url
        user.setImageLink("");//optional,pass your image link
        user.setAuthenticationTypeId(authenticationType.getValue());

        Applozic.connectUser(this, user, new AlLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                // After successful registration with Applozic server the callback will come here
                Log.d(MainActivity.class.getSimpleName(), "onSuccess: register account chat successful");
                Toast.makeText(context, " register account chat successful", Toast.LENGTH_SHORT).show();
                registerPushNoti();
                Intent chatList = new Intent(MainActivity.this, ConversationActivity.class);
                context.startActivity(chatList);
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                // If any failure in registration the callback  will come here
            }
        });

        //Applozic.connectPublish(getApplicationContext());
    }


    private void registerPushNoti() {
        if (MobiComUserPreference.getInstance(this).isRegistered()) {

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(MainActivity.class.getSimpleName(), "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            Log.d(MainActivity.class.getSimpleName(), "onComplete - Token: " + token);

                            Applozic.registerForPushNotification(MainActivity.this, token, new AlPushNotificationHandler() {
                                @Override
                                public void onSuccess(RegistrationResponse registrationResponse) {
                                    Log.d(MainActivity.class.getSimpleName(), "onSuccess: register noti success");
                                }

                                @Override
                                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                                }
                            });

                            // Log and toast
                        }
                    });

        }
    }

    private void logOutChat() {
        Applozic.logoutUser(this, new AlLogoutHandler() {
            @Override
            public void onSuccess(Context context) {
                Toast.makeText(context, "logout successful now you can login again", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exception) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("hieptq", "onPause: ");
        unRegisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApplozicClient.getInstance(this).enableNotification();
        Log.d("hieptq", "onDestroy: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("hieptq", "onStop: ");
    }

    private BroadcastReceiver registerApplozic = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Applozic.isConnected(getApplicationContext())) {
                Log.d("broadcast applozic", "applozic still connect");
            } else {
                Log.d("broadcast applozic", "applozic disconnected");
            }
        }
    };

    private synchronized void unRegisterReceiver() {
        if (receiverRegister) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(registerApplozic);
            receiverRegister = false;
        }
    }

    private synchronized void registerReceiver() {
        if (!receiverRegister) {
            final IntentFilter intentFilter = new IntentFilter(ACTION_RELOAD_DATA);
            LocalBroadcastManager.getInstance(this).registerReceiver(registerApplozic, intentFilter);
            receiverRegister = true;
        }
    }
}