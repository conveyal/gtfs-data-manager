package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Auth0SecuredController;
import jobs.FetchSingleFeedJob;
import models.*;
import play.Play;
import play.api.libs.Files;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.Auth0UserProfile;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


//@Security.Authenticated(Secured.class)
public class FeedSourceController extends Auth0SecuredController {
    private static JsonManager<FeedSource> json =
            new JsonManager<FeedSource>(FeedSource.class, JsonViews.UserInterface.class);
    
    public static Result get (String id) throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedSource fs = FeedSource.get(id);

        if (userProfile.canAdministerProject(fs.feedCollectionId) || userProfile.canViewFeed(fs.feedCollectionId, fs.id))
            return ok(json.write(fs));

        else
            return unauthorized();
    }

    public static Result getAll () throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        // parse the query parameters
        String fcId = request().getQueryString("feedcollection");
        FeedCollection fc = null;
        if (fcId != null)
            fc = FeedCollection.get(fcId);
 
        Collection<FeedSource> feedSources;
        if (fc == null) {
            feedSources = FeedSource.getAll();
        }
        else {
            feedSources = fc.getFeedSources();
        }

        if(!userProfile.canAdministerProject(fcId)) {
            // filter the list, only show the ones this user has permission to access
            List<FeedSource> filtered = new ArrayList<FeedSource>();

            for (FeedSource fs : feedSources) {
                if (userProfile.canViewFeed(fs.feedCollectionId, fs.id)) {
                    filtered.add(fs);
                }
            }

            feedSources = filtered;
        }

        return ok(json.write(feedSources)).as("application/json");
    }
    
    // common code between create and update
    private static void applyJsonToFeedSource (FeedSource s, JsonNode params) throws JsonProcessingException, MalformedURLException {
        s.name = params.get("name").asText();

        if(params.has("retrievalMethod")) {
            String retMethod = params.get("retrievalMethod").asText();
            if(retMethod != "null") s.retrievalMethod = FeedSource.FeedRetrievalMethod.valueOf(params.get("retrievalMethod").asText());
        }

        if (params.has("editorId"))
            s.editorId = params.get("editorId").asText();

        if (params.has("snapshotVersion"))
            s.snapshotVersion = params.get("snapshotVersion").asText();

        if (params.has("shortName")) {
            s.shortName = params.get("shortName").asText();
        }

        if (params.has("AgencyPhone")) {
            s.AgencyPhone = params.get("AgencyPhone").asText();
        }

        if (params.has("RttAgencyName")) {
            s.RttAgencyName = params.get("RttAgencyName").asText();
        }

        if (params.has("RttEnabled")) {
            s.RttEnabled = params.get("RttEnabled").asText();
        }

        if (params.has("AgencyShortName")) {
            s.AgencyShortName = params.get("AgencyShortName").asText();
        }

        if (params.has("AgencyPublicId")) {
            s.AgencyPublicId = params.get("AgencyPublicId").asText();
        }

        if (params.has("AddressLat")) {
            s.AddressLat = params.get("AddressLat").asText();
        }

        if (params.has("AddressLon")) {
            s.AddressLon = params.get("AddressLon").asText();
        }

        if (params.has("DefaultRouteType")) {
            s.DefaultRouteType = params.get("DefaultRouteType").asText();
        }

        if (params.has("CarrierStatus")) {
            s.CarrierStatus = params.get("CarrierStatus").asText();
        }

        if (params.has("AgencyAddress")) {
            s.AgencyAddress = params.get("AgencyAddress").asText();
        }

        if (params.has("AgencyEmail")) {
            s.AgencyEmail = params.get("AgencyEmail").asText();
        }

        if (params.has("AgencyUrl")) {
            s.AgencyUrl = params.get("AgencyUrl").asText();
        }

        if (params.has("AgencyFareUrl")) {
            s.AgencyFareUrl = params.get("AgencyFareUrl").asText();
        }

        s.isPublic = params.get("isPublic").asBoolean();
        s.deployable = params.get("deployable").asBoolean();
        // the last fetched/updated cannot be updated from the web interface, only internally
        String url = params.get("url").asText();
        if (url != null && !"null".equals(url)) {
            if ("".equals(url)) {
                s.url = null;
            }
            else {
                s.url = new URL(url);
                // reset the last fetched date so it can be fetched again
                s.lastFetched = null;
            }
        }

        // sync w/ RTD
        FeedCollectionController.RtdCarrier carrier = new FeedCollectionController.RtdCarrier();
        carrier.AgencyId = checkValue(s.defaultGtfsId);
        carrier.AgencyPhone = checkValue(s.AgencyPhone);
        carrier.RttAgencyName = checkValue(s.RttAgencyName);
        carrier.RttEnabled = checkValue(s.RttEnabled);
        carrier.AgencyShortName = checkValue(s.shortName);
        carrier.AgencyPublicId = checkValue(s.AgencyPublicId);
        carrier.AddressLat = checkValue(s.AddressLat);
        carrier.AddressLon = checkValue(s.AddressLon);
        carrier.DefaultRouteType = checkValue(s.DefaultRouteType);
        carrier.CarrierStatus = checkValue(s.CarrierStatus);
        carrier.AgencyAddress = checkValue(s.AgencyAddress);
        carrier.AgencyEmail = checkValue(s.AgencyEmail);
        carrier.AgencyUrl = checkValue(s.AgencyUrl);
        carrier.AgencyFareUrl = checkValue(s.AgencyFareUrl);
        System.out.println("writing to RTD...");


        ObjectMapper mapper = new ObjectMapper();

        String carrierJson = mapper.writeValueAsString(carrier);
        System.out.println("carrierJson = " + carrierJson);

        try {
            URL rtdUrl = new URL(Play.application().configuration().getString("application.extensions.rtd_integration.api")+ "/" + carrier.AgencyId);
            System.out.println("rtdUrl = " + rtdUrl);
            HttpURLConnection connection = (HttpURLConnection) rtdUrl.openConnection();

            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(carrierJson);
            osw.flush();
            osw.close();
            System.out.println(connection.getResponseCode());
        } catch (Exception e) {
            System.out.println("error writing to RTD");
            e.printStackTrace();
        }
    }

    public static String checkValue(String str) {
        if(str == "null") return null;
        return str;
    }
    
    public static Result update (String id) throws JsonProcessingException, MalformedURLException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedSource s = FeedSource.get(id);

        JsonNode params = request().body().asJson();
        if (userProfile.canAdministerProject(s.feedCollectionId) || userProfile.canManageFeed(s.feedCollectionId, s.id)) {
            applyJsonToFeedSource(s, params);
            s.save();
            return ok(json.write(s)).as("application/json");
        }
        return unauthorized();
    }
    
    public static Result create () throws MalformedURLException, JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();
        
        // parse the result
        JsonNode params = request().body().asJson();
        
        FeedCollection c = FeedCollection.get(params.get("feedCollection").get("id").asText());
        
        if (userProfile.canAdministerProject(c.id)) {
            FeedSource s = new FeedSource(params.get("name").asText());
            // not setting user because feed sources are automatically assigned a unique user
            s.setFeedCollection(c);
            
            applyJsonToFeedSource(s, params);
            
            s.save();
            
            return ok(json.write(s)).as("application/json");
        }
        else {
            return unauthorized();
        }
    }
    
    public static Result delete (String id) {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedSource s = FeedSource.get(id);
        if (userProfile.canAdministerProject(s.getFeedCollection().id)) {
            s.delete();
            return ok();
        }
        else {
            return unauthorized();
        }
    }
    
    /**
     * Get the userId and key that will allow a user to edit just this feed, without being an admin.
     * Only admins can retrieve this information.
     */
    public static Result getUserIdAndKey (String id) {
        User currentUser = User.getUserByUsername(session("username"));
        
        if (currentUser.admin) {
            FeedSource s = FeedSource.get(id);
            User u = s.getUser();
            
            ObjectNode result = Json.newObject();
            result.put("userId", u.id);
            result.put("key", u.key);
            return ok(result);
        }
        else {
            return unauthorized();
        }
    }
    
    /**
     * Refetch this feed
     * @throws JsonProcessingException 
     */
    public static Result fetch (String id) throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedSource s = FeedSource.get(id);

        // ways to have permission to do this:
        // 1) be an admin
        // 2) have access to this feed through project permissions
        // if all fail, the user cannot do this.
        if (!userProfile.canAdministerProject(s.feedCollectionId) && !userProfile.canManageFeed(s.feedCollectionId, s.id))
            return unauthorized();
        
        FetchSingleFeedJob job = new FetchSingleFeedJob(s);
        job.run();
        return ok(FeedVersionController.getJsonManager().write(job.result)).as("application/json");
    }

    public static Result uploadAgencyLogo(String id) {

        Auth0UserProfile userProfile = getSessionProfile();
        if(userProfile == null) return unauthorized();

        FeedSource feedSource = FeedSource.get(id);
        if(feedSource == null) return badRequest();

        if (!userProfile.canAdministerProject(feedSource.feedCollectionId) && !userProfile.canManageFeed(feedSource.feedCollectionId, feedSource.id))
            return unauthorized();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("picture");
        if (picture != null) {
            //String contentType = picture.getContentType();
            File formFile = picture.getFile();

            String agencyId = body.asFormUrlEncoded().get("agencyId")[0];

            // create the file in the branding assets directory
            String agencyDir = Play.application().configuration().getString("application.data.branding_internal") + File.separator + agencyId;
            new File(agencyDir).mkdirs();
            File brandingFile = new File(agencyDir + File.separator + "logo.png");
            Files.copyFile(formFile, brandingFile, true);

            // register the branding with the FeedSource
            AgencyBranding agencyBranding = feedSource.getAgencyBranding(agencyId);
            if(agencyBranding == null) { // if branding does not already exist for this agency, create it
                agencyBranding = new AgencyBranding(agencyId);
                feedSource.addAgencyBranding(agencyBranding);
            }
            agencyBranding.hasLogo = true;

            feedSource.save();
            return redirect("/#feed/" + feedSource.id);
        } else {
            flash("error", "Missing file");
            return redirect("/#feed/" + feedSource.id);
        }
    }
}
