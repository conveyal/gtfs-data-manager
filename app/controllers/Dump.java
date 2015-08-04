package controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import controllers.api.JsonManager;
import models.*;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.io.IOException;
import java.util.Collection;

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
        public Collection<Deployment> deployments;
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
        db.deployments = Deployment.getAll();
        
        return ok(json.write(db)).as("application/json");
    }
    
    // this is not authenticated, because it has to happen with a bare database (i.e. no users)
    // this method in particular is coded to allow up to 500MB of data to be posted
    @BodyParser.Of(value=BodyParser.Json.class, maxLength = 500 * 1024 * 1024)
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
        
        for (Deployment d : db.deployments) {
            d.save(false);
        }
        Deployment.commit();
        
        return ok("done");
    }
    
}
