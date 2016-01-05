-- Adds the locked column to the program table

alter table program add column locked tinyint(1); -- Max value 255