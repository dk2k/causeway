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
package org.apache.isis.applib.mixins.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactn.Interaction;


/**
 * Allows domain objects that represent or are associated with a system
 * {@link Interaction} to act as a mixee in order that other modules can
 * contribute behaviour.
 *
 * @since 2.0 {@index}
 */
public interface HasInteractionId {

    @Property(
            hidden = Where.EVERYWHERE,
            maxLength = InteractionIdStr.MAX_LENGTH
    )
    @Parameter(
            maxLength = InteractionIdStr.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface InteractionIdStr {
        int MAX_LENGTH = 36;
        boolean NULLABLE = InteractionId.NULLABLE;
        String ALLOWS_NULL = InteractionId.ALLOWS_NULL;
        String NAME = "interactionId";
    }


    @Property(
            editing = Editing.DISABLED
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteractionId {
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }


    /**
     * A unique identifier (a GUID).
     */
    @InteractionId
    UUID getInteractionId();


}
