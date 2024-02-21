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

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.viewer.graphql.model.context.Context;

import lombok.Getter;
import lombok.val;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class GqlvDomainService
        extends GqlvAbstractCustom
        implements GqlvAction.Holder {

    @Getter private final ObjectSpecification objectSpecification;
    @Getter private final Object servicePojo;

    private final List<GqlvAction> actions = new ArrayList<>();

    public static GqlvDomainService of(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final Context context) {
        return context.domainServiceBySpec.computeIfAbsent(objectSpecification, spec -> new GqlvDomainService(spec, servicePojo, context));
    }

    public GqlvDomainService(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final Context context) {
        super(TypeNames.objectTypeNameFor(objectSpecification), context);

        this.objectSpecification = objectSpecification;
        this.servicePojo = servicePojo;

        if(isBuilt()) {
            return;
        }

        addActions();
        if (hasActions()) {
            buildObjectTypeAndField(TypeNames.objectTypeNameFor(this.objectSpecification), this.objectSpecification.getDescription());
        }
    }

    public boolean hasActions() {
        return !actions.isEmpty();
    }

    private void addActions() {

        val apiVariant = context.causewayConfiguration.getViewer().getGraphql().getApiVariant();
        objectSpecification.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isSafeInNature() ||
                        apiVariant != CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_ONLY    // the other variants have an entry for all actions.
                )
                .forEach(act -> actions.add(addChildFieldFor(new GqlvAction(this, act, context))));
    }


    @Override
    protected void addDataFetchersForChildren() {
        if (hasActions()) {
            actions.forEach(act -> act.addDataFetcher(this));
        }
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return getServicePojo();
    }


    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}