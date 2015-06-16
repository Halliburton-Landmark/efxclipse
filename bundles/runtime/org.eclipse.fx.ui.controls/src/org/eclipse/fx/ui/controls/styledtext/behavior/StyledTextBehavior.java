/*******************************************************************************
 * Copyright (c) 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ui.controls.styledtext.behavior;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;

import java.util.ArrayList;
import java.util.List;

import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.eclipse.fx.ui.controls.styledtext.ActionEvent;
import org.eclipse.fx.ui.controls.styledtext.ActionEvent.ActionType;
import org.eclipse.fx.ui.controls.styledtext.StyledTextArea;
import org.eclipse.fx.ui.controls.styledtext.TextSelection;
import org.eclipse.fx.ui.controls.styledtext.VerifyEvent;
import org.eclipse.fx.ui.controls.styledtext.skin.StyledTextSkin.LineCell;
import org.eclipse.jdt.annotation.NonNull;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.text.HitInfo;

/**
 * Behavior for styled text
 */
@SuppressWarnings("restriction")
public class StyledTextBehavior extends BehaviorBase<StyledTextArea> {
	private static final List<KeyBinding> KEY_BINDINGS = new ArrayList<KeyBinding>();

	static {
		KEY_BINDINGS.add(new KeyBinding(null, KEY_PRESSED, "Consume")); //$NON-NLS-1$
	}

	/**
	 * Create a new behavior
	 *
	 * @param styledText
	 *            the styled text control
	 */
	public StyledTextBehavior(StyledTextArea styledText) {
		super(styledText, KEY_BINDINGS);
	}

	@Override
	protected void callActionForEvent(KeyEvent arg0) {
		if (arg0.getEventType() == KeyEvent.KEY_PRESSED) {
			_keyPressed(arg0);
		} else if( arg0.getEventType() == KeyEvent.KEY_TYPED ) {
			_keyTyped(arg0);
		}
		super.callActionForEvent(arg0);
	}

	@Override
	protected void callAction(String arg0) {
		super.callAction(arg0);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		super.mousePressed(arg0);
		getControl().requestFocus();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);

	}

	/**
	 * Invoke an action
	 *
	 * @param action
	 *            the action
	 * @return true is consumed
	 */
	public boolean invokeAction(ActionType action) {
		ActionEvent evt = new ActionEvent(getControl(), getControl(), action);
		Event.fireEvent(getControl(), evt);
		return evt.isConsumed();
	}

	@SuppressWarnings("deprecation")
	private void _keyPressed(KeyEvent event) {
		VerifyEvent evt = new VerifyEvent(getControl(), getControl(), event);
		Event.fireEvent(getControl(), evt);

		if (evt.isConsumed()) {
			event.consume();
			return;
		}

		int currentRowIndex = getControl().getContent().getLineAtOffset(getControl().getCaretOffset());

		final int offset = getControl().getCaretOffset();

		switch (event.getCode()) {
		case SHIFT:
		case ALT:
		case CONTROL:
			break;
		case LEFT: {
			if (event.isAltDown()) {
				invokeAction(ActionType.WORD_PREVIOUS);
			} else {
				if (offset == 0) {
					event.consume();
					break;
				}
				int newOffset = offset - 1;
				@SuppressWarnings("unused")
				int currentLine = getControl().getContent().getLineAtOffset(offset);
				@SuppressWarnings("unused")
				int newLine = getControl().getContent().getLineAtOffset(newOffset);
				getControl().impl_setCaretOffset(newOffset,event.isShiftDown());
				event.consume();
			}
			break;
		}
		case RIGHT: {
			if (event.isAltDown()) {
				invokeAction(ActionType.WORD_NEXT);
			} else if (event.isMetaDown()) {
				int currentLine = getControl().getContent().getLineAtOffset(offset);
				int lineOffset = getControl().getContent().getOffsetAtLine(currentLine);
				String lineContent = getControl().getContent().getLine(currentLine);

				getControl().impl_setCaretOffset(lineOffset + lineContent.length(),event.isShiftDown());
				event.consume();
			} else {
				if (offset + 1 > getControl().getContent().getCharCount()) {
					break;
				}
				int newOffset = offset + 1;
//				@SuppressWarnings("unused")
//				int currentLine = getControl().getContent().getLineAtOffset(offset);
//				@SuppressWarnings("unused")
//				int newLine = getControl().getContent().getLineAtOffset(newOffset);
				getControl().impl_setCaretOffset(newOffset,event.isShiftDown());
				event.consume();
			}
			break;
		}
		case UP: {
			int rowIndex = currentRowIndex;

			if (rowIndex == 0) {
				break;
			}

			int colIdx = offset - getControl().getContent().getOffsetAtLine(rowIndex);
			rowIndex -= 1;

			int lineOffset = getControl().getContent().getOffsetAtLine(rowIndex);
			int newCaretPosition = lineOffset + colIdx;
			int maxPosition = lineOffset + getControl().getContent().getLine(rowIndex).length();

			getControl().impl_setCaretOffset(Math.min(newCaretPosition, maxPosition),event.isShiftDown());
			break;
		}
		case DOWN: {
			int rowIndex = currentRowIndex;
			if (rowIndex + 1 == getControl().getContent().getLineCount()) {
				break;
			}

			int colIdx = offset - getControl().getContent().getOffsetAtLine(rowIndex);
			rowIndex += 1;

			int lineOffset = getControl().getContent().getOffsetAtLine(rowIndex);
			int newCaretPosition = lineOffset + colIdx;
			int maxPosition = lineOffset + getControl().getContent().getLine(rowIndex).length();

			getControl().impl_setCaretOffset(Math.min(newCaretPosition, maxPosition),event.isShiftDown());
			break;
		}
		case ENTER:
			if( getControl().getEditable() ) {
				int line = getControl().getContent().getLineAtOffset(getControl().getCaretOffset());
				String lineContent = getControl().getContent().getLine(line);

				// FIXME Temp hack
				char[] chars = lineContent.toCharArray();
				String prefix = ""; //$NON-NLS-1$
				for (int i = 0; i < chars.length; i++) {
					if (chars[i] == ' ') {
						prefix += " "; //$NON-NLS-1$
					} else {
						break;
					}
				}

				getControl().getContent().replaceTextRange(getControl().getCaretOffset(), 0, event.getText() + prefix);
				// listView.getSelectionModel().select(listView.getSelectionModel().getSelectedIndex()+1);
				getControl().setCaretOffset(offset + 1 + prefix.length());
			}
			break;
		case DELETE:
			if( getControl().getEditable() ) {
				if (event.isMetaDown()) {
					invokeAction(ActionType.DELETE_WORD_NEXT);
				} else {
					getControl().getContent().replaceTextRange(getControl().getCaretOffset(), 1, ""); //$NON-NLS-1$
					getControl().setCaretOffset(offset);
				}
				break;
			}
		case BACK_SPACE:
			if( getControl().getEditable() ) {
				if (event.isMetaDown()) {
					invokeAction(ActionType.DELETE_WORD_PREVIOUS);
				} else {
					TextSelection selection = getControl().getSelection();
					if( selection.length > 0 ) {
						getControl().getContent().replaceTextRange(selection.offset, selection.length, ""); //$NON-NLS-1$
						getControl().setCaretOffset(selection.offset);
					} else {
						getControl().getContent().replaceTextRange(getControl().getCaretOffset() - 1, 1, ""); //$NON-NLS-1$
						getControl().setCaretOffset(offset - 1);
					}
				}
				break;
			}
		case TAB:
			if( getControl().getEditable() ) {
				event.consume();
				if (event.isShiftDown()) {
					// TODO Remove first 4 white space chars???
					break;
				} else {
					// FIXME Need to should fix this but it currently completely
					// break cursor positioning
					getControl().getContent().replaceTextRange(getControl().getCaretOffset(), 0, "    "); //$NON-NLS-1$
					getControl().setCaretOffset(offset + 4);
					break;
				}
			}
		case V:
			if( getControl().getEditable() ) {
				if( event.isShortcutDown() ) {
					getControl().paste();
					event.consume();
					break;
				}
			}
		case C: {
			if( event.isShortcutDown() ) {
				getControl().copy();
				event.consume();
				break;
			}
		}
		default:
			if( getControl().getEditable() ) {
				String text = event.getText();
				if (text.length() > 0) {
					getControl().getContent().replaceTextRange(getControl().getCaretOffset(), 0, text);
					getControl().setCaretOffset(offset + 1);
				}
				break;
			}
		}

		// Event.fireEvent(getControl(), event.copyFor(getControl(),
		// getControl()));
	}

	private void _keyTyped(KeyEvent e) {

	}

	/**
	 * Send a mouse pressed
	 *
	 * @param event
	 *            the event
	 * @param visibleCells
	 *            the visible cells
	 */
	@SuppressWarnings("deprecation")
	public void updateCursor(MouseEvent event, List<LineCell> visibleCells, boolean selection) {
		LineCell lastCell = null;

		for (LineCell tmp : visibleCells) {
			Bounds boundsInParent = tmp.getBoundsInParent();
			if ( boundsInParent.getMinY() > event.getY()) {
				if( lastCell == null ) {
					lastCell = tmp;
				}

				if (lastCell.getDomainElement() != null) {
					Region g = (Region) lastCell.getGraphic();
					Pane flow = (Pane) g.getChildrenUnmodifiable().get(0);
					for (Node n : flow.getChildren()) {
						if( n.localToScene(n.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY()) ) {
							TextFlow textFlow = (TextFlow) n;
							Text text = (Text)textFlow.getChildren().get(0);

							HitInfo info = text.impl_hitTestChar(text.sceneToLocal(event.getSceneX(), event.getSceneY()));
							if (info.getInsertionIndex() >= 0) {
								int offset = ((Integer) textFlow.getUserData()).intValue() + info.getInsertionIndex();
								getControl().impl_setCaretOffset(offset, selection);
								return;
							}
						}
					}

					int offset = lastCell.getDomainElement().getLineOffset() + lastCell.getDomainElement().getLineLength();
					getControl().impl_setCaretOffset(offset, selection);

				}
				break;
			}
			lastCell = tmp;
		}
		getControl().requestFocus();
		Event.fireEvent(getControl(), event.copyFor(getControl(), getControl()));
	}

//	public void mouseDragged(MouseEvent event, List<LineCell> visibleCells) {
//
//	}
}
