package com.expedia.bookings.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    List<ChatMessage> data;
    private LinearLayoutManager layoutManager;
    private GalleryAdapter adapter;
    private RecyclerView recyclerView;
    private int count = 0;

    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, MY_IMAGE = 2, OTHER_IMAGE = 3;

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        super(context, R.layout.item_mine_message, data);
        this.data = data;
    }

    public ChatMessageAdapter(Context context, List<ChatMessage> data, LinearLayoutManager layoutManager, GalleryAdapter adapter) {
        super(context, R.layout.item_mine_message, data);
        this.data = data;
        this.layoutManager = layoutManager;
        this.adapter = adapter;
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
                convertView.findViewById(R.id.search_btn_hack).setOnClickListener(mOnClickListener);
                convertView.findViewById(R.id.search_btn_hack2).setOnClickListener(mOnClickListener);
                convertView.findViewById(R.id.search_btn_hack3).setOnClickListener(mOnClickListener);
                convertView.findViewById(R.id.lx_carousel).setVisibility(View.VISIBLE);
                recyclerView = (RecyclerView)convertView.findViewById(R.id.recycle);
                if(count == 0){
                    recyclerView.setLayoutManager(layoutManager);
                    count++;
                }
                recyclerView.setAdapter(adapter);
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
        return convertView;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            data.add(new ChatMessage(((Button)v).getText().toString(), true, false));
            notifyDataSetChanged();

        }
    };
}
