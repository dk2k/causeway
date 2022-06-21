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
package org.apache.isis.extensions.commandlog.applib.dom;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.Getter;
import lombok.val;

public abstract class CommandLogEntryRepository<C extends CommandLogEntry> {

    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Command not found");
            this.interactionId = interactionId;
        }
    }

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;

    private final Class<C> commandLogClass;

    protected CommandLogEntryRepository(Class<C> commandLogClass) {
        this.commandLogClass = commandLogClass;
    }

    /** Creates a transient (yet not persisted) {@link CommandLogEntry} instance. */
    public C createCommandLog(final Command command) {
        C c = factoryService.detachedEntity(commandLogClass);
        c.setCommandDto(command.getCommandDto());
        return c;
    }

    public Optional<C> findByInteractionId(final UUID interactionId) {
        return repositoryService().firstMatch(
                Query.named(commandLogClass,  CommandLogEntry.Nq.FIND_BY_INTERACTION_ID_STR)
                        .withParameter("interactionIdStr", interactionId.toString()));
    }

    public List<C> findByParent(final CommandLogEntry parent) {
        return repositoryService().allMatches(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_PARENT)
                        .withParameter("parent", parent));
    }

    public List<C> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<C> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND);
            }
        }
        return repositoryService().allMatches(query);
    }

    public List<C> findCurrent() {
        return repositoryService().allMatches(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_CURRENT));
    }

    public List<C> findCompleted() {
        return repositoryService().allMatches(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_COMPLETED));
    }


    public List<C> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {

        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<C> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_TARGET)
                        .withParameter("target", target);
            }
        }
        return repositoryService().allMatches(query);
    }

    public List<C> findRecentByUsername(final String username) {
        return repositoryService().allMatches(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username));
    }



    public List<C> findRecentByTarget(final Bookmark target) {
        return repositoryService().allMatches(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target));
    }


    /**
     * Intended to support the replay of commands on a secondary instance of
     * the application.
     *
     * This finder returns all (completed) {@link CommandLogEntry}s started after
     * the command with the specified interactionId.  The number of commands
     * returned can be limited so that they can be applied in batches.
     *
     * If the provided interactionId is null, then only a single
     * {@link CommandLogEntry command} is returned.  This is intended to support
     * the case when the secondary does not yet have any
     * {@link CommandLogEntry command}s replicated.  In practice this is unlikely;
     * typically we expect that the secondary will be set up to run against a
     * copy of the primary instance's DB (restored from a backup), in which
     * case there will already be a {@link CommandLogEntry command} representing the
     * current high water mark on the secondary system.
     *
     * If the interactionId is not null but the corresponding
     * {@link CommandLogEntry command} is not found, then <tt>null</tt> is returned.
     * In the replay scenario the caller will probably interpret this as an
     * error because it means that the high water mark on the secondary is
     * inaccurate, referring to a non-existent {@link CommandLogEntry command} on
     * the primary.
     *
     * @param interactionId - the identifier of the {@link CommandLogEntry command} being
     *                   the replay HWM (using {@link #findMostRecentReplayed()} on the
     *                   secondary), or null if no HWM was found there.
     * @param batchSize - to restrict the number returned (so that replay
     *                   commands can be batched).
     */
    public List<C> findSince(final UUID interactionId, final Integer batchSize) {
        if(interactionId == null) {
            return findFirst();
        }
        final C from = findByInteractionIdElseNull(interactionId);
        if(from == null) {
            return Collections.emptyList();
        }
        return findSince(from.getTimestamp(), batchSize);
    }

    private List<C> findFirst() {
        Optional<C> firstCommandIfAny = repositoryService().firstMatch(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_FIRST));
        return firstCommandIfAny
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    /**
     * The most recent replayed command previously replicated from primary to
     * secondary.
     *
     * <p>
     * This should always exist except for the very first times
     * (after restored the prod DB to secondary).
     * </p>
     */
    public Optional<C> findMostRecentReplayed() {
        return repositoryService().firstMatch(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_MOST_RECENT_REPLAYED));
    }

    /**
     * The most recent completed command, as queried on the
     * secondary.
     *
     * <p>
     *     After a restart following the production database being restored
     *     from primary to secondary, would correspond to the last command
     *     run on primary before the production database was restored to the
     *     secondary.
     * </p>
     */
    public Optional<C> findMostRecentCompleted() {
        return repositoryService().firstMatch(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_MOST_RECENT_COMPLETED));
    }

    public List<C> findNotYetReplayed() {
        return repositoryService().allMatches(
                Query.named(commandLogClass, CommandLogEntry.Nq.FIND_NOT_YET_REPLAYED).withLimit(10));
    }


    public C saveForReplay(final CommandDto dto) {

        if(dto.getMember().getInteractionType() == InteractionType.ACTION_INVOCATION) {
            final MapDto userData = dto.getUserData();
            if (userData == null ) {
                throw new IllegalStateException(String.format(
                        "Can only persist action DTOs with additional userData; got: \n%s",
                        CommandDtoUtils.toXml(dto)));
            }
        }

        final C commandJdo = factoryService.detachedEntity(commandLogClass);

        commandJdo.setInteractionIdStr(dto.getInteractionId());
        commandJdo.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()));
        commandJdo.setUsername(dto.getUser());

        commandJdo.setReplayState(ReplayState.PENDING);

        final OidDto firstTarget = dto.getTargets().getOid().get(0);
        commandJdo.setTarget(Bookmark.forOidDto(firstTarget));
        commandJdo.setCommandDto(dto);
        commandJdo.setLogicalMemberIdentifier(dto.getMember().getLogicalMemberIdentifier());

        persist(commandJdo);

        return commandJdo;
    }


    public List<C> saveForReplay(final CommandsDto commandsDto) {
        List<CommandDto> commandDto = commandsDto.getCommandDto();
        List<C> commands = new ArrayList<>();
        for (final CommandDto dto : commandDto) {
            commands.add(saveForReplay(dto));
        }
        return commands;
    }


    public void persist(final C commandLog) {
        repositoryService().persist(commandLog);
    }

    public void truncateLog() {
        repositoryService().removeAll(commandLogClass);
    }

    // --


    public List<C> findCommandsOnPrimaryElseFail(
            final @Nullable UUID interactionId,
            final @Nullable Integer batchSize) throws NotFoundException {

        final List<C> commands = findSince(interactionId, batchSize);
        if(commands == null) {
            throw new NotFoundException(interactionId);
        }
        return commands;
    }



    private C findByInteractionIdElseNull(final UUID interactionId) {
        val q = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_BY_INTERACTION_ID_STR)
                .withParameter("interactionIdStr", interactionId.toString());
        return repositoryService().uniqueMatch(q).orElse(null);
    }

    private List<C> findSince(
            final Timestamp timestamp,
            final Integer batchSize) {

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        // XXX that's a historic workaround, should rather be fixed upstream
        val needsTrimFix = batchSize != null && batchSize == 1;

        val q = Query.named(commandLogClass, CommandLogEntry.Nq.FIND_SINCE)
                .withParameter("timestamp", timestamp)
                .withRange(QueryRange.limit(
                        needsTrimFix ? 2L : batchSize
                ));

        final List<C> commandJdos = repositoryService().allMatches(q);
        return needsTrimFix && commandJdos.size() > 1
                ? commandJdos.subList(0,1)
                : commandJdos;
    }





    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

    private static Timestamp toTimestampStartOfDayWithOffset(
            final @Nullable LocalDate dt,
            final int daysOffset) {

        return dt!=null
                ? new java.sql.Timestamp(
                Instant.from(dt.atStartOfDay().plusDays(daysOffset).atZone(ZoneId.systemDefault()))
                        .toEpochMilli())
                : null;
    }



}