alter table events
    alter column long_description type text using long_description::text;

alter table events
    alter column advisories type varchar(255) using advisories::varchar(255);
