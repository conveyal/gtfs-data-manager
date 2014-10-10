package models;

import java.util.Date;

/** 
 * A feed fetchable over HTTP, from a static URL
 * (i.e. the way feeds are provided to Google)
 */
public class HttpFeedSource extends FetchableFeedSource {
    public String url;
    
    /**
     * Fetch frequency for this feed, in minutes.
     */
    public int fetchFrequency;
    
    /**
     * When this feed was last fetched (whether updated or not)
     */
    public Date lastFetched;
    
    /**
     * When this feed was last updated.
     */
    public Date lastUpdated;

    public void fetch() {
        // TODO: send an HTTP request 
    }

}
