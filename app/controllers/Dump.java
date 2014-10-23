package controllers;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import controllers.api.JsonManager;
import models.FeedCollection;
import models.FeedSource;
import models.FeedVersion;
import models.JsonViews;
import models.Note;
import models.User;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;

/**
 * Handle database dump/reload.
 * @author mattwigway
 *
 */
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
    
    private static JsonManager<DatabaseState> json =
            new JsonManager<DatabaseState>(DatabaseState.class, JsonViews.DataDump.class);
    
    @Security.Authenticated(Admin.class)
    public static Result dump () throws JsonProcessingException {
        DatabaseState db = new DatabaseState();
        db.feedCollections = FeedCollection.getAll();
        db.feedSources = FeedSource.getAll();
        db.feedVersions = FeedVersion.getAll();
        db.notes = Note.getAll();
        db.users = User.getAll();
        
        return ok(json.write(db)).as("application/json");
    }
    
    // this is not authenticated, because it has to happen with a bare database (i.e. no users)
    // however, annotated so that it can only be called when the database is empty; the annotation
    // takes effect before the bodyparser
    // this method in particular is coded to allow up to 100MB of data to be posted
    @With(FreshInstall.class)
    @BodyParser.Of(value=BodyParser.Json.class, maxLength = 100 * 1024 * 1024)
    public static Result load () throws JsonParseException, JsonMappingException, IOException {
        // TODO: really ought to check all tables
        if (User.usersExist())
            return badRequest("database not empty");

        DatabaseState db = json.read(request().body().asJson());
        
        for (FeedCollection c : db.feedCollections) {
            c.save(false);
        }
        FeedCollection.commit();
        
        for (FeedSource s : db.feedSources) {
            s.save(false);
        }
        FeedSource.commit();
        
        for (FeedVersion v : db.feedVersions) {
            v.save(false);
        }
        FeedVersion.commit();
        
        for (Note n : db.notes) {
            n.save(false);
        }
        Note.commit();
        
        for (User u : db.users) {
            u.save(false);
        }
        User.commit();
        
        return ok("done");
    }
    
}
