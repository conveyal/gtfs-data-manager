package models;

/**
 * A feed source for a feed that can be automatically fetched at will.
 * @author mattwigway
 *
 */
public abstract class FetchableFeedSource extends FeedSource {
    /**
     * Fetch and save the latest version of this feed.
     */
    public abstract void fetch ();
}
