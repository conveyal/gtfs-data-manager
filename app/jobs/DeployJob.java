package jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import play.Logger;
import models.Deployment;

/**
 * Deploy the given deployment to the OTP servers specified by targets.
 * @author mattwigway
 *
 */
public class DeployJob implements Runnable {
    /** The URLs to deploy to */
    private List<String> targets;
    
    /** The deployment to deploy */
    private Deployment deployment;
    
    public DeployJob(Deployment deployment, List<String> targets) {
        this.deployment = deployment;
        this.targets = targets;
    }

    public void run() {
        // create a temporary file in which to save the deployment
        File temp;
        try {
            temp = File.createTempFile("deployment", ".zip");
        } catch (IOException e) {
            Logger.error("Could not create temp file");
            e.printStackTrace();
            return;
        }
        
        // dump the deployment bundle
        try {
            this.deployment.dump(temp);
        } catch (IOException e) {
            Logger.error("Error dumping deployment");
            e.printStackTrace();
            return;
        }
        
        // load it to OTP
        for (String rawUrl : this.targets) {
            URL url;
            try {
                url = new URL(rawUrl + "/routers/default");
            } catch (MalformedURLException e) {
                Logger.error("Malformed deployment URL", rawUrl);
                continue;
            }
            
            // grab them synchronously, so that we only take down one OTP server at a time
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Logger.error("Unable to open URL of OTP server {}", url);
                continue;
            }
            
            conn.addRequestProperty("Content-Type", "application/zip");
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(temp.length());
            
            // this makes it a post request so that we can upload our file
            WritableByteChannel post;
            try {
                post = Channels.newChannel(conn.getOutputStream());
            } catch (IOException e) {
                Logger.error("Could not open channel to OTP server {}", url);
                e.printStackTrace();
                continue;
            }
            
            // get the input file
            FileChannel input;
            try {
                input = new FileInputStream(temp).getChannel();
            } catch (FileNotFoundException e) {
                Logger.error("Internal error: could not read dumped deployment!");
                continue;
            }
            
            try {
                conn.connect();
            } catch (IOException e) {
                Logger.error("Unable to open connection to OTP server {}", url);
                continue;
            }
            
            // copy
            try {
                input.transferTo(0, Long.MAX_VALUE, post);
            } catch (IOException e) {
                Logger.error("Unable to transfer deployment to server {}" , url);
                e.printStackTrace();
                continue;
            }
            
            try {
                post.close();
            } catch (IOException e) {
                Logger.error("Error finishing connection to server {}", url);
                e.printStackTrace();
                continue;
            }
            
            try {
                input.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            Logger.info("Done deploying to {}", url);
        }
    }
}
