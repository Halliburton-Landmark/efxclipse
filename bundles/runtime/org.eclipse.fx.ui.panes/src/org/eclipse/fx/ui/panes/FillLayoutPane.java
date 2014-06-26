/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl<tom.schindl@bestsolution.at> - ported to JavaFX
 *******************************************************************************/
package org.eclipse.fx.ui.panes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableIntegerProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;

import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.SizeConverter;

public class FillLayoutPane extends AbstractLayoutPane<FillLayoutPane.FillData> {
	private static final CssMetaData<FillLayoutPane, Number> MARGIN_WIDTH = new CssMetaData<FillLayoutPane, Number>("-fx-margin-width", SizeConverter.getInstance(), 0) {

		@Override
		public boolean isSettable(FillLayoutPane node) {
			return node.marginWidthProperty() == null || !node.marginWidthProperty().isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(FillLayoutPane node) {
			return (StyleableProperty<Number>) node.marginWidthProperty();
		}
	};

	private static final CssMetaData<FillLayoutPane, Number> MARGIN_HEIGHT = new CssMetaData<FillLayoutPane, Number>("-fx-margin-height", SizeConverter.getInstance(), 0) {

		@Override
		public boolean isSettable(FillLayoutPane node) {
			return node.marginHeightProperty() == null || !node.marginHeightProperty().isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(FillLayoutPane node) {
			return (StyleableProperty<Number>) node.marginHeightProperty();
		}
	};
	
	private static final CssMetaData<FillLayoutPane, Number> SPACING = new CssMetaData<FillLayoutPane, Number>("-fx-spacing", SizeConverter.getInstance(), 0) {

		@Override
		public boolean isSettable(FillLayoutPane node) {
			return node.spacingProperty() == null || !node.spacingProperty().isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(FillLayoutPane node) {
			return (StyleableProperty<Number>) node.spacingProperty();
		}
	};
	
	private static final CssMetaData<FillLayoutPane, Boolean> HORIZONTAL = new CssMetaData<FillLayoutPane, Boolean>("-fx-horizontal", BooleanConverter.getInstance(), true) {

		@Override
		public boolean isSettable(FillLayoutPane node) {
			return node.horizontalProperty() == null || !node.horizontalProperty().isBound();
		}

		@Override
		public StyleableProperty<Boolean> getStyleableProperty(FillLayoutPane node) {
			return (StyleableProperty<Boolean>) node.horizontalProperty();
		}
	};
	
	private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
    
    static {

        final List<CssMetaData<? extends Styleable, ?>> styleables =
            new ArrayList<CssMetaData<? extends Styleable, ?>>(AbstractLayoutPane.getClassCssMetaData());
        styleables.add(MARGIN_WIDTH);
        styleables.add(MARGIN_HEIGHT);
        styleables.add(SPACING);
        styleables.add(HORIZONTAL);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }
    
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

	/**
	 * type specifies how controls will be positioned within the layout.
	 * 
	 * The default value is HORIZONTAL.
	 * 
	 * Possible values are:
	 * <ul>
	 * <li>HORIZONTAL: Position the controls horizontally from left to right</li>
	 * <li>VERTICAL: Position the controls vertically from top to bottom</li>
	 * </ul>
	 */
	private BooleanProperty horizontal = new SimpleStyleableBooleanProperty(HORIZONTAL, this, "horizontal", true); //$NON-NLS-1$

	/**
	 * marginWidth specifies the number of pixels of horizontal margin that will
	 * be placed along the left and right edges of the layout.
	 * 
	 * The default value is 0.
	 * 
	 * @since 3.0
	 */
	private IntegerProperty marginWidth = new SimpleStyleableIntegerProperty(MARGIN_WIDTH, this, "marginWidth", Integer.valueOf(0)); //$NON-NLS-1$

	/**
	 * marginHeight specifies the number of pixels of vertical margin that will
	 * be placed along the top and bottom edges of the layout.
	 * 
	 * The default value is 0.
	 * 
	 * @since 3.0
	 */
	private IntegerProperty marginHeight = new SimpleStyleableIntegerProperty(MARGIN_HEIGHT, this, "marginHeight", Integer.valueOf(0)); //$NON-NLS-1$

	/**
	 * spacing specifies the number of pixels between the edge of one cell and
	 * the edge of its neighbouring cell.
	 * 
	 * The default value is 0.
	 * 
	 * @since 3.0
	 */
	private IntegerProperty spacing = new SimpleStyleableIntegerProperty(SPACING, this, "spacing", Integer.valueOf(0)); //$NON-NLS-1$

	static class FillData {

		double defaultWidth = -1, defaultHeight = -1;
		double currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;

		Size computeSize(Node control, double wHint, double hHint, boolean flushCache) {
			if (flushCache)
				flushCache();
			if (wHint == FX_DEFAULT && hHint == FX_DEFAULT) {
				if (defaultWidth == -1 || defaultHeight == -1) {
					// Size size = computeSize (wHint, hHint, flushCache);
					defaultWidth = control.prefWidth(hHint);
					defaultHeight = control.prefHeight(wHint);
				}
				return new Size(defaultWidth, defaultHeight);
			}
			if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
				// Point size = control.computeSize (wHint, hHint, flushCache);
				currentWhint = wHint;
				currentHhint = hHint;
				currentWidth = control.getLayoutBounds().getWidth();
				currentHeight = control.getLayoutBounds().getHeight();
			}
			return new Size(currentWidth, currentHeight);
		}

		void flushCache() {
			defaultWidth = defaultHeight = -1;
			currentWidth = currentHeight = -1;
		}
	}

	private static WeakHashMap<Node, FillData> CONSTRAINTS = new WeakHashMap<Node, FillData>();

	public static void setConstraint(Node n, FillData griddata) {
		CONSTRAINTS.put(n, griddata);
	}

	public static FillData getConstraint(Node n) {
		return CONSTRAINTS.get(n);
	}

	public void setHorizontal(boolean horizontal) {
		this.horizontal.set(horizontal);
	}

	public boolean isHorizontal() {
		return this.horizontal.get();
	}

	public BooleanProperty horizontalProperty() {
		return horizontal;
	}

	public void setMarginWidth(int marginWidth) {
		this.marginWidth.set(marginWidth);
	}

	public int getMarginWidth() {
		return this.marginWidth.get();
	}

	public IntegerProperty marginHeightProperty() {
		return marginHeight;
	}

	public void setMarginHeight(int marginHeight) {
		this.marginHeight.set(marginHeight);
	}

	public int getMarginHeight() {
		return this.marginHeight.get();
	}

	public IntegerProperty marginWidthProperty() {
		return marginWidth;
	}
	
	public int getSpacing() {
		return spacing.get();
	}
	
	public void setSpacing(int spacing) {
		this.spacing.set(spacing);
	}
	
	public IntegerProperty spacingProperty() {
		return this.spacing;
	}

	protected Size computeSize(double wHint, double hHint, boolean flushCache) {
		Node[] children = getChildren().toArray(new Node[0]);
		int count = children.length;
		double maxWidth = 0, maxHeight = 0;
		for (int i = 0; i < count; i++) {
			Node child = children[i];
			double w = wHint, h = hHint;
			if (count > 0) {
				if (horizontal.get() && wHint != FX_DEFAULT) {
					w = Math.max(0, (wHint - (count - 1) * spacing.get()) / count);
				}
				if (!horizontal.get() && hHint != FX_DEFAULT) {
					h = Math.max(0, (hHint - (count - 1) * spacing.get()) / count);
				}
			}
			Size size = computeChildSize(child, w, h, flushCache);
			maxWidth = Math.max(maxWidth, size.width);
			maxHeight = Math.max(maxHeight, size.height);
		}
		double width = 0, height = 0;
		if (horizontal.get()) {
			width = count * maxWidth;
			if (count != 0)
				width += (count - 1) * spacing.get();
			height = maxHeight;
		} else {
			width = maxWidth;
			height = count * maxHeight;
			if (count != 0)
				height += (count - 1) * spacing.get();
		}
		width += marginWidth.get() * 2;
		height += marginHeight.get() * 2;
		if (wHint != FX_DEFAULT)
			width = wHint;
		if (hHint != FX_DEFAULT)
			height = hHint;
		return new Size(width, height);
	}

	Size computeChildSize(Node control, double wHint, double hHint, boolean flushCache) {
		FillData data = getConstraint(control);
		if (data == null) {
			data = new FillData();
			setConstraint(control, data);
		}
		Size size = null;
		if (wHint == FX_DEFAULT && hHint == FX_DEFAULT) {
			size = data.computeSize(control, wHint, hHint, flushCache);
		} else {
			// TEMPORARY CODE
			int trimX, trimY;
			trimX = trimY = 0;
			// FIXME
			// if (control instanceof Scrollable) {
			// Rectangle rect = ((Scrollable) control).computeTrim(0, 0, 0, 0);
			// trimX = rect.width;
			// trimY = rect.height;
			// } else {
			// trimX = trimY = control.getBorderWidth() * 2;
			// }
			double w = wHint == FX_DEFAULT ? wHint : Math.max(0, wHint - trimX);
			double h = hHint == FX_DEFAULT ? hHint : Math.max(0, hHint - trimY);
			size = data.computeSize(control, w, h, flushCache);
		}
		return size;
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		Bounds rect = getLayoutBounds();
		Node[] children = getChildren().toArray(new Node[0]);
		int count = children.length;
		if (count == 0)
			return;
		double width = rect.getWidth() - marginWidth.get() * 2;
		double height = rect.getHeight() - marginHeight.get() * 2;
		if (horizontal.get()) {
			width -= (count - 1) * spacing.get();
			double x = rect.getMinX() + marginWidth.get(), extra = width % count;
			double y = rect.getMinY() + marginHeight.get(), cellWidth = width / count;
			for (int i = 0; i < count; i++) {
				Node child = children[i];
				double childWidth = cellWidth;
				if (i == 0) {
					childWidth += extra / 2;
				} else {
					if (i == count - 1)
						childWidth += (extra + 1) / 2;
				}
				child.resizeRelocate(x, y, childWidth, height);
				x += childWidth + spacing.get();
			}
		} else {
			height -= (count - 1) * spacing.get();
			double x = rect.getMinX() + marginWidth.get(), cellHeight = height / count;
			double y = rect.getMinY() + marginHeight.get(), extra = height % count;
			for (int i = 0; i < count; i++) {
				Node child = children[i];
				double childHeight = cellHeight;
				if (i == 0) {
					childHeight += extra / 2;
				} else {
					if (i == count - 1)
						childHeight += (extra + 1) / 2;
				}
				child.resizeRelocate(x, y, width, childHeight);
				y += childHeight + spacing.get();
			}
		}
	}
}
