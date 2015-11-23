package utils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class MapDBMigrateTest {

    @Test
    public void test_import() throws IOException {
        File t = new File(System.getProperty("java.io.tmpdir"),"mapdb"+Math.random());

        File importDir = new File("test-migrate-data");
        while(!importDir.exists()){
            importDir = new File("../"+importDir.getPath());
        }

        //run import
        MapDBMigrate.main(new String[]{importDir.getPath(), t.getPath()});

        for(String name:MapDBMigrate.STORE_NAMES){
            if("feedversions".equals(name))
                continue;
            DataStore s = new DataStore(t,name);
            assertTrue(name, s.map.size()>0);
        }
    }
}