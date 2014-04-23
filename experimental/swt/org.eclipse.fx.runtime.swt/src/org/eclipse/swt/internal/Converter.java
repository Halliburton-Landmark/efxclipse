/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation, BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   IBM Corporation - initial API
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal;

import javafx.geometry.Bounds;

import org.eclipse.swt.graphics.Rectangle;

public class Converter {
	public static Rectangle fromBounds(Bounds b) {
		return new Rectangle((int)b.getMinX(), (int)b.getMinY(), (int)b.getWidth(), (int)b.getHeight());
	}
}
