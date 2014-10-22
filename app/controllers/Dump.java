package controllers;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.api.JsonManager;
import models.FeedCollection;
import models.FeedSource;
import models.FeedVersion;
import models.JsonViews;
import models.Note;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Handle database dump/reload.
 * @author mattwigway
 *
 */
@Security.Authenticated(Admin.class)
public class Dump extends Controller {
    /**
     * Represents a snapshot of the database. This require loading the entire database into RAM.
     * This shouldn't be an issue, though, as the feeds are stored separately. This is only metadata.
     */
    public static class DatabaseState {
        public Collection<FeedCollection> feedCollections;
        public Collection<FeedSource> feedSources;
        public Collection<FeedVersion> feedVersions;
        public Collection<Note> notes;
        public Collection<User> users;
        // TODO enable dump restore of deployments when the time comes
        //public Collection<Deployment> deployments;
    }
    
    private static JsonManager<DatabaseState> json = new JsonManager<DatabaseState>(JsonViews.DataDump.class);
    
    public static Result dump () throws JsonProcessingException {
        DatabaseState db = new DatabaseState();
        db.feedCollections = FeedCollection.getAll();
        db.feedSources = FeedSource.getAll();
        db.feedVersions = FeedVersion.getAll();
        db.notes = Note.getAll();
        db.users = User.getAll();
        
        return ok(json.write(db)).as("application/json");
    }
    
}
