package com.gco129.parstagram.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gco129.parstagram.LoginActivity;
import com.gco129.parstagram.Post;
import com.gco129.parstagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProfileFragment extends PostsFragment{
    public static final String TAG = "ProfileFragment";
    public final static int PICK_PHOTO_CODE = 1046;
    private Button btnLogout;
    private Button btnProfilePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnProfilePhoto = view.findViewById(R.id.btnProfilePhoto);
        // Button on click listener for logging out
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        btnProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(view);
            }
        });
    }

    @Override
    protected void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with getting posts: " + e.toString());
                    return;
                }
                adapter.clear();
                for(Post post: posts){
                    Log.i(TAG, "Post: " + post.getDescription() + "; Username: " + post.getUser().getUsername());
                }
                adapter.addAll(posts);
                swipeContainer.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Bring up gallery to select a photo
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = loadFromUri(photoUri);
            File selectedPhoto = getPhotoFileUri("avatar.jpg");
            ParseFile parseFile = new ParseFile(selectedPhoto);

            // Show photo for debugging
            //ImageView ivPreview = (ImageView) getView().findViewById(R.id.imageView2);
            //ivPreview.setImageBitmap(selectedImage); */

            // Change profile photo of current user to selected image
            Log.i(TAG, "Attempting to change profile photo");
            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.put("profilePhoto", parseFile);
            currentUser.saveInBackground(e -> {
                if(e == null){
                    Log.i(TAG, "Profile photo update success");
                    Toast.makeText(getContext(), "Profile photo has been updated.", Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(TAG, "Profile photo update failure");
                    Toast.makeText(getContext(), "Error updating profile photo: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        if(!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "Failed to create directory");
        }
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
}
