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
package org.apache.isis.core.metamodel.facets.value.semantics;

import java.math.BigDecimal;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.ValueSemantics;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.TypedHolderAbstract;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;

import lombok.val;

public class ValueSemanticsAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ValueSemanticsAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        val valueSemanticsIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        ValueSemantics.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                            .addValidationFailure(processMethodContext.getFacetHolder(), ValueSemantics.class));
        processProvider(processMethodContext.getFacetHolder(), valueSemanticsIfAny);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        if(BigDecimal.class != processParameterContext.getParameterType()) {
            return;
        }
        val valueSemanticsIfAny = processParameterContext.synthesizeOnParameter(ValueSemantics.class);
        processProvider(processParameterContext.getFacetHolder(), valueSemanticsIfAny);
    }

    // -- HELPER

    void processProvider(
            final TypedHolderAbstract facetHolder,
            final Optional<ValueSemantics> valueSemanticsIfAny) {

        // check for @ValueSemantics(provider=...)
        addFacetIfPresent(
                ValueSemanticsSelectingFacetForAnnotation
                .create(valueSemanticsIfAny, facetHolder));
    }

}
