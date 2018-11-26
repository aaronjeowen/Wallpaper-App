package aaronwallpaperapp.com.wallpaperapp;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.net.URL;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private ArrayList<String> mImageTags = new ArrayList<>();
    private ArrayList<String> mImageTitle = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mImageIds = new ArrayList<>();
    private ArrayList<String> mImageUser = new ArrayList<>();
    private ArrayList<String> mImageDescription = new ArrayList<>();
    private ArrayList<String> mImageUserId = new ArrayList<>();
    private DrawerLayout mDrawerLayout;
    private com.github.clans.fab.FloatingActionButton mFABCamera,mFABGallery;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();

        //menu values
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        if(getIntent().hasExtra("search")){
            String title =getIntent().getStringExtra("search");
            actionBar.setTitle(title);
        }else {
            actionBar.setTitle("Wallpapers");
        }

        //menu
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        switch (menuItem.getItemId()) {
                            case android.R.id.home:
                                //open navigation pane
                                Intent intentHome = new Intent(HomeActivity.this, MainActivity.class);
                                startActivity(intentHome);
                                return true;
                            case R.id.goHome:
                                //home page
                                mDrawerLayout.closeDrawers();
                                return true;
                            case R.id.addPicture:
                                // add media
                                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                startActivity(intent);
                               // mDrawerLayout.closeDrawers();
                                return true;
                            case R.id.goProfile:
                                //profile
                                Intent intentProfile = new Intent(HomeActivity.this,ProfileActivity.class);
                                startActivity(intentProfile);
                                return true;
                            case R.id.logoutButton:
                                //logout
                                FirebaseAuth.getInstance().signOut();
                                Intent intentLogout = new Intent(HomeActivity.this,LoginActivity.class);
                                startActivity(intentLogout);
                                return true;
                            case R.id.landscape:
                                Intent intentLandscape = new Intent(HomeActivity.this,HomeActivity.class);
                                intentLandscape.putExtra("search","Landscape");
                                startActivity(intentLandscape);
                                return true;
                            case R.id.Architecture:
                                Intent intentArchitecture = new Intent(HomeActivity.this,HomeActivity.class);
                                intentArchitecture.putExtra("search","Architecture");
                                startActivity(intentArchitecture);
                                return true;
                            case R.id.Art:
                                Intent intentArt = new Intent(HomeActivity.this,HomeActivity.class);
                                intentArt.putExtra("search","Art");
                                startActivity(intentArt);
                                return true;
                            case R.id.City:
                                Intent intentCity = new Intent(HomeActivity.this,HomeActivity.class);
                                intentCity.putExtra("search","City");
                                startActivity(intentCity);
                                return true;
                            case R.id.Textures:
                                Intent intentTextures = new Intent(HomeActivity.this,HomeActivity.class);
                                intentTextures.putExtra("search","Textures");
                                startActivity(intentTextures);
                            case R.id.Earth:
                                Intent intentEarth = new Intent(HomeActivity.this,HomeActivity.class);
                                intentEarth.putExtra("search","Earth");
                                startActivity(intentEarth);
                                return true;
                        }
                        return true;
                    }
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //set values into navigation bar
            // Name, email address, and profile photo Url
            String userUid = user.getUid();

            final View headerView = navigationView.getHeaderView(0);
            final TextView mUserNameHeader = headerView.findViewById(R.id.userNameHeader);
            TextView mUserEmailHeader = headerView.findViewById(R.id.userEmailHeader);

            //long winded way of getting users name, needs to be fixed with Firebase update change, see here //https://stackoverflow.com/questions/37661747/firebaseauth-getcurrentuser-return-null-displayname
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query =reference.child("Users").child(userUid).orderByChild("firstName");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        String name = dataSnapshot.child("firstName").getValue().toString();
                        mUserNameHeader.setText(name);
                        String userImgString = dataSnapshot.child("profileUri").getValue().toString();
                        ImageView userImageHeader = headerView.findViewById(R.id.userImageHeader);
                        Uri userImg  = Uri.parse(userImgString);
                        Glide.with(HomeActivity.this)
                                .asBitmap()
                                .load(userImg)
                                .into(userImageHeader);
                        userImageHeader.setImageURI(userImg);
                    }catch (Exception e){
                        Toast.makeText(HomeActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            String email = user.getEmail();
            mUserEmailHeader.setText(email);
        }


        //set listeners for FAB
        mFABCamera = findViewById(R.id.menu_item_camera);
        mFABCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openCameraIntent = new Intent(HomeActivity.this, MainActivity.class);
                openCameraIntent.putExtra("type","camera");
                startActivity(openCameraIntent);
            }
        });

        mFABGallery = findViewById(R.id.menu_item_gallery);
        mFABGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent  = new Intent(HomeActivity.this, MainActivity.class);
                openGalleryIntent.putExtra("type","gallery");
                startActivity(openGalleryIntent);

            }
        });
        imageBitmaps(); // call the image function

    } //end onCreate

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

    public void imageBitmaps() {
        //get posts and add them to arrays
        mAuth = FirebaseAuth.getInstance();
       // final String user_id = mAuth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if (getIntent().hasExtra("search")) {//check to see if user has opened the activity with a search or not
            final String search = getIntent().getStringExtra("search");
            Query query = reference.child("Posts");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.child("tags").getValue().toString().equals(search)) {
                                    //get values for images
                                    mImageIds.add(ds.getKey());
                                    mImageTags.add(ds.child("tags").getValue().toString());
                                    mImageTitle.add(ds.child("title").getValue().toString());
                                    mImageUrls.add(ds.child("link").getValue().toString());
                                    mImageUser.add(ds.child("username").getValue().toString());
                                    mImageDescription.add(ds.child("description").getValue().toString());
                                    mImageUserId.add(ds.child("userId").getValue().toString());
                                }
                            }
                        }
                        //for array list
                        RecyclerView();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }else {
            Query query = reference.child("Posts");
            //if user has searched for a value
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                mImageIds.add(ds.getKey());
                                mImageTags.add(ds.child("tags").getValue().toString());
                                mImageTitle.add(ds.child("title").getValue().toString());
                                mImageUrls.add(ds.child("link").getValue().toString());
                                mImageUser.add(ds.child("username").getValue().toString());
                                mImageDescription.add(ds.child("description").getValue().toString());
                                mImageUserId.add(ds.child("userId").getValue().toString());
                            }
                        }
                        //for array list
                        RecyclerView();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

    }

    private void RecyclerView(){
        //pass values to recycler view
        RecyclerView recyclerView = findViewById(R.id.homeRecyclerView);
        HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter(mImageTags,mImageTitle,mImageUrls,mImageIds,mImageUser,mImageDescription,mImageUserId,this);
        recyclerView.setAdapter(adapter);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(HomeActivity.this,3);// grid layout with two columns
        recyclerView.setLayoutManager(mGridLayoutManager);
    }

}
