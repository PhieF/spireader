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

package org.spireader.fbreader.formats;

import org.spireader.zlibrary.core.encodings.AutoEncodingCollection;
import org.spireader.zlibrary.core.filesystem.ZLFile;
import org.spireader.zlibrary.core.util.SystemInfo;

import org.spireader.fbreader.book.AbstractBook;
import org.spireader.fbreader.book.BookUtil;

public abstract class ExternalFormatPlugin extends FormatPlugin {
	protected ExternalFormatPlugin(SystemInfo systemInfo, String fileType) {
		super(systemInfo, fileType);
	}

	@Override
	public int priority() {
		return 10;
	}

	public abstract String packageName();

	@Override
	public PluginImage readCover(ZLFile file) {
		return new PluginImage(file, this);
	}

	@Override
	public AutoEncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(AbstractBook book) {
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return null;
	}

	@Override
	public void readUids(AbstractBook book) {
		if (book.uids().isEmpty()) {
			book.addUid(BookUtil.createUid(book, "SHA-256"));
		}
	}

	@Override
	public String toString() {
		return "ExternalFormatPlugin [" + supportedFileType() + "]";
	}
}