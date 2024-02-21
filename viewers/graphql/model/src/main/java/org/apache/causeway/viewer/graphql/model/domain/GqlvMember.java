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

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.fetcher.BookmarkedPojo;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;

import lombok.Getter;

public abstract class GqlvMember<T extends ObjectMember, H extends GqlvMember.Holder>
    extends GqlvAbstractCustom {

    @Getter final H holder;
    @Getter private final T objectMember;

    public GqlvMember(
            final H holder,
            final T objectMember,
            final String typeName,
            final Context context
    ) {
        super(typeName, context);
        this.holder = holder;
        this.objectMember = objectMember;

        objectMember.getCanonicalDescription().ifPresent(gqlObjectTypeBuilder::description);
    }

    public String getId() {
        return objectMember.getFeatureIdentifier().getFullIdentityString();
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment dataFetchingEnvironment) {
        return BookmarkedPojo.sourceFrom(dataFetchingEnvironment, context);
    }

    public interface Holder
            extends ObjectSpecificationProvider {

    }
}