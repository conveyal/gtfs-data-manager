package models;

import java.util.Date;

import utils.DataStore;

/**
 * A note about a particular model.
 * @author mattwigway
 *
 */
public class Note extends Model {
    private static DataStore<Note> noteStore = new DataStore<Note>("notes");
    
    /** The content of the note */
    public String note;
    
    /** What type of object it is recorded on */
    public NoteType type;
    
    /** What is the ID of the object it is recorded on */
    public String objectId;
    
    /** When was this comment made? */
    public Date date;
    
    public void save () {
        noteStore.save(id, this);
    }
    
    public static Note get (String id) {
        return noteStore.getById(id);
    }
    
    /**
     * The types of object that can have notes recorded on them.
     */
    public static enum NoteType {
        FEED_VERSION, FEED_SOURCE;
    }
}
