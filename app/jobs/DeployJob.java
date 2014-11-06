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

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.api.JsonManager;
import play.Logger;
import play.mvc.WebSocket;
import models.Deployment;
import models.JsonViews;

/**
 * Deploy the given deployment to the OTP servers specified by targets.
 * @author mattwigway
 *
 */
public class DeployJob implements Runnable {
    private static JsonManager<DeployStatus> json = 
            new JsonManager<DeployStatus>(DeployStatus.class, JsonViews.UserInterface.class);
    
    /** The URLs to deploy to */
    private List<String> targets;
    
    /** The number of servers that have successfully been deployed to */
    private DeployStatus status;
    
    /** The deployment to deploy */
    private Deployment deployment;
    
    private WebSocket.Out<String> out;
    
    public DeployJob (Deployment deployment, List<String> targets) {
        this(deployment, targets, null);
    }
    
    public DeployJob (Deployment deployment, List<String> targets, WebSocket.Out<String> out) {
        this.deployment = deployment;
        this.targets = targets;
        this.out = out;
        this.status = new DeployStatus();
        status.error = false;
        status.completed = false;
        status.numServersCompleted = 0;
        status.totalServers = targets.size();
        status.built = false;
    }

    public void run() {
        // kick things off
        send();
        
        // create a temporary file in which to save the deployment
        File temp;
        try {
            temp = File.createTempFile("deployment", ".zip");
        } catch (IOException e) {
            Logger.error("Could not create temp file");
            e.printStackTrace();
            
            status.error = true;
            status.completed = true;
            status.message = "app.deployment.error.dump";
            
            send();
            close();
            
            return;
        }
        
        // dump the deployment bundle
        try {
            this.deployment.dump(temp);
        } catch (IOException e) {
            Logger.error("Error dumping deployment");
            e.printStackTrace();
            
            status.error = true;
            status.completed = true;
            status.message = "app.deployment.error.dump";
            
            send();
            close();
            
            return;
        }
        
        status.built = true;
        send();
        
        // load it to OTP
        for (String rawUrl : this.targets) {
            URL url;
            try {
                url = new URL(rawUrl + "/routers/default");
            } catch (MalformedURLException e) {
                Logger.error("Malformed deployment URL {}", rawUrl);
                
                    status.error = true;
                    status.message = "app.deployment.error.config";
                
                send();
                
                continue;
            }
            
            // grab them synchronously, so that we only take down one OTP server at a time
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Logger.error("Unable to open URL of OTP server {}", url);
                
                    status.error = true;
                    status.message = "app.deployment.error.net";
                
                    send();
                
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
                
                    status.error = true;
                    status.message = "app.deployment.error.net";
                
                    send();
                
                continue;
            }
            
            // get the input file
            FileChannel input;
            try {
                input = new FileInputStream(temp).getChannel();
            } catch (FileNotFoundException e) {
                Logger.error("Internal error: could not read dumped deployment!");
                
                    status.error = true;
                    status.message = "app.deployment.error.dump";
                
                    send();
                
                continue;
            }
            
            try {
                conn.connect();
            } catch (IOException e) {
                Logger.error("Unable to open connection to OTP server {}", url);
                
                    status.error = true;
                    status.message = "app.deployment.error.net";
                
                    send();
                
                continue;
            }
            
            // copy
            try {
                input.transferTo(0, Long.MAX_VALUE, post);
            } catch (IOException e) {
                Logger.error("Unable to transfer deployment to server {}" , url);
                e.printStackTrace();
                
                    status.error = true;
                    status.message = "app.deployment.error.net";
                
                    send();
                
                continue;
            }
            
            try {
                post.close();
            } catch (IOException e) {
                Logger.error("Error finishing connection to server {}", url);
                e.printStackTrace();
                
                    status.error = true;
                    status.message = "app.deployment.error.net";
                
                    send();
                
                continue;
            }
            
            try {
                input.close();
            } catch (IOException e) {
                // do nothing
            }
            
            Logger.info("Done deploying to {}", url);
            
                status.numServersCompleted++;
            
                send();
        }
        
            status.completed = true;
        
            send();
            close();
    }
    
    /**
     * Send the status down the wire
     */
    private void send() {
        if (out != null) {        
        try {
            out.write(json.write(status));
        } catch (JsonProcessingException e) {
            // this had better not happen
            close();
            throw new RuntimeException(e);
        }
        }
    }
    
    /**
     * Close the websocket.
     */
    private void close() {
        if (out != null)
            out.close();
    }

    /**
     * Represents the current status of this job.
     */
    public static class DeployStatus implements Cloneable {
        public String message;
        public boolean completed;
        public boolean error;
        public boolean built;
        public int numServersCompleted;
        public int totalServers;
        
        public DeployStatus clone () {
            return (DeployStatus) this.clone();
        }
    }
}
