package jobs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import play.Logger;
import play.Play;
import utils.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;

import models.FeedCollection;
import models.FeedSource;
import models.FeedVersion;

/**
 * Make public feed files for the public to download.
 * 
 * TODO: currently does all feed collections; we probably want this to be per feed collection.
 * 
 * @author matthewc
 *
 */
public class MakePublicJob implements Runnable {
    public final FeedCollection feedCollection;
    
    public MakePublicJob(FeedCollection feedCollection) {
        this.feedCollection = feedCollection;
    }

    @Override
    public void run() {
        Collection<FeedSource> feedSources = feedCollection.getFeedSources();
                
        // filter the collection to just the feeds that are public
        feedSources = Collections2.filter(feedSources, new Predicate<FeedSource>() {
            @Override
            public boolean apply(FeedSource fs) {
                return fs.isPublic;
            }
        });
        
        // convert to an array so we can sort
        // FeedSource has a comparator that will put them in alphabetical order
        FeedSource[] sortedFeedSources = feedSources.toArray(new FeedSource[feedSources.size()]);
        Arrays.sort(sortedFeedSources);
        
        // find the feed versions for these feed sources
        // this list is implicitly sorted, because it is made from the sorted feed source list.
        List<FeedVersion> versions = new ArrayList<FeedVersion>(sortedFeedSources.length);
        
        for (FeedSource fs : sortedFeedSources) {
            // get the latest version that passed validation, more or less
            FeedVersion fv = fs.getLatest();
            
            while (fv != null && fv.hasCriticalErrors())
                fv = fv.getPreviousVersion();
            
            if (fv != null)
                versions.add(fv);
        }
        
        // empty out the public directory
        // TODO: feed collection name collisions.
        // TODO: feed collection name changes will leave stale paths.
        File publicDirectory = new File(Play.application().configuration().getString("application.data.public"),
                StringUtils.getCleanName(feedCollection.name));
        
        publicDirectory.mkdirs();
        
        for (File file : publicDirectory.listFiles())
            file.delete();
        
        // copy over the feed files, giving them appropriate names
        Set<String> usedNames = new HashSet<String>();
        // keep track of what every feed version is called
        Map<String, String> feedVersionFileNames = new HashMap<String, String>();
        
        for (Iterator<FeedVersion> it = versions.iterator(); it.hasNext();) {
            FeedVersion v = it.next();
            
            // get a name for the feed version
            String name = StringUtils.getCleanName(v.getFeedSource().name);
            
            // make a unique name
            if (usedNames.contains(name)) {
                int i = 1;
                while (usedNames.contains(name + "_" + i))
                    i++;
                
                name = name + "_" + i;
            }
            
            usedNames.add(name);
            
            String fileName = name + ".zip";
            feedVersionFileNames.put(v.id, fileName);
            
            File out = new File(publicDirectory, fileName);
            try {
                Files.copy(v.getFeed(), out);
            } catch (IOException e) {
                Logger.error("Exception copying feed " + v.getFeedSource().name);
                e.printStackTrace();
                // take this feed version out of the list so it is not rendered by the template
                it.remove();
            }
        }
        
        // we render the template to a string and write it to a file
        String index = views.html.Public.index.render(versions, feedVersionFileNames).body();
        
        File indexFile = new File(publicDirectory, "index.html");
        
        try {
            Files.write(index, indexFile, Charset.forName("UTF-8"));
        } catch (Exception e) {
            Logger.error("Failed to write public index page!");
            // unrecoverable situations
            throw new RuntimeException(e);
        }
    }
}
