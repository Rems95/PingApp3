package com.example.ping3.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ping3.utils.CacheUtils;
import com.example.ping3.utils.SoundUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.example.ping3.R;
import com.example.ping3.models.Message;

public class ChatAdapter extends ArrayAdapter<Message> {

    private final List<Message> messages;

    private FirebaseUser user;

    private Activity activity;

    private MediaPlayer mediaPlayer;

    private SimpleDateFormat dateFormat;

    public ChatAdapter(@NonNull Activity activity, List<Message> messages) {
        super(activity, 0, messages);

        this.activity = activity;
        this.messages = messages;

        dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        user = FirebaseAuth.getInstance().getCurrentUser();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getId().hashCode();
    }

    @Override
    public int getViewTypeCount() {
        return Message.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    private View getInflatedLayoutForType(int type) {

        if (type == Message.Type.TEXT.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_text, null);
        } else if (type == Message.Type.IMAGE.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_image, null);
        } else if (type == Message.Type.SOUND.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_sound, null);
        } else {
            return null;
        }
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Message message = messages.get(position);

        if (convertView == null) {
            int type = getItemViewType(position);
            convertView = getInflatedLayoutForType(type);
        }


        RelativeLayout.LayoutParams mainViewParams = null;
        View abstractView = null;

        // Text
        if (message.getType() == Message.Type.TEXT) {

            String textContent = (String) message.getContent();

            TextView messageView = (TextView) convertView.findViewById(R.id.chat_message);
            abstractView = messageView;

            messageView.setText(textContent);
            //messageView.setPadding(20, 10, 20, 10);

            if (message.getSenderId().equals(user.getUid())) {
                messageView.setTextColor(Color.BLACK);
            } else {
                messageView.setTextColor(Color.WHITE);
            }

            mainViewParams = (RelativeLayout.LayoutParams) messageView.getLayoutParams();

            // Image
        } else if (message.getType() == Message.Type.IMAGE) {

            String imageUrl = (String) message.getContent();
            ImageView imageView = (ImageView) convertView.findViewById(R.id.chat_image);
            abstractView = imageView;


            mainViewParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();


            StorageReference storageReference = FirebaseStorage.getInstance("gs://ping3-4cb4a.appspot.com").getReferenceFromUrl(imageUrl);

            loadImage(storageReference,
                    bitmap -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    },
                    null);
        }

        else if (message.getType() == Message.Type.SOUND) {
            String recordUrl = (String) message.getContent();
            ImageButton button = (ImageButton) convertView.findViewById(R.id.chat_audio);
            abstractView = button;

            mainViewParams = (RelativeLayout.LayoutParams) button.getLayoutParams();

            button.setPadding(20, 10, 20, 10);

            StorageReference storageReference = FirebaseStorage.getInstance("gs://ping3-4cb4a.appspot.com").
                    getReferenceFromUrl(recordUrl);

            loadRecord(storageReference, message.getId(),
                    audioRecordInputStream -> {

                        if (audioRecordInputStream != null) {

                            //event for on click
                            button.setOnClickListener(v -> {
                                if (!mediaPlayer.isPlaying()) {
                                    try {
                                        button.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_stop_black_24px));

                                        mediaPlayer.reset();

                                        mediaPlayer.setDataSource(audioRecordInputStream.getFD());

                                        mediaPlayer.prepare();
                                        mediaPlayer.start();

                                        mediaPlayer.setOnCompletionListener(mp -> button.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_arrow_black_24px)));

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    button.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_arrow_black_24px));
                                    mediaPlayer.stop();
                                }
                            });


                        }
                    },
                    null, activity);

        } else {
            Log.w("Chat", "Wrong type of message");
        }

        TextView dateView = (TextView) convertView.findViewById(R.id.chat_date);
        String dateText = dateFormat.format(message.getDate());
        dateView.setText(dateText);

        RelativeLayout.LayoutParams dateViewParams = (RelativeLayout.LayoutParams) dateView.getLayoutParams();

        if (message.getSenderId().equals(user.getUid())) {
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            abstractView.setBackgroundResource(R.drawable.rounded_corner_sent);

        } else {
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            abstractView.setBackgroundResource(R.drawable.rounded_corner_received);
        }

        abstractView.setPadding(20, 10, 20, 10);

        return convertView;
    }
    public static void loadImage(StorageReference storageReference, OnSuccessListener<Bitmap> onSuccessListener, OnFailureListener onFailureListener) {
        Bitmap bitmapFromMemCache = CacheUtils.getBitmapFromMemCache(storageReference.getPath());
        if (bitmapFromMemCache != null) {
            Log.d("Cache", "loadImage: ok");
            onSuccessListener.onSuccess(bitmapFromMemCache);
        } else {
            try {

                final long ONE_MEGABYTE = 1024 * 1024;

                Task<byte[]> task = storageReference.getBytes(ONE_MEGABYTE);//async storage exception when the image doesn't exist

                if (onFailureListener != null) {
                    task.addOnFailureListener(onFailureListener);
                }
                task.addOnSuccessListener(bytes -> {
                    Log.d("Chat", "loadImage() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    if (image != null) {
                        CacheUtils.addBitmapToMemoryCache(storageReference.getPath(), image);
                        if (onSuccessListener != null) {
                            onSuccessListener.onSuccess(image);
                        }
                    }

                    Log.d("Cache", "storeImage:ok ");

                }).addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.d("Chat", "loadImage() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
                    Log.w("Chat", "loadProfileImage: ", exception);
                });
            } catch (RuntimeException e) {
                Log.w("Chat", "loadImage: ", e);
            }
        }
    }
    public static void loadRecord(StorageReference storageReference, String identifier, OnSuccessListener<FileInputStream> onSuccessListener, OnFailureListener onFailureListener, Activity activity) {
        //Retrieve the record from the cache
        FileInputStream fileInputStreamFromCache = CacheUtils.getRecordFromMemCache(identifier, activity);
        if (fileInputStreamFromCache != null) {
            //cache work
            Log.d("Cache", "loadRecord: ok identifier = [" + identifier + "]+activity = [" + activity + "]");
            onSuccessListener.onSuccess(fileInputStreamFromCache);
        } else {
            try {
                final long ONE_MEGABYTE = 1024 * 1024;

                Task<byte[]> task = storageReference.getBytes(ONE_MEGABYTE);

                if (onSuccessListener != null) {
                    task.addOnSuccessListener(bytes -> {
                        try {
                            File file = SoundUtils.decodeByteArray(identifier, bytes, activity);
                            if (file != null) {
                                Log.d("Cache", "SaveRecord: ok identifier = [" + identifier + "]+activity = [" + activity + "]");
                                CacheUtils.addRecordToMemoryCache(identifier, file.getAbsolutePath());
                                onSuccessListener.onSuccess(new FileInputStream(file));
                            }

                        } catch (FileNotFoundException e) {
                            Log.e("Chat", "loadRecord: fail ", e);
                        }
                    });
                }
                if (onFailureListener != null) {
                    task.addOnFailureListener(onFailureListener);
                }
                task.addOnSuccessListener(bytes -> Log.d("Chat", "loadRecord() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]"));
                task.addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.d("Chat", "loadRecord() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
                    Log.w("Chat", "loadRecord: ", exception);
                });
            } catch (RuntimeException e) {
                Log.w("Chat", "loadRecord: ", e);
            }
        }
    }

}
