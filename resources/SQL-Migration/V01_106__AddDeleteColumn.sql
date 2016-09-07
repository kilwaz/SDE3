-- Adds scheduled removal for data to certain columns - 0 for keep - 1 for delete

alter table test_command add column forDelete tinyint(1);
alter table test add column forDelete tinyint(1);
alter table http_headers add column forDelete tinyint(1);
alter table recorded_requests add column forDelete tinyint(1);
alter table http_proxies add column forDelete tinyint(1);