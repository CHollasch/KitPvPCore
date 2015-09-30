package us.supremeprison.kitpvp.core.database;

import lombok.Getter;
import us.supremeprison.kitpvp.core.util.config.ConfigOption;

public class MySQLDatabaseInformation {

    @Getter
    @ConfigOption("DATABASE.HOSTNAME")
    private String hostname = "127.0.0.1";
    @Getter
    @ConfigOption("DATABASE.PORT")
    private int port = 3306;
    @Getter
    @ConfigOption("DATABASE.USERNAME")
    private String username = "username";
    @Getter
    @ConfigOption("DATABASE.PASSWORD")
    private String password = "password";
    @Getter
    @ConfigOption("DATABASE.DATABASE-NAME")
    private String database_name = "KitPvPCore";
    @Getter
    @ConfigOption("DATABASE.CONNECTION-COUNT")
    private int connection_count = 10;

    public String buildURL() {
        return "jdbc:mysql://" + hostname + ":" + port + "/" + database_name + "?autoReconnect=true";
    }
}
