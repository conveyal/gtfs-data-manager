package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The base class for all of the models used by GTFS Data Manager.
 * We don't use the Play model object because we're not saving things to a relational database.
 * @author mattwigway
 */
public abstract class Model {
 
    public Model () {
        this.id = UUID.randomUUID().toString();
    }
    
    protected String id;
    
    /**
     * IDs can't be set by any class, but they are visible to any class.
     */
    public String getId () {
        return id;
    }
    
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
        return User.getUser(userId);
    }
    
    /**
     * Set the owner of this object
     */
    public void setUser (User user) {
        userId = user.username;
    }
}
