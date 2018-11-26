package aaronwallpaperapp.com.wallpaperapp;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ArrayList<String> mImageTags = new ArrayList<>();
    private ArrayList<String> mImageTitle = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mImageIds = new ArrayList<>();
    private ArrayList<String> mImageUser = new ArrayList<>();
    private ArrayList<String> mImageDescription = new ArrayList<>();
    private ArrayList<String> mImageUserId = new ArrayList<>();

    private TextView profile,profileName;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private ImageView imageView2;
    private DrawerLayout mDrawerLayout;
    private  String userUid;
    private FirebaseUser user;
    private com.github.clans.fab.FloatingActionButton mFABCamera,mFABGallery;
    private Object userprofile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        profileName = findViewById(R.id.ProfileName);



        // set listeners and actions for the menu
        mDrawerLayout = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        //mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case android.R.id.home:
                                //open navigation pane
                                mDrawerLayout.openDrawer(GravityCompat.START);
                                return true;
                            case R.id.goHome:
                                //go to home page
                                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.addPicture:
                                // add media
                                Intent intentPicture = new Intent(ProfileActivity.this, MainActivity.class);
                                startActivity(intentPicture);
                                return true;
                            case R.id.goProfile:
                                //profile, close draw
                                mDrawerLayout.closeDrawers();
                                return true;
                            case R.id.logoutButton:
                                //logout and start login activity
                                FirebaseAuth.getInstance().signOut();
                                Intent intentLogout = new Intent(ProfileActivity.this,LoginActivity.class);
                                startActivity(intentLogout);
                                return true;
                            case R.id.landscape:
                                Intent intentLandscape = new Intent(ProfileActivity.this,HomeActivity.class);
                                intentLandscape.putExtra("search","Landscape");
                                startActivity(intentLandscape);
                                return true;
                            case R.id.Architecture:
                                Intent intentArchitecture = new Intent(ProfileActivity.this,HomeActivity.class);
                                intentArchitecture.putExtra("search","Architecture");
                                startActivity(intentArchitecture);
                                return true;
                            case R.id.Art:
                                Intent intentArt = new Intent(ProfileActivity.this,HomeActivity.class);
                                intentArt.putExtra("search","Art");
                                startActivity(intentArt);
                                return true;
                            case R.id.City:
                                Intent intentCity = new Intent(ProfileActivity.this,HomeActivity.class);
                                intentCity.putExtra("search","City");
                                startActivity(intentCity);
                                return true;
                            case R.id.Textures:
                                Intent intentTextures = new Intent(ProfileActivity.this,HomeActivity.class);
                                intentTextures.putExtra("search","Textures");
                                startActivity(intentTextures);
                            case R.id.Earth:
                                Intent intentEarth = new Intent(ProfileActivity.this,HomeActivity.class);
                                intentEarth.putExtra("search","Earth");
                                startActivity(intentEarth);
                                return true;
                        }
                        return true;
                    }
                });


        if(getIntent().hasExtra("image_UserId")){
            final String userId = getIntent().getStringExtra("image_UserId");

            //long winded way of getting users name, needs to be fixed with Firebase update change, see here //https://stackoverflow.com/questions/37661747/firebaseauth-getcurrentuser-return-null-displayname
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query =reference.child("Users").child(userId).orderByChild("firstName");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {

                    String name = dataSnapshot.child("firstName").getValue().toString();
                    String newName = name.substring(0, 1).toUpperCase() + name.substring(1);
                    profileName.setText(newName);
                    String userImgString = dataSnapshot.child("profileUri").getValue().toString();
                    Uri userImg = Uri.parse(userImgString);

                    //user profile
                    ImageView profileImage = findViewById(R.id.profileImage);
                    Glide.with(ProfileActivity.this)
                            .asBitmap()
                            .load(userImg)
                            .into(profileImage);
                }catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "" + e, Toast.LENGTH_SHORT).show();

                }
                    String username = dataSnapshot.child("username").getValue().toString();
                    actionBar.setTitle(username);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
            userUid = user.getUid();

            //long winded way of getting users name, needs to be fixed with Firebase update change, see here //https://stackoverflow.com/questions/37661747/firebaseauth-getcurrentuser-return-null-displayname
            DatabaseReference newreference = FirebaseDatabase.getInstance().getReference();
            Query newquery =newreference.child("Users").child(userUid).orderByChild("firstName");
            newquery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
                        userUid = user.getUid();
                        View headerView = navigationView.getHeaderView(0);
                        TextView mUserNameHeader = headerView.findViewById(R.id.userNameHeader);
                        TextView mUserEmailHeader = headerView.findViewById(R.id.userEmailHeader);

                        String name = dataSnapshot.child("firstName").getValue().toString();
                        String newName = name.substring(0, 1).toUpperCase() + name.substring(1);
                        mUserNameHeader.setText(newName);

                        String email = user.getEmail();
                        mUserEmailHeader.setText(email);

                        String userProfileImage = dataSnapshot.child("profileUri").getValue().toString();
                        ImageView userImageHeader = headerView.findViewById(R.id.userImageHeader);
                        Uri userImgURI = Uri.parse(userProfileImage);
                        Glide.with(ProfileActivity.this)
                                .asBitmap()
                                .load(userImgURI)
                                .into(userImageHeader);

                    }catch (Exception e) {
                        Toast.makeText(ProfileActivity.this, "" + e, Toast.LENGTH_SHORT).show();

                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
            userUid = user.getUid();
            //long winded way of getting users name, needs to be fixed with Firebase update change, see here //https://stackoverflow.com/questions/37661747/firebaseauth-getcurrentuser-return-null-displayname
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query =reference.child("Users").child(userUid).orderByChild("firstName");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
                        userUid = user.getUid();
                        View headerView = navigationView.getHeaderView(0);
                         TextView mUserNameHeader = headerView.findViewById(R.id.userNameHeader);
                        TextView mUserEmailHeader = headerView.findViewById(R.id.userEmailHeader);
                        String name = dataSnapshot.child("firstName").getValue().toString();
                        String newName = name.substring(0, 1).toUpperCase() + name.substring(1);
                        mUserNameHeader.setText(newName);
                        profileName.setText(newName);

                        String userImgString = dataSnapshot.child("profileUri").getValue().toString();
                        ImageView userImageHeader = headerView.findViewById(R.id.userImageHeader);
                        Uri userImg = Uri.parse(userImgString);
                        Glide.with(ProfileActivity.this)
                                .asBitmap()
                                .load(userImg)
                                .into(userImageHeader);

                    //user profile
                    ImageView profileImage = findViewById(R.id.profileImage);
                    Glide.with(ProfileActivity.this)
                            .asBitmap()
                            .load(userImg)
                            .into(profileImage);

                        Uri photoUrl = user.getPhotoUrl();
                        String email = user.getEmail();
                        mUserEmailHeader.setText(email);
                        String username = dataSnapshot.child("username").getValue().toString();
                        actionBar.setTitle(username);

                    }catch (Exception e) {
                        Toast.makeText(ProfileActivity.this, "" + e, Toast.LENGTH_SHORT).show();

                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        imageBitmaps(); // call the image function

        //set on click for FAB button
        mFABCamera = findViewById(R.id.menu_item_camera);
        mFABCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openCameraIntent = new Intent(ProfileActivity.this, MainActivity.class);
                openCameraIntent.putExtra("type","camera");
                startActivity(openCameraIntent);
            }
        });

        mFABGallery = findViewById(R.id.menu_item_gallery);
        mFABGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent  = new Intent(ProfileActivity.this, MainActivity.class);
                openGalleryIntent.putExtra("type","gallery");
                startActivity(openGalleryIntent);
            }
        });
    }


    //Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!getIntent().hasExtra("image_UserId")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_menu, menu);
            return true;
        }else {
            return true;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.goHome:
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                //return true;
            case R.id.changeProfileImage:
                Toast.makeText(this, "IT WORKS", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void imageBitmaps() {
        //get user id and user posts, populate arrays
        if(getIntent().hasExtra("image_UserId")) {
            String userId =  getIntent().getStringExtra("image_UserId");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child("Posts").child(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //set value into arrays
                            mImageIds.add(snapshot.getKey());
                            mImageTags.add(snapshot.child("tags").getValue().toString());
                            mImageTitle.add(snapshot.child("title").getValue().toString());
                            mImageUrls.add(snapshot.child("link").getValue().toString());
                            mImageUser.add(snapshot.child("username").getValue().toString());
                            mImageDescription.add(snapshot.child("description").getValue().toString());
                            mImageUserId.add(snapshot.child("userId").getValue().toString());
                        }
                        RecyclerView();//call recycler view to display users images
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            mAuth = FirebaseAuth.getInstance();
            final String user_id = mAuth.getCurrentUser().getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child("Posts").child(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //set values into arrays
                            mImageIds.add(snapshot.getKey());
                            mImageTags.add(snapshot.child("tags").getValue().toString());
                            mImageTitle.add(snapshot.child("title").getValue().toString());
                            mImageUrls.add(snapshot.child("link").getValue().toString());
                            mImageUser.add(snapshot.child("username").getValue().toString());
                            mImageDescription.add(snapshot.child("description").getValue().toString());
                            mImageUserId.add(snapshot.child("userId").getValue().toString());
                        }
                        RecyclerView();//call recycler view to display users images
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void RecyclerView(){
        //pass arrays into recycler
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter(mImageTags,mImageTitle,mImageUrls,mImageIds,mImageUser,mImageDescription,mImageUserId,this);
        recyclerView.setAdapter(adapter);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(ProfileActivity.this,2);// grid layout with two columns
        recyclerView.setLayoutManager(mGridLayoutManager);
        }
    }





