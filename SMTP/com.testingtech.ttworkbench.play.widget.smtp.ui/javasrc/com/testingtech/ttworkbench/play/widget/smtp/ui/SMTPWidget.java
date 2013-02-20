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
package com.testingtech.ttworkbench.play.widget.smtp.ui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.testingtech.ttworkbench.core.ui.CommonColors;
import com.testingtech.ttworkbench.core.ui.SWTUtil;
import com.testingtech.ttworkbench.play.dashboard.widget.AbstractDashboardWidget;
import com.testingtech.ttworkbench.play.dashboard.widget.DashboardWidgetFactoryDescriptor;
import com.testingtech.ttworkbench.play.dashboard.widget.IDashboard;
import com.testingtech.ttworkbench.play.generated.PROTO_Interface;
import com.testingtech.ttworkbench.play.generated.PROTO_Interface.ACTIONS.BlockingInterface;
import com.testingtech.ttworkbench.play.generated.PROTO_Interface.SendMessage;
import com.testingtech.ttworkbench.play.widget.smtp.ui.model.EmailMessage;

public class SMTPWidget extends AbstractDashboardWidget<ISMTPModel, PROTO_Interface.ACTIONS.BlockingInterface> 
implements ISMTPModel {
  private static final int TEXT_FIELD_DISTANCE = 5;
  private static final int BUTTON_DISTANCE = 10;
  private static final int BUTTON_BAR_HEIGHT = 50;

  private Text toText;
  private Text ccText;
  private Text bccText;
  private Text fromText;
  private Text subjectText;
  private Text messageText;
  private Button clearButton;
  private Button sendButton;

  public SMTPWidget(SMTPWidgetFactory smtpWidgetFactory, IDashboard dashboard) {
    super(smtpWidgetFactory, dashboard);
  }

  public BlockingService createEventsService(int eventsServicePortNumber) {
    BlockingService eventsService = 
        PROTO_Interface.EVENTS.newReflectiveBlockingService(new EventsServiceImpl(getModel()));
    return eventsService;
  }

  /**
   * Create a new execution client
   * @param host remote actions service address
   * @param actionsServicePort remote actions service port number
   * @return a new instance of a client
   * @throws IOException is thrown on initialization error
   */
  public ActionsClient createActionsClient(String host, int actionsServicePort) throws IOException {
    ActionsClient actionsClient = new ActionsClient();
    actionsClient.connect(host, actionsServicePort);
    return actionsClient;
  }

  public Control createWidgetControl(Composite parent) {
    DashboardWidgetFactoryDescriptor descriptor = getFactory().getDescriptor();

    Group group = new Group(parent, SWT.NONE);
    group.setText("Send E-Mail (SMTP)");
    group.setLayout(new FormLayout());
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    ((GridData)group.getLayoutData()).widthHint = 400;
    ((GridData)group.getLayoutData()).heightHint = 300;
    ((GridData)group.getLayoutData()).minimumWidth = 400;
    ((GridData)group.getLayoutData()).minimumHeight = 300;
    group.setToolTipText(descriptor.getDescription());
    group.setBackground(CommonColors.WHITE);

    toText = addLabeledTextField(group, "To:", null);
    ccText = addLabeledTextField(group, "CC:", toText);
    bccText = addLabeledTextField(group, "BCC:", ccText);
    fromText = addLabeledTextField(group, "From:", bccText);
    subjectText = addLabeledTextField(group, "Subject:", fromText);
    messageText = addLabeledTextField(group, "Message:", subjectText, SWT.BORDER | SWT.MULTI);
    ((FormData)messageText.getLayoutData()).bottom = new FormAttachment(100, -BUTTON_BAR_HEIGHT);

    clearButton = addButtonToBar(group, "Clear", null);
    sendButton = addButtonToBar(group, "Send", clearButton);

    clearButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        toText.setText("");
        ccText.setText("");
        bccText.setText("");
        fromText.setText("");
        subjectText.setText("");
        messageText.setText("");
      }
    });

    sendButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        final EmailMessage emailMessage = 
            new EmailMessage(System.currentTimeMillis(), 
                             fromText.getText(), 
                             list(toText), 
                             list(ccText), 
                             list(bccText), 
                             subjectText.getText(), 
                             messageText.getText(), 
                             null);
        pool.execute(new Runnable() {
          public void run() {
            try {
              sendMessage(emailMessage);
              SWTUtil.showStatusMessage(null, "Email sent!");
            } catch (IOException e) {
              SWTUtil.showStatusErrorMessage(null, "Email not sent: "+e.getMessage());
            }
          }
        });
      }
    });
    return group;
  }

  static ExecutorService pool = Executors.newCachedThreadPool();

  private Button addButtonToBar(Composite group, String label, Control previous) {
    FormData fd = new FormData();

    Button button = new Button(group, SWT.PUSH);
    int top = (BUTTON_BAR_HEIGHT - button.computeSize(SWT.DEFAULT, SWT.DEFAULT).y) / 2; 
    button.setText(label);
    fd.bottom = new FormAttachment(100, -top);
    if (previous != null) {
      fd.left = new FormAttachment(previous, BUTTON_DISTANCE);
    } else {
      fd.left = new FormAttachment(0, BUTTON_DISTANCE);
    }
    button.setLayoutData(fd);

    return button;
  }

  private Text addLabeledTextField(Composite group, String label, Control previous) {
    return addLabeledTextField(group, label, previous, SWT.BORDER | SWT.SINGLE);
  }

  private Text addLabeledTextField(Composite group, String label, Control previous, int style) {
    FormData fd;

    Text t1 = new Text(group, style);
    fd = new FormData();
    if (previous != null) {
      fd.top = new FormAttachment(previous, TEXT_FIELD_DISTANCE, SWT.BOTTOM);
    } else {
      fd.top = new FormAttachment(0, 10);
    }
    fd.left = new FormAttachment(0, 80);
    fd.right = new FormAttachment(100, -10);
    t1.setLayoutData(fd);

    Label l1 = new Label(group, SWT.RIGHT);
    l1.setText(label);
    fd = new FormData();
    fd.top = new FormAttachment(t1, 0, SWT.CENTER);
    fd.right = new FormAttachment(t1, -10);
    fd.left = new FormAttachment(0, 10);
    l1.setLayoutData(fd);

    l1.setBackground(CommonColors.WHITE);

    return t1;
  }

  protected void enableActions() {
    if (!getControl().isDisposed()) {
      sendButton.setEnabled(true);
      clearButton.setEnabled(true);
    }
  }

  protected void disableActions() {
    if (!getControl().isDisposed()) {
      sendButton.setEnabled(false);
      clearButton.setEnabled(false);
    }
  }

  protected List<String> list(Text text) {
    String value = text.getText();
    List<String> result = new LinkedList<String>();
    String[] tokens = value.split(",");
    for (String token : tokens) {
      result.add(token);
    }
    return result;
  }

  private RpcController getActionsController() {
    return getActionsClient().getController();
  }

  protected ActionsClient getActionsClient() {
    return (ActionsClient)super.getActionsClient();
  }

  public BlockingInterface getActionsService() {
    return getActionsClient().getActionsService();
  }

  private void sendMessage(EmailMessage emailMessage) throws IOException {
    try {
      try {
        SendMessage.Builder request = SendMessage.newBuilder();
        request.setSenderId(emailMessage.getFrom());
        request.addAllRecipientIds(emailMessage.getTo());
        request.addAllRecipientCCIds(emailMessage.getCc());
        request.addAllRecipientBCCIds(emailMessage.getBcc());
        request.setSubject(emailMessage.getSubject());
        if (emailMessage.getMessageBodyAsText() != null) 
        request.setMessageBody(emailMessage.getMessageBodyAsText());
        else if (emailMessage.getMessageBodyAsHtml() != null) {
          request.setMessageBody(emailMessage.getMessageBodyAsHtml());
        }
        getActionsService().interfaceSendMessage(getActionsController(), request.build());
        getActionsClient().checkCommunicationResponse();
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        throw ActionsClient.failure("SendMail: Communication error", e);
      }
    } catch (IOException e) {
      showStatus(e.getMessage());
    }

  }

  /* (non-Javadoc)
   * @see com.testingtech.ttworkbench.play.widget.smtp.ui.ISMTPModel#updateStatus(com.testingtech.ttworkbench.play.widget.smtp.ui.ConnectionState, long, java.lang.String)
   */
  public void updateStatus(ConnectionState connectionState, long statusCode, String statusMessage) {
    showStatus(connectionState+" "+statusMessage);
  }

  protected void showStatus(String message) {
    SWTUtil.showStatusMessage(null, "SMTP: "+message);
  }

  public ISMTPModel getModel() {
    return this;
  }
}
