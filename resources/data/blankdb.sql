-- Creates a blank database with all the basic information

drop table if exists data_table_values;
drop table if exists http_headers;
drop table if exists input;
drop table if exists node_colour;
drop table if exists node_details;
drop table if exists program;
drop table if exists recorded_requests;
drop table if exists http_proxies;
drop table if exists serialized;
drop table if exists switch;
drop table if exists test_step;
drop table if exists test_result;
drop table if exists trigger_condition;
drop table if exists user;
drop table if exists data_table_rows;
drop table if exists node;
drop table if exists schema_version;
drop table if exists test;
drop table if exists test_command;

create table node(
    uuid char(36) NOT NULL,
    program_id char(36),
    node_type VARCHAR(100),
    PRIMARY KEY (uuid));

create table http_proxies(
    uuid char(36) NOT NULL,
    request_count INT,
    PRIMARY KEY (uuid));

create table input(
    uuid char(36) NOT NULL,
    node_id char(36),
    variable_name VARCHAR(100),
    variable_value VARCHAR(100),
    PRIMARY KEY (uuid));

create table data_table_rows(
    uuid char(36) NOT NULL,
    node_id char(36),
    PRIMARY KEY (uuid));

create table data_table_values(
    uuid char(36) NOT NULL,
    data_table_id char(36),
    data_key VARCHAR(1000),
    data_value VARCHAR(1000),
    PRIMARY KEY (uuid));

create table node_colour(
    uuid char(36) NOT NULL,
    node_type VARCHAR(100),
    colour_r INT,
    colour_g INT,
    colour_b INT,
    PRIMARY KEY (uuid));

create table node_details(
    uuid char(36) NOT NULL,
    node_id char(36),
    object_name VARCHAR(100),
    object_value BLOB,
    object_class VARCHAR(100),
    PRIMARY KEY (uuid));

create table recorded_requests(
    uuid char(36) NOT NULL,
    http_proxy_id char(36),
    url VARCHAR(3000),
    duration INT,
    request_size INT,
    response_size INT,
    request_content MEDIUMTEXT,
    response_content MEDIUMTEXT,
    PRIMARY KEY (uuid));

create table serialized(
    uuid char(36) NOT NULL,
    node_id char(36),
    serial_object MEDIUMBLOB,
    serial_reference VARCHAR(1000),
    PRIMARY KEY (uuid));

create table switch(
    uuid char(36) NOT NULL,
    node_id char(36),
    target VARCHAR(100),
    enabled tinyint,
    PRIMARY KEY (uuid));

create table test_result(
    uuid char(36) NOT NULL,
    PRIMARY KEY (uuid));

create table test_step(
    uuid char(36) NOT NULL,
    test_string VARCHAR(1000),
    expected_equal VARCHAR(1000),
    observed_equal VARCHAR(1000),
    screenshot mediumblob,
    successful tinyint,
    test_result char(36),
    test_type INT,
    PRIMARY KEY (uuid));

create table trigger_condition(
    uuid char(36) NOT NULL,
    node_id char(36),
    trigger_watch VARCHAR(100),
    trigger_when VARCHAR(100),
    trigger_then VARCHAR(100),
    PRIMARY KEY (uuid));

create table user(
    uuid char(36) NOT NULL,
    username VARCHAR(100),
    last_program char(36),
    PRIMARY KEY (uuid));

create table program(
    uuid char(36) NOT NULL,
    name VARCHAR(100),
    start_node char(36),
    view_offset_height DOUBLE,
    view_offset_width DOUBLE,
    user_id char(36),
    PRIMARY KEY (uuid));

create table http_headers(
    uuid char(36) NOT NULL,
    request_id char(36),
    header_name VARCHAR(1000),
    header_value VARCHAR(1000),
    header_type VARCHAR(100),
    PRIMARY KEY (uuid));

create table test(
    uuid char(36) NOT NULL,
    node_id char(36),
    text VARCHAR(10000),
    PRIMARY KEY (uuid));

create table test_command(
    uuid char(36) NOT NULL,
    test_id char(36),
    main_command VARCHAR(1000),
    raw_command VARCHAR(1000),
    command_position INT,
    PRIMARY KEY (uuid));