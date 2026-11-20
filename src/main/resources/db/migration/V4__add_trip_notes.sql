CREATE TABLE trip_notes
(
    trip_id    UUID         NOT NULL
        PRIMARY KEY
        CONSTRAINT fk_trip_notes_trip
            REFERENCES trips (id)
            ON DELETE CASCADE,
    content    TEXT         NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL,
    updated_by VARCHAR(255)
);
