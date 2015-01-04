create table proxies (
  id int not null auto_increment primary key,
  host varchar(45),
  port int not null,
  proxy_type varchar(45) comment 'http, https, socks',
  status int not null default 0 comment '状态，1：可用；0：不可用',
  latency int DEFAULT 0 COMMENT 'latency in ms',
  last_check int not null default 0,
  source VARCHAR(500),
  ok_cnt int default 0 not null comment 'check ok times',
  fail_cnt int default 0 not null comment 'fail times when check',
  source_domain VARCHAR(200),
  created_at timestamp not null DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT into proxies(host, port, proxy_type, status, source)
  select host, port, proxyType, 0, 'chenjie' from proxy
