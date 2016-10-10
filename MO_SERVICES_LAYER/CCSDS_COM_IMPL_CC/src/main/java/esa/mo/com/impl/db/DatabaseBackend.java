/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.com.impl.db;

import esa.mo.com.impl.provider.ArchiveManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Holds all the fields of a COM Event.
 *
 * @author Cesar Coelho
 */
public class DatabaseBackend {

    private static final String DROP_TABLE_PROPERTY = "esa.mo.com.impl.provider.ArchiveManager.droptable";
    private static final String PERSISTENCE_UNIT_NAME = "ArchivePersistenceUnit";

    private final Semaphore emAvailability = new Semaphore(1, true);  // true for fairness, because we want FIFO
    private EntityManagerFactory emf;
    private EntityManager em;
    private Connection serverConnection;
//    private boolean wasKeptOpen = false;

//    private static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver"; // Derby Embedded Driver
//    private static final String DATABASE_NAME = "derby"; // Derby
//    private static final String DATABASE_LOCATION_NAME = "databaseV0.4";
    private static final String DRIVER_CLASS_NAME = "org.sqlite.JDBC"; // SQLite JDBC Driver
    private static final String DATABASE_NAME = "sqlite"; // SQLite
    private static final String DATABASE_LOCATION_NAME = "comArchive.db";

    private final String url;
    
    public DatabaseBackend(){
        // Create unique URL that identifies the connection
        this.url = "jdbc:" + DATABASE_NAME + ":" + DATABASE_LOCATION_NAME;
        this.startBackendDatabase();
    }

    private void startBackendDatabase() {
        final AtomicBoolean acquired = new AtomicBoolean(false);
        
        final Thread startDatabase = new Thread() {
            @Override
            public void run() {
                try {
                    emAvailability.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                acquired.set(true);
                this.notify();

                startServer();
                createEMFactory();
                emAvailability.release();

                Logger.getLogger(DatabaseBackend.class.getName()).log(Level.INFO, "The database was initialized and the Archive service is ready!");
            }
        };

        startDatabase.start();
        
        synchronized(startDatabase){
            while(acquired.get() == false){  // Check if it is acquired...
                try {
                    startDatabase.wait(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DatabaseBackend.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    
    private void startServer() {
//        System.setProperty("derby.drda.startNetworkServer", "true");
        // Loads a new instance of the database driver
        try {
            Logger.getLogger(DatabaseBackend.class.getName()).log(Level.INFO, "Creating a new instance of the database driver: " + DRIVER_CLASS_NAME);
            Class.forName(DRIVER_CLASS_NAME).newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DatabaseBackend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DatabaseBackend.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Create unique URL that identifies the driver to use for the connection
//        String url2 = this.url + ";decryptDatabase=true"; // new
        String url2 = this.url;

        try {
            // Connect to the database
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Attempting to establish a connection to the database: " + url2);
            serverConnection = DriverManager.getConnection(url2);
        } catch (SQLException ex) {
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO,
                    "There was an SQLException, maybe the " + DATABASE_LOCATION_NAME + " folder/file does not exist. Attempting to create it...");

            try {
                // Connect to the database but also create the database if it does not exist
                serverConnection = DriverManager.getConnection(url2 + ";create=true");
                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Successfully created!");
            } catch (SQLException ex2) {
                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Derby connection already exists! Error: {0}", ex2);
                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Most likely there is another instance of the same application already running. Two instances of the same application are not allowed. The application will exit.");
                System.exit(0);
            }
        }
    }
    
    private void createEMFactory() {
        boolean dropTable = "true".equals(System.getProperty(DROP_TABLE_PROPERTY));  // Is the status of the dropTable flag on?
        Map<String, String> persistenceMap = new HashMap<String, String>();

        // Add the url property of the connection to the database
        persistenceMap.put("javax.persistence.jdbc.url", this.url);

        if (dropTable) {
            persistenceMap.put("javax.persistence.schema-generation.database.action", "drop-and-create");
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "The droptable flag in the properties file is enabled! The table will be dropped upon start-up.");
        }

        Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Creating Entity Manager Factory...");
        this.emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, persistenceMap);
    }

    public void createEntityManager() {
        try {
            this.emAvailability.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        if(!wasKeptOpen){
            this.em = this.emf.createEntityManager();
//        }
    }

    public void closeEntityManager() {
        // If it has Thread open, then keep it open...
//        wasKeptOpen = this.emAvailability.hasQueuedThreads();
        
//        if(!wasKeptOpen){
            this.em.close();
//        }
        
        this.emAvailability.release();
    }
    
    public EntityManager getEM(){
        return this.em;
    }
    
    public void restartEMF(){
        this.emf.close();
        this.createEMFactory();
        this.emAvailability.release();
    }

}
