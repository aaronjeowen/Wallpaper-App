package aaronwallpaperapp.com.wallpaperapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION = 1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private StorageReference mStorageRef;
    private EditText emailRegister,passwordRegister,firstNameRegister,lastNameRegister,usernameRegister;
    private Button registerButton,loginChange;
    private com.github.clans.fab.FloatingActionButton chooseProfileImage;
    private ImageView imageViewProfile;
    private Uri imageUri;
    private static  int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //set values
        emailRegister = (EditText) findViewById(R.id.emailRegister);
        passwordRegister = (EditText) findViewById(R.id.passwordRegister);
        usernameRegister =(EditText) findViewById(R.id.usernameRegister);
        firstNameRegister =(EditText) findViewById(R.id.firstNameRegister);
        lastNameRegister =(EditText) findViewById(R.id.lastNameRegister);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginChange = (Button) findViewById(R.id.loginChange);
        chooseProfileImage= findViewById(R.id.chooseProfileImage);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        //Firebase Values
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        loginChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        chooseProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        //choose image from gallery
                if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                        Intent pickImageIntent = new Intent();
                        pickImageIntent.setType("image/*");
                        pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST);
                    }else{
                        getWriteStoragePermision();
                    }
                }else{
                    getStoragePermision();
                }

            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user registers
                //check values are not null
               if(emailRegister!= null && passwordRegister!= null && usernameRegister != null && imageUri != null && lastNameRegister != null && firstNameRegister != null){
                final String email = emailRegister.getText().toString();
                final String password = passwordRegister.getText().toString();
                final String username = usernameRegister.getText().toString();

                Query checkUsername = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(username);
                checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            Toast.makeText(RegisterActivity.this, "Username is already taken, please choose another", Toast.LENGTH_LONG).show();
                        } else {
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        if(!task.isSuccessful()) {
                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthWeakPasswordException e) {
                                                Toast.makeText(RegisterActivity.this, "Please use a stronger password", Toast.LENGTH_LONG).show();
                                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                                Toast.makeText(RegisterActivity.this, "Please use a valid email", Toast.LENGTH_LONG).show();
                                            } catch (FirebaseAuthEmailException e) {
                                                Toast.makeText(RegisterActivity.this, "Email is already in use", Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                                Toast.makeText(RegisterActivity.this, "Sign up error", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        } else {

                                        //get user unique id
                                        final String user_id = mAuth.getCurrentUser().getUid();
                                        //make database reference for where the data will be held
                                        final StorageReference storageReference = mStorageRef.child("Users").child(user_id);
                                        final DatabaseReference newUser = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                                        InputStream imageStream = null;
                                        try {
                                            imageStream = getContentResolver().openInputStream(imageUri);
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }

                                        //compress avatar image
                                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                        imageViewProfile.setImageBitmap(selectedImage);
                                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, out);
                                        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                                        Uri newUri = getImageUri(RegisterActivity.this, decoded);
                                        storageReference.putFile(newUri)//upload image

                                             .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                 @Override
                                                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                     //get uploaded image name and url to pull later
                                                     String link = taskSnapshot.getDownloadUrl().toString();
                                                     final String firstName = firstNameRegister.getText().toString();
                                                     final String lastName = lastNameRegister.getText().toString();

                                                     Map userData = new HashMap();
                                                     userData.put("username", username);
                                                     userData.put("firstName", firstName);
                                                     userData.put("lastName", lastName);
                                                     userData.put("profileUri", link);
                                                     newUser.setValue(userData);
                                                     mAuth.addAuthStateListener(firebaseAuthListener);}
                                                });
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }else{
                   Toast.makeText(RegisterActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            // for image from gallery
            imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri);
        }
    }

    public void getStoragePermision(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);

        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION);
        }
    }
    public void getWriteStoragePermision(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);

        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION){
            if(grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        // might need to be removed, this could be compressing image twice.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
