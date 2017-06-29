-- Adds columns for test recording

alter table test add column recording_file LONGBLOB;
alter table test add column web_driver_id varchar(100);