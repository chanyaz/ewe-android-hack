package com.expedia.bookings.launch.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
//import org.alicebot.ab.AIMLProcessor;
//import org.alicebot.ab.Bot;
//import org.alicebot.ab.Chat;
//import org.alicebot.ab.Graphmaster;
//import org.alicebot.ab.MagicBooleans;
//import org.alicebot.ab.MagicStrings;
//import org.alicebot.ab.PCAIMLProcessorExtension;
//import org.alicebot.ab.Timer;
//import com.hariofspades.chatbot.Adapter.ChatMessageAdapter;
//import com.hariofspades.chatbot.Pojo.ChatMessage;

import com.expedia.bookings.R;
import com.expedia.bookings.adapter.ChatMessageAdapter;
import com.expedia.bookings.data.ChatMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ChatBotActivity extends AppCompatActivity {


    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    private ChatMessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mAdapter.add(new ChatMessage("Hi Silvy", false, false));
        mAdapter.add(new ChatMessage("The day will be rainy today." + System.getProperty("line.separator") +
                "Would you like to find some Things to Do in this weather.", false, false));
        mAdapter.add(new ChatMessage(String.valueOf(R.drawable.weather), false, true));
        mListView.setAdapter(mAdapter);

//code for sending the message
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();
                sendMessage(message.trim());
                mEditTextMessage.setText("");
                mListView.setSelection(mAdapter.getCount() - 1);
            }
        });
    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);
        mimicOtherMessage(chatMessage.getContent());
    }

    private void mimicOtherMessage(String message) {
        String reply;
        switch (message)
        {
            case "no":
            case "NO":
                reply = "Sure. Have a nice day. We hope you enjoy your activity.";
                Intent intent = new Intent(getApplicationContext(), PhoneLaunchActivity.class);
                intent.putExtra("ARG_FORCE_SHOW_WATERFALL", true);
                startActivity(intent);
                break;
            case "yes":
            case "YES":
                reply = "Here are the Expedia recommended Things to Do for you.";
                break;
            default:
                reply = "Sorry, We could not understand what you said.";
        }
        ChatMessage chatMessage = new ChatMessage(reply, false, false);
        mAdapter.add(chatMessage);

    }

    private void sendMessage() {
        ChatMessage chatMessage = new ChatMessage(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        ChatMessage chatMessage = new ChatMessage(null, false, true);
        mAdapter.add(chatMessage);
    }
}
