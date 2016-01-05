-- Adds the data order column to data table values

alter table data_table_values add column data_order tinyint(1); -- Max value 255