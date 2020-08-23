package com.nnoboa.duchesschat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nnoboa.duchesschat.R;
import com.nnoboa.duchesschat.model.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class UsersAdapter extends ArrayAdapter<Users> {

    public UsersAdapter(@NonNull Context context, int resource, @NonNull List<Users> objects) {
        super(context, resource, objects);


    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_view, parent, false);
        }

        ImageView userPhoto = convertView.findViewById(R.id.user_photo);
        TextView userName = convertView.findViewById(R.id.user_name);

        Users user = getItem(position);

        assert user != null;
        if(user.getmUserDisplayPhotoUrl() != null) {
            Picasso.get().load(user.getmUserDisplayPhotoUrl())
            .transform(new CropCircleTransformation())
            .into(userPhoto);
        }else {
            userPhoto.setVisibility(View.GONE);
        }

        userName.setText(user.getmUserDisplayName());

        return convertView;

    }
}
