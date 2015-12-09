// $Id: Import.java,v 1.90 2006/03/27 13:09:01 linus Exp $
// Copyright (c) 1996-2006 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.reveng;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.application.api.Configuration;
import org.argouml.application.api.PluggableImport;
import org.argouml.cognitive.Designer;
import org.argouml.i18n.Translator;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.ui.ProjectBrowser;
import org.argouml.ui.explorer.ExplorerEventAdaptor;
import org.argouml.uml.diagram.static_structure.ClassDiagramGraphModel;
import org.argouml.uml.diagram.static_structure.layout.ClassdiagramLayouter;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.util.logging.SimpleTimer;
import org.tigris.gef.base.Globals;

/**
 * This is the main class for all import classes.<p>
 *
 * It provides JPanels for tailoring the import run in the FileChooser.<p>
 *
 * The Import run is started by calling doFile(Project, File)<p>
 *
 * Supports recursive search in folder for all .java classes.<p>
 *
 * There are now 3 levels of detail for import:<p>
 *
 * <ol>
 *   <li> 0 - classifiers only
 *   <li> 1 - classifiers plus feature specifications
 *   <li> 2 - full import, feature detail (ie. operations with methods)
 * </ol>
 *
 * TODO: Add registration methods for new languages.
 *
 * @author Andreas Rueckert a_rueckert@gmx.net
 */
public class Import {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(Import.class);

    /**
     * Imported directory.
     */
    private String srcPath;

    /**
     * Create a interface to the current diagram.
     */
    private DiagramInterface diagramInterface;

    /**
     * Current language module.
     */
    private PluggableImport module;

    /**
     * keys are module name, values are PluggableImport instance.
     */
    private Hashtable modules;

    private JComponent configPanel;

    private JCheckBox descend;

    private JCheckBox changedOnly;

    private JCheckBox createDiagrams;

    private JCheckBox minimiseFigs;

    private JCheckBox layoutDiagrams;

    // level 0 import detail
    private JRadioButton classOnly;

    // level 1 import detail
    private JRadioButton classAndFeatures;

    // level 2 import detail
    private JRadioButton fullImport;

    //import detail level var:
    private int importLevel;

    private JTextField inputSourceEncoding;

    private JDialog dialog;

    private ImportStatusScreen iss;

    private StringBuffer problems = new StringBuffer();

    private Hashtable attributes = new Hashtable();




    /**
     * @return the text of this textfield
     */
    public String getInputSourceEncoding() {
        return inputSourceEncoding.getText();
    }

    /**
     * Close dialog window.
     *
     */
    public void disposeDialog() {
        Configuration.setString(Argo.KEY_INPUT_SOURCE_ENCODING,
            getInputSourceEncoding());
        dialog.setVisible(false);
        dialog.dispose();
    }

    /**
     * Get the panel that lets the user set reverse engineering
     * parameters.
     *
     * @param importInstance the instance of the import
     * @return the panel
     */
    public JComponent getConfigPanel(final Import importInstance) {

        final JTabbedPane tab = new JTabbedPane();

        // build the configPanel:
        if (configPanel == null) {
            JPanel general = new JPanel();
            general.setLayout(new GridLayout(13, 1));

            general.add(new JLabel(
                    Translator.localize("action.import-select-lang")));

            Vector languages = new Vector();

            for (Enumeration keys = modules.keys(); keys.hasMoreElements();) {
                languages.add(keys.nextElement());
            }
            JComboBox selectedLanguage = new JComboBox(languages);
            selectedLanguage.addActionListener(
                    new SelectedLanguageListener(importInstance, tab));
            general.add(selectedLanguage);

            descend =
                new JCheckBox(Translator.localize(
                        "action.import-option-descend-dir-recur"));
            descend.setSelected(true);
            general.add(descend);

            changedOnly =
                new JCheckBox(Translator.localize(
                        "action.import-option-changed_new"), true);
            general.add(changedOnly);

            createDiagrams =
                new JCheckBox(Translator.localize(
                        "action.import-option-create-diagram"), true);
            general.add(createDiagrams);

            minimiseFigs =
                new JCheckBox(Translator.localize(
                        "action.import-option-min-class-icon"), true);
            general.add(minimiseFigs);

            layoutDiagrams =
                new JCheckBox(Translator.localize(
                        "action.import-option-perform-auto-diagram-layout"),
                        true);
            general.add(layoutDiagrams);

           

            // select the level of import
            // 0 - classifiers only
            // 1 - classifiers plus feature specifications
            // 2 - full import, feature detail

            JLabel importDetailLabel =
                new JLabel(Translator.localize(
                        "action.import-level-of-import-detail"));
            ButtonGroup detailButtonGroup = new ButtonGroup();

            classOnly =
                new JRadioButton(Translator.localize(
                        "action.import-option-classfiers"));
            detailButtonGroup.add(classOnly);

            classAndFeatures =
                new JRadioButton(Translator.localize(
                        "action.import-option-classifiers-plus-specs"));
            detailButtonGroup.add(classAndFeatures);

            fullImport =
                new JRadioButton(Translator.localize(
                        "action.import-option-full-import"));
            fullImport.setSelected(true);
            detailButtonGroup.add(fullImport);

            general.add(importDetailLabel);
            general.add(classOnly);
            general.add(classAndFeatures);
            general.add(fullImport);

            general.add(new JLabel(
                    Translator.localize("action.import-file-encoding")));
            String enc =
                Configuration.getString(Argo.KEY_INPUT_SOURCE_ENCODING);
            if (enc == null || enc.trim().equals("")) {
                inputSourceEncoding =
                    new JTextField(System.getProperty("file.encoding"));
            } else {
                inputSourceEncoding = new JTextField(enc);
            }
            general.add(inputSourceEncoding);

            tab.add(general, Translator.localize("action.import-general"));
            tab.add(module.getConfigPanel(), module.getModuleName());
            configPanel = tab;
        }
        return configPanel;
    }

    /**
     * This method is called by ActionImportFromSources to start the
     * import run.<p>
     *
     * The method that for all parsing actions. It calls the actual
     * parser methods depending on the type of the file.<p>
     */
    public void doFile() {

        // determine how many files to process
        Vector files = module.getList(this);

        if (changedOnly.isSelected()) {
            // filter out all unchanged files
            Object model =
                ProjectManager.getManager().getCurrentProject().getModel();
            for (int i = files.size() - 1; i >= 0; i--) {
                File f = (File) files.elementAt(i);
                String fn = f.getAbsolutePath();
                String lm = String.valueOf(f.lastModified());
                if (lm.equals(
                        Model.getFacade().getTaggedValueValue(model, fn))) {
                    files.remove(i);
                }
            }
        }

        if (!classOnly.isSelected()) {
            // 2 passes needed
            files.addAll(files); // for the second pass

            if (classAndFeatures.isSelected()) {
                importLevel = 1;
            } else {
                importLevel = 2;
            }
        } else {
            importLevel = 0;
        }

        // we always start with a level 0 import
        setAttribute("level", new Integer(0));

        diagramInterface = getCurrentDiagram();

        ProjectBrowser.getInstance().setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // now start importing (with an empty problem list)
        problems = new StringBuffer();
        iss = new ImportStatusScreen("Importing", "Splash");
        SwingUtilities.invokeLater(
                   new ImportRun(files, layoutDiagrams.isSelected()));
        iss.setVisible(true);
    }

    /**
     * Set path for processed directory.
     *
     * @param path the given path
     */
    public void setSrcPath(String path) {
        srcPath = path;
    }

    /**
     * @return path for processed directory.
     */
    public String getSrcPath() {
        return srcPath;
    }


    /**
     * Check, if "Create diagrams from imported code" is selected.<p>
     *
     * @return true, if "Create diagrams from imported code" is selected
     */
    public boolean isCreateDiagramsChecked() {
        if (createDiagrams != null) {
            return createDiagrams.isSelected();
        }
        return true;
    }

    /**
     * Check, if "Discend directories recursively" is selected.<p>
     *
     * @return true, if "Discend directories recursively" is selected
     */
    public boolean isDiscendDirectoriesRecursively() {
        if (descend != null) {
            return descend.isSelected();
        }
        return true;
    }

    /**
     * Check, if "Minimise Class icons in diagrams" is selected.<p>
     *
     * @return true, if "Minimise Class icons in diagrams" is selected
     */
    public boolean isMinimizeFigsChecked() {
        if (minimiseFigs != null) {
            return minimiseFigs.isSelected();
        }
        return false;
    }

    /**
     * If we have modified any diagrams, the project was modified and
     * should be saved. I don't consider a import, that only modifies
     * the metamodel, at this point (Andreas Rueckert).
     * Calling Project.setNeedsSave(true) doesn't work here, because
     * Project.postLoad() is called after the import and it sets the
     * needsSave flag to false.<p>
     *
     * @return true, if any diagrams where modified and the project
     * should be saved before exit.
     */
    public boolean needsSave() {
        return (diagramInterface.getModifiedDiagrams().size() > 0);
    }

}


