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
package org.apache.causeway.core.metamodel.facets.members.publish.command;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.internal.base._Optionals;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.PropertyConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.val;

public abstract class CommandPublishingFacetForPropertyAnnotation extends CommandPublishingFacetAbstract {

    static class Enabled extends CommandPublishingFacetForPropertyAnnotation {
        Enabled(CommandDtoProcessor processor, FacetHolder holder, ServiceInjector servicesInjector) {
            super(processor, holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class Disabled extends CommandPublishingFacetForPropertyAnnotation {
        Disabled(CommandDtoProcessor processor, FacetHolder holder, ServiceInjector servicesInjector) {
            super(processor, holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static CommandPublishingFacet create(
            final Optional<Property> propertyIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {

        val publishingPolicy = PropertyConfigOptions.propertyCommandPublishingPolicy(configuration);

        return propertyIfAny
            .filter(property -> property.commandPublishing() != Publishing.NOT_SPECIFIED)
            .map(property -> {
                Publishing publishing = property.commandPublishing();

                final Class<? extends CommandDtoProcessor> processorClass = property.commandDtoProcessor();
                final CommandDtoProcessor processor = newProcessorElseNull(processorClass);

                if(processor != null) {
                    publishing = Publishing.ENABLED;
                }
                switch (publishing) {
                    case AS_CONFIGURED:
                        switch (publishingPolicy) {
                            case NONE:
                                return (CommandPublishingFacet)new CommandPublishingFacetForPropertyAnnotationAsConfigured.None(holder, servicesInjector);
                            case ALL:
                                return new CommandPublishingFacetForPropertyAnnotationAsConfigured.All(holder, servicesInjector);
                            default:
                                throw new IllegalStateException(String.format("configured publishingPolicy '%s' not recognised", publishingPolicy));
                        }
                    case DISABLED:
                        return new CommandPublishingFacetForPropertyAnnotation.Disabled(processor, holder, servicesInjector);
                    case ENABLED:
                        return new CommandPublishingFacetForPropertyAnnotation.Enabled(processor, holder, servicesInjector);
                    default:
                        throw new IllegalStateException(String.format("commandPublishing '%s' not recognised", publishing));
                }
            }).orElseGet(() -> {
                switch (publishingPolicy) {
                    case NONE:
                        return new CommandPublishingFacetForPropertyFromConfiguration.None(holder, servicesInjector);
                    case ALL:
                        return new CommandPublishingFacetForPropertyFromConfiguration.All(holder, servicesInjector);
                    default:
                        throw new IllegalStateException(String.format("configured publishingPolicy '%s' not recognised", publishingPolicy));
                }
            }
        );
    }


    CommandPublishingFacetForPropertyAnnotation(
            final CommandDtoProcessor processor,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(processor, holder, servicesInjector);
    }


}
