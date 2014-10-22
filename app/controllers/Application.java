package controllers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jobs.FetchGtfsJob;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.api.UserController;
import models.User;
import play.*;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.*;
import scala.concurrent.duration.Duration;
import views.html.*;

public class Application extends Controller {
    private static ObjectMapper json = new ObjectMapper();
    public static String dataPath = Play.application().configuration().getString("application.data.mapdb");
    public static String secret = Play.application().configuration().getString("application.secret");
    final static jsmessages.JsMessages messages = jsmessages.JsMessages.create(play.Play.application());

    public static Result authenticate () throws JsonProcessingException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        // username+password-based auth
        if (params.containsKey("username") && params.containsKey("password")) {

            // check if the user successfully authenticated
            User u = User.getUserByUsername(params.get("username")[0]);
            if (u != null && u.checkPassword(params.get("password")[0])) {
                session("username", u.username);            
                return getLoggedInUser();
            }
            else {
                return unauthorized();
            }
        }
        // userid+key-based auth
        else if (params.containsKey("userId") && params.containsKey("key")) {
            User u = User.getUser(params.get("userId")[0]);
            
            if (u.checkKey(params.get("key")[0])) {
                session("username", u.username);
                return getLoggedInUser();
            }
            else {
                return unauthorized();
            }
        }
        else {
            return unauthorized();
        }
    }
    
    public static Result logout () {
        session().clear();
        ObjectNode result = Json.newObject();
        result.put("status", "logged_out");
        return ok(result);
    }
    
    // used by the web app to see who is logged in
    public static Result getLoggedInUser () throws JsonProcessingException {
        if (session("username") != null) {
            return ok(UserController.getJsonManager().write(User.getUserByUsername(session("username"))))
                    .as("application/json");
        }
        else {
            return unauthorized();
        }
    }
    
    public static Result createInitialUser () throws JsonParseException, JsonMappingException, IOException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        if (User.usersExist())
            return unauthorized();

        User u = new User(params.get("username")[0], params.get("password")[0], params.get("email")[0]);
        
        if (params.containsKey("admin"))
            u.admin = "true".equals(params.get("admin")[0]);
        
        u.save();
        
        return ok("user created");
    }
    
    public static Result jsMessages() {
        return ok(messages.generate("window.Messages"));
    }
    
    // kick off a gtfs update
    @Security.Authenticated(Admin.class)
    public static Result fetchGtfs () {
        User u = User.getUserByUsername(session("username"));
        if (!Boolean.TRUE.equals(u.admin))
            return unauthorized("Only admins may trigger GTFS refetch");
        
        Akka.system().scheduler().scheduleOnce(
                Duration.create(50, TimeUnit.MILLISECONDS),
                new FetchGtfsJob(),
                Akka.system().dispatcher()
                );
        
        return ok("Running");
    }
    // all of the hard stuff is in controllers.api
}
