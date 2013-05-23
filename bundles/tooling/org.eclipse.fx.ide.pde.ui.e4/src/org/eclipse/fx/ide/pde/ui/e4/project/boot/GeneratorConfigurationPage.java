/*******************************************************************************
 * Copyright (c) 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ide.pde.ui.e4.project.boot;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

class GeneratorConfigurationPage extends WizardPage {
	private AppBundleProjectData data;
	private Button jemmyButton;
	private Button tychoButton;
	private Button nativePackaging;
	private Text productName;
	
	private ModifyListener propertiesListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setPageComplete(validate());
		}
	};
	
	private SelectionListener listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			setPageComplete(validate());
		}
	};
	
	
	public GeneratorConfigurationPage(AppBundleProjectData data, String pageName, String title) {
		super(pageName,title,null);
		this.data = data;
		setPageComplete(false);
	}
	
	protected boolean validate() {
		setErrorMessage(null);
		
		if( productName.getText().trim().isEmpty() ) {
			setErrorMessage("You need to enter a product name");
			return false;
		}
		
		data.setProductName(productName.getText());
		data.setJemmyTest(jemmyButton.getSelection());
		data.setTychoIntegration(tychoButton.getSelection());
		data.setNativeExport(nativePackaging.getSelection());
		
		return true;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		Group group = new Group(container, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Customizations");
		group.setLayout(new GridLayout(2, false));
		
		{
			createLabel(group, "Product Name:");
			productName = createText(group, propertiesListener, 1);
			productName.setMessage("Enter a product name");
		}
		
		{
			createLabel(group, "Jemmy Unittest stubs:");
			jemmyButton = createCheckbox(group, listener);
			jemmyButton.setSelection(data.isJemmyTest());
			jemmyButton.setEnabled(data.isJemmyTest());
		}
		
		{
			createLabel(group, "Tycho Build Stubs:");
			tychoButton = createCheckbox(group, listener);
			tychoButton.setSelection(data.isTychoIntegration());
		}
		
		{
			createLabel(group, "Native packaging:");
			nativePackaging = createCheckbox(group, listener);
			nativePackaging.setSelection(data.isNativeExport());
		}
		
		setControl(container);
	}

	protected Text createText(Composite parent, ModifyListener listener, int horizSpan) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = horizSpan;
		text.setLayoutData(data);
		text.addModifyListener(listener);
		return text;
	}
	
	private Button createCheckbox(Composite container, SelectionListener listener) {
		Button b = new Button(container, SWT.CHECK);
		b.addSelectionListener(listener);
		return b;
	}
	
	private Label createLabel(Composite container, String text) {
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		GridData gd = new GridData();
		label.setLayoutData(gd);
		return label;
	}
}
