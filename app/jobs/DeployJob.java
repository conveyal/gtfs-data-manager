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
    
    /** The number of servers that have successfully been deployed to */
    private DeployStatus status;
    
    /** The deployment to deploy */
    private Deployment deployment;
    
    public DeployJob(Deployment deployment, List<String> targets) {
        this.deployment = deployment;
        this.targets = targets;
        this.status = new DeployStatus();
        status.error = false;
        status.completed = false;
        status.built = false;
        status.numServersCompleted = 0;
        status.totalServers = targets.size();
    }
    
    public DeployStatus getStatus () {
        synchronized (status) {
            return status.clone();
        } 
    }

    public void run() {
        // create a temporary file in which to save the deployment
        File temp;
        try {
            temp = File.createTempFile("deployment", ".zip");
        } catch (IOException e) {
            Logger.error("Could not create temp file");
            e.printStackTrace();
            
            synchronized (status) {
                status.error = true;
                status.completed = true;
                status.message = "app.deployment.error.dump";
            }
            
            return;
        }
        
        // dump the deployment bundle
        try {
            this.deployment.dump(temp);
        } catch (IOException e) {
            Logger.error("Error dumping deployment");
            e.printStackTrace();
            
            synchronized (status) {
                status.error = true;
                status.completed = true;
                status.message = "app.deployment.error.dump";
            }
            
            return;
        }
        
        synchronized (status) {
            status.built = true;
        }
        
        // figure out what router we're using
        String router = deployment.routerId != null ? deployment.routerId : "default"; 
        
        // load it to OTP
        for (String rawUrl : this.targets) {
            synchronized (status) {
                status.uploading = true;
            }
            
            URL url;
            try {
                url = new URL(rawUrl + "/routers/" + router);
            } catch (MalformedURLException e) {
                Logger.error("Malformed deployment URL {}", rawUrl);
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.config";
                }
                
                continue;
            }
            
            // grab them synchronously, so that we only take down one OTP server at a time
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Logger.error("Unable to open URL of OTP server {}", url);
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.net";
                }
                
                continue;
            }
            
            conn.addRequestProperty("Content-Type", "application/zip");
            conn.setDoOutput(true);
            // graph build can take a long time but not more than an hour, I should think
            conn.setConnectTimeout(60 * 60 * 1000);
            conn.setFixedLengthStreamingMode(temp.length());
            
            // this makes it a post request so that we can upload our file
            WritableByteChannel post;
            try {
                post = Channels.newChannel(conn.getOutputStream());
            } catch (IOException e) {
                Logger.error("Could not open channel to OTP server {}", url);
                e.printStackTrace();
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.net";
                    status.completed = true;
                }
                
                return;
            }
            
            // get the input file
            FileChannel input;
            try {
                input = new FileInputStream(temp).getChannel();
            } catch (FileNotFoundException e) {
                Logger.error("Internal error: could not read dumped deployment!");
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.dump";
                    status.completed = true;
                }
                
                return;
            }
            
            try {
                conn.connect();
            } catch (IOException e) {
                Logger.error("Unable to open connection to OTP server {}", url);
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.net";
                    status.completed = true;
                }
                
                return;
            }
            
            // copy
            try {
                input.transferTo(0, Long.MAX_VALUE, post);
            } catch (IOException e) {
                Logger.error("Unable to transfer deployment to server {}" , url);
                e.printStackTrace();
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.net";
                    status.completed = true;
                }
                
                return;
            }
            
            try {
                post.close();
            } catch (IOException e) {
                Logger.error("Error finishing connection to server {}", url);
                e.printStackTrace();
                
                synchronized (status) {
                    status.error = true;
                    status.message = "app.deployment.error.net";
                    status.completed = true;
                }
                
                return;
            }
            
            try {
                input.close();
            } catch (IOException e) {
                // do nothing
            }
            
            synchronized (status) {
                status.uploading = false;
            }
            
            // wait for the server to build the graph
            // TODO: timeouts?
            try {
                if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    Logger.error("Got response code {} from server", conn.getResponseCode());
                    synchronized (status) {
                        status.error = true;
                        status.message = "app.deployment.error.graph_build_failed";
                        status.completed = true;
                    }
                    
                    // no reason to take out more servers, it's going to have the same result
                    return;
                }
            } catch (IOException e) {
                Logger.error("Could not finish request to server {}", url);
                
                synchronized (status) {
                    status.completed = true;
                    status.error = true;
                    status.message = "app.deployment.error.net";
                }
            }
            
            synchronized (status) {
                status.numServersCompleted++;
            }
        }
        
        synchronized (status) {
            status.completed = true;
        }
    }
    
    /**
     * Represents the current status of this job.
     */
    public static class DeployStatus implements Cloneable {
        /** What error message (defined in messages.<lang>) should be displayed to the user? */
        public String message;
        
        /** Is this deployment completed (successfully or unsuccessfully) */
        public boolean completed;
        
        /** Was there an error? */
        public boolean error;
        
        /** Did the manager build the bundle successfully */
        public boolean built;
        
        /** Is the bundle currently being uploaded to the server? */
        public boolean uploading;
        
        /** To how many servers have we successfully deployed thus far? */
        public int numServersCompleted;
        
        /** How many servers are we attempting to deploy to? */
        public int totalServers;
        
        public DeployStatus clone () {
            DeployStatus ret = new DeployStatus();
            ret.message = message;
            ret.completed = completed;
            ret.error = error;
            ret.built = built;
            ret.uploading = uploading;
            ret.numServersCompleted = numServersCompleted;
            ret.totalServers = totalServers;
            return ret;
        }
    }
}
