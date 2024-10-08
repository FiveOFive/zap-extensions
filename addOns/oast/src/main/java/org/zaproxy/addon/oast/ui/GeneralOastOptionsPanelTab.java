/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.oast.ui;

import java.awt.GridBagConstraints;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.View;
import org.zaproxy.addon.oast.ExtensionOast;
import org.zaproxy.addon.oast.OastParam;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

/** Contains general options not specific to one OAST service. */
@SuppressWarnings("serial")
public class GeneralOastOptionsPanelTab extends OastOptionsPanelTab {

    private static final long serialVersionUID = 1L;
    private JComboBox<String> activeScanServices;
    private JCheckBox usePermanentDatabase;
    private ZapNumberSpinner daysToKeepSpinner;
    private JButton clearRecordsButton;
    private ExtensionOast ext;

    public GeneralOastOptionsPanelTab(ExtensionOast ext) {
        super(Constant.messages.getString("oast.options.general.title"));
        this.ext = ext;
        int rowIndex = -1;
        JLabel activeScanServiceLabel =
                new JLabel(Constant.messages.getString("oast.options.activeScanService"));
        activeScanServiceLabel.setLabelFor(getActiveScanServicesComboBox());
        JLabel daysToKeepLabel = new JLabel(Constant.messages.getString("oast.options.daysToKeep"));
        daysToKeepLabel.setLabelFor(getDaysToKeepSpinner());
        add(
                activeScanServiceLabel,
                LayoutHelper.getGBC(0, ++rowIndex, GridBagConstraints.RELATIVE, 1.0, 0));
        add(
                getActiveScanServicesComboBox(),
                LayoutHelper.getGBC(1, rowIndex, GridBagConstraints.REMAINDER, 1.0, 0));
        add(
                getUsePermanentDatabaseCheckbox(),
                LayoutHelper.getGBC(0, ++rowIndex, GridBagConstraints.REMAINDER, 1.0, 0));
        add(
                daysToKeepLabel,
                LayoutHelper.getGBC(0, ++rowIndex, GridBagConstraints.RELATIVE, 1.0, 0));
        add(
                getDaysToKeepSpinner(),
                LayoutHelper.getGBC(1, rowIndex, GridBagConstraints.REMAINDER, 1.0, 0));
        add(
                getClearRecordsButton(),
                LayoutHelper.getGBC(1, ++rowIndex, GridBagConstraints.REMAINDER, 1.0, 0));
        add(
                new JLabel(),
                LayoutHelper.getGBC(0, ++rowIndex, GridBagConstraints.REMAINDER, 1.0, 1.0));
    }

    @Override
    public void initParam(OptionsParam options) {
        final OastParam param = options.getParamSet(OastParam.class);
        getActiveScanServicesComboBox().setSelectedItem(param.getActiveScanServiceName());
        getUsePermanentDatabaseCheckbox().setSelected(param.isUsePermanentDatabase());
    }

    @Override
    public void saveParam(OptionsParam options) {
        final OastParam param = options.getParamSet(OastParam.class);
        param.setActiveScanServiceName(
                Optional.ofNullable(getActiveScanServicesComboBox().getSelectedItem())
                        .orElse(OastParam.NO_ACTIVE_SCAN_SERVICE_SELECTED_OPTION)
                        .toString());
        param.setUsePermanentDatabase(getUsePermanentDatabaseCheckbox().isSelected());
        int prevDays = param.getDaysToKeepRecords();
        int newDays = getDaysToKeepSpinner().getValue();
        param.setDaysToKeepRecords(newDays);
        if (newDays > 0 && newDays < prevDays) {
            ext.trimDatabase(newDays);
        }
    }

    private JComboBox<String> getActiveScanServicesComboBox() {
        if (activeScanServices == null) {
            ExtensionOast extOast =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionOast.class);
            Set<String> oastServices = new HashSet<>(extOast.getOastServices().keySet());
            oastServices.remove(extOast.getCallbackService().getName());
            activeScanServices = new JComboBox<>(oastServices.toArray(new String[0]));
            activeScanServices.insertItemAt(OastParam.NO_ACTIVE_SCAN_SERVICE_SELECTED_OPTION, 0);
            activeScanServices.setToolTipText(
                    Constant.messages.getString("oast.options.activeScanService.tooltip"));
        }
        return activeScanServices;
    }

    private JCheckBox getUsePermanentDatabaseCheckbox() {
        if (usePermanentDatabase == null) {
            usePermanentDatabase =
                    new JCheckBox(Constant.messages.getString("oast.options.usePermanentDatabase"));
            usePermanentDatabase.setToolTipText(
                    Constant.messages.getString("oast.options.usePermanentDatabase.tooltip"));
        }
        return usePermanentDatabase;
    }

    private ZapNumberSpinner getDaysToKeepSpinner() {
        if (daysToKeepSpinner == null) {
            daysToKeepSpinner = new ZapNumberSpinner(0, 45, Integer.MAX_VALUE);
            daysToKeepSpinner.setToolTipText(
                    Constant.messages.getString("oast.options.daysToKeep.tooltip"));
        }
        return daysToKeepSpinner;
    }

    private JButton getClearRecordsButton() {
        if (clearRecordsButton == null) {
            clearRecordsButton =
                    new JButton(Constant.messages.getString("oast.options.clearRecords"));
            clearRecordsButton.addActionListener(
                    l -> {
                        if (View.getSingleton()
                                        .showConfirmDialog(
                                                GeneralOastOptionsPanelTab.this,
                                                Constant.messages.getString(
                                                        "oast.options.clearRecords.confirm"))
                                == JOptionPane.OK_OPTION) {
                            ext.clearAllRecords();
                        }
                    });
        }
        return clearRecordsButton;
    }
}
