package models;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import com.conveyal.gtfs.validator.json.FeedValidationResult;
import com.conveyal.gtfs.validator.json.LoadStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a subset of a feed validation result, just enough for display, without overwhelming the browser
 * or sending unnecessary amounts of data over the wire
 */
public class FeedValidationResultSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    public LoadStatus loadStatus;
    
    @JsonInclude(Include.ALWAYS)
    public String loadFailureReason;
    public Collection<String> agencies;

    public int errorCount;
    
    // statistics
    public int agencyCount;
    public int routeCount;
    public int tripCount;
    public int stopTimesCount;
    
    /** The first date the feed has service, either in calendar.txt or calendar_dates.txt */
    @JsonInclude(Include.ALWAYS)
    public Date startDate;
    
    /** The last date the feed has service, either in calendar.txt or calendar_dates.txt */
    @JsonInclude(Include.ALWAYS)
    public Date endDate;
    
    @JsonInclude(Include.ALWAYS)
    public Rectangle2D bounds;
    
    /**
     * Construct a summarized version of the given FeedValidationResult.
     * @param result
     */
    public FeedValidationResultSummary (FeedValidationResult result) {
        this.loadStatus = result.loadStatus;
        this.loadFailureReason = result.loadFailureReason;
        this.agencies = result.agencies;
        
        if (loadStatus == LoadStatus.SUCCESS) {
            this.errorCount = 
                    result.routes.invalidValues.size() +
                    result.stops.invalidValues.size() +
                    result.trips.invalidValues.size() +
                    result.shapes.invalidValues.size();
            
            this.agencyCount = result.agencyCount;
            this.routeCount = result.routeCount;
            this.tripCount = result.tripCount;
            this.stopTimesCount = result.stopTimesCount;
            this.startDate = result.startDate;
            this.endDate = result.endDate;
            this.bounds = result.bounds;
        }
    }
}