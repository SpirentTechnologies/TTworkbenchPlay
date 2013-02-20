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
package com.testingtech.ttworkbench.play.widget.smtp.ui.model;

import java.util.List;

public class EmailMessage {
	private long time;
	private String from;
	private List<String> to;
	private List<String> cc;
	private List<String> bcc;
	private String subject;
	private String messageBodyAsHtml;
	private String messageBodyAsText;

	
	
	public EmailMessage() {
		super();
	}
	
	public EmailMessage(long time,
			String from, List<String> to, List<String> cc, List<String> bcc, 
			String subject, String messageBodyAsText, String messageBodyAsHtml) {
		super();
		this.time = time;
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.messageBodyAsText = messageBodyAsText;
		this.messageBodyAsHtml = messageBodyAsHtml;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public List<String> getTo() {
		return to;
	}
	public void setTo(List<String> to) {
		this.to = to;
	}
	public List<String> getCc() {
		return cc;
	}
	public void setCc(List<String> cc) {
		this.cc = cc;
	}
	public List<String> getBcc() {
		return bcc;
	}
	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}
	public String getMessageBodyAsHtml() {
		return messageBodyAsHtml;
	}
	public void setMessageBodyAsHtml(String messageBodyAsHtml) {
		this.messageBodyAsHtml = messageBodyAsHtml;
	}
	public String getMessageBodyAsText() {
		return messageBodyAsText;
	}
	public void setMessageBodyAsText(String messageBodyAsText) {
		this.messageBodyAsText = messageBodyAsText;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
}
