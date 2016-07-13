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
package esa.mo.nanosatmoframework.interfaces;

import java.io.IOException;
import java.io.Serializable;
import org.ccsds.moims.mo.mc.structures.ArgumentValueList;

/**
 * The SimpleMonitorAndControlListener interface provides a simpler way to
 * send commands to a consumer using the NanoSat MO Framework. Hides the
 * complexity of the MO services Back-End by providing direct Java data types
 * injection.
 * 
 */
public interface SimpleMonitoringInterface {
    
    /**
     * Reports the execution of the current action progress stage
     *
     * @param success Flag stating the successfulness of the stage
     * @param errorNumber Error number code. The interpretation of the 
     * value is left to the implementer. If the success flag is set to false, 
     * this field will not be used.
     * @param progressStage The progress stage. The first stage is represented as 1.
     * @param totalNumberOfProgressStages The total number of progress stages.
     * @param actionInstId The action instance identifier. This value allows
     * the consumer to match the action instance that initiated the action.
     * @throws java.io.IOException
     */
    public void reportActionExecutionProgress(final boolean success, final int errorNumber,
            final int progressStage, final int totalNumberOfProgressStages, final long actionInstId) throws IOException;
    
    /**
     * The publishAlertEvent operation allows an external software entity to
     * publish Alert events through the Alert service
     *
     * @param alertDefinitionName The Alert Definition name
     * @param argumentValues The argument values to be published, the complete
     * list can be replaced with a null.
     * @return Returns the object instance identifier of the published event.
     * If there is any error, then a null shall be returned instead
     * @throws java.io.IOException
     */
    public Long publishAlertEvent(final String alertDefinitionName, final ArgumentValueList argumentValues) throws IOException;
    
    /**
     * The pushParameterValue operation allows an external software 
     * entity to push Attribute values through the monitorValue operation of
     * the Parameter service. If there is no parameter definition with the
     * submitted name, the method shall automatically create the parameter
     * definition in the Parameter service.
     *
     * @param name The name of the Parameter as set in the parameter definition
     * @param content The value of the parameter to be pushed 
     * @return Returns the flag reporting if the push was successful
     * @throws java.io.IOException
     */
    public Boolean pushParameterValue(final String name, final Serializable content) throws IOException;
    
    
}
