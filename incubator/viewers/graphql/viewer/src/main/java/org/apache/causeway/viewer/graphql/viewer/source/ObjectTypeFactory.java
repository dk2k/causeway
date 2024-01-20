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
package org.apache.causeway.viewer.graphql.viewer.source;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class ObjectTypeFactory {

    private final GraphQLTypeRegistry graphQLTypeRegistry;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final SpecificationLoader specificationLoader;

    public void createGqlObjectTypeWithFetchers(
            final ObjectSpecification objectSpec,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val gqlvDomainObject = new GqlvDomainObject(objectSpec, codeRegistryBuilder, bookmarkService, objectManager, specificationLoader);

        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlvDomainObject.getMetaField().getType());
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(gqlvDomainObject.getGqlInputObjectType());

        gqlvDomainObject.addPropertiesAsFields();
        gqlvDomainObject.addCollectionsAsLists();
        gqlvDomainObject.addActionsAsFields();

        gqlvDomainObject.getMutatorsTypeIfAny()
                .ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);

        // build and register object type
        GraphQLObjectType graphQLObjectType = gqlvDomainObject.buildGqlObjectType();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(graphQLObjectType);

        // create and register data fetchers
        gqlvDomainObject.createAndRegisterDataFetchersForMetaData();
        gqlvDomainObject.createAndRegisterDataFetchersForMutators();

        gqlvDomainObject.createAndRegisterDataFetchersForField();
        gqlvDomainObject.createAndRegisterDataFetchersForCollection();
    }

}
