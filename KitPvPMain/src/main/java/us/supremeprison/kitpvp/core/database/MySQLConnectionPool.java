package us.supremeprison.kitpvp.core.database;

import com.google.common.collect.Lists;
import us.supremeprison.kitpvp.core.KitPvP;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.List;

/**
 * @author Connor Hollasch
 * @since 3/10/2015
 */
public class MySQLConnectionPool {
    private final KitPvP plugin_instance;
    private final List<Connection> connections;
    private final MySQLDatabaseInformation database_information;

    private int last_used_connection = -1;

    public MySQLConnectionPool(KitPvP plugin_instance, MySQLDatabaseInformation database_information)
    {
        this.plugin_instance = plugin_instance;
        this.connections = Lists.newArrayList();
        this.database_information = database_information;

        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception exception)
        {
            plugin_instance.logMessage("MySQLConnectionPool: &cDrivers not found.");
            exception.printStackTrace();
            return;
        }

        plugin_instance.logMessage("MySQLConnectionPool: &aOpening &e" + database_information.getConnection_count() + "&a connections.");
        for (int i = 0; i < database_information.getConnection_count(); i++)
        {
            try
            {
                connections.add(DriverManager.getConnection(database_information.buildURL(), database_information.getUsername(), database_information.getPassword()));
            } catch (Exception exception)
            {
                plugin_instance.logMessage("MySQLConnectionPool: &cError while opening a new connection.");
                exception.printStackTrace();
                break;
            }
            plugin_instance.logMessage("MySQLConnectionPool: &aSuccessfuly opened a new connection. &7(&eConnection &f" + i + "&7)");
        }
    }

    public void closeConnections()
    {
        plugin_instance.logMessage("MySQLConnectionPool: &aClosing &e" + database_information.getConnection_count() + "&a connections.");

        Iterator<Connection> connection_iterator = connections.iterator();
        while (connection_iterator.hasNext())
        {
            Connection next = connection_iterator.next();

            try
            {
                if (!next.isClosed())
                    next.close();
            } catch (Exception exception)
            {
                plugin_instance.logMessage("MySQLConnectionPool: &cError while closing a connection.");
                exception.printStackTrace();
                continue;
            }
            plugin_instance.logMessage("MySQLConnectionPool: &aSuccessfuly closed a connection.");
        }
    }

    public Connection getNextConnection()
    {
        // Increment.
        last_used_connection += 1;

        // Check that we can use the next connection.
        if (connections.size() >= last_used_connection)
            last_used_connection = 0;

        // Return the connection.
        return connections.get(last_used_connection);
    }
}
