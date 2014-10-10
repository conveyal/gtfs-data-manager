package models;

import java.util.List;
import java.util.UUID;

/**
 * The base class for all of the models used by GTFS Data Manager.
 * We don't use the Play model object because we're not saving things to a relational database.
 * @author mattwigway
 */
public abstract class Model {
    public Model (User user) {
        this(user, null);
    }
    
    public Model (User user, List<Note> notes) {
        this.userId = user.id;
        this.notes = notes;
        this.id = UUID.randomUUID().toString();
    }
    
    protected String id;
    
    /**
     * The ID of the user who owns this object.
     * For accountability, every object is owned by a user.
     */
    protected String userId;
    
    /**
     * Notes on this object
     */
    public List<Note> notes;
    
    /**
     * Get the user who owns this object.
     * @return the User object
     */
    public User getUser () {
        return User.get(userId);
    }
}
