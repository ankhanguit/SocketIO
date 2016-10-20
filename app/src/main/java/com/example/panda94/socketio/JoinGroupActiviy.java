package com.example.panda94.socketio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class JoinGroupActiviy extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.panda94.MESSAGE";

    EditText edIpInput;
    Button btnJoinServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_activiy);

        edIpInput = (EditText) findViewById(R.id.ed_input_ip);
        btnJoinServer = (Button) findViewById(R.id.btn_join_server);

        btnJoinServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Main.class);
                String message = edIpInput.getText().toString();
                Toast.makeText(v.getContext(), "Connecting to server ...", Toast.LENGTH_LONG);

                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        });
    }
}
