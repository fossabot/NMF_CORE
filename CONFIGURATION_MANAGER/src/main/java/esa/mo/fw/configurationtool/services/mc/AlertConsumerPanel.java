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
package esa.mo.fw.configurationtool.services.mc;

import esa.mo.com.impl.provider.ArchivePersistenceObject;
import esa.mo.com.impl.util.HelperArchive;
import esa.mo.tools.mowindow.MOWindow;
import esa.mo.helpertools.connections.ConfigurationProvider;
import esa.mo.mc.impl.consumer.AlertConsumerServiceImpl;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ccsds.moims.mo.com.structures.InstanceBooleanPair;
import org.ccsds.moims.mo.com.structures.InstanceBooleanPairList;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.ccsds.moims.mo.mc.alert.AlertHelper;
import org.ccsds.moims.mo.mc.alert.structures.AlertDefinitionDetails;
import org.ccsds.moims.mo.mc.alert.structures.AlertDefinitionDetailsList;
import org.ccsds.moims.mo.mc.structures.ArgumentDefinitionDetails;
import org.ccsds.moims.mo.mc.structures.ArgumentDefinitionDetailsList;
import org.ccsds.moims.mo.mc.structures.Severity;

/**
 *
 * @author Cesar Coelho
 */
public class AlertConsumerPanel extends javax.swing.JPanel {

    private AlertConsumerServiceImpl serviceMCAlert;
    private AlertTablePanel alertTable;
    private ConfigurationProvider configuration = new ConfigurationProvider();

    /**
     *
     * @param serviceMCAlert
     */
    public AlertConsumerPanel(AlertConsumerServiceImpl serviceMCAlert) {
        initComponents();

        alertTable = new AlertTablePanel(serviceMCAlert.getCOMServices().getArchiveService());
        jScrollPane2.setViewportView(alertTable);

        this.serviceMCAlert = serviceMCAlert;
        
        this.listDefinitionAllButtonActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the
     * formAddModifyParameter. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        actionDefinitionsTable = new javax.swing.JTable();
        parameterTab = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        enableDefinitionAllAgg = new javax.swing.JButton();
        enableDefinitionButtonAgg = new javax.swing.JButton();
        listDefinitionButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        addDefinitionButton = new javax.swing.JButton();
        updateDefinitionButton = new javax.swing.JButton();
        removeDefinitionButton = new javax.swing.JButton();
        listDefinitionAllButton = new javax.swing.JButton();
        removeDefinitionAllButton = new javax.swing.JButton();

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Alert Service");
        jLabel6.setToolTipText("");

        jScrollPane2.setHorizontalScrollBar(null);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(796, 380));
        jScrollPane2.setRequestFocusEnabled(false);

        actionDefinitionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null,  new Boolean(true), null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Obj Inst Id", "name", "description", "rawType", "rawUnit", "generationEnabled", "updateInterval"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        actionDefinitionsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        actionDefinitionsTable.setAutoscrolls(false);
        actionDefinitionsTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        actionDefinitionsTable.setMaximumSize(null);
        actionDefinitionsTable.setMinimumSize(null);
        actionDefinitionsTable.setPreferredSize(null);
        actionDefinitionsTable.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                actionDefinitionsTableComponentAdded(evt);
            }
        });
        jScrollPane2.setViewportView(actionDefinitionsTable);

        parameterTab.setLayout(new java.awt.GridLayout(2, 1));

        enableDefinitionAllAgg.setText("enableGeneration(group=false, 0)");
        enableDefinitionAllAgg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableDefinitionAllAggActionPerformed(evt);
            }
        });
        jPanel1.add(enableDefinitionAllAgg);

        enableDefinitionButtonAgg.setText("enableGeneration");
        enableDefinitionButtonAgg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableDefinitionButtonAggActionPerformed(evt);
            }
        });
        jPanel1.add(enableDefinitionButtonAgg);

        listDefinitionButton.setText("listDefinition()");
        listDefinitionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listDefinitionButtonActionPerformed(evt);
            }
        });
        jPanel1.add(listDefinitionButton);

        parameterTab.add(jPanel1);

        addDefinitionButton.setText("addDefinition");
        addDefinitionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDefinitionButtonActionPerformed(evt);
            }
        });
        jPanel5.add(addDefinitionButton);

        updateDefinitionButton.setText("updateDefinition");
        updateDefinitionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateDefinitionButtonActionPerformed(evt);
            }
        });
        jPanel5.add(updateDefinitionButton);

        removeDefinitionButton.setText("removeDefinition");
        removeDefinitionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDefinitionButtonActionPerformed(evt);
            }
        });
        jPanel5.add(removeDefinitionButton);

        listDefinitionAllButton.setText("listDefinition(\"*\")");
        listDefinitionAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listDefinitionAllButtonActionPerformed(evt);
            }
        });
        jPanel5.add(listDefinitionAllButton);

        removeDefinitionAllButton.setText("removeDefinition(0)");
        removeDefinitionAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDefinitionAllButtonActionPerformed(evt);
            }
        });
        jPanel5.add(removeDefinitionAllButton);

        parameterTab.add(jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(parameterTab, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 882, Short.MAX_VALUE)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parameterTab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listDefinitionButtonActionPerformed

        IdentifierList actionNames = new IdentifierList();
        MOWindow actionNamesWindow = new MOWindow(actionNames, true);

        try {
            LongList objIds;
            try {
                objIds = this.serviceMCAlert.getAlertStub().listDefinition((IdentifierList) actionNamesWindow.getObject());
            } catch (InterruptedIOException ex) {
                return;
            }

            String str = "Object instance identifiers on the provider: \n";
            for (Long objId : objIds) {
                str += objId.toString() + "\n";
            }

            JOptionPane.showMessageDialog(null, str, "Returned List from the Provider", JOptionPane.PLAIN_MESSAGE);

        } catch (MALInteractionException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_listDefinitionButtonActionPerformed

    private void addDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDefinitionButtonActionPerformed

        // Create and Show the Action Definition to the user
        AlertDefinitionDetails alertDefinition = new AlertDefinitionDetails();
        alertDefinition.setName(new Identifier("Alert1"));
        alertDefinition.setDescription("This Alert is generated 10 seconds after taking the picture.");
        alertDefinition.setSeverity(Severity.INFORMATIONAL);
        alertDefinition.setGenerationEnabled(true);

        ArgumentDefinitionDetails details = new ArgumentDefinitionDetails();
        details.setRawType((byte) 1);

        ArgumentDefinitionDetailsList detailsList = new ArgumentDefinitionDetailsList();
        detailsList.add(null);
        alertDefinition.setArguments(detailsList);
        alertDefinition.setArgumentIds(null);
        MOWindow alertDefinitionWindow = new MOWindow(alertDefinition, true);

        AlertDefinitionDetailsList alertDefinitionList = new AlertDefinitionDetailsList();
        try {
            alertDefinitionList.add((AlertDefinitionDetails) alertDefinitionWindow.getObject());
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            LongList objIds = this.serviceMCAlert.getAlertStub().addDefinition(alertDefinitionList);

            if (objIds.size() == 0) {
                return;
            }

            Thread.sleep(500);
            // Get the stored Action Definition from the Archive
            ArchivePersistenceObject comObject = HelperArchive.getArchiveCOMObject(this.serviceMCAlert.getCOMServices().getArchiveService().getArchiveStub(),
                    AlertHelper.ALERTDEFINITION_OBJECT_TYPE, serviceMCAlert.getConnectionDetails().getDomain(), objIds.get(0));

            // Add the Action Definition to the table
            alertTable.addEntry(comObject);

        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "There was an error with the submitted alert instance.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "There was an error with the submitted alert instance.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_addDefinitionButtonActionPerformed

    private void updateDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDefinitionButtonActionPerformed

        if (alertTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        ArchivePersistenceObject obj = alertTable.getSelectedCOMObject();
        MOWindow moObject = new MOWindow(obj.getObject(), true);

        LongList objIds = new LongList();
        objIds.add(alertTable.getSelectedObjId());

        AlertDefinitionDetailsList defs = new AlertDefinitionDetailsList();
        try {
            defs.add((AlertDefinitionDetails) moObject.getObject());
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            this.serviceMCAlert.getAlertStub().updateDefinition(objIds, defs);
            this.listDefinitionAllButtonActionPerformed(null);
        } catch (MALInteractionException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_updateDefinitionButtonActionPerformed

    private void removeDefinitionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDefinitionButtonActionPerformed

        if (alertTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }

        Long objId = alertTable.getSelectedObjId();
        LongList longlist = new LongList();
        longlist.add(objId);

        try {
            this.serviceMCAlert.getAlertStub().removeDefinition(longlist);
            alertTable.removeSelectedEntry();
        } catch (MALInteractionException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_removeDefinitionButtonActionPerformed

    private void listDefinitionAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listDefinitionAllButtonActionPerformed

        IdentifierList idList = new IdentifierList();
        idList.add(new Identifier("*"));

        LongList output;
        try {
            output = this.serviceMCAlert.getAlertStub().listDefinition(idList);
            alertTable.refreshTableWithIds(output, serviceMCAlert.getConnectionDetails().getDomain(), AlertHelper.ALERTDEFINITION_OBJECT_TYPE);
        } catch (MALInteractionException ex) {
            JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (MALException ex) {
            JOptionPane.showMessageDialog(null, "There was an error during the listDefinition operation.", "Error", JOptionPane.PLAIN_MESSAGE);
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.INFO, "listDefinition(\"*\") returned {0} object instance identifiers", output.size());

    }//GEN-LAST:event_listDefinitionAllButtonActionPerformed

    private void removeDefinitionAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDefinitionAllButtonActionPerformed

        Long objId = (long) 0;
        LongList longlist = new LongList();
        longlist.add(objId);

        try {
            this.serviceMCAlert.getAlertStub().removeDefinition(longlist);
            alertTable.removeAllEntries();
        } catch (MALInteractionException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_removeDefinitionAllButtonActionPerformed

    private void actionDefinitionsTableComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_actionDefinitionsTableComponentAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_actionDefinitionsTableComponentAdded

    private void enableDefinitionAllAggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableDefinitionAllAggActionPerformed

        Boolean curState;

        String str;
        if (alertTable.getSelectedRow() == -1) {  // Used to avoid problems if no row is selected
            AlertDefinitionDetails alertDefinition = (AlertDefinitionDetails) alertTable.getFirstCOMObject().getObject();
            if (alertDefinition != null) {
                curState = alertDefinition.getGenerationEnabled();
            } else {
                curState = true;
            }
        } else {
            curState = ((AlertDefinitionDetails) alertTable.getSelectedCOMObject().getObject()).getGenerationEnabled();
        }

        InstanceBooleanPairList BoolPairList = new InstanceBooleanPairList();
        BoolPairList.add(new InstanceBooleanPair((long) 0, !curState));  // Zero is the wildcard

        try {
            this.serviceMCAlert.getAlertStub().enableGeneration(false, BoolPairList);
            alertTable.switchEnabledstatusAll(!curState);
        } catch (MALInteractionException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_enableDefinitionAllAggActionPerformed

    private void enableDefinitionButtonAggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableDefinitionButtonAggActionPerformed

        if (alertTable.getSelectedRow() == -1) { // The row is not selected?
            return;  // Well, then nothing to be done here folks!
        }
/*
        Long objId = alertTable.getSelectedObjId();
        LongList longlist = new LongList();
        longlist.add(objId);
*/
        Boolean curState = ((AlertDefinitionDetails) alertTable.getSelectedCOMObject().getObject()).getGenerationEnabled();
        InstanceBooleanPairList BoolPairList = new InstanceBooleanPairList();
        BoolPairList.add(new InstanceBooleanPair((long) 0, !curState));  // Zero is the wildcard

        try {
            this.serviceMCAlert.getAlertStub().enableGeneration(false, BoolPairList);
            alertTable.switchEnabledstatus(!curState);
        } catch (MALInteractionException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(AlertConsumerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }//GEN-LAST:event_enableDefinitionButtonAggActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionDefinitionsTable;
    private javax.swing.JButton addDefinitionButton;
    private javax.swing.JButton enableDefinitionAllAgg;
    private javax.swing.JButton enableDefinitionButtonAgg;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton listDefinitionAllButton;
    private javax.swing.JButton listDefinitionButton;
    private javax.swing.JPanel parameterTab;
    private javax.swing.JButton removeDefinitionAllButton;
    private javax.swing.JButton removeDefinitionButton;
    private javax.swing.JButton updateDefinitionButton;
    // End of variables declaration//GEN-END:variables
}
