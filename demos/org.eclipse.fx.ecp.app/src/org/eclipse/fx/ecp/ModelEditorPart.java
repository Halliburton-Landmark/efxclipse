/******************************************************************************* 
 * Copyright (c) 2012 TESIS DYNAware GmbH and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Torsten Sommer <torsten.sommer@tesis.de> - initial API and implementation 
 *******************************************************************************/
package org.eclipse.fx.ecp;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fx.ecp.ui.ModelElementEditor;
import org.eclipse.fx.ecp.ui.form.DefaultModelElementForm;

public class ModelEditorPart implements ModelElementEditor {

	private ScrollPane scrollPane;
	private MPart part;
	private EObject modelElement;
	private DefaultModelElementForm form;

	@Inject
	public ModelEditorPart(BorderPane parent, final MApplication application, MPart part) {
		this.part = part;
		scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

		parent.setCenter(scrollPane);
	}

	@Override
	public void setInput(final EObject modelElement) {
		this.modelElement = modelElement;
		
		if(form != null)
			form.dispose();

		form = new DefaultModelElementForm(modelElement, null);
		scrollPane.setContent(form);

//		modelElement.eAdapters().add(new AdapterImpl() {
//
//			@Override
//			public void notifyChanged(Notification msg) {
//				update();
//			}
//
//		});

//		update();
	}

//	public void update() {
//		IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory.adapt(modelElement, IItemLabelProvider.class);
//		if (labelProvider != null) {
//			part.setLabel(labelProvider.getText(modelElement));
//			Object image = labelProvider.getImage(modelElement);
//			if (image instanceof URL)
//				part.setIconURI(((URL) image).toExternalForm());
//		} else {
//			part.setLabel("Model Editor");
//		}
//	}

}
