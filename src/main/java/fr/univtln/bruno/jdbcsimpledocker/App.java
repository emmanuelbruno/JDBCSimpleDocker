package fr.univtln.bruno.jdbcsimpledocker;

import lombok.extern.java.Log;

import java.sql.*;

/**
 * A simple JDBC application that connect to a RDMS, show it product name and version and create a new table if needed.
 */
@Log
public class App {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1)
            log.info("One parameter missing, give the jdbc URL : jdbc:postgresql://localhost:5432/postgres?user=postgres&password=mysecretpassword");
        else {
            //We just wait for the database to be ready
            log.info("Waiting 5s for the database");
            Thread.sleep(5000);

            //We use a try that will autoclose the resources.
            try (Connection connection = DriverManager.getConnection(args[0])) {

                DatabaseMetaData metadata = connection.getMetaData();

                //Print info about the database system
                log.info("Database :" + metadata.getDatabaseProductName()
                        + " " + metadata.getDatabaseMajorVersion() + "." + metadata.getDatabaseMinorVersion());

                log.info(connection.getCatalog() + " " + connection.getSchema());

                //Retrieving the list of database names
                ResultSet tables = metadata.getTables(connection.getCatalog(),
                        connection.getSchema(),
                        "SIMPLEJDBC_PERSON", null);

                if (tables.next()) {
                    log.info("Table " + tables.getString("TABLE_NAME") + " already exist.");
                } else {
                    connection.createStatement().execute("CREATE TABLE \"SIMPLEJDBC_PERSON\"(" +
                            "    id INT PRIMARY KEY NOT NULL, " +
                            "    firstname VARCHAR(100))");
                    log.info("Table SIMPLEJDBC_PERSON created.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
