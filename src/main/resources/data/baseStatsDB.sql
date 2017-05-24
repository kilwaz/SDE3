-- Creates a blank database with all the basic information for the app statistics

drop table if exists app_statistics;

-- Tables
create table app_statistics(
    uuid char(36) NOT NULL,
    total_requests BIGINT,
    total_up_time BIGINT,
    total_request_size BIGINT,
    total_response_size BIGINT,
    total_application_starts BIGINT,
    total_commands BIGINT,
    total_program_starts BIGINT,
    PRIMARY KEY (uuid));