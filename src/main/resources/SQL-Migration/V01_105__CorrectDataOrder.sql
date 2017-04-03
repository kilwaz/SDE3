-- Turns the data

create table data_table_values_new(
    uuid char(36) NOT NULL,
    data_table_id char(36),
    data_key VARCHAR(1000),
    data_value VARCHAR(1000),
    data_order INT,
    PRIMARY KEY (uuid));

insert into data_table_values_new(uuid,data_table_id,data_key,data_value) select uuid,data_table_id,data_key,data_value from data_table_values;
drop table data_table_values;
alter table data_table_values_new rename to data_table_values;