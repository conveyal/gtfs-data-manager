package models;

/**
 * Represents a collection of feed sources that can be made into a deployment.
 * Generally, this would represent one agency that is managing the data.
 * For now, there is one FeedCollection per deployment, but we're trying to write the code in such a way that this
 * is not necessary.
 * 
 * @author mattwigway
 *
 */
public class FeedCollection extends Model {
    /** The name of this feed collection, e.g. NYSDOT. */
    public String name;
}
