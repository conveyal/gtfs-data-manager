package controllers;

import com.auth0.jwt.JWTVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import play.Play;
import play.mvc.Controller;
import utils.Auth0UserProfile;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by demory on 1/17/16.
 */

public class Auth0SecuredController extends Controller {

    protected static Auth0UserProfile verifyUser() {
        String token = getToken();
        if(token == null) return null;
        return verifyUser(token);
    }

    protected static Auth0UserProfile verifyUser(String token) {
        String clientID = Play.application().configuration().getString("application.auth0.client_id");
        String clientSecret = Play.application().configuration().getString("application.auth0.client_secret");
        JWTVerifier jwtVerifier = new JWTVerifier(
                new Base64(true).decode(clientSecret), clientID);

        try {
            Map<String, Object> decoded = jwtVerifier.verify(token);

            String userInfo = getUserInfo(token);

            ObjectMapper m = new ObjectMapper();
            session().put("profile", userInfo);
            return m.readValue(userInfo, Auth0UserProfile.class);

        } catch (Exception e) {
            System.out.println("error validating token");
            e.printStackTrace();
        }

        return null;
    }

    protected static Auth0UserProfile getSessionProfile() {
        try {
            String profile = session("profile");
            ObjectMapper m = new ObjectMapper();
            return m.readValue(profile, Auth0UserProfile.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected static String getToken() {
        String token = null;
        final String authorizationHeader = request().getHeader("authorization");
        if (authorizationHeader == null) return null;

        // check format (Authorization: Bearer [token])
        String[] parts = authorizationHeader.split(" ");
        if (parts.length != 2) return null;

        String scheme = parts[0];
        String credentials = parts[1];

        Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(scheme).matches()) {
            token = credentials;
        }
        return token;
    }

    protected static String getUserInfo(String token) throws Exception {

        URL url = new URL("https://" + Play.application().configuration().getString("application.auth0.domain") + "/tokeninfo");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "id_token=" + token;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

}
