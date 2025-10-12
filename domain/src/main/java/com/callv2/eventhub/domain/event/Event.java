package com.callv2.eventhub.domain.event;

import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.callv2.eventhub.domain.AggregateRoot;
import com.callv2.eventhub.domain.validation.ValidationError;
import com.callv2.eventhub.domain.validation.ValidationHandler;

public class Event extends AggregateRoot<EventID> {

    private final Key key;
    private final Source source;
    private final Instant occurredAt;
    private final Instant ingestedAt;
    private final Set<EventEntity> relatedEntities;
    private final Map<String, Object> data;

    private Event(
            final EventID id,
            final Key key,
            final Source source,
            final Instant occurredAt,
            final Instant ingestedAt,
            final Set<EventEntity> relatedEntities,
            final Map<String, Object> data) {
        super(id);
        this.key = key;
        this.source = source;
        this.occurredAt = occurredAt;
        this.ingestedAt = ingestedAt;
        this.relatedEntities = nonNull(relatedEntities) ? new HashSet<>(relatedEntities) : new HashSet<>();
        this.data = nonNull(data) ? new HashMap<>(data) : new HashMap<>();
    }

    @Override
    public void validate(final ValidationHandler handler) {

        if (id == null)
            handler.append(ValidationError.with("'id' must not be null"));

        if (key == null)
            handler.append(ValidationError.with("'key' must not be null"));

        if (source == null)
            handler.append(ValidationError.with("'source' must not be null"));

        if (occurredAt == null)
            handler.append(ValidationError.with("'occurredAt' must not be null"));

        if (ingestedAt == null)
            handler.append(ValidationError.with("'ingestedAt' must not be null"));

        if (relatedEntities == null)
            handler.append(ValidationError.with("'relatedEntities' must not be null"));

        if (data == null)
            handler.append(ValidationError.with("'data' must not be null"));

        key.validate(handler);
        source.validate(handler);
        relatedEntities.forEach(entity -> entity.validate(handler));

        final String keyDomain = getKey().domain();
        if (!keyDomain.equals(getSource().domain()))
            handler.append(
                    ValidationError.with("'source.domain' must be equal to key.domain [%s]".formatted(keyDomain)));

        if (getRelatedEntities().stream().anyMatch(entity -> !keyDomain.equals(entity.domain())))
            handler.append(
                    ValidationError.with("'relation.domain' must be equal to key.domain [%s]".formatted(keyDomain)));

    }

    public static Event create(
            final String domain,
            final String entity,
            final String action,
            final String service,
            final String version,
            final Instant occurredAt,
            final Set<EventEntity> relatedEntities,
            final Map<String, Object> data) {

        return new Event(
                EventID.unique(),
                Key.from(domain, entity, action),
                Source.from(domain, service, version),
                occurredAt,
                Instant.now(),
                relatedEntities, data);
    }

    public static Event with(
            final EventID id,
            final Key key,
            final Source source,
            final Instant occurredAt,
            final Instant ingestedAt,
            final Set<EventEntity> relatedEntities,
            final Map<String, Object> data) {
        return new Event(id, key, source, occurredAt, ingestedAt, relatedEntities, data);
    }

    public Key getKey() {
        return key;
    }

    public Source getSource() {
        return source;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public Set<EventEntity> getRelatedEntities() {
        return relatedEntities;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

}
