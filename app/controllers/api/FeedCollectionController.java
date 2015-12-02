package controllers.api;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import controllers.Secured;
import jobs.FetchProjectFeedsJob;
import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Security.Authenticated(Secured.class)
public class FeedCollectionController extends Controller {
    public static final Logger LOG = LoggerFactory.getLogger(FeedCollectionController.class);

    private static JsonManager<FeedCollection> json = 
            new JsonManager<FeedCollection>(FeedCollection.class, JsonViews.UserInterface.class);
    
    public static Result getAll () throws JsonProcessingException {
        // it's fine to show all feed collections here. they're just folders, the user knows
        // nothing about what is in them.
        // TODO: When we get to a point where we're managing lots of them, for multiple clients on the same server
        // we'll want to change this.
        return ok(json.write(FeedCollection.getAll())).as("application/json");
    }
    
    public static Result get (String id) throws JsonProcessingException {
        FeedCollection c = FeedCollection.get(id);
        
        return ok(json.write(c)).as("application/json");
    }
    
    public static Result update (String id) throws JsonProcessingException {
        FeedCollection c = FeedCollection.get(id);
        
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!currentUser.admin)
            return unauthorized();
        
        JsonNode params = request().body().asJson();

        JsonNode name = params.get("name");
        if (name != null) {
            c.name = name.asText();
        }

        JsonNode useCustomOsmBounds = params.get("useCustomOsmBounds");
        if (useCustomOsmBounds != null) {
            c.useCustomOsmBounds = useCustomOsmBounds.asBoolean();
        }

        JsonNode osmWest = params.get("osmWest");
        if (osmWest != null) {
            c.osmWest = osmWest.asDouble();
        }

        JsonNode osmSouth = params.get("osmSouth");
        if (osmSouth != null) {
            c.osmSouth = osmSouth.asDouble();
        }

        JsonNode osmEast = params.get("osmEast");
        if (osmEast != null) {
            c.osmEast = osmEast.asDouble();
        }

        JsonNode osmNorth = params.get("osmNorth");
        if (osmNorth != null) {
            c.osmNorth = osmNorth.asDouble();
        }

        if (params.has("buildConfig")) {
            JsonNode buildConfig = params.get("buildConfig");
            if(c.buildConfig == null) c.buildConfig = new OtpBuildConfig();

            if(buildConfig.has("subwayAccessTime")) {
                JsonNode subwayAccessTime = buildConfig.get("subwayAccessTime");
                // allow client to un-set option via 'null' value
                c.buildConfig.subwayAccessTime = subwayAccessTime.isNull() ? null : subwayAccessTime.asDouble();
            }

            if(buildConfig.has("fetchElevationUS")) {
                JsonNode fetchElevationUS = buildConfig.get("fetchElevationUS");
                c.buildConfig.fetchElevationUS = fetchElevationUS.isNull() ? null : fetchElevationUS.asBoolean();
            }

            if(buildConfig.has("stationTransfers")) {
                JsonNode stationTransfers = buildConfig.get("stationTransfers");
                c.buildConfig.stationTransfers = stationTransfers.isNull() ? null : stationTransfers.asBoolean();
            }

            if (buildConfig.has("fares")) {
                JsonNode fares = buildConfig.get("fares");
                c.buildConfig.fares = fares.isNull() ? null : fares.asText();
            }
        }

        if (params.has("routerConfig")) {
            JsonNode routerConfig = params.get("routerConfig");
            if (c.routerConfig == null) c.routerConfig = new OtpRouterConfig();

            if (routerConfig.has("numItineraries")) {
                JsonNode numItineraries = routerConfig.get("numItineraries");
                c.routerConfig.numItineraries = numItineraries.isNull() ? null : numItineraries.asInt();
            }

            if (routerConfig.has("walkSpeed")) {
                JsonNode walkSpeed = routerConfig.get("walkSpeed");
                c.routerConfig.walkSpeed = walkSpeed.isNull() ? null : walkSpeed.asDouble();
            }

            if (routerConfig.has("carDropoffTime")) {
                JsonNode carDropoffTime = routerConfig.get("carDropoffTime");
                c.routerConfig.carDropoffTime = carDropoffTime.isNull() ? null : carDropoffTime.asDouble();
            }

            if (routerConfig.has("stairsReluctance")) {
                JsonNode stairsReluctance = routerConfig.get("stairsReluctance");
                c.routerConfig.stairsReluctance = stairsReluctance.isNull() ? null : stairsReluctance.asDouble();
            }

            if (routerConfig.has("updaters")) {
                JsonNode updaters = routerConfig.get("updaters");
                if (updaters.isArray()) {
                    c.routerConfig.updaters = new ArrayList<>();
                    for (int i = 0; i < updaters.size(); i++) {
                        JsonNode updater = updaters.get(i);

                        OtpRouterConfig.Updater updaterObj = new OtpRouterConfig.Updater();
                        if(updater.has("type")) {
                            JsonNode type = updater.get("type");
                            updaterObj.type = type.isNull() ? null : type.asText();
                        }

                        if(updater.has("sourceType")) {
                            JsonNode sourceType = updater.get("sourceType");
                            updaterObj.sourceType = sourceType.isNull() ? null : sourceType.asText();
                        }

                        if(updater.has("defaultAgencyId")) {
                            JsonNode defaultAgencyId = updater.get("defaultAgencyId");
                            updaterObj.defaultAgencyId = defaultAgencyId.isNull() ? null : defaultAgencyId.asText();
                        }

                        if(updater.has("url")) {
                            JsonNode url = updater.get("url");
                            updaterObj.url = url.isNull() ? null : url.asText();
                        }

                        if(updater.has("frequencySec")) {
                            JsonNode frequencySec = updater.get("frequencySec");
                            updaterObj.frequencySec = frequencySec.isNull() ? null : frequencySec.asInt();
                        }

                        c.routerConfig.updaters.add(updaterObj);
                    }
                }
            }
        }

        // only allow admins to change feed collection owners
        if (currentUser.admin) {
            JsonNode uname = params.get("user");
        
            // TODO: test
            User u = null;
            if (uname != null && uname.has("username"))
                u = User.getUserByUsername(uname.get("username").asText());
        
            if (u != null)
                c.setUser(u);
        }
        
        c.save();
        
        return ok(json.write(c)).as("application/json");
    }
    
    public static Result create () throws JsonParseException, JsonMappingException, IOException {
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!currentUser.admin)
            return unauthorized();
        
        JsonNode params = request().body().asJson();
        
        FeedCollection c = new FeedCollection();
        
        // TODO: fail gracefully
        c.name = params.get("name").asText();
        JsonNode uname = params.get("user/username");
        
        User u = null;
        if (uname != null)
            u = User.getUserByUsername(uname.asText());
        
        if (u == null)
            u = currentUser;
            
        c.setUser(u);
        
        c.save();
        
        return ok(json.write(c)).as("application/json");
    }
    
    public static Promise<Result> getEditorAgencies () {
        // note: this is accessible to anyone; for now this is fine but in the future we will want to handle this better
        
        // first, get a token
        String url = Play.application().configuration().getString("application.editor.internal_url");
        
        if (!url.endsWith("/"))
            url += "/";
        
        final String baseUrl = url;
        
        String tokenUrl = baseUrl + "get_token";
        tokenUrl += "?client_id=" + Play.application().configuration().getString("application.oauth.client_id");
        tokenUrl += "&client_secret=" + Play.application().configuration().getString("application.oauth.client_secret");
        
        final Promise<String> tokenPromise = WS.url(tokenUrl).get().map(new Function<WSResponse, String> () {
            public String apply(WSResponse wsr) throws Throwable {
                if (wsr.getStatus() != 200)
                    return null;
                
                else
                    return wsr.getBody();
            }
        });
        
        final Promise<WSResponse> agencyPromise = tokenPromise.flatMap(new Function<String, Promise<WSResponse>> () {
            public Promise<WSResponse> apply(String token) throws Throwable {
                String agencyUrl = baseUrl + "api/agency?oauth_token=" + token;
                return WS.url(agencyUrl).get();
            }
        });
        
        return agencyPromise.map(new Function<WSResponse, Result> () {
            public Result apply (WSResponse wsr) {
                if (wsr.getStatus() != 200)
                    return internalServerError();
                else
                    return ok(wsr.getBody()).as("application/json");
            }
        });
    }

    public static Promise<Result> getEditorSnapshots (final String agencyId) {
        // note: this is accessible to anyone; for now this is fine but in the future we will want to handle this better

        // first, get a token
        String url = Play.application().configuration().getString("application.editor.internal_url");

        if (!url.endsWith("/"))
            url += "/";

        final String baseUrl = url;

        String tokenUrl = baseUrl + "get_token";
        tokenUrl += "?client_id=" + Play.application().configuration().getString("application.oauth.client_id");
        tokenUrl += "&client_secret=" + Play.application().configuration().getString("application.oauth.client_secret");

        final Promise<String> tokenPromise = WS.url(tokenUrl).get().map(new Function<WSResponse, String> () {
            public String apply(WSResponse wsr) throws Throwable {
                if (wsr.getStatus() != 200)
                    return null;

                else
                    return wsr.getBody();
            }
        });

        final Promise<WSResponse> agencyPromise = tokenPromise.flatMap(new Function<String, Promise<WSResponse>> () {
            public Promise<WSResponse> apply(String token) throws Throwable {
                String snapUrl = baseUrl + "api/snapshot?agencyId=" + UrlEscapers.urlFormParameterEscaper().escape(agencyId) + "&oauth_token=" + token;
                return WS.url(snapUrl).get();
            }
        });

        return agencyPromise.map(new Function<WSResponse, Result> () {
            public Result apply (WSResponse wsr) {
                if (wsr.getStatus() != 200)
                    return internalServerError();
                else
                    return ok(wsr.getBody()).as("application/json");
            }
        });
    }

    public static Result fetchAllFeeds(String id) throws JsonProcessingException {
        FeedCollection c = FeedCollection.get(id);
        FetchProjectFeedsJob job = new FetchProjectFeedsJob(c);
        job.run();

        return ok(FeedVersionController.json.write(job.result)).as("application/json");
    }

    public static Result downloadFeeds (String id) throws Exception {
        User currentUser = User.getUserByUsername(session("username"));

        if (!currentUser.admin)
            return unauthorized();

        FeedCollection c = FeedCollection.get(id);

        // create a zip bundle of all feeds
        File temp = File.createTempFile("feeds", ".zip");
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temp)));
        // we're zipping zipfiles, no point in trying to compress further
        // stored evidently requires precomputing crcs which I don't want to figure out right now:
        // https://community.oracle.com/thread/1317430?start=0&tstart=0
        //zos.setMethod(ZipOutputStream.STORED);

        for (FeedSource s : c.getFeedSources()) {
            FeedVersion v = s.getLatest();
            if (v != null) {
                File feed = v.getFeed();

                if (feed == null) {
                    // TODO why does this happen?
                    LOG.warn("Feed version {} has no feed file", v);
                    continue;
                }

                // give human readable name but add ID to ensure uniqueness.
                String name = s.name.replaceAll("[^a-zA-Z0-9\\-]", "_") + "_" + s.id + ".zip";
                ZipEntry ze = new ZipEntry(name);

                ze.setSize(feed.length());

                zos.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(feed));
                ByteStreams.copy(is, zos);
                is.close();
                zos.closeEntry();
            }
        }

        zos.close();

        FileInputStream fis = new FileInputStream(temp);

        // will not be deleted until input stream is closed
        temp.delete();

        response().setContentType("application/zip");
        response().setHeader("Content-Disposition", "attachment;filename=" + c.name.replaceAll("[^a-zA-Z0-9]", "") + ".zip");
        return ok(fis);
    }
}
