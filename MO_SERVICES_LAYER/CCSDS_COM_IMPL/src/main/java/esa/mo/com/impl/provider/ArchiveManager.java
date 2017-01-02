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
package esa.mo.com.impl.provider;

import esa.mo.com.impl.archive.db.BackendInteractionsProcessor;
import esa.mo.com.impl.archive.db.FastObjId;
import esa.mo.com.impl.archive.db.FastDomain;
import esa.mo.com.impl.util.HelperCOM;
import esa.mo.helpertools.connections.ConfigurationProvider;
import esa.mo.helpertools.helpers.HelperAttributes;
import esa.mo.com.impl.archive.db.DatabaseBackend;
import esa.mo.com.impl.archive.db.FastNetwork;
import esa.mo.com.impl.archive.db.FastProviderURI;
import esa.mo.com.impl.archive.db.SourceLinkContainer;
import esa.mo.com.impl.archive.entities.COMObjectEntity;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.com.archive.ArchiveHelper;
import org.ccsds.moims.mo.com.archive.structures.ArchiveDetails;
import org.ccsds.moims.mo.com.archive.structures.ArchiveDetailsList;
import org.ccsds.moims.mo.com.archive.structures.ArchiveQuery;
import org.ccsds.moims.mo.com.archive.structures.CompositeFilter;
import org.ccsds.moims.mo.com.archive.structures.CompositeFilterList;
import org.ccsds.moims.mo.com.archive.structures.CompositeFilterSet;
import org.ccsds.moims.mo.com.archive.structures.ExpressionOperator;
import org.ccsds.moims.mo.com.structures.ObjectDetails;
import org.ccsds.moims.mo.com.structures.ObjectId;
import org.ccsds.moims.mo.com.structures.ObjectIdList;
import org.ccsds.moims.mo.com.structures.ObjectKey;
import org.ccsds.moims.mo.com.structures.ObjectType;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.structures.Attribute;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Composite;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.mal.structures.ElementList;
import org.ccsds.moims.mo.mal.structures.Enumeration;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.UIntegerList;
import org.ccsds.moims.mo.mal.structures.URI;

/**
 *
 * @author Cesar Coelho
 */
public class ArchiveManager {

    private final DatabaseBackend dbBackend;
    private final BackendInteractionsProcessor dbProcessor;

    private final FastDomain fastDomain;
    private final FastNetwork fastNetwork;
    private final FastProviderURI fastProviderURI;
    private final FastObjId fastObjId;

    private EventProviderServiceImpl eventService;
    private final ConfigurationProvider configuration = new ConfigurationProvider();

//    private final LinkedBlockingQueue<ArrayList<ArchivePersistenceObject>> storeQueue;
//    private Thread storingThread;
    /**
     * Initializes the Archive manager
     *
     * @param eventService
     */
    public ArchiveManager(EventProviderServiceImpl eventService) {
        this.eventService = eventService;

        try {
            ArchiveHelper.init(MALContextFactory.getElementFactoryRegistry());
        } catch (MALException ex) {
        }

//        this.storeQueue = new LinkedBlockingQueue<ArrayList<ArchivePersistenceObject>>();
        this.dbBackend = new DatabaseBackend();
        this.dbProcessor = new BackendInteractionsProcessor(dbBackend);

        // Start the separate lists for the "fast" generation of objIds
        this.fastDomain = new FastDomain(dbBackend);
        this.fastNetwork = new FastNetwork(dbBackend);
        this.fastProviderURI = new FastProviderURI(dbBackend);
        this.fastObjId = new FastObjId(dbBackend);
    }

    public void init() {
        this.dbBackend.startBackendDatabase();
        this.fastDomain.init();
        this.fastNetwork.init();
        this.fastProviderURI.init();

        /*
        storingThread = new Thread() {
            @Override
            public void run() {
                this.setName("ArchiveManager_StoringThread");
                ArrayList<ArchivePersistenceObject> perObjs;
                ArrayList<ArchivePersistenceObject> all;

                while (true) {
                    all = new ArrayList<ArchivePersistenceObject>();
                    
                    try {
                        perObjs = storeQueue.take();
                        all.addAll(perObjs);

                        dbBackend.createEntityManager();  // 0.166 ms

                        for (int i = 0; i < 2; i++) {  // Give it two tries (will lock the db for a limited time)

//                long startTime = System.currentTimeMillis();
                            dbBackend.getEM().getTransaction().begin(); // 0.480 ms

//                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Time 1: " + (System.currentTimeMillis() - startTime));
                            while (perObjs != null) {
                                persistObjects(perObjs); // store

                                perObjs = storeQueue.poll(); // get next if there is one available

                                if (perObjs != null) {
                                    all.addAll(perObjs);  // Make it ready to be persisted
                                }
                            }
                            
//                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Time 2: " + (System.currentTimeMillis() - startTime));
                            dbBackend.safeCommit();

                            if(i == 0){  // Only works for the first iteration of the for loop
                                perObjs = storeQueue.poll(); // get next if there is one available

                                if (perObjs != null) {
                                    all.addAll(perObjs);  // Make it ready to be persisted
                                }
                                
                            }

                        }

//                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Time 3: " + (System.currentTimeMillis() - startTime));
                        dbBackend.closeEntityManager(); // 0.410 ms
//                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Time 4: " + (System.currentTimeMillis() - startTime));

                        // Generate and Publish the Events - requirement: 3.4.2.1
//                        generateAndPublishEvents(ArchiveHelper.OBJECTSTORED_OBJECT_TYPE, all, interaction);
                        generateAndPublishEvents(ArchiveHelper.OBJECTSTORED_OBJECT_TYPE, all, null);
//                Logger.getLogger(ArchiveManager.class.getName()).log(Level.INFO, "Time 5: " + (System.currentTimeMillis() - startTime));

                    } catch (InterruptedException e) {
                        Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        };

        storingThread.start();
         */
    }

    protected void setEventService(EventProviderServiceImpl eventService) {
        this.eventService = eventService;
    }

    private class ResetMainTableRunnable implements Callable {

        @Override
        public Integer call() {
            dbBackend.createEntityManager();
            dbBackend.getEM().getTransaction().begin();
            dbBackend.getEM().createQuery("DELETE FROM COMObjectEntity").executeUpdate();
            dbBackend.getEM().getTransaction().commit();

            fastObjId.resetFastIDs();
            fastDomain.resetFastDomain();
            fastNetwork.resetFastNetwork();
            fastProviderURI.resetFastProviderURI();

            dbBackend.getEM().close();
            dbBackend.restartEMF();

            return null;
        }
    }

    /**
     * Needs to be synchronized with the insertEntries method because the fast
     * objects are being called simultaneously. The Testbeds don't pass without 
     * the synchronization.
     * 
     */
    protected synchronized void resetTable() {
        Logger.getLogger(ArchiveProviderServiceImpl.class.getName()).info("Reset table triggered!");
        this.dbProcessor.resetMainTable(new ResetMainTableRunnable());
        /*
        this.fastObjId.resetFastIDs();
        this.fastDomain.resetFastDomain();
        this.fastNetwork.resetFastNetwork();
        this.fastProviderURI.resetFastProviderURI();

        dbBackend.getEM().close();

        dbBackend.restartEMF();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }

    protected ArchivePersistenceObject getPersistenceObject(final ObjectType objType, final IdentifierList domain, final Long objId) {
        Integer domainId = this.fastDomain.getDomainId(domain);
        COMObjectEntity comEntity = this.dbProcessor.getCOMObject(objType, domainId, objId);

        if (comEntity == null) {
            return null;
        }

        return this.convert2ArchivePersistenceObject(comEntity, domain, objId);
    }

    private ArchivePersistenceObject convert2ArchivePersistenceObject(COMObjectEntity comEntity, IdentifierList domain, Long objId) {
        Identifier network = null;
        URI providerURI = null;

        try {
            network = this.fastNetwork.getNetwork(comEntity.getNetwork());
            providerURI = this.fastProviderURI.getProviderURI(comEntity.getProviderURI());
        } catch (Exception ex) {
            Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        SourceLinkContainer sourceLink = comEntity.getSourceLink();
        ObjectId objectId = null;

        if (sourceLink.getObjectType() != null
                || sourceLink.getDomainId() != null
                || sourceLink.getObjId() != null) {
            try {
                ObjectKey ok = new ObjectKey(this.fastDomain.getDomain(sourceLink.getDomainId()), sourceLink.getObjId());
                objectId = new ObjectId(sourceLink.getObjectType(), ok);
            } catch (Exception ex) {
                Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ArchiveDetails archiveDetails = new ArchiveDetails(
                comEntity.getObjectId(),
                new ObjectDetails(comEntity.getRelatedLink(), objectId),
                network,
                comEntity.getTimestamp(),
                providerURI);

        return new ArchivePersistenceObject(comEntity.getObjectType(), domain,
                objId, archiveDetails, comEntity.getObject());
    }

    protected Object getObject(final ObjectType objType, final IdentifierList domain, final Long objId) {
        return this.getPersistenceObject(objType, domain, objId).getObject();
    }

    protected ArchiveDetails getArchiveDetails(final ObjectType objType, final IdentifierList domain, final Long objId) {
        return this.getPersistenceObject(objType, domain, objId).getArchiveDetails();
    }

    protected Boolean objIdExists(final ObjectType objType, final IdentifierList domain, final Long objId) {
        return (this.getPersistenceObject(objType, domain, objId) != null);
    }

    protected LongList getAllObjIds(final ObjectType objType, final IdentifierList domain) {
        return this.dbProcessor.getAllCOMObjects(objType, this.fastDomain.getDomainId(domain));
    }

    private SourceLinkContainer createSourceContainerFromObjectId(ObjectId source) {
        Integer sourceDomainId = null;
        ObjectType sourceObjectType = null;
        Long sourceObjId = null;

        if (source != null) {
            final IdentifierList sourceDomain = source.getKey().getDomain();
            if (sourceDomain != null) {
                sourceDomainId = this.fastDomain.getDomainId(sourceDomain);
            }

            sourceObjectType = source.getType();
            sourceObjId = source.getKey().getInstId();
        }

        return new SourceLinkContainer(sourceObjectType, sourceDomainId, sourceObjId);
    }

    protected synchronized LongList insertEntries(final ObjectType objType, final IdentifierList domain,
            ArchiveDetailsList lArchiveDetails, final ElementList objects, final MALInteraction interaction) {
        final LongList outIds = new LongList();
        final ArrayList<COMObjectEntity> perObjsEntities = new ArrayList<COMObjectEntity>();
        final int domainId = this.fastDomain.getDomainId(domain);

        // Generate the object Ids if needed and the persistence objects to be stored
        for (int i = 0; i < lArchiveDetails.size(); i++) {
            final Long objId = this.fastObjId.getUniqueObjId(objType, domainId, lArchiveDetails.get(i).getInstId());
            final int providerURIId = this.fastProviderURI.getProviderURIId(lArchiveDetails.get(i).getProvider());
            final int networkId = this.fastNetwork.getNetworkId(lArchiveDetails.get(i).getNetwork());
            final SourceLinkContainer sourceLink = this.createSourceContainerFromObjectId(lArchiveDetails.get(i).getDetails().getSource());

            // If there are no objects in the list, inject null...
            final Object objBody = (objects == null) ? null : ((objects.get(i) == null) ? null : objects.get(i));

            final COMObjectEntity perObjEntity = new COMObjectEntity(objType,
                    domainId,
                    objId,
                    lArchiveDetails.get(i).getTimestamp().getValue(),
                    providerURIId,
                    networkId,
                    sourceLink,
                    lArchiveDetails.get(i).getDetails().getRelated(),
                    objBody);

            perObjsEntities.add(perObjEntity);
            outIds.add(objId);
        }

        Thread publishEvents = new Thread() {
            @Override
            public void run() {
                // Generate and Publish the Events - requirement: 3.4.2.1
                generateAndPublishEvents(ArchiveHelper.OBJECTSTORED_OBJECT_TYPE, 
                        ArchiveManager.generateSources(objType, domain, outIds), 
                        interaction);
            }
        };

        this.dbProcessor.insert(perObjsEntities, publishEvents);

        return outIds;
    }

    protected void updateEntries(final ObjectType objType, final IdentifierList domain,
            final ArchiveDetailsList lArchiveDetails, final ElementList objects, final MALInteraction interaction) {
        final int domainId = this.fastDomain.getDomainId(domain);
        final ArrayList<COMObjectEntity> newObjs = new ArrayList<COMObjectEntity>();
        final LongList objIds = new LongList();

        // Generate the object Ids if needed and the persistence objects to be stored
        for (int i = 0; i < lArchiveDetails.size(); i++) {
            final Integer providerURIId = this.fastProviderURI.getProviderURIId(lArchiveDetails.get(i).getProvider());
            final Integer networkId = this.fastNetwork.getNetworkId(lArchiveDetails.get(i).getNetwork());

            // If there are no objects in the list, inject null...
            Object objBody = (objects == null) ? null : ((objects.get(i) == null) ? null : objects.get(i));

            SourceLinkContainer sourceLink = this.createSourceContainerFromObjectId(lArchiveDetails.get(i).getDetails().getSource());

            final COMObjectEntity newObj = new COMObjectEntity(objType,
                    domainId,
                    lArchiveDetails.get(i).getInstId(),
                    lArchiveDetails.get(i).getTimestamp().getValue(),
                    providerURIId,
                    networkId,
                    sourceLink,
                    lArchiveDetails.get(i).getDetails().getRelated(),
                    objBody); // 0.170 ms

            newObjs.add(newObj);
            objIds.add(lArchiveDetails.get(i).getInstId());
        }

        Thread publishEvents = new Thread() {
            @Override
            public void run() {
                // Generate and Publish the Events - requirement: 3.4.2.1
                generateAndPublishEvents(ArchiveHelper.OBJECTUPDATED_OBJECT_TYPE, 
                        ArchiveManager.generateSources(objType, domain, objIds), 
                        interaction);
            }
        };

        this.dbProcessor.update(newObjs, publishEvents);
    }

    protected LongList removeEntries(final ObjectType objType, final IdentifierList domain,
            final LongList objIds, final MALInteraction interaction) {
        final int domainId = this.fastDomain.getDomainId(domain);

        Thread publishEvents = new Thread() {
            @Override
            public void run() {
                // Generate and Publish the Events - requirement: 3.4.2.1
                generateAndPublishEvents(ArchiveHelper.OBJECTDELETED_OBJECT_TYPE, 
                        ArchiveManager.generateSources(objType, domain, objIds), 
                        interaction);
            }
        };

        this.dbProcessor.remove(objType, domainId, objIds, publishEvents);
        this.fastObjId.delete(objType, domainId);

        return objIds;
    }

    protected ArrayList<ArchivePersistenceObject> query(final ObjectType objType, final ArchiveQuery archiveQuery) {
        boolean domainContainsWildcard = HelperCOM.domainContainsWildcard(archiveQuery.getDomain());
        Integer domainId = (!domainContainsWildcard) ? this.fastDomain.getDomainId(archiveQuery.getDomain()) : null;
        Integer providerURIId = (archiveQuery.getProvider() != null) ? this.fastProviderURI.getProviderURIId(archiveQuery.getProvider()) : null;
        Integer networkId = (archiveQuery.getNetwork() != null) ? this.fastNetwork.getNetworkId(archiveQuery.getNetwork()) : null;
        SourceLinkContainer sourceLink = this.createSourceContainerFromObjectId(archiveQuery.getSource());

        ArrayList<COMObjectEntity> perObjs = this.dbProcessor.query(objType, archiveQuery, domainId, providerURIId, networkId, sourceLink);

        // Add domain filtering by subpart
        if (archiveQuery.getDomain() != null) {
            if (domainContainsWildcard) {  // It does contain a wildcard
                perObjs = this.filterByDomainSubpart(perObjs, archiveQuery.getDomain());
            }
        }

        // If objectType contains a wildcard then we have to filter them
        if (ArchiveManager.objectTypeContainsWildcard(objType)) {
            perObjs = ArchiveManager.filterByObjectIdMask(perObjs, objType, false);
        }

        // Source field
        if (archiveQuery.getSource() != null) {
            if (ArchiveManager.objectTypeContainsWildcard(archiveQuery.getSource().getType())
                    || HelperCOM.domainContainsWildcard(archiveQuery.getSource().getKey().getDomain())
                    || archiveQuery.getSource().getKey().getInstId() == 0) { // Any Wildcards?
                // objectType filtering   (in the source link)
                if (ArchiveManager.objectTypeContainsWildcard(archiveQuery.getSource().getType())) {
                    perObjs = ArchiveManager.filterByObjectIdMask(perObjs, archiveQuery.getSource().getType(), true);
                }

                // Add domain filtering by subpart  (in the source link)
                if (HelperCOM.domainContainsWildcard(archiveQuery.getSource().getKey().getDomain())) {  // Does it contain a wildcard?
                    perObjs = this.filterByDomainSubpart(perObjs, archiveQuery.getSource().getKey().getDomain());
                }
            }
        }

        // Convert to ArchivePersistenceObject
        ArrayList<ArchivePersistenceObject> outs = new ArrayList<ArchivePersistenceObject>();
        IdentifierList domain;

        for (COMObjectEntity perObj : perObjs) {
            try {
                domain = this.fastDomain.getDomain(perObj.getDomainId());
                ArchivePersistenceObject out = this.convert2ArchivePersistenceObject(perObj, domain, perObj.getObjectId());
                outs.add(out);
            } catch (Exception ex) {
                Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return outs;
    }

    private static ArrayList<COMObjectEntity> filterByObjectIdMask(
            final ArrayList<COMObjectEntity> perObjs,
            final ObjectType objectTypeMask, final boolean isSource) {
        final long bitMask = ArchiveManager.objectType2Mask(objectTypeMask);
        final long objTypeId = HelperCOM.generateSubKey(objectTypeMask);

        final ArrayList<COMObjectEntity> tmpPerObjs = new ArrayList<COMObjectEntity>();
        long tmpObjectTypeId;
        long objTypeANDed;
        for (COMObjectEntity perObj : perObjs) {
            tmpObjectTypeId = (isSource)
                    ? HelperCOM.generateSubKey(perObj.getSourceLink().getObjectType())
                    : perObj.getObjectTypeId();
            objTypeANDed = (tmpObjectTypeId & bitMask);
            if (objTypeANDed == objTypeId) { // Comparison
                tmpPerObjs.add(perObj);
            }
        }

        return tmpPerObjs;  // Assign new filtered list and discard old one
    }

    private ArrayList<COMObjectEntity> filterByDomainSubpart(
            final ArrayList<COMObjectEntity> perObjs, final IdentifierList wildcardDomain) {
        ArrayList<COMObjectEntity> tmpPerObjs = new ArrayList<COMObjectEntity>();
        IdentifierList tmpDomain;

        for (COMObjectEntity perObj : perObjs) {
            try {
                tmpDomain = this.fastDomain.getDomain(perObj.getDomainId());
                if (HelperCOM.domainMatchesWildcardDomain(tmpDomain, wildcardDomain)) {  // Does the domain matches the wildcard?
                    tmpPerObjs.add(perObj);
                }
            } catch (Exception ex) {
                Logger.getLogger(ArchiveManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return tmpPerObjs;  // Assign new filtered list and discard old one
    }

    protected static ArrayList<ArchivePersistenceObject> filterQuery(
            final ArrayList<ArchivePersistenceObject> perObjs, 
            final CompositeFilterSet filterSet) throws MALInteractionException {
        if (filterSet == null) {
            return perObjs;
        }

        final CompositeFilterList compositeFilterList = filterSet.getFilters();
        ArrayList<ArchivePersistenceObject> outPerObjs = perObjs;
        ArrayList<ArchivePersistenceObject> tmpPerObjs;
        Object obj;

        // Cycle the Filters
        for (CompositeFilter compositeFilter : compositeFilterList) {
            tmpPerObjs = new ArrayList<ArchivePersistenceObject>();

            if (compositeFilter == null) {
                continue;
            }

            // Cycle the objects
            for (ArchivePersistenceObject outPerObj : outPerObjs) {
                obj = outPerObj.getObject();

                // Check if Composite Filter is valid
                if (!ArchiveManager.isCompositeFilterValid(compositeFilter, obj)) {
                    throw new IllegalArgumentException();
                }

                // Requirement from the Composite filter: page 57:
                // For the dots: "If a field is nested, it can use the dot to separate"
                try {
                    obj = HelperCOM.getNestedObject(obj, compositeFilter.getFieldName());
                } catch (NoSuchFieldException ex) {
                    // requirement from the Composite filter: page 57
                    // "If the field does not exist in the Composite then the filter shall evaluate to false."
                    continue;
                }

                Element leftHandSide = (Element) HelperAttributes.javaType2Attribute(obj);
                Boolean evaluation = HelperCOM.evaluateExpression(leftHandSide, compositeFilter.getType(), compositeFilter.getFieldValue());

                if (evaluation == null) {
                    continue;
                }

                if (evaluation) {
                    tmpPerObjs.add(outPerObj);
                }
            }

            outPerObjs = tmpPerObjs;
        }

        return outPerObjs;
    }

    private static ObjectIdList generateSources(final ObjectType objType,
            final IdentifierList domain, final LongList objIds) {
        final ObjectIdList sourceList = new ObjectIdList();

        for (int i = 0; i < objIds.size(); i++) {
            final ObjectId source = new ObjectId(objType, new ObjectKey(domain, objIds.get(i)));

            // Is the COM Object an Event coming from the archive?
            if (source.getType().equals(HelperCOM.generateCOMObjectType(ArchiveHelper.ARCHIVE_SERVICE, source.getType().getNumber()))) {
                continue; // requirement: 3.4.2.5
            }

            sourceList.add(source);
        }

        return sourceList;
    }

    private void generateAndPublishEvents(final ObjectType objType,
            final ObjectIdList sourceList, final MALInteraction interaction) {
        if (eventService == null) {
            return;
        }

        if (sourceList.isEmpty()) { // Don't store anything if the list is empty...
            return;
        }

        Logger.getLogger(ArchiveManager.class.getName()).log(Level.FINE, "\nobjType: " + objType.toString() + "\nDomain: " + configuration.getDomain().toString() + "\nSourceList: " + sourceList.toString());

        // requirement: 3.4.2.4
        final LongList eventObjIds = eventService.generateAndStoreEvents(objType, configuration.getDomain(), null, sourceList, interaction);
        Logger.getLogger(ArchiveManager.class.getName()).log(Level.FINE, "The eventObjIds are: " + eventObjIds.toString());

        URI sourceURI = new URI("");

        if (interaction != null) {
            if (interaction.getMessageHeader() != null) {
                sourceURI = interaction.getMessageHeader().getURITo();
            }
        }

        eventService.publishEvents(sourceURI, eventObjIds, objType, null, sourceList, null);
    }

    protected static ObjectId archivePerObj2source(final ArchivePersistenceObject obj) {
        return new ObjectId(obj.getObjectType(), new ObjectKey(obj.getDomain(), obj.getObjectId()));
    }

    public static Boolean objectTypeContainsWildcard(final ObjectType objType) {
        return (objType.getArea().getValue() == 0
                || objType.getService().getValue() == 0
                || objType.getVersion().getValue() == 0
                || objType.getNumber().getValue() == 0);
    }

    private static Long objectType2Mask(final ObjectType objType) {
        long areaVal = (objType.getArea().getValue() == 0) ? (long) 0 : (long) 0xFFFF;
        long serviceVal = (objType.getService().getValue() == 0) ? (long) 0 : (long) 0xFFFF;
        long versionVal = (objType.getVersion().getValue() == 0) ? (long) 0 : (long) 0xFF;
        long numberVal = (objType.getNumber().getValue() == 0) ? (long) 0 : (long) 0xFFFF;

        return (new Long(areaVal << 48)
                | new Long(serviceVal << 32)
                | new Long(versionVal << 24)
                | new Long(numberVal));
    }

    public static UIntegerList checkForDuplicates(ArchiveDetailsList archiveDetailsList) {
        UIntegerList dupList = new UIntegerList();

        for (int i = 0; i < archiveDetailsList.size() - 1; i++) {
            if (archiveDetailsList.get(i).getInstId().intValue() == 0) { // Wildcard? Then jump over it
                continue;
            }

            for (int j = i + 1; j < archiveDetailsList.size(); j++) {
                if (archiveDetailsList.get(i).getInstId().intValue() == archiveDetailsList.get(j).getInstId().intValue()) {
                    dupList.add(new UInteger(j));
                }
            }
        }

        return dupList;
    }

    public static boolean isCompositeFilterValid(CompositeFilter compositeFilter, Object obj) {
        if (compositeFilter.getFieldName().contains("\\.")) {  // Looking into a nested field?
            if (!(obj instanceof Composite)) {
                return false;  // If it is not a composite, we can not check fields inside...
            } else {
                try { // Does the Field asked for, exists?
                    HelperCOM.getNestedObject(obj, compositeFilter.getFieldName());
                } catch (NoSuchFieldException ex) {
                    return false;
                }
            }
        }

        ExpressionOperator expressionOperator = compositeFilter.getType();

        if (compositeFilter.getFieldValue() == null) {
            if (expressionOperator.equals(ExpressionOperator.CONTAINS)
                    || expressionOperator.equals(ExpressionOperator.ICONTAINS)
                    || expressionOperator.equals(ExpressionOperator.GREATER)
                    || expressionOperator.equals(ExpressionOperator.GREATER_OR_EQUAL)
                    || expressionOperator.equals(ExpressionOperator.LESS)
                    || expressionOperator.equals(ExpressionOperator.LESS_OR_EQUAL)) {
                return false;
            }
        }

        if (obj instanceof Enumeration) {
            Attribute fieldValue = compositeFilter.getFieldValue();
//            if (!(fieldValue instanceof UInteger) || !(fieldValue.getTypeShortForm() == 11) ) {
            if (!(fieldValue instanceof UInteger)) {
                return false;
            }
        }

        if (obj instanceof Blob) {
            if (!(expressionOperator.equals(ExpressionOperator.EQUAL))
                    && !(expressionOperator.equals(ExpressionOperator.DIFFER))) {
                return false;
            }
        }

        if (expressionOperator.equals(ExpressionOperator.CONTAINS)
                || expressionOperator.equals(ExpressionOperator.ICONTAINS)) {
            if (compositeFilter.getFieldValue().getTypeShortForm() != 15) {  // Is it String?
                return false;
            }
        }

        return true;
    }

}