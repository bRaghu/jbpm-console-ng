/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.ht.client.editors.taskcomments;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import java.util.Comparator;
import java.util.Date;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.ht.model.CommentSummary;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.event.Event;
import org.jbpm.console.ng.ht.client.i18n.Constants;
import org.jbpm.console.ng.ht.client.resources.HumanTasksImages;
import org.jbpm.console.ng.ht.client.util.ResizableHeader;
import org.jbpm.console.ng.pr.model.ProcessInstanceSummary;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
@Templated(value = "TaskCommentsViewImpl.html")
public class TaskCommentsViewImpl extends Composite implements TaskCommentsPresenter.TaskCommentsView {
    private Constants constants = GWT.create(Constants.class);
    private HumanTasksImages images = GWT.create( HumanTasksImages.class );

    private TaskCommentsPresenter presenter;

    @Inject
    @DataField
    public ControlLabel commentsAccordionLabel;

    @Inject
    @DataField
    public TextArea newTaskCommentTextArea;
    
    @Inject
    @DataField
    public Label newTaskCommentLabel;

    @Inject
    @DataField
    public Button addCommentButton;

   

    @Inject
    @DataField
    public DataGrid<CommentSummary> commentsListGrid;

    @Inject
    @DataField
    public SimplePager pager;

    @Inject
    @DataField
    public FlowPanel listContainer;

    private ListHandler<CommentSummary> sortHandler;

    @Inject
    private Event<NotificationEvent> notification;

    @Override
    public TextArea getNewTaskCommentTextArea() {
        return newTaskCommentTextArea;
    }

    @Override
    public Button addCommentButton() {
        return addCommentButton;
    }

    @Override
    public DataGrid<CommentSummary> getDataGrid() {
        return commentsListGrid;
    }

    @Override
    public SimplePager getPager() {
        return pager;
    }

    @Override
    public void init(TaskCommentsPresenter presenter) {
        this.presenter = presenter;
        listContainer.add(commentsListGrid);
        listContainer.add(pager);
        commentsListGrid.setHeight("100px");
        
        commentsAccordionLabel.add(new HTMLPanel( constants.Add_Comment()) );
        
        commentsListGrid.setEmptyTableWidget(new HTMLPanel(constants.No_Comments_For_This_Task()));
        // Attach a column sort handler to the ListDataProvider to sort the list.
        sortHandler = new ListHandler<CommentSummary>(presenter.getDataProvider().getList());
        commentsListGrid.addColumnSortHandler(sortHandler);
        initTableColumns();
        presenter.addDataDisplay(commentsListGrid);
        // Create a Pager to control the table.
        pager.setVisible(false);
        pager.setDisplay(commentsListGrid);
        pager.setPageSize(6);
        newTaskCommentTextArea.setWidth("300px");
        addCommentButton.setText(constants.Add_Comment());
        newTaskCommentLabel.setText(constants.Comment());
    }

    @EventHandler("addCommentButton")
    public void addCommentButton(ClickEvent e) {
        if(!newTaskCommentTextArea.getText().equals("")){
            presenter.addTaskComment(newTaskCommentTextArea.getText(), new Date());
        }else{
            displayNotification("The Comment cannot be empty!");
        }
    }
    
    @Override
    public void displayNotification( String text ) {
        notification.fire( new NotificationEvent( text ) );
    }

    private void initTableColumns() {
        // addedBy
        Column<CommentSummary, String> addedByColumn = new Column<CommentSummary, String>(new TextCell()) {
            @Override
            public String getValue(CommentSummary c) {
                // for some reason the username comes in format [User:'<name>'], so parse just the <name>
                int first = c.getAddedBy().indexOf('\'');
                int last = c.getAddedBy().lastIndexOf('\'');
                return c.getAddedBy().substring(first + 1, last);
            }
        };
        addedByColumn.setSortable(false);
        commentsListGrid.addColumn(addedByColumn, constants.Added_By());
        commentsListGrid.setColumnWidth(addedByColumn, "100px");

        // date
        Column<CommentSummary, String> addedAtColumn = new Column<CommentSummary, String>(new TextCell()) {
            @Override
            public String getValue(CommentSummary c) {
                DateTimeFormat format = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");
                return format.format(c.getAddedAt());
            }
        };
        addedAtColumn.setSortable(true);
        addedAtColumn.setDefaultSortAscending(true);
        commentsListGrid.addColumn(addedAtColumn, constants.At());
        sortHandler.setComparator(addedAtColumn, new Comparator<CommentSummary>() {
            @Override
            public int compare(CommentSummary o1, CommentSummary o2) {
                return o1.getAddedAt().compareTo(o2.getAddedAt());
            }
        });

        // comment text
        Column<CommentSummary, String> commentTextColumn = new Column<CommentSummary, String>(new TextCell()) {
            @Override
            public String getValue(CommentSummary object) {
                return object.getText();
            }
        };
        addedByColumn.setSortable(false);
        commentsListGrid.addColumn(commentTextColumn, constants.Comment());
        
        List<HasCell<CommentSummary, ?>> cells = new LinkedList<HasCell<CommentSummary, ?>>();
        
        cells.add( new DeleteCommentActionHasCell( "Delete", new Delegate<CommentSummary>() {
            @Override
            public void execute( CommentSummary comment ) {

                presenter.removeTaskComment(comment.getId());
            }
        } ) );

        CompositeCell<CommentSummary> cell = new CompositeCell<CommentSummary>( cells );
        Column<CommentSummary, CommentSummary> actionsColumn = new Column<CommentSummary, CommentSummary>(
                cell ) {
            @Override
            public CommentSummary getValue( CommentSummary object ) {
                return object;
            }
        };
        commentsListGrid.addColumn( actionsColumn, new ResizableHeader( "", commentsListGrid,
                                                                                    actionsColumn ) );
    }
    
    private class DeleteCommentActionHasCell implements HasCell<CommentSummary, CommentSummary> {

        private ActionCell<CommentSummary> cell;

        public DeleteCommentActionHasCell( String text,
                                   Delegate<CommentSummary> delegate ) {
            cell = new ActionCell<CommentSummary>( text, delegate ) {
                @Override
                public void render( Cell.Context context,
                                    CommentSummary value,
                                    SafeHtmlBuilder sb ) {
                    
                        AbstractImagePrototype imageProto = AbstractImagePrototype.create( images.abortGridIcon() );
                        SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                        mysb.appendHtmlConstant( "<span title='" + constants.Delete() + "' style='margin-right:5px;'>");
                        mysb.append( imageProto.getSafeHtml() );
                        mysb.appendHtmlConstant( "</span>" );
                        sb.append( mysb.toSafeHtml() );
                    
                }
            };
        }

        @Override
        public Cell<CommentSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<CommentSummary, CommentSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public CommentSummary getValue( CommentSummary object ) {
            return object;
        }
    }
}
