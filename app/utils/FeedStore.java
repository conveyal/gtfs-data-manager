package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Store a feed on the file system
 * @author mattwigway
 *
 */
public class FeedStore {
    private File path;
    
    public FeedStore(File path) {
        if (!path.exists() || !path.isDirectory()) {
            throw new IllegalArgumentException("Not a directory or not found: " + path.getAbsolutePath());
        }
        
        this.path = path;
    }
    
    public FeedStore(String path) {
        this(new File(path));
    }

    public List<String> getAllFeeds () {
        ArrayList<String> ret = new ArrayList<String>();
        for (File file : path.listFiles()) {
            ret.add(file.getName());
        }
        return ret;
    }
    
    /**
     * Get the feed with the given ID.
     */
    public File getFeed (String id) {
        File feed = new File(path, id);
        if (!feed.exists()) return null;
        else return feed;
    }
    
    /**
     * Create a new feed with the given ID.
     */
    public File newFeed (String id) {
        return new File(path, id);
    }
}
