package com.example.ping3.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.ping3.utils.FileUtils;
import com.example.ping3.utils.ImageUtils;
import com.example.ping3.utils.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.ping3.R;
import com.example.ping3.adapters.ChatAdapter;
import com.example.ping3.models.Message;
import com.google.firebase.storage.UploadTask;


public class ChatActivity extends AppCompatActivity {


    private static final int CAMERA = 1;
    private static final int GALLERY = 2;
    private static final int RECORD_AUDIO = 3;

    private static final String[] WRITE_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final String[] RECORD_AUDIO_PERMISSION = new String[]{Manifest.permission.RECORD_AUDIO};

    private static  String id1=null;

    private List<Message> messages;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;

    private boolean chatStatus = true;


    private final TextWatcher editMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() == 0) {
                messageSendButton.setEnabled(false);
            } else if(chatStatus){
                messageSendButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private String imagePath;
    private String imageUrl;
    private String pseudo;
    private Uri imageUri;
    private Bitmap resizedImage;
    private ListView messageListView;
    private ProgressBar progressBar;
    private final ChildEventListener messageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            if (message != null) {
                chatAdapter.add(message);
                messageListView.setSelection(messages.size() - 1);
            } else {
                Log.w("Chat", "No messages");
            }
            if (progressBar.getVisibility() == View.VISIBLE) {
                hideProgressBar();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("Chat", "loadPost:onCancelled", databaseError.toException());
        }
    };
    private MediaRecorder mediaRecorder;
    private boolean recording;
    private String recordPath;
    private String recordUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        progressBar = (ProgressBar) findViewById(R.id.chat_spinner);


        messageEditView = (EditText) findViewById(R.id.message_edit);
        messageEditView.addTextChangedListener(editMessageTextWatcher);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setEnabled(false);
        messageSendButton.setOnClickListener(v -> sendMessage());


        recording = false;
        messages = new ArrayList<>();
        Bundle extra = getIntent().getExtras();
        id1 = extra.getString("id");
        pseudo = extra.getString("pseudo");
        System.out.println("pseudo"+pseudo);
        System.out.println("id1"+id1);


        FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id1).child("userMessages").child("messages")
                .addChildEventListener(messageListener);

        FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id1).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Integer.parseInt(snapshot.getValue().toString()) == 98){
                    chatStatus = false;
                    onChatMute();
                }
                else {
                    chatStatus = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatAdapter = new ChatAdapter(this, messages);
        messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setVisibility(View.GONE);

        messageListView.setAdapter(chatAdapter);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initializeMediaRecord(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }

    private void sendMessage() {
        Message newMessage = new Message();

        // text message
        if (imageUrl == null && recordUrl == null) {

            String textContent = messageEditView.getText().toString();
            newMessage.setType(Message.Type.TEXT);
            newMessage.setContent(textContent);

            messageEditView.setText("");
        }
        // image message
        else if(imageUrl != null) {

            newMessage.setType(Message.Type.IMAGE);
            newMessage.setContent(imageUrl);

            imageUrl = null;
        }
        else if(recordUrl != null){
            newMessage.setType(Message.Type.SOUND);
            newMessage.setContent(recordUrl);

            recordUrl = null;
        }
        else{
            Log.e("Chat", "Unknow message type");
        }

        newMessage.setDate(new Date());
        newMessage.setSenderId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        newMessage.setSenderPseudo(pseudo);

        String id = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id1).child("userMessages").child("messages").push().getKey();

        newMessage.setId(id);

        FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id1).child("userMessages").child("messages")
                .child(id)
                .setValue(newMessage);
    }

    private void sendImage(Bitmap image) {

        StorageReference storageReference = FirebaseStorage.getInstance("gs://ping3-4cb4a.appspot.com").getReference(imagePath);
        savePictureOnline(image, storageReference, taskSnapshot -> {
            Log.w("Chat", "Image uploaded, now sending message");
            // send a image message
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                imageUrl = e.toString();
                sendMessage();
            });

        }, e -> Log.w("Chat", e.getMessage()));
    }

    private void sendRecord(){
        StorageReference storageReference=FirebaseStorage.getInstance("gs://ping3-4cb4a.appspot.com").getReference(imagePath);
        saveRecordOnline(recordPath, storageReference, taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                recordUrl = e.toString();
                sendMessage();
            });
        }, e -> Log.w("Chat", e.getMessage()));

    }

    private void showImageAttachementDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasWritePermission(this)) {
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_PERMISSION, GALLERY);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasWritePermission(this)) {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(galleryIntent, GALLERY);
        }
    }

    private void takePhotoFromCamera() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasCameraPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION[0], WRITE_EXTERNAL_PERMISSION[0]}, CAMERA);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasCameraPermission(this) ||
                PermissionUtils.hasWritePermission(this)) {
            Intent takePhotoIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            System.out.println(getApplicationContext().getPackageName() + ".my.package.name.provider");
            imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".my.package.name.provider",
                    FileUtils.createFileWithExtension("jpg"));

            takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePhotoIntent, CAMERA);
        }
    }

    private void voiceRecordingAction(){

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasAudioRecordPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_PERMISSION[0], RECORD_AUDIO_PERMISSION[0]}, RECORD_AUDIO);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasAudioRecordPermission(this)
                || PermissionUtils.hasWritePermission(this)) {

            if(!recording){
                Toast.makeText(ChatActivity.this, "Started voice recording", Toast.LENGTH_SHORT).show();

                initializeMediaRecord();
                startRecordingAudio();
            }
            else{
                Toast.makeText(ChatActivity.this, "Stopped voice recording", Toast.LENGTH_SHORT).show();

                stopRecordingAudio();
                sendRecord();
            }
            recording = !recording;
        }
    }

    private void startRecordingAudio(){
        File audioFile = FileUtils.createFileWithExtension("3gpp");
        recordUrl = null;
        recordPath = audioFile.getAbsolutePath();
        mediaRecorder.setOutputFile(recordPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio(){

        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();

            mediaRecorder = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    imagePath = saveImage(image);

                    sendImage(resizedImage);

                    Toast.makeText(ChatActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePath = saveImage(image);
                Toast.makeText(ChatActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                sendImage(resizedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromGallery();

                } else {
                    Toast.makeText(this, "GALLERY DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();

                } else {
                    Toast.makeText(this, "CAMERA DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    voiceRecordingAction();
                } else {
                    Toast.makeText(this, "RECORD AUDIO DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }


        }
    }

    public String saveImage(Bitmap myBitmap) {

        File file = FileUtils.createFileWithExtension("jpg");
        resizedImage = ImageUtils.resizeImage(myBitmap);
        ByteArrayOutputStream bytes = ImageUtils.compressImage(resizedImage);

        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(bytes.toByteArray());

            MediaScannerConnection.scanFile(this,
                    new String[]{file.getPath()},
                    new String[]{"image/jpeg"}, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TAG", "File Saved::--->" + file.getAbsolutePath());

        return file.getAbsolutePath();
    }



    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

        messageListView.setVisibility(View.VISIBLE);

    }
    public static void saveRecordOnline(String audioPath, StorageReference reference, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {

        Uri audioUri = Uri.fromFile(new File(audioPath));
        UploadTask uploadTask = reference.putFile(audioUri);

        if (onSuccessListener != null) {
            uploadTask.addOnSuccessListener(onSuccessListener);
        }
        if (onFailureListener != null) {
            uploadTask.addOnFailureListener(onFailureListener);
        }

        uploadTask.addOnFailureListener(exception -> {
            Log.w("Chat", "save audio online: ko ", exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d("Chat", "save audio: ok");
        });
    }
    public static void savePictureOnline(Bitmap bitmap, StorageReference reference, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);

        if (onSuccessListener != null) {
            uploadTask.addOnSuccessListener(onSuccessListener);
        }
        if (onFailureListener != null) {
            uploadTask.addOnFailureListener(onFailureListener);
        }

        uploadTask.addOnFailureListener(exception -> {
            Log.w("Chat", "save picture online: ko ", exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d("Chat", "save picture: ok");
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id1).child("userMessages").child("messages").removeEventListener(messageListener);
    }

    public void onChatMute(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Attention");
        builder.setMessage("Impossible d'utiliser chat room");
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // myRef_exit.removeValue();
            }
        });
        // Create the alert dialog using alert dialog builder
        AlertDialog dialog = builder.create();

        // Finally, display the dialog when user press back button
        dialog.show();
    }
}
