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
package org.apache.isis.core.metamodel.facets.object.navparent.annotation;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJupiterTestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.isis.core.metamodel.facets.object.navparent.annotation.NavigableParentTestSamples.DomainObjectA;
import org.apache.isis.core.metamodel.facets.object.navparent.method.NavigableParentFacetViaGetterMethod;

//FIXME[ISIS-3207]
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class NavigableParentAnnotationFacetFactoryTest
extends AbstractFacetFactoryJupiterTestCase {

    private NavigableParentAnnotationFacetFactory facetFactory;

    @BeforeEach
    void setUp() throws Exception {
        facetFactory = new NavigableParentAnnotationFacetFactory(metaModelContext);
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Test
    protected void testParentAnnotatedMethod() throws Exception {
        testParentMethod(new DomainObjectA(), "root");
    }

    // -- HELPER

    private void testParentMethod(final Object domainObject, final String parentMethodName) throws Exception {

        final Class<?> domainClass = domainObject.getClass();

        facetFactory.process(ProcessClassContext
                .forTesting(domainClass, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NavigableParentFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof NavigableParentFacetViaGetterMethod);

        final NavigableParentFacetViaGetterMethod navigableParentFacetMethod = (NavigableParentFacetViaGetterMethod) facet;
        final Method parentMethod = domainClass.getMethod(parentMethodName);

        assertEquals(
                parentMethod.invoke(domainObject, _Constants.emptyObjects),
                navigableParentFacetMethod.navigableParent(domainObject)	);

    }



}
