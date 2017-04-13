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
package esa.mo.nmf.groundmoadapter;

import esa.mo.nmf.NMFException;
import java.io.Serializable;
import org.ccsds.moims.mo.mc.structures.AttributeValueList;

/**
 * The SimpleCommandingInterface interface provides a simpler way to send
 * commands to a NanoSat MO Framework-based provider. Hides the complexity of
 * the MO services by providing direct Java data types injection.
 *
 * @author Cesar Coelho
 */
public interface SimpleCommandingInterface {

    /**
     * The setParameter method allows an external software entity to send Data
     * to the NanoSat MO Framework provider using the Parameter service. If
     * there is no parameter definition with the submitted name, the method
     * shall automatically create the parameter definition in the Parameter
     * service. Any sort of data can be exchanged as long as the content is
     * serializable.
     *
     * @param parameterName The name of the Parameter as set in the parameter
     * definition
     * @param content The content of the Parameter
     */
    void setParameter(String parameterName, Serializable content);

    /**
     * The addDataReceivedListener method allows an external software entity to
     * submit a DataReceivedListener in order to receive Data from the NanoSat
     * MO Framework provider using the Parameter service.
     *
     * @param dataReceivedListener The Listener where the data will be received
     */
    void addDataReceivedListener(DataReceivedListener dataReceivedListener);

    /**
     * The invokeAction method allows an external software entity to submit an
     * Action to the NanoSat MO Framework provider just by selecting the name of
     * the action and the respective implementer-specific data necessary for the
     * execution of that action.
     *
     * @param actionName The name of the Action to be executed
     * @param objects Implementer-specific data necessary to execute the action
     */
    void invokeAction(String actionName, Serializable[] objects);

    /**
     * The invokeAction method allows an external software entity to submit an
     * Action to the NanoSat MO Framework provider just by selecting the name of
     * the action and the respective implementer-specific data necessary for the
     * execution of that action.
     *
     * @param defInstId The object instance identifier of the ActionDefinition
     * @param argumentValues List containing the values of the arguments. The
     * ordering of the list matches that of the definition
     * @return The object instance identifier of the ActionInstance. This can be
     * used to track the action via the Activity Tracking service.
     * @throws NMFException in case something goes wrong
     */
    Long invokeAction(Long defInstId, AttributeValueList argumentValues) throws NMFException;

}
