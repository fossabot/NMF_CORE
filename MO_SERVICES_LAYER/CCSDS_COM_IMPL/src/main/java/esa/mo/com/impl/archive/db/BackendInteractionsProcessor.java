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
package esa.mo.com.impl.archive.db;

import esa.mo.com.impl.archive.entities.COMObjectEntity;
import esa.mo.com.impl.provider.ArchiveManager;
import esa.mo.com.impl.util.HelperCOM;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Query;
import org.ccsds.moims.mo.com.archive.structures.ArchiveQuery;
import org.ccsds.moims.mo.com.structures.ObjectType;
import org.ccsds.moims.mo.mal.structures.LongList;

/**
 *
 * @author Cesar Coelho
 */
public class BackendInteractionsProcessor {

    private static final String QUERY_SELECT_ALL = "SELECT PU.objId FROM COMObjectEntity PU WHERE PU.objectTypeId=:objectTypeId AND PU.domainId=:domainId";

    private static final Boolean SAFE_MODE = true;
    private static final Class<COMObjectEntity> CLASS_ENTITY = COMObjectEntity.class;
    private final DatabaseBackend dbBackend;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean sequencialStoring;

    private final LinkedBlockingQueue<StoreCOMObjectsContainer> storeQueue;

    public BackendInteractionsProcessor(DatabaseBackend dbBackend) {
        this.dbBackend = dbBackend;
        this.storeQueue = new LinkedBlockingQueue<StoreCOMObjectsContainer>();
        this.sequencialStoring = new AtomicBoolean(false);
    }

    private class GetCOMObjectCallable implements Callable {

        private final COMObjectPK2 id;

        public GetCOMObjectCallable(final COMObjectPK2 id) {
            this.id = id;
        }

        @Override
        public COMObjectEntity call() {
            dbBackend.createEntityManager();
            final COMObjectEntity perObj = dbBackend.getEM().find(CLASS_ENTITY, id);
            dbBackend.closeEntityManager();

            return perObj;
        }
    }

    public COMObjectEntity getCOMObject(final ObjectType objType, final Integer domain, final Long objId) {
        this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
        final GetCOMObjectCallable task = new GetCOMObjectCallable(COMObjectEntity.generatePK(objType, domain, objId));
        Future<COMObjectEntity> future = executor.submit(task);

        try {
            return future.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private class GetAllCOMObjectsCallable implements Callable {

        private final Long objTypeId;
        private final Integer domainId;

        public GetAllCOMObjectsCallable(final ObjectType objType, final Integer domainId) {
            this.objTypeId = HelperCOM.generateSubKey(objType);
            this.domainId = domainId;
        }

        @Override
        public LongList call() {
            dbBackend.createEntityManager();
            Query query = dbBackend.getEM().createQuery(QUERY_SELECT_ALL);
            query.setParameter("objectTypeId", objTypeId);
            query.setParameter("domainId", domainId);
            LongList objIds = new LongList();
            objIds.addAll(query.getResultList());
            dbBackend.closeEntityManager();

            return objIds;
        }
    }

    public LongList getAllCOMObjects(final ObjectType objType, final Integer domainId) {
        this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
        final GetAllCOMObjectsCallable task = new GetAllCOMObjectsCallable(objType, domainId);
        Future<LongList> future = executor.submit(task);

        try {
            return future.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private class InsertCOMObjectRunnable implements Runnable {

        private final Thread publishEventsThread;

        public InsertCOMObjectRunnable(final Thread publishEventsThread) {
            this.publishEventsThread = publishEventsThread;
        }

        @Override
        public void run() {
            StoreCOMObjectsContainer container = storeQueue.poll();

            if (container != null) {
                dbBackend.createEntityManager();  // 0.166 ms
                dbBackend.getEM().getTransaction().begin(); // 0.480 ms
                persistObjects(container.getPerObjs()); // store

                while (true) {
                    container = storeQueue.peek(); // get next if there is one available
                    if (container != null && container.isContinuous()) {
                        container = storeQueue.poll();
                        persistObjects(container.getPerObjs()); // store
                    }else{
                        break;
                    }
                }

                dbBackend.safeCommit();
                dbBackend.closeEntityManager(); // 0.410 ms
            }

            publishEventsThread.start();

            /*
            
            dbBackend.createEntityManager();  // 0.166 ms
            dbBackend.getEM().getTransaction().begin(); // 0.480 ms
            persistObjects(perObjs);
            dbBackend.safeCommit();
            dbBackend.closeEntityManager(); // 0.410 ms

            publishEventsThread.start();
             */
        }

    }

    private void persistObjects(final ArrayList<COMObjectEntity> perObjs) {
        for (int i = 0; i < perObjs.size(); i++) { // 6.510 ms per cycle
            if (SAFE_MODE) {
                final COMObjectEntity objCOM = dbBackend.getEM().find(CLASS_ENTITY, perObjs.get(i).getPrimaryKey()); // 0.830 ms

                if (objCOM == null) { // Last minute safety, the db crashes if one tries to store an object with a used pk
                    final COMObjectEntity perObj = perObjs.get(i); // The object to be stored  // 0.255 ms
                    dbBackend.getEM().persist(perObj);  // object    // 0.240 ms
                } else {
                    Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, "The Archive could not store the object: " + perObjs.get(i).toString());
                }
            } else {
                dbBackend.getEM().persist(perObjs.get(i));  // object
            }

            // Flush every 1k objects...
            if (i != 0) {
                if ((i % 1000) == 0) {
                    Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.FINE, "Flushing the data after 1000 serial stores...");
                    dbBackend.getEM().flush();
                    dbBackend.getEM().clear();
                }
            }
        }
    }

    public void insert(final ArrayList<COMObjectEntity> perObjs, final Thread publishEventsThread) {

        final boolean isSequential = this.sequencialStoring.get();
        StoreCOMObjectsContainer container = new StoreCOMObjectsContainer(perObjs, isSequential);

        this.sequencialStoring.set(true);

        try { // Insert into queue
            storeQueue.put(container);
        } catch (InterruptedException ex) {
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final InsertCOMObjectRunnable task = new InsertCOMObjectRunnable(publishEventsThread);
        executor.execute(task);

    }

    private class RemoveCOMObjectRunnable implements Runnable {

        private final ObjectType objType;
        private final Integer domainId;
        private final LongList objIds;
        private final Thread publishEventsThread;

        public RemoveCOMObjectRunnable(final ObjectType objType, final Integer domainId,
                final LongList objIds, final Thread publishEventsThread) {
            this.objType = objType;
            this.domainId = domainId;
            this.objIds = objIds;
            this.publishEventsThread = publishEventsThread;
        }

        @Override
        public void run() {
            dbBackend.createEntityManager();  // 0.166 ms

            // Generate the object Ids if needed and the persistence objects to be removed
            for (int i = 0; i < objIds.size(); i++) {
                final COMObjectPK2 id = COMObjectEntity.generatePK(objType, domainId, objIds.get(i));
                COMObjectEntity perObj = dbBackend.getEM().find(CLASS_ENTITY, id);
                dbBackend.getEM().getTransaction().begin();
                dbBackend.getEM().remove(perObj);
                dbBackend.getEM().getTransaction().commit();
            }

            dbBackend.closeEntityManager(); // 0.410 ms

            publishEventsThread.start();
        }
    }

    public void remove(final ObjectType objType, final Integer domainId,
            final LongList objIds, final Thread publishEventsThread) {
        this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
        final RemoveCOMObjectRunnable task = new RemoveCOMObjectRunnable(
                objType, domainId, objIds, publishEventsThread);
        executor.execute(task);
    }

    private class UpdateCOMObjectRunnable implements Runnable {

        private final ArrayList<COMObjectEntity> newObjs;
        private final Thread publishEventsThread;

        public UpdateCOMObjectRunnable(final ArrayList<COMObjectEntity> newObjs,
                final Thread publishEventsThread) {
            this.newObjs = newObjs;
            this.publishEventsThread = publishEventsThread;
        }

        @Override
        public void run() {
            dbBackend.createEntityManager();  // 0.166 ms

            for (int i = 0; i < newObjs.size(); i++) {
                final COMObjectPK2 id = newObjs.get(i).getPrimaryKey();
                COMObjectEntity previousObj = dbBackend.getEM().find(CLASS_ENTITY, id);

                dbBackend.getEM().getTransaction().begin();
                dbBackend.getEM().remove(previousObj);
                dbBackend.getEM().getTransaction().commit();

                // Maybe we can replace the 3 lines below with a persistObjects(newObjs) after the for loop
                dbBackend.getEM().getTransaction().begin();
                dbBackend.getEM().persist(newObjs.get(i));
                dbBackend.getEM().getTransaction().commit();
            }

            dbBackend.closeEntityManager(); // 0.410 ms

            publishEventsThread.start();

        }
    }

    public void update(final ArrayList<COMObjectEntity> newObjs, final Thread publishEventsThread) {
        this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
        final UpdateCOMObjectRunnable task = new UpdateCOMObjectRunnable(newObjs, publishEventsThread);
        executor.execute(task);
    }

    private class QueryCallable implements Callable {

        private final ObjectType objType;
        private final ArchiveQuery archiveQuery;
        private final Integer domainId;
        private final Integer providerURIId;
        private final Integer networkId;
        private final SourceLinkContainer sourceLink;

        public QueryCallable(final ObjectType objType,
                final ArchiveQuery archiveQuery, final Integer domainId,
                final Integer providerURIId, final Integer networkId,
                final SourceLinkContainer sourceLink) {
            this.objType = objType;
            this.archiveQuery = archiveQuery;
            this.domainId = domainId;
            this.providerURIId = providerURIId;
            this.networkId = networkId;
            this.sourceLink = sourceLink;
        }

        @Override
        public ArrayList<COMObjectEntity> call() {
            final boolean domainContainsWildcard = HelperCOM.domainContainsWildcard(archiveQuery.getDomain());
            final boolean objectTypeContainsWildcard = ArchiveManager.objectTypeContainsWildcard(objType);
            final boolean relatedContainsWildcard = (archiveQuery.getRelated().equals((long) 0));
            final boolean startTimeContainsWildcard = (archiveQuery.getStartTime() == null);
            final boolean endTimeContainsWildcard = (archiveQuery.getEndTime() == null);
            final boolean providerURIContainsWildcard = (archiveQuery.getProvider() == null);
            final boolean networkContainsWildcard = (archiveQuery.getNetwork() == null);

            final boolean sourceContainsWildcard = (archiveQuery.getSource() == null);
            boolean sourceTypeContainsWildcard = true;
            boolean sourceDomainContainsWildcard = true;
            boolean sourceObjIdContainsWildcard = true;

            if (!sourceContainsWildcard) {
                sourceTypeContainsWildcard = ArchiveManager.objectTypeContainsWildcard(archiveQuery.getSource().getType());
                sourceDomainContainsWildcard = HelperCOM.domainContainsWildcard(archiveQuery.getSource().getKey().getDomain());
                sourceObjIdContainsWildcard = (archiveQuery.getSource().getKey().getInstId() == null || archiveQuery.getSource().getKey().getInstId() == 0);
            }

            final boolean hasSomeDefinedFields
                    = (!domainContainsWildcard
                    || !objectTypeContainsWildcard
                    || !relatedContainsWildcard
                    || !startTimeContainsWildcard
                    || !endTimeContainsWildcard
                    || !providerURIContainsWildcard
                    || !networkContainsWildcard
                    || !sourceContainsWildcard);

            // Generate the query string
            String queryString = "SELECT PU FROM COMObjectEntity PU ";
            queryString += (hasSomeDefinedFields) ? "WHERE " : "";

            queryString += (domainContainsWildcard) ? "" : "PU.domainId=:domainId AND ";
            queryString += (objectTypeContainsWildcard) ? "" : "PU.objectTypeId=:objectTypeId AND ";
            queryString += (relatedContainsWildcard) ? "" : "PU.relatedLink=:relatedLink AND ";
            queryString += (startTimeContainsWildcard) ? "" : "PU.timestampArchiveDetails>=:startTime AND ";
            queryString += (endTimeContainsWildcard) ? "" : "PU.timestampArchiveDetails<=:endTime AND ";
            queryString += (providerURIContainsWildcard) ? "" : "PU.providerURI=:providerURI AND ";
            queryString += (networkContainsWildcard) ? "" : "PU.network=:network AND ";

            if (!sourceContainsWildcard) {
                queryString += (sourceTypeContainsWildcard) ? "" : "PU.sourceLinkObjectTypeId=:sourceLinkObjectTypeId AND ";
                queryString += (sourceDomainContainsWildcard) ? "" : "PU.sourceLinkDomainId=:sourceLinkDomainId AND ";
                queryString += (sourceObjIdContainsWildcard) ? "" : "PU.sourceLinkObjId=:sourceLinkObjId AND ";
            }

            if (hasSomeDefinedFields) { // 4 because we are removing the "AND " part
                queryString = queryString.substring(0, queryString.length() - 4);
            }

            ArrayList<COMObjectEntity> perObjs = new ArrayList<COMObjectEntity>();

            dbBackend.createEntityManager();
            Query query = dbBackend.getEM().createQuery(queryString); // Make the query

            if (!objectTypeContainsWildcard) {
                query.setParameter("objectTypeId", HelperCOM.generateSubKey(objType));
            }

            if (!domainContainsWildcard) {
                query.setParameter("domainId", domainId);
            }

            if (!relatedContainsWildcard) {
                query.setParameter("relatedLink", archiveQuery.getRelated());
            }

            if (!startTimeContainsWildcard) {
                query.setParameter("startTime", archiveQuery.getStartTime().getValue());
            }

            if (!endTimeContainsWildcard) {
                query.setParameter("endTime", archiveQuery.getEndTime().getValue());
            }

            if (!providerURIContainsWildcard) {
                query.setParameter("providerURI", providerURIId);
            }

            if (!networkContainsWildcard) {
                query.setParameter("network", networkId);
            }

            if (!sourceTypeContainsWildcard) {
                query.setParameter("sourceLinkObjectTypeId", HelperCOM.generateSubKey(sourceLink.getObjectType()));
            }

            if (!sourceDomainContainsWildcard) {
                query.setParameter("sourceLinkDomainId", sourceLink.getDomainId());
            }

            if (!sourceObjIdContainsWildcard) {
                query.setParameter("sourceLinkObjId", sourceLink.getObjId());
            }

            Thread tThread = new Thread(new Runnable() { // Display a message in case the query gets stuck...
                @Override
                public void run() {
                    try {
                        this.wait(10 * 1000); // 10 seconds
                        Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "The query is taking longer than 10 seconds. The query might be too broad to be handled by the database.");
                    } catch (InterruptedException ex) {
                    } catch (IllegalMonitorStateException ex) {
                    }
                }
            });

            tThread.start();

            // BUG: The code will get stuck on the line below if the database is too big (tested with Derby)
            final List resultList = query.getResultList();
            tThread.interrupt();
            perObjs.addAll(resultList);
            dbBackend.closeEntityManager();

            return perObjs;
        }
    }

    public ArrayList<COMObjectEntity> query(final ObjectType objType,
            final ArchiveQuery archiveQuery, final Integer domainId,
            final Integer providerURIId, final Integer networkId,
            final SourceLinkContainer sourceLink) {
        this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
        final QueryCallable task = new QueryCallable(objType, archiveQuery,
                domainId, providerURIId, networkId, sourceLink);

        Future<ArrayList<COMObjectEntity>> future = executor.submit(task);

        try {
            return future.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void resetMainTable(final Callable task) {
        this.sequencialStoring.set(false); // Sequential stores can no longer happen otherwise we break order
        Future<Integer> nullValue = executor.submit(task);
        Logger.getLogger(BackendInteractionsProcessor.class.getName()).info("Reset table submitted!");

        try {
            Integer dummyInt = nullValue.get(); // Dummy code to Force a wait until the actual restart is done!
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).info("Reset table callback!");
        } catch (InterruptedException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BackendInteractionsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}