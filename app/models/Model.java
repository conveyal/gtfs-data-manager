package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import utils.Auth0UserProfile;

import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The base class for all of the models used by GTFS Data Manager.
 * We don't use the Play model object because we're not saving things to a relational database.
 * @author mattwigway
 */
@MappedSuperclass
public abstract class Model {
    private static final long serialVersionUID = 1L;

    public Model () {
        // This autogenerates an ID
        // this is OK for dump/restore, because the ID will simply be overridden
        this.id = UUID.randomUUID().toString();
    }
        
    public String id;
    
    /**
     * The ID of the user who owns this object.
     * For accountability, every object is owned by a user.
     */
    @JsonView(JsonViews.DataDump.class)
    public String userId;
    
    /**
     * Notes on this object
     */
    @JsonView(JsonViews.DataDump.class)
    public List<String> noteIds;
    
    /**
     * Get the notes for this object    
     */
    // notes are handled through a separate controller and in a separate DB
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
    @JsonView(JsonViews.UserInterface.class)
    public User getUser () {
        return null;//User.getUser(userId);
    }
    
    /**
     * Set the owner of this object
     */
    public void setUser (Auth0UserProfile profile) {
        userId = profile.getUser_id();
    }

    public void addNote(Note n) {
        if (noteIds == null) {
            noteIds = new ArrayList<String>();
        }
        
        noteIds.add(n.id);
        n.objectId = this.id;
    }

    public abstract void save();

    //public transient List<Note> notes;
}
