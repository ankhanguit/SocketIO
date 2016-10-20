package com.example.panda94.socketio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.panda94.socketio.adapter.ChatAdapter;
import com.example.panda94.socketio.objs.ChatMessage;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main extends AppCompatActivity {

    private String serverHost = "http://192.168.0.107/";
    public static final String USERID = "580185d0f0802806088f7a0f";
    public static final String GROUPID = "58038c6a12d46b1b0020950a";
    public static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1ODAxODVkMGYwODAyODA2MDg4ZjdhMGYiLCJpYXQiOjE0NzY4OTUxODN9.cVsKtIlH0vbIooEu4Xgmjh128TXLqG0k4jWq6u8JwXc";

    private Socket mSocket;

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String message = intent.getStringExtra(JoinGroupActiviy.EXTRA_MESSAGE);
        if(message != null && !message.equals("")){
            this.serverHost = message;
        }

        try{
            mSocket = IO.socket(this.serverHost);
            chatManage();
        }catch (URISyntaxException e){
            Toast.makeText(this,"Can not connect to this host", Toast.LENGTH_LONG);
        }

        initControls();
    }

    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.list_message);
        messageET = (EditText) findViewById(R.id.ed_chat);
        sendBtn = (Button) findViewById(R.id.btn_chat);

        loadDummyHistory();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMsgMode("myMsg");
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(true);

                messageET.setText("");

                sendMessage(messageText);
                displayMessage(chatMessage);
            }
        });
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory(){

        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        msg.setMsgMode("friendMsg");
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setMsgMode("friendMsg");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        adapter = new ChatAdapter(Main.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

    private Emitter.Listener mConnect = new Emitter.Listener(){

        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String userJoin;
                    String username;
                    try {
                        Log.i("Response Json: ", data.toString());
                        JSONObject info = data.getJSONObject("data");
                        Log.i("Response Json: ", info.toString());
                        userJoin = info.getString("userId");
                        username = info.getString("firstname") + info.getString("lastname");


                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    if (!USERID.equals(userJoin)) {
                        addNewMemberMessage(username);
                    }
                }
            });
        }
    };

    private  void sendMessage(String message){
        JSONObject jsonMessage = new JSONObject();

        try {
            jsonMessage.put("message", message);
        }catch (JSONException e){

        }
        mSocket.emit("mMessage", jsonMessage);
    }

    private Emitter.Listener mMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String message;
                String username;
                try {
                    Log.i("Response Json: ", data.toString());
                    message = data.getString("message");
                    username = data.getString("flag");
                } catch (JSONException e) {
                    return;
                }
                if (!USERID.equals(username)) {
                    addNewMessage(message);
                }
            }
        });
        }
    };

    public void addNewMemberMessage(String userJoin){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(122);//dummy
        chatMessage.setMsgMode("newMemberMsg");
        chatMessage.setMessage("");
        chatMessage.setDate(userJoin + " has joined the group.");
        chatMessage.setMe(true);

        displayMessage(chatMessage);
    }

    public void addNewMessage(String msg){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(122);//dummy
        chatMessage.setMsgMode("friendMsg");
        chatMessage.setMessage(msg);
        chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatMessage.setMe(true);

        displayMessage(chatMessage);
    }


    private void chatManage(){
        mSocket.connect();
        JSONObject joinRequestMsg = new JSONObject();

        try {
            joinRequestMsg.put("userId", USERID);
            joinRequestMsg.put("groupId", GROUPID);
            joinRequestMsg.put("token", TOKEN);
        }catch (JSONException e){

        }

        mSocket.emit("mConnect", joinRequestMsg);

        mSocket.on("mConnect", mConnect);
        mSocket.on("mMessage", mMessage);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}


