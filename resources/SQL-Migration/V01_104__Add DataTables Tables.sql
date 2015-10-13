-- Adds new table to store values used in DataTable
create table data_table_rows(
    id INT NOT NULL AUTO_INCREMENT,
    node_id INT,
    PRIMARY KEY (id),
    FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE ON UPDATE CASCADE);

create table data_table_values(
    id INT NOT NULL AUTO_INCREMENT,
    data_table_id INT,
    data_key VARCHAR(1000),
    data_value VARCHAR(1000),
    PRIMARY KEY (id),
    FOREIGN KEY (data_table_id) REFERENCES data_table_rows(id) ON DELETE CASCADE ON UPDATE CASCADE);