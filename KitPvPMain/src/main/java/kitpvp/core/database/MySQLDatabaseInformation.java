package kitpvp.core.database;

import lombok.Getter;
import kitpvp.core.util.config.ConfigOption;

public class MySQLDatabaseInformation
{

      @Getter
      @ConfigOption(configuration_section = "DATABASE.HOSTNAME")
      private String hostname = "127.0.0.1";
      @Getter
      @ConfigOption(configuration_section = "DATABASE.PORT")
      private int port = 3306;
      @Getter
      @ConfigOption(configuration_section = "DATABASE.USERNAME")
      private String username = "username";
      @Getter
      @ConfigOption(configuration_section = "DATABASE.PASSWORD")
      private String password = "password";
      @Getter
      @ConfigOption(configuration_section = "DATABASE.DATABASE-NAME")
      private String database_name = "KitPvPCore";
      @Getter
      @ConfigOption(configuration_section = "DATABASE.CONNECTION-COUNT")
      private int connection_count = 10;

      public String buildURL()
      {
            return "jdbc:mysql://" + hostname + ":" + port + "/" + database_name + "?autoReconnect=true";
      }
}
