package controllers;

import models.User;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Context;

/**
 * Check if a user is authenticated, and 401 if not
 * @author mattwigway
 *
 */
public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Context ctx) {
        User u = User.getUserByUsername(ctx.session().get("username"));
        if (u != null && u.active) {
            return u.username;
        }
        else {
            return null;
        }
    }
}
