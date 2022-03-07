package com.gco129.parstagram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    public static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts){
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfilePhoto;
        private TextView tvUsername;
        private ImageView ivImage;
        private TextView tvDescription;
        private TextView tvCreatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePhoto = itemView.findViewById(R.id.ivProfilePhoto);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }

        public void bind(Post post) {
            // Bind post data to view elements
            tvUsername.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            tvCreatedAt.setText(getDateStr(post.getCreatedAt().toString()));
            ParseFile profilePhoto = post.getUser().getParseFile("profilePhoto");
            ParseFile image = post.getImage();
            if(profilePhoto != null){
                Log.i(TAG, "Attempting to load profile photo");
                Glide.with(context).load(profilePhoto.getUrl()).circleCrop().into(ivProfilePhoto);
            }
            if(image != null){
                Log.i(TAG, "Attempting to load image");
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
        }

        private String getDateStr(String createdAt) {
            return createdAt.substring(4,10) + ", " + createdAt.substring(createdAt.length()-4) + " - " + createdAt.substring(11,23);
        }
    }

    // Clear all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Post> list){
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
