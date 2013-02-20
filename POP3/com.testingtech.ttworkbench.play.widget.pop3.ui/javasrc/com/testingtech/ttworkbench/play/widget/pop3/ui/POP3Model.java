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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;

import com.testingtech.ttworkbench.core.ui.SWTUtil;
import com.testingtech.ttworkbench.play.widget.pop3.ui.model.EmailMessage;

public class POP3Model {
  private final List<EmailMessage> mails = new LinkedList<EmailMessage>();
  private final Set<String> serverMessageIds = new HashSet<String>();
  String status;
  
  public List<EmailMessage> getMails() {
    return mails;
  }

  public Collection<String> getServerMessageIds() {
    return serverMessageIds;
  }

  public void setServerMessageIds(List<String> serverMessageIds) {
    this.serverMessageIds.clear();
    this.serverMessageIds.addAll(serverMessageIds);
    notifyListeners();
  }
  
  public void addEmail(EmailMessage email) {
    mails.add(email);
    notifyListeners();
  }

  public void newEmail(LinkedList<String> messageIds) {
    messageIds.addAll(messageIds);
    showStatus(NLS.bind("{0} new email(s) on server", messageIds.size()));
  }
  
  public void deleteEmail(long messageId) {
    serverMessageIds.remove(messageId);
    EmailMessage message = findEmail(messageId);
    if (message != null) {
      mails.remove(message);
    }
    notifyListeners();  
  }

  protected EmailMessage findEmail(long messageId) {
    EmailMessage message = null;
    for (EmailMessage email : getMails()) {
      if (messageId == email.getId()) {
        message = email;
        break;
      }
    }
    return message;
  }
  
  public void updateStatus(ConnectionState connectionState, long statusCode, String statusMessage) {
    showStatus(connectionState+" "+statusMessage);
  }
  
  public void showStatus(String message) {
    this.status = message;
    notifyListeners();
  }
  
  public String getStatus() {
    return status;
  }
  
  private Set<IPop3ModelListener> listeners = new HashSet<IPop3ModelListener>();

  public void addListener(IPop3ModelListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }
  
  public void removeListener(IPop3ModelListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }
  
  private void notifyListeners() {
    synchronized (listeners) {
      for (IPop3ModelListener listener : listeners) {
        listener.notifyModelChange();
      }
    }
  }

  

}