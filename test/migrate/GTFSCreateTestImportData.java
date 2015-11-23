/*
package com.conveyal.osmlib;

import com.google.common.collect.ImmutableMap;
import models.*;
import org.junit.Test;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;
import utils.DataStore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

//
// Creates test data in 1.0 format
// Is commented out to prevent compilation errors
//
public class GTFSCreateTestImportData extends WithApplication {


    @Override
    protected FakeApplication provideFakeApplication() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir")+"/mapdbtest"+System.currentTimeMillis());
        tmpDir.mkdirs();
        return new FakeApplication(new java.io.File("."), Helpers.class.getClassLoader(),
                ImmutableMap.of("application.data.mapdb",tmpDir.getAbsolutePath()), new ArrayList<String>(), null);
    }


    @Test
    public void main() throws MalformedURLException {
        File f = new File(System.getProperty("mapdbOut"));
        if (f.exists())
            throw new RuntimeException("Dir already exists " + f);
        f.mkdirs();


        {
            Deployment d1 = new Deployment();
            d1.setUser(new User("name1", "pw", "mail@example.com"));
            Deployment d2 = new Deployment();
            d2.setUser(new User("name2", "pw", "mail@example.com"));

            DataStore<Deployment> deployment = new DataStore<Deployment>(f, "deployments");
            deployment.save("aa1",d1);
            deployment.save("aa2",d2);

            deployment.commit();
        }

        {
            DataStore<FeedCollection> feedCollections = new DataStore<FeedCollection>(f,"feedcollections");
            FeedCollection f1 = new FeedCollection();
            f1.name = "name1";
            f1.useCustomOsmBounds = true;
            f1.osmNorth = 1.1;
            f1.osmSouth = 2.2;
            f1.osmEast = 3.3;
            f1.osmWest = 4.4;

            FeedCollection f2 = new FeedCollection();
            f2.name = "name2";
            f2.useCustomOsmBounds = false;
            f2.osmNorth = 61.1;
            f2.osmSouth = 62.2;
            f2.osmEast = 63.3;
            f2.osmWest = 64.4;


            feedCollections.save("aa1",f1);
            feedCollections.save("aa2",f2);
            feedCollections.commit();

        }

        {
            DataStore<FeedSource> feedSources = new DataStore<FeedSource>(f,"feedsources");
            FeedSource f1 = new FeedSource();
            f1.name = "name1";
            f1.url = new URL("http://www.example.com/a1");
            feedSources.save("aa1", f1);
            feedSources.commit();
        }

        {
            DataStore<Note> notes = new DataStore<Note>(f,"notes");
            Note f1 = new Note();
            f1.note = "name1";
            f1.date = new Date(111L);
            notes.save("aa1", f1);
            notes.commit();
        }

        {
            DataStore<User> users = new DataStore(f, "users");
            users.save("user1", new User("name1", "password1", "mail1@example.com"));
            users.save("user2", new User("name2", "password2", "mail2@example.com"));
            users.commit();
        }



    }

}
*/