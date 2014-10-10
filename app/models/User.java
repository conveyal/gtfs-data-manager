package models;

import java.io.Serializable;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

import play.Play;

/**
 * Represents a user of the GTFS Data Manager.
 * @author mattwigway
 *
 */
public class User extends Model implements Serializable {    
    private static final long serialVersionUID = 1L;

    private static DataStore<User> userStore = new DataStore<User>("users");
    
    /**
     * This user's login name
     */
    public String username;
    
    /**
     * The user's real/display name
     */
    public String displayName;
    
    private String hashedPassword;
    
    /**
     * The method used to hash this password. We store this with the user to allow
     * password hash upgrades easily; since hashes are (intended to be) one-way, there
     * is no way to upgrade the hash method en masse, so when upgrades are needed we upgrade as
     * users log in.
     */
    private String hashMethod;
    
    /**
     * Is this user an admin user?
     */
    public boolean admin;
    
    /**
     * What is this user's email address?
     */
    public String email;
    
    public User (String username, String password, String displayName, String email, boolean admin) {
        // make sure username is unique
        if (userStore.hasId(username)) {
            // don't make a new user
            throw new IllegalArgumentException("User with username " + username + " already exists.");
        }
        
        this.username = username;
        this.id = username;
        // TODO: make configurable
        this.hashMethod = "SHA-256";
        this.hashedPassword = hash(password);
        this.displayName = displayName;
        this.email = email;
        this.admin = admin;
        // users are not owned by other users
        this.userId = null;
        // no call to super because Users don't have generated IDs
    }
    
    private String hash(String password) {
        try
        {
            // TODO: include application secret here
            byte[] bytes = (password.trim()).getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance(this.hashMethod);
            byte[] digest = md.digest(bytes);

            String hexString = new String(Hex.encodeHex(digest));

            return hexString;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static User get (String id) {
        return userStore.getById(id);
    }
}
