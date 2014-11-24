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

/**
 * @author cochrane343
 * @since 0.1
 */
public class MainActivity extends Activity {
    private static final String PREFS_NAME = "twelveDaysDoors";
    private static final String PREFIX_DOOR_PREF ="door";
    
    private static final String PREFIX_IMAGE_VIEW ="image";
    private static final String PREFIX_OVERLAY_VIEW ="overlay";
    private static final String PREFIX_NUMBER_VIEW ="number";
    
    private static final String PREFIX_SOUND_FILE ="sound";
    
    private static final String RESOURCE_TYPE_RAW = "raw";
    private static final String RESOURCE_TYPE_ID = "id";
    
    private static final int NO_RESOURCE = 0;
    
    private MediaPlayer mediaPlayer;
    private boolean doors[];

    /* - - - - - OnClickListener - - - - - */
    
    /**
     * The {@link View.OnClickListener OnClickListener} for the {@link TextView TextViews}
     * displaying the numbers of the individual doors. It opens the respective door and
     * plays its sound, if the current date is within December and the day of the month is
     * greater or equal to the door's number.
     */
    private final View.OnClickListener numberOnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {           
            final int doorNumber = getDoorNumber(view); 
            
            final GregorianCalendar today = new GregorianCalendar();
            final boolean canOpenDoor = (today.get(Calendar.MONTH) == Calendar.DECEMBER) 
               && (today.get(Calendar.DAY_OF_MONTH) >= doorNumber);
            
            if (canOpenDoor) {
                doors[doorNumber - 1] = true;
                    
                openDoor(doorNumber);
                playSound(doorNumber);
           }
        }
    };
    
    /**
     * The {@link View.OnClickListener OnClickListener} for the {@link ImageView ImageViews}
     * displaying the images behind the individual doors. Plays the door's sound, so the
     * Advent calendar can be used as a sound board.
     */
    private final View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            final int doorNumber = getDoorNumber(view);
        
            playSound(doorNumber);
        }
    };
        
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
    
    /**
     * Saves the current state of the doors to a {@link SharedPreferences} file,
     * i.e. which ones are already opened and which ones are still closed.
     */
    private void saveDoorState() {
        final SharedPreferences doorPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editor = doorPreferences.edit();
     
        for (int i = 0; i < doors.length; i++) {
            editor.putBoolean(PREFIX_DOOR_PREF + i, doors[i]);
        }
       
        editor.commit();
    }
    
    /**
     * Loads the current state of the doors from a {@link SharedPreferences} file,
     * i.e. which ones are already opened and which ones are still closed.
     * Per default all doors are closed.
     */
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

    /**
     * Sets the closed state of an individual door, e.g. sets the {@link OnClickListener}
     * to allow the user to open the door.
     * @param doorNumber the number of the door to set closed (1-based)
     */
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
    
    /**
     * Sets the closed state of an individual door, e.g. hiding the {@link TextView}
     * displaying the number of the door.
     * @param doorNumber the number of the door to set opened (1-based)
     */
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
        final int numberViewIdentifier = getIdentifier(PREFIX_NUMBER_VIEW + doorNumber, RESOURCE_TYPE_ID);
        return (TextView) findViewById(numberViewIdentifier);
    }   
     
    private final ImageView getOverlayView(final int doorNumber) {
        final int overlayViewIdentifier = getIdentifier(PREFIX_OVERLAY_VIEW + doorNumber, RESOURCE_TYPE_ID);
        return (ImageView) findViewById(overlayViewIdentifier);
    }     
     
    private ImageView getImageView(final int doorNumber) {
        final int imageViewIdentifier = getIdentifier(PREFIX_IMAGE_VIEW + doorNumber, RESOURCE_TYPE_ID);
        return (ImageView) findViewById(imageViewIdentifier);
    }

    /**
     * @param view the number view or image view of an individual door
     * @return the number of the respective door (1-based)
     */
    private int getDoorNumber(final View view) {
        final String contentDescription = view.getContentDescription().toString();
        return Integer.valueOf(contentDescription);
    }
    
    private int getIdentifier(final String name, final String resourceType) {
        return getResources().getIdentifier(name, resourceType, getPackageName());
    }

    /* - - - - MediaPlayer - - - - - */
    
    private void playSound(final int doorNumber) {
         int soundIdentifier = getIdentifier(PREFIX_SOUND_FILE + doorNumber, RESOURCE_TYPE_RAW);

         if (soundIdentifier == NO_RESOURCE) {
             /* Falling back to default sound file */
             soundIdentifier = getIdentifier(PREFIX_SOUND_FILE , RESOURCE_TYPE_RAW);
         }
         
         if (soundIdentifier != NO_RESOURCE) {
             releaseMediaPlayer();
        
             mediaPlayer = MediaPlayer.create(this, soundIdentifier);
             mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                 @Override
                 public void onCompletion(MediaPlayer mp) {
                     releaseMediaPlayer();
                 }
             });
             mediaPlayer.start(); 
         }
    }
    
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
