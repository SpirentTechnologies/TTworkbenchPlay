/*
 * ----------------------------------------------------------------------------
 *  (C) Copyright Testing Technologies, 2012-2013.  All Rights Reserved.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     Testing Technologies - initial API and implementation
 *
 *  All copies of this program, whether in whole or in part, and whether
 *  modified or not, must display this and all other embedded copyright
 *  and ownership notices in full.
 *
 *  See the file COPYRIGHT for details of redistribution and use.
 *
 *  You should have received a copy of the COPYRIGHT file along with
 *  this file; if not, write to the Testing Technologies,
 *  Michaelkirchstr. 17/18, 10179 Berlin, Germany.
 *
 *  TESTING TECHNOLOGIES DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
 *  SOFTWARE. IN NO EVENT SHALL TESTING TECHNOLOGIES BE LIABLE FOR ANY
 *  SPECIAL, DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN
 *  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 *  THIS SOFTWARE.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 *  EITHER EXPRESSED OR IMPLIED, INCLUDING ANY KIND OF IMPLIED OR
 *  EXPRESSED WARRANTY OF NON-INFRINGEMENT OR THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * -----------------------------------------------------------------------------
 */
package com.testingtech.ttworkbench.play.widget.pop3.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.testingtech.ttworkbench.play.widget.pop3.ui.model.EmailMessage;

public class EmailMessageLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm dd/MM/yyyy");

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		EmailMessage msg = null;
		if (element instanceof EmailMessage) {
			msg = (EmailMessage) element;
		}
		if (msg == null) {
			return null;
		}

		switch (columnIndex) {
		case 0:
			return msg.getFrom();
		case 1:
			return msg.getSubject();
		case 2: {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(msg.getTime());
			return DATE_FORMAT.format(cal.getTime());
		}
		default:
			return null;
		}
	}

	public String getText(Object element) {
		if (element instanceof EmailMessage) {
			EmailMessage msg = (EmailMessage) element;
			return msg.getSubject();
		}
		return null;
	}
}
