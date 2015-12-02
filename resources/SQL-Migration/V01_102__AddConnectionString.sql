-- Adds the connection string column to the http proxies table

alter table http_proxies add column connection_string VARCHAR(1000);