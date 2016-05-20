-- Turns the data

alter table data_table_values drop data_order;
alter table data_table_values add column data_order INT;