package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import utils.DataStore;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * A note about a particular model.
 * @author mattwigway
 *
 */
@JsonInclude(Include.ALWAYS)
public class Note extends Model implements Serializable {
    private static final long serialVersionUID = 1L;

    private static DataStore<Note> noteStore = new DataStore<Note>("notes");
    
    /** The content of the note */
    public String note;
    
    /** What type of object it is recorded on */
    public NoteType type;
    
    /** What is the ID of the object it is recorded on */
    public String objectId;

    public String userEmail;

    /** When was this comment made? */
    public Date date;
    
    public void save () {
        save(true);
    }
    
    public void save (boolean commit) {
        if (commit)
            noteStore.save(id, this);
        else
            noteStore.saveWithoutCommit(id, this);
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

    public static Collection<Note> getAll() {
        return noteStore.getAll();
    }

    public static void commit() {
        noteStore.commit();
    }
}
