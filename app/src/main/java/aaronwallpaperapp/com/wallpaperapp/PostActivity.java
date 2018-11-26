package aaronwallpaperapp.com.wallpaperapp;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aaronowen on 15/03/2018.
 */

public class PostActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
//    private ImageView like;
//    private URI imageUri;
//    private String mImageFileLocation = "";
//    private TextView mPostTitle,mPostDescription, mUser;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpost);
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        getIncomingData();// get data passed to the activity
        //tool bar instead of navigation bar, can only go back
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("image_Title"));

        mAuth = FirebaseAuth.getInstance();
        final  String user_id = mAuth.getCurrentUser().getUid();
        final ImageView like = findViewById(R.id.like);
        //like a post
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check to see if the user has liked this before
                //get post id
                final String imageIds = getIntent().getStringExtra("image_Ids");
                //make db ref
                final Query checkLike = FirebaseDatabase.getInstance().getReference().child("Likes").child(imageIds).child(user_id);
                checkLike.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            // if the data exists then we know the user has already liked this, remove like
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference likePost = database.getReference().child("Likes").child(imageIds).child(user_id);
                            likePost.removeValue();
                            //update number of likes for this post
                            checkLikes(imageIds);
                        }else{
                            //User has found some fire wallpaper and wants to like it, add like
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference likePost = database.getReference().child("Likes").child(imageIds);
                            Map like = new HashMap();
                            like.put("username",user_id);
                            likePost.child(user_id).setValue(like);
                            //update number of likes for this post
                            checkLikes(imageIds);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        });
        ImageButton button = findViewById(R.id.downloadButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                openDialogBox();
            }});
    }


    public void openDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Input")
                .setItems(R.array.setWallpaper, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                            try {
                                String url = getIntent().getStringExtra("image_url"); //image to be set as wallpaper
                                Bitmap bitmap = getBitmapFromURL(url); // url to bitmap
                                myWallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                                Toast.makeText(PostActivity.this, "Wallpaper Set", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else if(which == 1){
                            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                            try {
                                String url = getIntent().getStringExtra("image_url"); //image to be set as wallpaper
                                Bitmap bitmap = getBitmapFromURL(url); // url to bitmap
                                myWallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                                Toast.makeText(PostActivity.this, "Wallpaper Set", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                            try {
                                String url = getIntent().getStringExtra("image_url"); //image to be set as wallpaper
                                Bitmap bitmap = getBitmapFromURL(url); // url to bitmap
                                myWallpaperManager.setBitmap(bitmap);
                                Toast.makeText(PostActivity.this, "Wallpaper Set", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static Bitmap getBitmapFromURL(String src) {
        //get url from bitmap
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getIncomingData(){
        //take incoming data and assign to to vars, use this to set image.
        if (getIntent().hasExtra("image_url") && getIntent().hasExtra("image_name") && getIntent().hasExtra("image_Ids")){
            String imageUrl = getIntent().getStringExtra("image_url");
            String imageName = getIntent().getStringExtra("image_name");
            String imageIds = getIntent().getStringExtra("image_Ids");
            String imageUser = getIntent().getStringExtra("image_User");
            String imageDescription = getIntent().getStringExtra("image_Description");
           final String imageUserId = getIntent().getStringExtra("image_UserId");

            TextView username = findViewById(R.id.username);
            username.setText(imageUser);

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PostActivity.this, ProfileActivity.class);
                    intent.putExtra("image_UserId",imageUserId);
                    startActivity(intent);
                }
            });
            TextView description = findViewById(R.id.description);
            description.setText(imageDescription);

            ImageView image =findViewById(R.id.image);
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(image);

            checkLikes(imageIds);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void checkLikes(String imageIds){
        //check likes for this post
        //get user's uid
        mAuth = FirebaseAuth.getInstance();
        final  String user_id = mAuth.getCurrentUser().getUid();

        Query checkLike = FirebaseDatabase.getInstance().getReference().child("Likes").child(imageIds).child(user_id);
        checkLike.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()==0){
                    // if count is 0 set the image to no likes
                    ImageView image =findViewById(R.id.like);
                    Glide.with(PostActivity.this)
                            .asBitmap()
                            .load(R.drawable.ic_fav_border)
                            .into(image);
                }else{
                    //set image to liked image
                    ImageView image =findViewById(R.id.like);
                    Glide.with(PostActivity.this)
                            .asBitmap()
                            .load(R.drawable.ic_fav)
                            .into(image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Query checkLikeAmount = FirebaseDatabase.getInstance().getReference().child("Likes").child(imageIds);
        checkLikeAmount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0){
                    //get number of likes and set the counter
                    long count = dataSnapshot.getChildrenCount();
                    String likeCount = Long.toString(count);
                    TextView likeCounter = findViewById(R.id.likeCounter);
                    likeCounter.setText(likeCount);
                }else {
                    TextView likeCounter = findViewById(R.id.likeCounter);
                    likeCounter.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
