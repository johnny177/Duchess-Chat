package com.nnoboa.duchesschat.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nnoboa.duchesschat.R;
import com.nnoboa.duchesschat.model.Messages;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.CropSquareTransformation;

public class MessagesAdapter extends ArrayAdapter<Messages> {

    public MessagesAdapter(@NonNull Context context, int resource, @NonNull List<Messages> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_view, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.photoImageView);
        TextView message = convertView.findViewById(R.id.text_message);
        TextView timeStamp = convertView.findViewById(R.id.message_time);
        LinearLayout parentView = convertView.findViewById(R.id.parent_view);
        Messages messages = getItem(position);

        assert messages !=null;
        boolean isFileUrl = messages.getmFileUrl() != null;
        if(isFileUrl){
            message.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(messages.getmFileUrl()).into(imageView);
        }else{
            imageView.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            message.setText(messages.getmText());
        }

        timeStamp.setText(time(messages.getmTimeStamp()));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



        if( user.getUid().equals(messages.getmUID())){
            RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) parentView.getLayoutParams();
            parentView.setBackground(getContext().getDrawable(R.drawable.incoming_message_bubble));
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
//            message.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            parentView.setLayoutParams(params);
        }else{
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) parentView.getLayoutParams();
            parentView.setBackground(getContext().getDrawable(R.drawable.outgoing_message_bubble));
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
//            message.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            parentView.setLayoutParams(params);
        }

        return convertView;
    }

    private String time(long time){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(time);
        String dateString = dateFormat.format(date);
        return dateString;
    }


}
