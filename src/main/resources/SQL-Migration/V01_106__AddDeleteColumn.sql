-- Adds scheduled removal for data to certain columns - 0 for keep - 1 for delete

alter table test_command add column forDelete tinyint(1);
alter table test add column forDelete tinyint(1);
alter table http_headers add column forDelete tinyint(1);
alter table recorded_requests add column forDelete tinyint(1);
alter table http_proxies add column forDelete tinyint(1);

create index ind_forDelete_test_command on test_command (forDelete);
create index ind_forDelete_test on test (forDelete);
create index ind_forDelete_http_headers on http_headers (forDelete);
create index ind_forDelete_recorded_requests on recorded_requests (forDelete);
create index ind_forDelete_http_proxies on http_proxies (forDelete);