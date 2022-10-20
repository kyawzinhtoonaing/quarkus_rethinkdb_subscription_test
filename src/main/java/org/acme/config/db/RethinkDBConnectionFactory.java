package org.acme.config.db;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@Startup
@ApplicationScoped
public class RethinkDBConnectionFactory {
    public static final String TABLE_PERSON_VIEWONLY = "personViewOnly";
    private Connection connection;

    public RethinkDBConnectionFactory(@ConfigProperty(name = "rethinkdb.host") String host,
                                      @ConfigProperty(name = "rethinkdb.port") Integer port,
                                      @ConfigProperty(name = "rethinkdb.database") String database) {
        final RethinkDB r = RethinkDB.r;
        this.connection = r.connection().hostname(host).port(port).connect();

        // Create database if it does not exist.
        List<String> dbList = r.dbList().run(connection, ArrayList.class).first();
        if (!dbList.contains(database)) {
            r.dbCreate(database).run(connection);
        }

        List<String> tables = r.db(database).tableList().run(connection, ArrayList.class).first();

        // Create "personViewOnly" table if it does not exist.
        if(!tables.contains(TABLE_PERSON_VIEWONLY)) {
            r.db(database).tableCreate(TABLE_PERSON_VIEWONLY).run(connection);
            r.db(database).table(TABLE_PERSON_VIEWONLY).indexCreate("time").run(connection);
        }
        this.connection = this.connection.use(database);
    }

    public Connection getConnection() {
        return this.connection;
    }
}
