insert into contacts (id, name, phone, email)
values (gen_random_uuid(), 'Joshua Freedman', '012-345-6789', 'example@example.com');

insert into contact_sso_sub_binding (contact_id, sub_value, oauth_provider)
values ((SELECT id FROM contacts WHERE email = 'example@example.com'),
        'fe2410c0-1124-4b33-a4e0-52d20adbaf96',
        'DallasMakerspace');