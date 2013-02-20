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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.testingtech.ttworkbench.core.ui.CommonColors;
import com.testingtech.ttworkbench.core.ui.SWTUtil;
import com.testingtech.ttworkbench.core.ui.preferences.common.AbstractConfigurationBlock;
import com.testingtech.ttworkbench.play.dashboard.widget.AbstractDashboardWidget;
import com.testingtech.ttworkbench.play.dashboard.widget.DashboardWidgetFactoryDescriptor;
import com.testingtech.ttworkbench.play.dashboard.widget.IDashboard;
import com.testingtech.ttworkbench.play.generated.PROTO_Interface;
import com.testingtech.ttworkbench.play.generated.PROTO_Interface.ACTIONS.BlockingInterface;
import com.testingtech.ttworkbench.play.generated.PROTO_Interface.ListMessages;
import com.testingtech.ttworkbench.play.widget.pop3.ui.model.EmailMessage;

public class POP3Widget 
extends AbstractDashboardWidget<POP3Model, PROTO_Interface.ACTIONS.BlockingInterface>
implements IPop3ModelListener, DisposeListener {

  private POP3Model model = new POP3Model();
  
  public POP3Model getModel() {
    return model;
  }
  
  private Label fromLabel;
	private Label subjectLabel;
	private Text messageText;
	private TableViewer inboxTable;
  private Button getMailButton;
  private Button deleteMailButton;
  private Button configureButton;

	public POP3Widget(POP3WidgetFactory widgetFactory, IDashboard dashboard) {
		super(widgetFactory, dashboard);
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
  
  public void widgetDisposed(DisposeEvent arg0) {
    model.removeListener(this);
    super.widgetDisposed(arg0);
  }
	
	public Control createWidgetControl(Composite parent) {
	  model.addListener(this);
		DashboardWidgetFactoryDescriptor descriptor = getFactory().getDescriptor();
		Group group = AbstractConfigurationBlock.addGroup(parent, descriptor.getName());
    ((GridData)group.getLayoutData()).widthHint = 500;
    ((GridData)group.getLayoutData()).heightHint = 400;
		group.setToolTipText(descriptor.getDescription());
		group.setBackground(CommonColors.WHITE);

		Composite buttons = AbstractConfigurationBlock.addComposite(group, 2);
		getMailButton = AbstractConfigurationBlock.addButton(buttons, "Get Mail");
		deleteMailButton = AbstractConfigurationBlock.addButton(buttons, "Delete");
		configureButton = AbstractConfigurationBlock.addButton(buttons, "Configure");
		
		getMailButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
			  new Thread(new Runnable() {
          public void run() {
            getMail();
          }
        }).start();
			}
		});
		deleteMailButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
			  if (inboxTable.getSelection() instanceof IStructuredSelection) {
			    final Object[] selection = ((IStructuredSelection)inboxTable.getSelection()).toArray();
			    if (selection.length > 0) {
			      new Thread(new Runnable() {
		          public void run() {
		            deleteEmailMessage(Arrays.asList(selection));
		          }
		        }).start();
			    }
			  }
			}
		});

		
		SashForm splitter = new SashForm(group, SWT.VERTICAL);
		splitter.setLayoutData(new GridData(GridData.FILL_BOTH));

		createInboxTable(splitter);
		createDetails(splitter);
		
		// TODO REMOVE: used only for debug
		Integer c = (Integer)null;
		dummyData();
		
		inboxTable.setInput(model.getMails());

		return group;
	}

  private void createDetails(SashForm splitter) {
    Composite detailComposite = AbstractConfigurationBlock.addComposite(splitter);
		fromLabel = addLabel(detailComposite, "From:");
		subjectLabel = addLabel(detailComposite, "Subject:");
		messageText = addMultilineTextField(detailComposite);
		messageText.setEditable(false);
  }

  private void createInboxTable(SashForm splitter) {
    inboxTable = new TableViewer(splitter, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
	    GridData gd = new GridData(GridData.FILL_BOTH);
	    gd.heightHint = 100;
	    inboxTable.getTable().setLayoutData(gd);
	    inboxTable.getTable().setLinesVisible(true);
	    inboxTable.getTable().setHeaderVisible(true);
	    createTableViewerColumn(inboxTable, "From", 100);
	    createTableViewerColumn(inboxTable, "Subject", 200);
	    createTableViewerColumn(inboxTable, "Date", 100);

		inboxTable.setLabelProvider(new EmailMessageLabelProvider());
		inboxTable.setContentProvider(new ListElementsProvider());
		inboxTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
			  EmailMessage emailMessage = null;
			  if (event.getSelection() instanceof IStructuredSelection) {
			    Object[] selection = ((IStructuredSelection)event.getSelection()).toArray();
			    if (selection.length > 0) {
            emailMessage = (EmailMessage)selection[0];
			    }
			  }
			  showEmailMessage(emailMessage);
			}
		});
  }


  protected void enableActions() {
    if (!getControl().isDisposed()) {
      getMailButton.setEnabled(true);
      deleteMailButton.setEnabled(true);
    }
  }

  protected void disableActions() {
    if (!getControl().isDisposed()) {
      getMailButton.setEnabled(false);
      deleteMailButton.setEnabled(false);
    }
  }

  protected void deleteEmailMessage(List<Object> list) {
    try {
      for (Object m : list) {
        EmailMessage message = (EmailMessage) m;
        try {
          PROTO_Interface.DeleteMessage.Builder request = PROTO_Interface.DeleteMessage.newBuilder();
          request.setMessageId(message.getId());
          getActionsService().interfaceDeleteMessage(getActionsController(), request.build());
          getActionsClient().checkCommunicationResponse();
        } catch (IOException e) {
          throw e;
        } catch (Exception e) {
          throw ActionsClient.failure("Delete: Communication error", e);
        }
      }
    } catch (IOException e) {
      model.showStatus(e.getMessage());
    }
	}
	
  protected void getMail() {
    try {
      try {
        ListMessages.Builder request = ListMessages.newBuilder();
        getActionsService().interfaceListMessages(getActionsController(), request.build());
        getActionsClient().checkCommunicationResponse();
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        throw ActionsClient.failure("GetMail: Communication error", e);
      }
    } catch (IOException e) {
      model.showStatus(e.getMessage());
    }
  }

	private RpcController getActionsController() {
    return getActionsClient().getController();
  }

  protected ActionsClient getActionsClient() {
    return (ActionsClient)super.getActionsClient();
  }

  protected void showEmailMessage(EmailMessage emailMessage) {
		if (emailMessage == null) {
			fromLabel.setText("From:");
			subjectLabel.setText("Subject:");
			messageText.setText("");
			return;
		}

		String messageBody = "";
		if (emailMessage.getMessageBodyAsText() != null) {
			messageBody += emailMessage.getMessageBodyAsText();
		}
		if (emailMessage.getMessageBodyAsHtml() != null) {
			if (messageBody.length() > 0) {
				messageBody += "---- HTML content ----";
			}
			messageBody += emailMessage.getMessageBodyAsHtml();
		}

		fromLabel.setText("From: "+emailMessage.getFrom());
		subjectLabel.setText("Subject: "+emailMessage.getSubject());
		messageText.setText(messageBody);
	}
	

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	// TODO remove after debugging finished !
	@Deprecated
	private void dummyData() {
	  model.addEmail(new EmailMessage(0, System.currentTimeMillis()-5600000, "Joe <joe@smith.com>", Collections.singletonList("Luke <luke@skywalker.com>"), null, null, "Let's meet tomorrow", "Hi Luke,\n\nMeet you tomorrow at 9 AM for golf?\n\nRegards,\nJoe", null));
	  model.addEmail(new EmailMessage(1, System.currentTimeMillis(), "Tim <tim@smith.com>", Collections.singletonList("Luke <luke@skywalker.com>"), null, null, "Let's meet tomorrow", "Hi Luke,\n\nstill wan't a training this week?\n\nRegards,\nTimmy", null));
	}
  
  public BlockingInterface getActionsService() {
    return getActionsClient().getActionsService();
  }

  public void notifyModelChange() {
    String status = model.getStatus();
    if (status != null) {
      SWTUtil.showStatusMessage(null, "POP3: "+status);          
    }
    else {
      SWTUtil.showStatusMessage(null, null);
    }
    SWTUtil.async(new Runnable() {
      public void run() {
        inboxTable.refresh(); 
      }
    });
  }

  
}
