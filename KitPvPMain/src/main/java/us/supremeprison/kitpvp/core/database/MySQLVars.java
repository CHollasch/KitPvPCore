package us.supremeprison.kitpvp.core.database;

import us.supremeprison.kitpvp.core.KitPvP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Connor Hollasch
 * @since 3/13/2015
 */
public enum MySQLVars {

    CREATE_ATTACHMENT_TABLE("CREATE TABLE IF NOT EXISTS `%db%`.`user_attachments`(" +
            "`uuid` VARCHAR(36) NOT NULL, " +
            "`attachment_data` TEXT NOT NULL, " +
            "PRIMARY KEY (`uuid`, `attachment_data`));"),
    GET_ALL_ATTACHMENTS("SELECT attachment_data FROM `%db%`.`user_attachments` WHERE uuid=?;"),
    INSERT_INTO_ATTACHMENTS("INSERT INTO `%db%`.`user_attachments`(`uuid`, `attachment_data`) " +
            "VALUES(?, ?) " +
            "ON DUPLICATE KEY UPDATE attachment_data = VALUES(attachment_data);");

    private MySQLEnumWrapper wrapper;

    private MySQLVars(String sql) {
        this.wrapper = new MySQLEnumWrapper(sql);
    }

    public void executeQuery(Object... args) {
        wrapper.executeQuery(args);
    }

    public ResultSet getResultSet(Object... args) {
        return wrapper.getResultSet(args);
    }

    public MySQLEnumWrapper createSQLQuery(String sql) {
        return new MySQLEnumWrapper(sql);
    }

    private class MySQLEnumWrapper {
        private String sql_call;

        private MySQLEnumWrapper(String sql) {
            this.sql_call = sql;
            formatRawSQLString();
        }

        public void executeQuery(Object... args) {
            try {
                PreparedStatement ps = KitPvP.getPlugin_instance().getConnection_pool().getNextConnection().prepareCall(sql_call);

                int index = 1;
                for (Object arg : args) {
                    ps.setObject(index, arg);
                    index++;
                }

                ps.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        public ResultSet getResultSet(Object... args) {
            try {
                PreparedStatement ps = KitPvP.getPlugin_instance().getConnection_pool().getNextConnection().prepareCall(sql_call);

                int index = 1;
                for (Object arg : args) {
                    ps.setObject(index, arg);
                    index++;
                }

                return ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void formatRawSQLString() {
            MySQLDatabaseInformation info = KitPvP.getPlugin_instance().getDatabase_information();
            sql_call = sql_call.replace("%db%", info.getDatabase_name());
            sql_call = sql_call.replace("%host%", info.getHostname());
            sql_call = sql_call.replace("%password%", info.getPassword());
            sql_call = sql_call.replace("%user%", info.getUsername());
            sql_call = sql_call.replace("%port%", info.getPort() + "");
        }
    }
}
