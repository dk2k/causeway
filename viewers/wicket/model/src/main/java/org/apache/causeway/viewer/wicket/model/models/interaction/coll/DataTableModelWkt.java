/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.wicket.model.models.interaction.coll;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.tabular.interactive.DataTableInteractive;
import org.apache.causeway.viewer.commons.model.object.HasUiParentObject;
import org.apache.causeway.viewer.commons.model.object.UiObject;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Bound to a BookmarkedObjectWkt, with the {@link DataTableInteractive}
 * representing either a <i>Collection</i> or an <i>Action</i>'s result.
 *
 * @implSpec the state of the DataTableModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle;
 * it is recreated on load
 *
 * @see HasBookmarkedOwnerAbstract
 */
public class DataTableModelWkt
extends HasBookmarkedOwnerAbstract<DataTableInteractive>
implements
    HasUiParentObject<UiObject> {

    // -- FACTORIES

    public static DataTableModelWkt forActionModel(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull ObjectAction actMetaModel,
            final @NonNull ManagedObject actionResult) {

        val managedAction = ManagedAction
                .of(bookmarkedObjectModel.getObject(), actMetaModel, Where.NOT_SPECIFIED);
        val tableInteractive = DataTableInteractive.forAction(
                managedAction,
                actionResult);
        return new DataTableModelWkt(
                bookmarkedObjectModel, actMetaModel.getFeatureIdentifier(), tableInteractive);
    }

    public static @NonNull DataTableModelWkt forCollection(
            final @NonNull BookmarkedObjectWkt bookmarkedObjectModel,
            final @NonNull OneToManyAssociation collMetaModel) {

        val tableInteractive = DataTableInteractive.forCollection(
                ManagedCollection
                .of(bookmarkedObjectModel.getObject(), collMetaModel, Where.NOT_SPECIFIED));
        return new DataTableModelWkt(
                bookmarkedObjectModel, collMetaModel.getFeatureIdentifier(), tableInteractive);
    }

    // -- CONSTRUCTION

    private static final long serialVersionUID = 1L;

    @Getter private final Identifier featureIdentifier;
    private final DataTableInteractive.Memento tableMemento;

    private DataTableModelWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final Identifier featureIdentifier,
            final DataTableInteractive tableInteractive) {
        super(bookmarkedObject);
        this.featureIdentifier = featureIdentifier;
        this.tableMemento = tableInteractive.getMemento();
        setObject(tableInteractive); // memoize
        setupBindings(tableInteractive);
    }

    // --

    @Override
    public UiObject getParentUiModel() {
        return ()->super.getBookmarkedOwner();
    }

    @Override
    protected DataTableInteractive load() {
        val tableInteractive = tableMemento.getDataTableModel(getBookmarkedOwner());
        setupBindings(tableInteractive);
        return tableInteractive;
    }

    // -- HELPER

    /**
     * Feed changes to the {@link DataTableInteractive} model back into our table memento.
     */
    private void setupBindings(final @NonNull DataTableInteractive tableInteractive) {
        tableInteractive.getSearchArgument().addListener((e, o, searchArg)->{
            tableMemento.setSearchArgument(searchArg);
        });
        tableInteractive.getSelectionChanges().addListener((e, o, n)->{
            tableMemento.setSelectedRowsAsBookmarks(tableInteractive.getSelectedRowsAsBookmarks());
        });
    }

}
