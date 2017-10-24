package com.expedia.bookings.launch.activity;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.adapter.ChatMessageAdapter;
import com.expedia.bookings.data.ChatMessage;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;

public class ChatBotActivity extends AppCompatActivity {


    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    private ChatMessageAdapter mAdapter;
    private CarouselView carouselView;

    int[] sampleImages = {R.drawable.i1, R.drawable.i2, R.drawable.i3, R.drawable.i4, R.drawable.i5};
    String[] sampleTitles = {"New York City Explorer Pass", "Empire State Building", "Statue of Liberty & Ellis Island Tour with Pedestal Access", "Hop-On Hop-Off Bus Tour", "National September 11 Memorial & Museum"};
    String[] price = {"$84", "$34", "$57", "$54", "$26"};

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

        carouselView = (CarouselView) findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);
        carouselView.setImageListener(imageListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            carouselView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    int position = ((CarouselView)view).getCurrentItem();
                    ((TextView)findViewById(R.id.act_title)).setText(sampleTitles[position]);
                    ((TextView)findViewById(R.id.act_price)).setText(price[position]);
                }
            });
        }

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
                findViewById(R.id.lx_carousel).setVisibility(View.VISIBLE);
                break;
            default:
                reply = "Sorry, We could not understand what you said.";
        }
        ChatMessage chatMessage = new ChatMessage(reply, false, false);
        mAdapter.add(chatMessage);

    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(sampleImages[position]);
            ((TextView)findViewById(R.id.act_title)).setText(sampleTitles[0]);
            ((TextView)findViewById(R.id.act_price)).setText(price[0]);
        }
    };

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