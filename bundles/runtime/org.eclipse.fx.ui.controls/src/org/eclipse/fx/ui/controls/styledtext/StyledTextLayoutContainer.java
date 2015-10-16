/*******************************************************************************
 * Copyright (c) 2015 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ui.controls.styledtext;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * A text layout container who is able to show selection
 *
 * @since 2.0
 */
public class StyledTextLayoutContainer extends Region {
	@SuppressWarnings("null")
	@NonNull
	private final ObservableList<@NonNull StyledTextNode> textNodes = FXCollections.observableArrayList();

	@NonNull
	private final IntegerProperty startOffset = new SimpleIntegerProperty(this, "startOffset"); //$NON-NLS-1$

	/**
	 * The start offset if used in a bigger context like {@link StyledTextArea}
	 *
	 * @return the offset property to observe
	 */
	public final IntegerProperty startOffsetProperty() {
		return this.startOffset;
	}

	/**
	 * The start offset if used in a bigger context like {@link StyledTextArea}
	 *
	 * @return the offset
	 */
	public final int getStartOffset() {
		return this.startOffsetProperty().get();
	}

	/**
	 * The start offset if used in a bigger context like {@link StyledTextArea}
	 *
	 * @param startOffset
	 *            the start offset
	 */
	public final void setStartOffset(final int startOffset) {
		this.startOffsetProperty().set(startOffset);
	}

	@NonNull
	private final ObjectProperty<@NonNull TextSelection> selection = new SimpleObjectProperty<>(this, "selection", TextSelection.EMPTY); //$NON-NLS-1$

	/**
	 * The selection currently shown
	 *
	 * @return the selection property to observe
	 */
	public final ObjectProperty<@NonNull TextSelection> selectionProperty() {
		return this.selection;
	}

	/**
	 * @return the text selection
	 */
	public final @NonNull TextSelection getSelection() {
		return this.selectionProperty().get();
	}

	/**
	 * The selection to be shown
	 *
	 * @param selection
	 *            the new selection
	 */
	public final void setSelection(final @NonNull TextSelection selection) {
		this.selectionProperty().set(selection);
	}

	private final Region selectionMarker = new Region();
	final Line caret = new Line();
	private final TextFlow textLayoutNode = new TextFlow();

	private StyledTextNode selectionStartNode;
	private StyledTextNode selectionEndNode;

	private double selectionStartX;
	private double selectionEndX;

	private Timeline flashTimeline;
	int caretIndex = -1;

	private final ReadOnlyBooleanProperty ownerFocusedProperty;

	/**
	 * Create a container to layout text and allows to show e.g. a selection
	 * range
	 */
	public StyledTextLayoutContainer() {
		this(new SimpleBooleanProperty(true));
	}

	/**
	 * Create a container to layout text and allows to show e.g. a selection
	 * range
	 *
	 * @param ownerFocusedProperty
	 *            property identifing if the owner of the text container has the
	 *            input focus
	 */
	@SuppressWarnings("null")
	public StyledTextLayoutContainer(ReadOnlyBooleanProperty ownerFocusedProperty) {
		this.ownerFocusedProperty = ownerFocusedProperty;
		getStyleClass().add("styled-text-layout-container"); //$NON-NLS-1$
		this.textNodes.addListener(this::recalculateOffset);
		this.selectionMarker.setVisible(false);
		this.selectionMarker.setManaged(false);
		this.selectionMarker.getStyleClass().add("selection-marker"); //$NON-NLS-1$
		this.caret.setVisible(false);
		this.caret.setStrokeWidth(2);
		this.caret.setManaged(false);
		this.caret.getStyleClass().add("text-caret"); //$NON-NLS-1$

		this.flashTimeline = new Timeline();
		this.flashTimeline.setCycleCount(Animation.INDEFINITE);

		EventHandler<ActionEvent> startEvent = e -> {
			if( ! ownerFocusedProperty.get() ) {
				StyledTextLayoutContainer.this.caret.setVisible(false);
			} else {
				StyledTextLayoutContainer.this.caret.setVisible(StyledTextLayoutContainer.this.caretIndex != -1);
			}
		};

		EventHandler<ActionEvent> endEvent = e -> {
			StyledTextLayoutContainer.this.caret.setVisible(false);
		};

		this.flashTimeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, startEvent), new KeyFrame(Duration.millis(500), endEvent), new KeyFrame(Duration.millis(1000)));

		Bindings.bindContent(this.textLayoutNode.getChildren(), this.textNodes);
		getChildren().setAll(this.selectionMarker, this.textLayoutNode, this.caret);
		selectionProperty().addListener(this::handleSelectionChange);
		ownerFocusedProperty.addListener( o -> {
			if( ! ownerFocusedProperty.get() ) {
				this.caret.setVisible(false);
			}
		});
	}

	private int getEndOffset() {
		return getStartOffset() + getText().length();
	}

	/**
	 * Check if the offset is between the start and end
	 *
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @return <code>true</code> if intersects the offset
	 */
	public boolean intersectOffset(int start, int end) {
		if (getStartOffset() > end) {
			return false;
		} else if (getEndOffset() < start) {
			return false;
		}
		return true;
	}

	private void recalculateOffset(Observable o) {
		int offset = 0;
		for (StyledTextNode t : this.textNodes) {
			t.setStartOffset(offset);
			offset = t.getEndOffset();
		}
	}

	private void handleSelectionChange(Observable o, TextSelection oldSelection, TextSelection newSelection) {
		if (newSelection.length == 0) {
			this.selectionMarker.setVisible(false);
			this.selectionMarker.resize(0, 0);
		} else {
			this.selectionMarker.setVisible(true);
			int start = newSelection.offset;
			int end = newSelection.offset + newSelection.length;

			this.selectionStartNode = null;
			this.selectionEndNode = null;
			for (StyledTextNode t : this.textNodes) {
				if (t.intersectOffset(start, end)) {
					if (this.selectionStartNode == null) {
						this.selectionStartNode = t;
					}
					this.selectionEndNode = t;
				}
			}

			if (this.selectionStartNode != null && this.selectionEndNode != null) {
				int charIndex = start - this.selectionStartNode.getStartOffset();
				this.selectionStartX = this.selectionStartNode.getCharLocation(charIndex);
				charIndex = end - this.selectionEndNode.getStartOffset();
				this.selectionEndX = this.selectionEndNode.getCharLocation(charIndex);
				requestLayout();
			}
		}
	}

	@Override
	protected double computePrefHeight(double width) {
		double d = super.computePrefHeight(width);
		return d;
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		this.textLayoutNode.relocate(getInsets().getLeft(), getInsets().getTop());

		if (this.selectionStartNode != null && this.selectionEndNode != null) {
			double x1 = this.textLayoutNode.localToParent(this.selectionStartNode.getBoundsInParent().getMinX(), 0).getX() + this.selectionStartX;
			double x2 = this.textLayoutNode.localToParent(this.selectionEndNode.getBoundsInParent().getMinX(), 0).getX() + this.selectionEndX;
			this.selectionMarker.resizeRelocate(x1, 0, x2 - x1, getHeight());
		}

		if (this.caretIndex >= 0) {
			this.textLayoutNode.layout();
			this.textLayoutNode.applyCss();
			for (StyledTextNode t : this.textNodes) {
				t.applyCss();
				if (t.getStartOffset() <= this.caretIndex && (t.getEndOffset() > this.caretIndex || this.textNodes.get(this.textNodes.size() - 1) == t)) {
					double caretX = t.getCharLocation(this.caretIndex - t.getStartOffset());
					double x = this.textLayoutNode.localToParent(t.getBoundsInParent().getMinX(), 0).getX() + caretX;
					double h = t.prefHeight(-1);

					this.caret.setStartX(x);
					this.caret.setEndX(x);
					this.caret.setStartY(getInsets().getTop() + 1);
					this.caret.setEndY(h + getInsets().getTop() + 1);
					this.caret.toFront();
					return;
				}
			}

			this.caret.setStartX(0);
			this.caret.setEndX(0);
			this.caret.setStartY(0);
			this.caret.setEndY(15);

		} else {
			this.caret.setStartX(0);
			this.caret.setEndX(0);
			this.caret.setStartY(0);
			this.caret.setEndY(getHeight());
		}
	}

	/**
	 * @return list of text nodes rendered
	 */
	public @NonNull ObservableList<@NonNull StyledTextNode> getTextNodes() {
		return this.textNodes;
	}

	/**
	 * Find the caret index at the give point
	 *
	 * @param point
	 *            the point relative to coordinate system of this node
	 * @return the index or <code>-1</code> if not found
	 */
	public int getCaretIndexAtPoint(Point2D point) {
		Point2D scenePoint = localToScene(point);
		for (StyledTextNode t : this.textNodes) {
			if (t.localToScene(t.getBoundsInLocal()).contains(scenePoint)) {
				return t.getCaretIndexAtPoint(t.sceneToLocal(scenePoint)) + t.getStartOffset();
			}
		}

		return -1;
	}

	private String getText() {
		StringBuilder b = new StringBuilder();
		for (StyledTextNode t : this.textNodes) {
			b.append(t.getText());
		}
		return b.toString();
	}

	/**
	 * Set the caret to show at the given index
	 *
	 * @param index
	 *            the index or <code>-1</code> if caret is to be hidden
	 */
	public void setCaretIndex(int index) {
		if (index >= 0) {
			this.caretIndex = index;
			if( this.ownerFocusedProperty.get() ) {
				this.caret.setVisible(true);
			} else {
				this.caret.setVisible(false);
			}
			this.flashTimeline.play();
			requestLayout();
		} else {
			this.caretIndex = -1;
			this.flashTimeline.stop();
			this.caret.setVisible(false);
			requestLayout();
		}
	}

	/**
	 * Find the position of a the caret at a given index
	 *
	 * @param index
	 *            the index
	 * @return the location relative to this node or <code>null</code>
	 */
	public @Nullable Point2D getCareLocation(int index) {
		for (StyledTextNode t : this.textNodes) {
			if (t.getStartOffset() <= index && t.getEndOffset() >= index) {
				double x = t.getCharLocation(index);
				return sceneToLocal(t.localToScene(x, 0));
			}
		}
		return null;
	}

	/**
	 * Releases resources immediately
	 */
	public void dispose() {
		this.flashTimeline.stop();
		this.flashTimeline.getKeyFrames().clear();
	}
}
