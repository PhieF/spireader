/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.spireader.android.fbreader;

import org.spireader.fbreader.fbreader.*;
import org.spireader.android.fbreader.dict.DictionaryUtil;

public class SelectionTranslateAction extends FBAndroidAction {
	SelectionTranslateAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		final DictionaryHighlighting dictionaryHilite = new DictionaryHighlighting(fbview);
		DictionaryUtil.openTextInDictionary(
			BaseActivity,
			fbview.getSelectedSnippet().getText(),
			fbview.getCountOfSelectedWords() == 1,
			fbview.getSelectionStartY(),
			fbview.getSelectionEndY(),
			new Runnable() {
				public void run() {
					fbview.addHighlighting(dictionaryHilite);
					Reader.getViewWidget().repaint();
				}
			}
		);
		fbview.clearSelection();
	}
}
