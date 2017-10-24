package com.expedia.bookings.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChatMessage;
import com.expedia.bookings.widget.TextView;

import java.util.List;

/**
 * Created by siaggarwal on 10/24/17.
 */

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, MY_IMAGE = 2, OTHER_IMAGE = 3;

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        super(context, R.layout.item_mine_message, data);
    }

    @Override
    public int getViewTypeCount() {
        // my message, other message, my image, other image
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = getItem(position);
        if (item.isMine() && !item.isImage()) return MY_MESSAGE;
        else if (!item.isMine() && !item.isImage()) return OTHER_MESSAGE;
        else if (item.isMine() && item.isImage()) return MY_IMAGE;
        else return OTHER_IMAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (viewType == MY_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
        }
        else if (viewType == OTHER_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
            if ("Here are the Expedia recommended Things to Do for you.".equals(getItem(position).getContent())) {
                convertView.findViewById(R.id.sort_buttons).setVisibility(View.VISIBLE);
            }
        }
        else if (viewType == MY_IMAGE) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_image, parent, false);
        }
        else {
             convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
             convertView.findViewById(R.id.text).setVisibility(View.INVISIBLE);
            ImageView weather = (ImageView) convertView.findViewById(R.id.weather_image);
            weather.setVisibility(View.VISIBLE);
            weather.setImageDrawable(getContext().getResources().getDrawable(Integer.parseInt(getItem(position).getContent())));
        }
        convertView.findViewById(R.id.chatMessageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "onClick", Toast.LENGTH_LONG).show();
            }
        });
        return convertView;
    }
}
