package controllers;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.User;
import play.*;
import play.libs.Json;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {
    private static ObjectMapper json = new ObjectMapper();
    public static String dataPath = Play.application().configuration().getString("application.data");
    public static String secret = Play.application().configuration().getString("application.secret");
    final static jsmessages.JsMessages messages = jsmessages.JsMessages.create(play.Play.application());

    public static Result authenticate () throws JsonProcessingException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        if (!params.containsKey("username") || !params.containsKey("password"))
            // TODO: not actually ok
            return unauthorized();
        
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
    
    public static Result logout () {
        session().clear();
        return ok();
    }
    
    // used by the web app to see who is logged in
    public static Result getLoggedInUser () throws JsonProcessingException {
        if (session("username") != null) {
            ObjectNode result = Json.newObject();
            result.put("username", session("username"));
            return ok(result);
        }
        else {
            return unauthorized();
        }
    }
    
    private static class LoginStatus {
        public String username;

        public LoginStatus(String username) {
            this.username = username;
        }
    }
    
    public static Result jsMessages() {
        return ok(messages.generate("window.Messages"));
    }
    
    // all of the hard stuff is in controllers.api
}
