package edu.ualberta.med.biobank.forms.reports;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.widgets.TopContainerListWidget;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class ContainerReport2Editor extends ReportsEditor {
    private static final I18n i18n = I18nFactory
        .getI18n(ContainerReport2Editor.class);

    @SuppressWarnings("nls")
    public static String ID =
        "edu.ualberta.med.biobank.editors.ContainerEmptyLocationsEditor";

    private BgcBaseText containerLabel;
    private TopContainerListWidget topContainers;
    private final IObservableValue listStatus = new WritableValue(
        Boolean.FALSE,
        Boolean.class);

    @Override
    protected void initReport() {
        List<Object> params = new ArrayList<Object>();
        params.add(containerLabel.getText());
        report.setContainerList(ReportsEditor
            .containerIdsToString(topContainers.getSelectedContainerIds()));
        report.setParams(params);
    }

    @SuppressWarnings("nls")
    @Override
    protected void createOptionSection(Composite parameterSection) {
        containerLabel = createCustomText(
            // label
            i18n.tr("Container Label"),
            parameterSection);
        topContainers = new TopContainerListWidget(parameterSection, toolkit);
        widgetCreator.addBooleanBinding(new WritableValue(Boolean.FALSE,
            Boolean.class), listStatus,
            // validation error message
            i18n.tr("Top Container List Empty"));
        topContainers.addSelectionChangedListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listStatus.setValue(topContainers.getEnabled());
            }
        });
    }

    protected BgcBaseText createCustomText(String labelText, Composite parent) {
        final BgcBaseText widget = (BgcBaseText) widgetCreator
            .createLabelledWidget(parent, BgcBaseText.class, SWT.NONE,
                labelText, StringUtil.EMPTY_STRING);
        widget.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.CR) {
                    validate(widget.getText());
                }
            }
        });
        widget.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.keyCode == SWT.TAB) {
                    validate(widget.getText());
                }
            }

        });
        widget.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                topContainers.setEnabled(false);
            }
        });
        return widget;
    }

    @SuppressWarnings("nls")
    protected void validate(String label) {
        try {
            if (label.isEmpty()
                || ContainerWrapper.getContainersByLabel(
                    SessionManager.getAppService(), label).size() > 0)
                filterList(label);
            else {
                throw new ApplicationException();
            }
        } catch (ApplicationException e) {
            BgcPlugin.openAsyncError(
                // dialog title
                i18n.tr("Invalid label"),
                // dialog message
                i18n.tr("No container labelled {0}.", label));
        }

    }

    protected void filterList(String text) {
        topContainers.filterBy(text);
        page.layout(true, true);
    }

    @SuppressWarnings("nls")
    @Override
    protected String[] getColumnNames() {
        return new String[] {
            // table column name
            i18n.tr("Location"),
            // table column name
            i18n.tr("Pallet Type") };
    }

    @SuppressWarnings("nls")
    @Override
    protected List<String> getParamNames() {
        List<String> paramNames = new ArrayList<String>();
        paramNames.add(
            // label
            i18n.tr("Container Label"));
        paramNames.add(
            // label
            i18n.tr("Top Containers"));
        return paramNames;
    }

    @Override
    protected List<Object> getPrintParams() {
        List<Object> params = new ArrayList<Object>();
        params.add(containerLabel.getText());
        params.add(topContainers.getSelectedContainerNames());
        return params;
    }

    @Override
    public void setValues() throws Exception {
        containerLabel.setText(StringUtil.EMPTY_STRING);
        topContainers.reset();
        validate(StringUtil.EMPTY_STRING);
        super.setValues();
    }
}
