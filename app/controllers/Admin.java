package controllers;

import models.User;
import play.mvc.Http.Context;

/**
 * Check if a user is both logged in and an admin.
 * @author mattwigway
 *
 */
public class Admin extends Secured {
    @Override
    public String getUsername(Context ctx) {
        User u = User.getUserByUsername(ctx.session().get("username"));
        if (u != null && Boolean.TRUE.equals(u.active) && Boolean.TRUE.equals(u.admin)) {
            return u.username;
        }
        else {
            return null;
        }
    }
}
