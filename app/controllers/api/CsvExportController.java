package controllers.api;

import com.conveyal.gtfs.validator.json.LoadStatus;
import com.csvreader.CsvWriter;
import models.*;
import org.joda.time.LocalDate;
import play.Play;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Export to CSV.
 */
// note: not authenticated, see config option to enable auth.
public class CsvExportController extends Controller {
    public static Result asCsv (String feedCollectionId) throws IOException {
        if (Play.application().configuration().getBoolean("application.require-auth-for-csv-download", true)) {
            String username = session().get("username");
            if (username == null || User.getUserByUsername(username) == null)
                return unauthorized();
        }

        FeedCollection fc = FeedCollection.get(feedCollectionId);
        if (fc == null)
            return notFound();

        // write it out as CSV
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(baos, ',', Charset.forName("UTF-8"));

        writer.writeRecord(new String[] {
                "id",
                Messages.get("app.feed_source.name"),
                Messages.get("app.feed_source.public"),
                Messages.get("app.feed_source.deployable"),
                Messages.get("app.feed_source.retrieval_method"),
                Messages.get("app.feed_source.last_updated"),
                Messages.get("app.feed_version.loaded_successfully"),
                Messages.get("app.feed_version.load_status"),
                Messages.get("app.feed_version.error_count"),
                Messages.get("app.feed_version.route_count"),
                Messages.get("app.feed_version.trip_count"),
                Messages.get("app.feed_version.stop_times_count"),
                Messages.get("app.feed_version.start_date"),
                Messages.get("app.feed_version.end_date")
        });

        for (FeedSource s : fc.getFeedSources()) {
            writer.write(s.id);
            writer.write(s.name);
            writer.write(Messages.get(s.isPublic ? "app.yes" : "app.no"));
            writer.write(Messages.get(s.deployable ? "app.yes" : "app.no"));
            writer.write(s.retrievalMethod.toString());

            FeedVersion v = s.getLatest();

            if (v != null) {
                FeedValidationResultSummary vr = new FeedValidationResultSummary(v.validationResult);

                writer.write(new LocalDate(v.updated).toString());
                writer.write(Messages.get(vr.loadStatus == LoadStatus.SUCCESS ? "app.yes" : "app.no"));
                writer.write(vr.loadFailureReason != null ? vr.loadFailureReason : "");
                writer.write("" + vr.errorCount);
                writer.write("" + vr.routeCount);
                writer.write("" + vr.tripCount);
                writer.write("" + vr.stopTimesCount);
                writer.write(new LocalDate(vr.startDate).toString());
                writer.write(new LocalDate(vr.endDate).toString());
            }

            writer.endRecord();
        }

        writer.flush();

        // make a less ugly filename
        String filename = fc.name.replaceAll("[^a-zA-Z]", "") + ".csv";

        response().setHeader("Content-Disposition", "attachment;filename=" + filename);

        return ok(baos.toString()).as("text/csv");
    }
}
