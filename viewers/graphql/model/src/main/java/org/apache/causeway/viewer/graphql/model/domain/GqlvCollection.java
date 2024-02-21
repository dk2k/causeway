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
package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;

public class GqlvCollection
        extends GqlvAssociation<OneToManyAssociation, GqlvCollection.Holder>
        implements GqlvAssociationGet.Holder<OneToManyAssociation>,
                   GqlvMemberHidden.Holder<OneToManyAssociation>,
                   GqlvMemberDisabled.Holder<OneToManyAssociation>,
                   GqlvAssociationDatatype.Holder<OneToManyAssociation> {

    private final GqlvMemberHidden<OneToManyAssociation> hidden;
    private final GqlvMemberDisabled<OneToManyAssociation> disabled;
    private final GqlvCollectionGet get;
    private final GqlvCollectionDatatype datatype;

    public GqlvCollection(
            final Holder holder,
            final OneToManyAssociation otma,
            final Context context
    ) {
        super(holder, otma, TypeNames.collectionTypeNameFor(holder.getObjectSpecification(), otma), context);

        if(isBuilt()) {
            this.hidden = null;
            this.disabled = null;
            this.get = null;
            this.datatype = null;
            return;
        }
        addChildFieldFor(this.hidden = new GqlvMemberHidden<>(this, context));
        addChildFieldFor(this.disabled = new GqlvMemberDisabled<>(this, context));
        addChildFieldFor(this.get = new GqlvCollectionGet(this, context));
        addChildFieldFor(this.datatype = new GqlvCollectionDatatype(this, context));

        buildObjectTypeAndField(otma.getId(), otma.getCanonicalDescription().orElse(otma.getCanonicalFriendlyName()));
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return holder.getObjectSpecification();
    }


    @Override
    protected void addDataFetchersForChildren() {
        if(hidden == null) {
            return;
        }
        hidden.addDataFetcher(this);
        disabled.addDataFetcher(this);
        get.addDataFetcher(this);
        datatype.addDataFetcher(this);
    }

}