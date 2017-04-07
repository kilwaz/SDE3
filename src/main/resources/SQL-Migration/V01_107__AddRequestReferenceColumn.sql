-- Adds scheduled removal for data to certain columns - 0 for keep - 1 for delete

alter table recorded_requests add column reference varchar(100);