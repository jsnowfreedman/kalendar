insert into events (id, name, short_description, long_description, advisories, event_start, event_end,
                    attendee_cancellation_by, creator_id, status)
values (gen_random_uuid(),
        'Kotlin 101',
        'Learn about all the fun of Kotlin..maybe',
        'no long description ;-;',
        '',
        now(),
        now() + (20 || ' minutes')::interval,
        now(),
        ((SELECT id FROM contacts WHERE email = 'example@example.com')),
        'approved');