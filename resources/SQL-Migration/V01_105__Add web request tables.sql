-- Adds new tables for recording request information

create table http_proxies(
    id INT NOT NULL AUTO_INCREMENT,
    request_count INT,
    PRIMARY KEY (id));

create table recorded_requests(
    id INT NOT NULL AUTO_INCREMENT,
    http_proxy_id INT,
    url VARCHAR(3000),
    duration INT,
    request_size INT,
    response_size INT,
    request_content MEDIUMTEXT,
	response_content MEDIUMTEXT,
    PRIMARY KEY (id),
    FOREIGN KEY (http_proxy_id) REFERENCES http_proxies(id) ON DELETE CASCADE ON UPDATE CASCADE);

create table http_headers(
    id INT NOT NULL AUTO_INCREMENT,
    request_id INT,
    header_name VARCHAR(1000),
    header_value VARCHAR(1000),
    header_type VARCHAR(100),
    PRIMARY KEY (id),
    FOREIGN KEY (request_id) REFERENCES recorded_requests(id) ON DELETE CASCADE ON UPDATE CASCADE);