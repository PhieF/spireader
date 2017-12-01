/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Parcelable;
import android.view.View;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;
import com.github.johnpersano.supertoasts.util.OnDismissWrapper;

import org.spireader.zlibrary.core.network.ZLNetworkException;
import org.spireader.zlibrary.core.resources.ZLResource;
import org.spireader.zlibrary.text.view.*;

import org.spireader.fbreader.Paths;
import org.spireader.fbreader.fbreader.FBReaderApp;
import org.spireader.fbreader.bookmodel.FBHyperlinkType;
import org.spireader.fbreader.network.NetworkLibrary;
import org.spireader.fbreader.util.AutoTextSnippet;

import org.spireader.android.fbreader.dict.DictionaryUtil;
import org.spireader.android.fbreader.image.ImageViewActivity;
import org.spireader.android.fbreader.network.*;
import org.spireader.android.fbreader.network.auth.ActivityNetworkContext;
import org.spireader.android.util.UIMessageUtil;

class ProcessHyperlinkAction extends FBAndroidAction {
	ProcessHyperlinkAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	public boolean isEnabled() {
		return Reader.getTextView().getOutlinedRegion() != null;
	}

	@Override
	protected void run(Object ... params) {
		final ZLTextRegion region = Reader.getTextView().getOutlinedRegion();
		if (region == null) {
			return;
		}

		final ZLTextRegion.Soul soul = region.getSoul();
		if (soul instanceof ZLTextHyperlinkRegionSoul) {
			Reader.getTextView().hideOutline();
			Reader.getViewWidget().repaint();
			final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul)soul).Hyperlink;
			switch (hyperlink.Type) {
				case FBHyperlinkType.EXTERNAL:
					openInBrowser(hyperlink.Id);
					break;
				case FBHyperlinkType.INTERNAL:
				case FBHyperlinkType.FOOTNOTE:
				{
					final AutoTextSnippet snippet = Reader.getFootnoteData(hyperlink.Id);
					if (snippet == null) {
						break;
					}

					Reader.Collection.markHyperlinkAsVisited(Reader.getCurrentBook(), hyperlink.Id);
					final boolean showToast;
					switch (Reader.MiscOptions.ShowFootnoteToast.getValue()) {
						default:
						case never:
							showToast = false;
							break;
						case footnotesOnly:
							showToast = hyperlink.Type == FBHyperlinkType.FOOTNOTE;
							break;
						case footnotesAndSuperscripts:
							showToast =
								hyperlink.Type == FBHyperlinkType.FOOTNOTE ||
								region.isVerticallyAligned();
							break;
						case allInternalLinks:
							showToast = true;
							break;
					}
					if (showToast) {
						final SuperActivityToast toast;
						if (snippet.IsEndOfText) {
							toast = new SuperActivityToast(BaseActivity, SuperToast.Type.STANDARD);
						} else {
							toast = new SuperActivityToast(BaseActivity, SuperToast.Type.BUTTON);
							toast.setButtonIcon(
								android.R.drawable.ic_menu_more,
								ZLResource.resource("toast").getResource("more").getValue()
							);
							toast.setOnClickWrapper(new OnClickWrapper("ftnt", new SuperToast.OnClickListener() {
								@Override
								public void onClick(View view, Parcelable token) {
									Reader.getTextView().hideOutline();
									Reader.tryOpenFootnote(hyperlink.Id);
								}
							}));
						}
						toast.setText(snippet.getText());
						toast.setDuration(Reader.MiscOptions.FootnoteToastDuration.getValue().Value);
						toast.setOnDismissWrapper(new OnDismissWrapper("ftnt", new SuperToast.OnDismissListener() {
							@Override
							public void onDismiss(View view) {
								Reader.getTextView().hideOutline();
								Reader.getViewWidget().repaint();
							}
						}));
						Reader.getTextView().outlineRegion(region);
						BaseActivity.showToast(toast);
					} else {
						Reader.tryOpenFootnote(hyperlink.Id);
					}
					break;
				}
			}
		} else if (soul instanceof ZLTextImageRegionSoul) {
			Reader.getTextView().hideOutline();
			Reader.getViewWidget().repaint();
			final String url = ((ZLTextImageRegionSoul)soul).ImageElement.URL;
			if (url != null) {
				try {
					final Intent intent = new Intent();
					intent.setClass(BaseActivity, ImageViewActivity.class);
					intent.putExtra(ImageViewActivity.URL_KEY, url);
					intent.putExtra(
						ImageViewActivity.BACKGROUND_COLOR_KEY,
						Reader.ImageOptions.ImageViewBackground.getValue().intValue()
					);
					OrientationUtil.startActivity(BaseActivity, intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (soul instanceof ZLTextWordRegionSoul) {
			DictionaryUtil.openTextInDictionary(
				BaseActivity,
				((ZLTextWordRegionSoul)soul).Word.getString(),
				true,
				region.getTop(),
				region.getBottom(),
				new Runnable() {
					public void run() {
						BaseActivity.outlineRegion(soul);
					}
				}
			);
		}
	}

	private void openInBrowser(final String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		final boolean externalUrl;
		if (BookDownloader.acceptsUri(Uri.parse(url), null)) {
			intent.setClass(BaseActivity, BookDownloader.class);
			intent.putExtra(BookDownloaderService.Key.SHOW_NOTIFICATIONS, BookDownloaderService.Notifications.ALL);
			externalUrl = false;
		} else {
			externalUrl = true;
		}
		final NetworkLibrary nLibrary = NetworkLibrary.Instance(Paths.systemInfo(BaseActivity));
		new Thread(new Runnable() {
			public void run() {
				if (!url.startsWith("fbreader-action:")) {
					try {
						nLibrary.initialize(new ActivityNetworkContext(BaseActivity));
					} catch (ZLNetworkException e) {
						e.printStackTrace();
						UIMessageUtil.showMessageText(BaseActivity, e.getMessage());
						return;
					}
				}
				intent.setData(Util.rewriteUri(Uri.parse(nLibrary.rewriteUrl(url, externalUrl))));
				BaseActivity.runOnUiThread(new Runnable() {
					public void run() {
						try {
							OrientationUtil.startActivity(BaseActivity, intent);
						} catch (ActivityNotFoundException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}).start();
	}
}
