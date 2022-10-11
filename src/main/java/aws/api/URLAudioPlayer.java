// Author: OrkhanGG
// Created: 10/10/2022
// Purpose: To play the audio with a given link






///////////////////////////////////////////////////////////////
/////////////////////     ////////////////////////////////////
////////////////////     ////////////////////////////////////
///////////////////     ////////////////////////////////////
//////////////////     ////////////////////////////////////
/////////////////     ////////////////////////////////////
////////////////     ////////////////////////////////////
///////////////     ////////////////////////////////////
///////////////////////////////////////////////////////
/////////////     ////////////////////////////////////
////////////     ////////////////////////////////////
////////////////////////////////////////////////////

// THIS API IS NOT READY AND MUST NOT BE USED UNDER ANY SITUATION!

package aws.api;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
public class URLAudioPlayer {
    public void Play(String _URL) {
        URL url = null;
        try {
            url = new URL(
                    "https://api.dictionaryapi.dev/media/pronunciations/en/hello-uk.mp3");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        // getAudioInputStream() also accepts a File or InputStream
        AudioInputStream ais = null;
        try {
            ais = AudioSystem.
                    getAudioInputStream( url );
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            clip.open(ais);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // A GUI element to prevent the Clip's daemon Thread
                // from terminating at the end of the main()
                JOptionPane.showMessageDialog(null, "Close to exit!");
            }
        });
    }
}
