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
package esa.mo.sm.impl.consumer;

import esa.mo.com.impl.util.COMServicesConsumer;
import esa.mo.helpertools.misc.ConsumerServiceImpl;
import esa.mo.helpertools.connections.SingleConnectionDetails;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.com.COMHelper;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.consumer.MALConsumer;
import org.ccsds.moims.mo.softwaremanagement.SoftwareManagementHelper;
import org.ccsds.moims.mo.softwaremanagement.memorymanagement.MemoryManagementHelper;
import org.ccsds.moims.mo.softwaremanagement.memorymanagement.consumer.MemoryManagementStub;

/**
 *
 * @author Cesar Coelho
 */
public class MemoryManagementConsumerServiceImpl extends ConsumerServiceImpl {
    
    private MemoryManagementStub memoryManagementService = null;
    private COMServicesConsumer comServices;

    public COMServicesConsumer getCOMServices() {
        return comServices;
    }

    @Override
    public Object getStub() {
        return this.getMemoryManagementStub();
    }

    public MemoryManagementStub getMemoryManagementStub() {
        return this.memoryManagementService;
    }

    @Override
    public Object generateServiceStub(MALConsumer tmConsumer) {
        return new MemoryManagementStub(tmConsumer);
    }

    public MemoryManagementConsumerServiceImpl(SingleConnectionDetails connectionDetails, COMServicesConsumer comServices) throws MALException, MalformedURLException, MALInteractionException {

        if (MALContextFactory.lookupArea(MALHelper.MAL_AREA_NAME, MALHelper.MAL_AREA_VERSION) == null) {
            MALHelper.init(MALContextFactory.getElementFactoryRegistry());
        }

        if (MALContextFactory.lookupArea(COMHelper.COM_AREA_NAME, COMHelper.COM_AREA_VERSION) == null) {
            COMHelper.init(MALContextFactory.getElementFactoryRegistry());
        }

        if (MALContextFactory.lookupArea(SoftwareManagementHelper.SOFTWAREMANAGEMENT_AREA_NAME, SoftwareManagementHelper.SOFTWAREMANAGEMENT_AREA_VERSION) == null) {
            SoftwareManagementHelper.init(MALContextFactory.getElementFactoryRegistry());
        }

        try {
            MemoryManagementHelper.init(MALContextFactory.getElementFactoryRegistry());
        } catch (MALException ex) {
            // nothing to be done..
        }

        this.connectionDetails = connectionDetails;
        this.comServices = comServices;

        // Close old connection
        if (tmConsumer != null) {
            try {
                tmConsumer.close();
            } catch (MALException ex) {
                Logger.getLogger(MemoryManagementConsumerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        tmConsumer = connection.startService(
                this.connectionDetails.getProviderURI(),
                this.connectionDetails.getBrokerURI(),
                this.connectionDetails.getDomain(),
                MemoryManagementHelper.MEMORYMANAGEMENT_SERVICE);

        this.memoryManagementService = new MemoryManagementStub(tmConsumer);
    }

}
