package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The base class for all of the models used by GTFS Data Manager.
 * We don't use the Play model object because we're not saving things to a relational database.
 * @author mattwigway
 */
@MappedSuperclass
public abstract class Model {
 
    public Model () {
        this.id = UUID.randomUUID().toString();
    }
        
    public String id;
    
    /**
     * The ID of the user who owns this object.
     * For accountability, every object is owned by a user.
     */
    @JsonIgnore
    public String userId;
    
    /**
     * Notes on this object
     */
    @JsonIgnore
    public List<String> noteIds;
    
    /**
     * Get the notes for this object    
     */
    // notes are handled through a separate controller
    @JsonIgnore
    public List<Note> getNotes() {
        ArrayList<Note> ret = new ArrayList<Note>(noteIds != null ? noteIds.size() : 0);

        if (noteIds != null) {
            for (String id : noteIds) {
                ret.add(Note.get(id));
            }
        }
        
        // even if there were no notes, return an empty list
        return ret;
    }
    
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
        userId = user.id != null ? user.id : User.getUserId(user.username);
    }

    public void addNote(Note n) {
        if (noteIds == null) {
            noteIds = new ArrayList<String>();
        }
        
        noteIds.add(n.id);
        n.objectId = this.id;
    }

    public abstract void save();
}
