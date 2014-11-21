package cochrane343.twelvedays;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "twelveDaysDoors";
    private static final String PREFIX_DOOR_PREF ="door";
    
    private static final String PREFIX_IMAGE_VIEW ="image";
    private static final String PREFIX_OVERLAY_VIEW ="overlay";
    private static final String PREFIX_NUMBER_VIEW ="number";
    
    private static final String PREFIX_SOUND_FILE ="sound";
    
    private static final String RESOURCE_TYPE_RAW = "raw";
    private static final String RESOURCE_TYPE_ID = "id";
    
    private MediaPlayer mediaPlayer;
    private boolean doors[];

    /* - - - - - OnClickListener - - - - - */
    
    private final View.OnClickListener numberOnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            final int doorNumber = getDoorNumber(view); 
            final GregorianCalendar today = new GregorianCalendar();
            
            if (today.get(Calendar.MONTH) == Calendar.DECEMBER) {
               if (today.get(Calendar.DAY_OF_MONTH) >= doorNumber) {
                    doors[doorNumber - 1] = true;
                    
                    openDoor(doorNumber);
                    playSound(doorNumber);
              }
           }
        }
    };
    
    private final View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            final int doorNumber = getDoorNumber(view);
        
            playSound(doorNumber);
        }
    };
    
    private int getDoorNumber(final View view) {
        final String contentDescription = view.getContentDescription().toString();
        return Integer.valueOf(contentDescription);
    }
        
    /* - - - - - Lifecycle Methods - - - - - - - - - */
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        loadDoorState();
    }
    
    @Override
    protected void onStop(){
        super.onStop();
        
        releaseMediaPlayer();
        
        saveDoorState();
    }

    /* - - - - - Doors - - - - - */ 
    
    private void saveDoorState() {
        final SharedPreferences doorPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editor = doorPreferences.edit();
     
        for (int i = 0; i < doors.length; i++) {
            editor.putBoolean(PREFIX_DOOR_PREF + i, doors[i]);
        }
        
        editor.commit();
    }
    
    private void loadDoorState() {
        final SharedPreferences doorPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        doors = new boolean[24];

        for (int i = 0; i < doors.length; i++) {
            doors[i] = doorPreferences.getBoolean(PREFIX_DOOR_PREF + i, false);
            
            if (doors[i]) {
                openDoor(i + 1);
            } else {
                closeDoor(i + 1);
            }
        }
    }

    private void closeDoor(final int doorNumber) {
        final TextView numberView = getNumberView(doorNumber);
        final ImageView overlayView = getOverlayView(doorNumber);
        final ImageView imageView = getImageView(doorNumber);
        
        numberView.setVisibility(View.VISIBLE);
        numberView.setOnClickListener(numberOnClickListener);
        
        overlayView.setImageResource(R.drawable.door);       
        
        imageView.setVisibility(View.GONE);
        imageView.setOnClickListener(null);           
    }
    
    private void openDoor(final int doorNumber) {
        final TextView numberView = getNumberView(doorNumber);  
        final ImageView overlayView = getOverlayView(doorNumber);
        final ImageView imageView = getImageView(doorNumber);

        numberView.setVisibility(View.GONE);
        numberView.setOnClickListener(null);
        
        overlayView.setImageResource(R.drawable.shadow);
        
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(imageOnClickListener);  
    }
    
    /* - - - - Views - - - - - */
    
     private final TextView getNumberView(final int doorNumber) {
        final int numberViewId = getIdentifier(PREFIX_NUMBER_VIEW + doorNumber);
        return (TextView) findViewById(numberViewId);
    }   
     
    private final ImageView getOverlayView(final int doorNumber) {
        final int overlayViewId = getIdentifier(PREFIX_OVERLAY_VIEW + doorNumber);
        return (ImageView) findViewById(overlayViewId);
    }     
     
    private ImageView getImageView(final int doorNumber) {
        final int imageViewId = getIdentifier(PREFIX_IMAGE_VIEW + doorNumber);
        return (ImageView) findViewById(imageViewId);
    }

    private int getIdentifier(final String name) {
        return getResources().getIdentifier(name, RESOURCE_TYPE_ID, getPackageName());
    }

    /* - - - - MediaPlayer - - - - - */
    
    private void playSound(final int doorNumber) {
        // TODO Fallback to default sound
        final int soundId = getResources().getIdentifier(PREFIX_SOUND_FILE + doorNumber, RESOURCE_TYPE_RAW, getPackageName());

        releaseMediaPlayer();
        
        mediaPlayer = MediaPlayer.create(this, soundId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseMediaPlayer();
            }
        });
        mediaPlayer.start(); 
    }
    
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
