package jobs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import models.Deployment;
import play.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

/**
 * Deploy the given deployment to the OTP servers specified by targets.
 * @author mattwigway
 *
 */
public class DeployJob implements Runnable {
    /** The URLs to deploy to */
    private List<String> targets;
    
    /** The base URL to otp.js on these targets */
    private String publicUrl;

    /** An optional AWS S3 bucket to copy the bundle to */
    private String s3Bucket;

    /** An AWS credentials file to use when uploading to S3 */
    private String s3CredentialsFilename;

    /** The number of servers that have successfully been deployed to */
    private DeployStatus status;
    
    /** The deployment to deploy */
    private Deployment deployment;
    
    public DeployJob(Deployment deployment, List<String> targets, String publicUrl, String s3Bucket, String s3CredentialsFilename) {
        this.deployment = deployment;
        this.targets = targets;
        this.publicUrl = publicUrl;
        this.s3Bucket = s3Bucket;
        this.s3CredentialsFilename = s3CredentialsFilename;
        this.status = new DeployStatus();
        status.error = false;
        status.completed = false;
        status.built = false;
        status.numServersCompleted = 0;
        status.totalServers = targets == null ? 0 : targets.size();
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

        Logger.info("Created deployment bundle file: " + temp.getAbsolutePath());
        
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

        // upload to S3, if applicable
        if(this.s3Bucket != null) {
            synchronized (status) {
                status.uploadingS3 = true;
            }


            try {
                AWSCredentials creds;
                if (this.s3CredentialsFilename != null) {
                    creds = new ProfileCredentialsProvider(this.s3CredentialsFilename, "default").getCredentials();
                }
                else {
                    // default credentials providers, e.g. IAM role
                    creds = new DefaultAWSCredentialsProviderChain().getCredentials();
                }

                TransferManager tx = new TransferManager(creds);
                String key = deployment.name + ".zip";
                final Upload upload = tx.upload(this.s3Bucket, key, temp);

                upload.addProgressListener(new ProgressListener() {
                    public void progressChanged(ProgressEvent progressEvent) {
                        synchronized (status) {
                            status.percentUploaded = upload.getProgress().getPercentTransferred();
                        }
                    }
                });

                upload.waitForCompletion();
                tx.shutdownNow();

                // copy to [name]-latest.zip
                String copyKey = deployment.getFeedCollection().name.toLowerCase() + "-latest.zip";
                AmazonS3 s3client = new AmazonS3Client(creds);
                CopyObjectRequest copyObjRequest = new CopyObjectRequest(
                    this.s3Bucket, key, this.s3Bucket, copyKey);
                s3client.copyObject(copyObjRequest);

            } catch (AmazonClientException|InterruptedException e) {
                Logger.error("Error uploading deployment bundle to S3");
                e.printStackTrace();

                synchronized (status) {
                    status.error = true;
                    status.completed = true;
                    status.message = "app.deployment.error.dump";
                }

                return;
            }

            synchronized (status) {
                status.uploadingS3 = false;
            }
        }

        // if no OTP targets (i.e. we're only deploying to S3), we're done
        if(this.targets == null) {
            synchronized (status) {
                status.completed = true;
            }

            return;
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
            status.baseUrl = this.publicUrl;
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

        /** Is the bundle currently being uploaded to an S3 bucket? */
        public boolean uploadingS3;

        /** How much of the bundle has been uploaded? */
        public double percentUploaded;

        /** To how many servers have we successfully deployed thus far? */
        public int numServersCompleted;
        
        /** How many servers are we attempting to deploy to? */
        public int totalServers;
        
        /** Where can the user see the result? */
        public String baseUrl;
        
        public DeployStatus clone () {
            DeployStatus ret = new DeployStatus();
            ret.message = message;
            ret.completed = completed;
            ret.error = error;
            ret.built = built;
            ret.uploading = uploading;
            ret.uploadingS3 = uploadingS3;
            ret.percentUploaded = percentUploaded;
            ret.numServersCompleted = numServersCompleted;
            ret.totalServers = totalServers;
            ret.baseUrl = baseUrl;
            return ret;
        }
    }
}
