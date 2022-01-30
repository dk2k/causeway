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
package org.apache.isis.core.metamodel.valuesemantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.ApplicationFeatureIdValueSemantics")
public class ApplicationFeatureIdValueSemantics
extends ValueSemanticsAbstract<ApplicationFeatureId>
implements
    EncoderDecoder<ApplicationFeatureId>,
    Parser<ApplicationFeatureId>,
    Renderer<ApplicationFeatureId> {

    @Override
    public Class<ApplicationFeatureId> getCorrespondingClass() {
        return ApplicationFeatureId.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final ApplicationFeatureId object) {
        return object!=null
                ? object.asEncodedString()
                : null;
    }

    @Override
    public ApplicationFeatureId fromEncodedString(final String data) {
        if(data==null) {
            return null;
        }
        return ApplicationFeatureId.parseEncoded(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final ApplicationFeatureId value) {
        return value == null ? "" : value.stringify();
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final ApplicationFeatureId value) {
        return value == null ? null : value.stringify();
    }

    @Override
    public ApplicationFeatureId parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? ApplicationFeatureId.parse(input)
                : null;
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 255;
    }

    @Override
    public Can<ApplicationFeatureId> getExamples() {
        return Can.of(
                ApplicationFeatureId.newNamespace("a.namespace.only"),
                ApplicationFeatureId.newMember("a.namespace", "with_member_id"));
    }

}