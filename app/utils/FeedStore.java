package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import play.Play;

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

/**
 * Store a feed on the file system
 * @author mattwigway
 *
 */
public class FeedStore {
    private File path;
    private String s3Bucket;
    public FeedStore() {
        // s3 storage
        if (Boolean.valueOf(Play.application().configuration().getString("application.work_offline"))){
            this.s3Bucket = Play.application().configuration().getString("application.s3.gtfs_bucket");
        }
        // local storage
        else {
            String pathString = Play.application().configuration().getString("application.data.gtfs");
            File path = new File(pathString);
            if (!path.exists() || !path.isDirectory()) {
                throw new IllegalArgumentException("Not a directory or not found: " + path.getAbsolutePath());
            }
            this.path = path;
        }

    }

    public List<String> getAllFeeds () {
        ArrayList<String> ret = new ArrayList<String>();

        // local storage
        if (path != null) {
            for (File file : path.listFiles()) {
                ret.add(file.getName());
            }
        }
        // s3 storage
        else {

        }
        return ret;
    }
    
    /**
     * Get the feed with the given ID.
     */
    public File getFeed (String id) {
        // local storage
        if (path != null) {
            File feed = new File(path, id);
            if (!feed.exists()) return null;
            // don't let folks get feeds outside of the directory
            if (!feed.getParentFile().equals(path)) return null;
            else return feed;
        }
        // s3 storage
        else {
            return null;
        }

    }
    
    /**
     * Create a new feed with the given ID.
     */
    public File newFeed (String id) {
        // local storage
        if (path != null) {
            return new File(path, id);
        }
        // s3 storage
        else {
            return null;
        }
    }
}
