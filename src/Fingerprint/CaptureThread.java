/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Fingerprint;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Reader.ReaderStatus;
import com.digitalpersona.uareu.UareUException;
import java.awt.event.ActionListener;
import javafx.event.ActionEvent;

import javafx.scene.image.ImageView;
import javafx.stage.Stage;


/**
 *
 * @author admin
 */
public class CaptureThread extends Thread{
    private Reader reader;
    private ImageView imageview;
    private CaptureEvent lastCapture;
//    public Stage stage;
//    public boolean isRunning = true;
    
    public CaptureThread(Reader reader, ImageView imageview){
        this.imageview = imageview;
        this.reader = reader;

    }
    
    public void startCapture(Reader reader, ImageView imageview) {
        int counter = 0;
        try {
            //reader.Open(Reader.Priority.COOPERATIVE);
            String readerStatus = reader.GetStatus()+"";
                //while (!(readerStatus.equals("FAILURE"))) {
                    System.out.println(counter); counter++;

                    System.out.println("Reader Status: " + reader.GetStatus());
                    Reader.CaptureResult captureResult = reader.Capture(Fid.Format.ISO_19794_4_2005, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500, -1);
                    lastCapture = new CaptureEvent(captureResult, reader.GetStatus());
                    System.out.println("Capture quality: " + captureResult.quality);
                     readerStatus = reader.GetStatus()+"";

                    //Store sigle fingerprint view
                    Fid fid = captureResult.image;       
                    Fid.Fiv view = fid.getViews()[0];
                    
                    //Display fingerprint image on imageview
                    Display.displayFingerprint(view, imageview);
                //}
                //System.out.println("Reader timed out");
                
            } catch (UareUException ex) {
                ex.printStackTrace();
        }
    }
    
    public void startStream(Reader reader, ImageView imageview) {
        int counter = 0;
        try {
            reader.Open(Reader.Priority.COOPERATIVE);
            reader.StartStreaming();
            
                while (true) {
                    System.out.println(counter); counter++;

                    System.out.println("Reader Status: " + reader.GetStatus());
                    Reader.CaptureResult captureResult = reader.GetStreamImage(Fid.Format.ISO_19794_4_2005, Reader.ImageProcessing.IMG_PROC_DEFAULT, 500);
                    System.out.println("Capture quality: " + captureResult.quality);
                   
                    
                    //Store sigle fingerprint view
                    Fid fid = captureResult.image;       
                    Fid.Fiv view = fid.getViews()[0];
                    
                    //Display fingerprint image on imageview
                    Display.displayFingerprint(view, imageview);
                }
            } catch (UareUException ex) {
                ex.printStackTrace();
        }
    }

    public class CaptureEvent{
        private static final long serialVersionUID = 101;

        public Reader.CaptureResult captureResult;
        public Reader.Status        readerStatus;
        public UareUException       exception;

        public CaptureEvent(Reader.CaptureResult captureResult, Reader.Status raderStatus){
                this.captureResult = captureResult;
                this.readerStatus = readerStatus;
        }
        
        
    }
    
    public CaptureEvent getLastCapture(){
        return lastCapture;
    }
    
//    public void stopCapture() {
//        isRunning = false;
//
//        try {
//            System.out.println("Capture Stopped");
//            reader.Close();
//        } catch (UareUException ex) {
//            ex.printStackTrace();
//        }
//    }

    @Override
    public void run(){
        startCapture(reader,imageview);
    }
}
