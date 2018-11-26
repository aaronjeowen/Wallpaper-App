package aaronwallpaperapp.com.wallpaperapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private Uri imageUri;
    private EditText textInput,descriptionInput;
    private ImageView imageView;
    private Button addButton;
    private ImageButton changeImage;
    private String mImageFileLocation;
    private String tagSelection;
    private ProgressBar progressBar;


    private static final int PICK_IMAGE_REQUEST = 3;
    static final int REQUEST_TAKE_PHOTO = 1;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set vars
        imageView =(ImageView) findViewById(R.id.imageView);
        textInput =(EditText)findViewById(R.id.textInput);
        addButton = (Button) findViewById(R.id.addButton);
        descriptionInput = findViewById(R.id.description);
        changeImage = findViewById(R.id.changeImage);
        Spinner spinner = (Spinner) findViewById(R.id.userTags);
        ImageButton changeImage = findViewById(R.id.changeImage);
        progressBar =findViewById(R.id.progressBar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dropDownArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        isExternalStorageWritable();
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
                                mDrawerLayout.openDrawer(GravityCompat.START);
                                return true;
                            case R.id.goHome:
                                //home page
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.addPicture:
                                //add media - this page
                                mDrawerLayout.closeDrawers();
                                return true;
                            case R.id.goProfile:
                                //user profile
                                Intent intentProfile = new Intent(MainActivity.this,ProfileActivity.class);
                                startActivity(intentProfile);
                                return true;
                            case R.id.logoutButton:
                                //logout
                                FirebaseAuth.getInstance().signOut();
                                Intent intentLogout = new Intent(MainActivity.this,LoginActivity.class);
                                startActivity(intentLogout);
                                return true;

                            case R.id.landscape:
                                Intent intentLandscape = new Intent(MainActivity.this,HomeActivity.class);
                                    intentLandscape.putExtra("search","Landscape");
                                startActivity(intentLandscape);
                                return true;
                            case R.id.Architecture:
                                Intent intentArchitecture = new Intent(MainActivity.this,HomeActivity.class);
                                intentArchitecture.putExtra("search","Architecture");
                                startActivity(intentArchitecture);
                                return true;
                            case R.id.Art:
                                Intent intentArt = new Intent(MainActivity.this,HomeActivity.class);
                                intentArt.putExtra("search","Art");
                                startActivity(intentArt);
                                return true;
                            case R.id.City:
                                Intent intentCity = new Intent(MainActivity.this,HomeActivity.class);
                                intentCity.putExtra("search","City");
                                startActivity(intentCity);
                                return true;
                            case R.id.Textures:
                                Intent intentTextures = new Intent(MainActivity.this,HomeActivity.class);
                                intentTextures.putExtra("search","Textures");
                                startActivity(intentTextures);
                            case R.id.Earth:
                                Intent intentEarth = new Intent(MainActivity.this,HomeActivity.class);
                                intentEarth.putExtra("search","Earth");
                                startActivity(intentEarth);
                                return true;
                        }
                        return true;
                    }
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
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
                        // set values to sidebar navigation
                    String name = dataSnapshot.child("firstName").getValue().toString();
                    mUserNameHeader.setText(name);
                    String userImgString = dataSnapshot.child("profileUri").getValue().toString();
                    ImageView userImageHeader = headerView.findViewById(R.id.userImageHeader);
                    Uri userImg  = Uri.parse(userImgString);
                    Glide.with(MainActivity.this)
                            .asBitmap()
                            .load(userImg)
                            .into(userImageHeader);
                    userImageHeader.setImageURI(userImg);
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            String email = user.getEmail();
            mUserEmailHeader.setText(email);
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when users clicks upload
                progressBar.setVisibility(View.VISIBLE); //set spinning loading
                final String title = textInput.getText().toString();
                final String user_id = mAuth.getCurrentUser().getUid();
                final String tags = tagSelection;
                final String desc = descriptionInput.getText().toString();
                    // perform check
                    if (!title.equals("") && !tags.equals("") && !desc.equals("") && imageView!=null){
                    //make connection
                    final DatabaseReference newPost = FirebaseDatabase.getInstance().getReference().child("Posts").child(user_id);
                    final DatabaseReference newPostRef = newPost.push();

                    final StorageReference storageReference = mStorageRef.child("Posts").child("Users").child(user_id).child(title + ".jpg");
                    //check then input
                    //get username name rather than the UID
                    //username is posted with the post along with url etc.
                    Query getUsername = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).child("username");
                    getUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String username = dataSnapshot.getValue().toString();
                            //compress image - without this images are too large and can't be downloaded.
                            //50 seems to be a good amount
                            InputStream imageStream = null;
                            try {
                                imageStream = getContentResolver().openInputStream(imageUri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            imageView.setImageBitmap(selectedImage);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
                            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                            Uri newUri = getImageUri(MainActivity.this, decoded);

                            storageReference.putFile(newUri)//upload image
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            //get uploaded image name and url to pull later
                                            String link = taskSnapshot.getDownloadUrl().toString();
                                            Map userData = new HashMap();
                                            //put data into hashmap and save
                                            userData.put("username", username);
                                            userData.put("title",title);
                                            userData.put("tags", tags);
                                            userData.put("link", link);
                                            userData.put("description", desc);
                                            userData.put("userId", user_id);
                                            newPostRef.setValue(userData);

                                            //set data into put extra and start new activity
                                            Intent intent = new Intent(MainActivity.this, PostActivity.class);
                                            intent.putExtra("image_url",link);
                                            intent.putExtra("image_name",tags);
                                            intent.putExtra("image_Title",title);
                                            intent.putExtra("image_Ids",newPostRef.getKey());
                                            intent.putExtra("image_User",username);
                                            intent.putExtra("image_Description",desc);
                                            intent.putExtra("image_UserId",user_id);
                                            startActivity(intent);
                                        }
                                    });
                                 }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    Toast.makeText(MainActivity.this, "Image upload successful", Toast.LENGTH_SHORT).show();

            }else {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // see if activity has been opened with the intent to use camera or gallery
        String getPassedData = getIntent().getStringExtra("type");
        if (getPassedData!= null) {
            //check for camera or gallery and open that intent
            if (getPassedData.equals("camera")) {  // see if user has opened activity with intention to use camera
                Intent takePictureIntent = new Intent();
                takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                File img = null; //make a file area for this image to be saved to
                try {
                    img = createImageFile(); // make file where image will be saved
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // file can only be saved by going through the file provider - defines area where app can use
                String authorities = getApplicationContext().getPackageName() + ".FileProvider";
                imageUri = FileProvider.getUriForFile(this, authorities, img);
                //add the image uri to the intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                // start activity
               startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            } else if (getPassedData.equals("gallery")) {
                Intent pickImageIntent = new Intent();
                pickImageIntent.setType("image/*");
                pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST);
            }else{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
            }else{
                //need to let user choose how they want to import an image
                openDialogBox();
            }

            //if user wants to change image open dialog
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openDialogBox();
            }
        });
    }


    public void openDialogBox(){
        //box to choose gallery or camera intent
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Input")
                .setItems(R.array.dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            Intent takePictureIntent = new Intent();
                            takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                            File img = null;
                            try {
                                img = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String authorities = getApplicationContext().getPackageName() + ".FileProvider";
                            imageUri = FileProvider.getUriForFile(MainActivity.this, authorities, img);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        }else{

                            Intent pickImageIntent = new Intent();
                            pickImageIntent.setType("image/*");
                            pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST);
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

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }else{
            Toast.makeText(this, "FALSE", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        tagSelection = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mPhotoFile = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(mPhotoFile,".jpg",storageDir);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        // might need to be removed, this could be compressing image twice.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                //for image from camera
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            // for image from gallery
          imageUri = data.getData();
          imageView.setImageURI(imageUri);
        }
    }

    }



