CREATE TABLE IF NOT EXISTS `mg_user_bools` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` BINARY(16) NOT NULL,
  `property` varchar(100) NOT NULL,
  `value` tinyint(1) DEFAULT '0',
  `version` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`uuid`,`property`)
);
CREATE TABLE IF NOT EXISTS `mg_user_ints` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` BINARY(16) NOT NULL,
  `property` varchar(100) NOT NULL,
  `value` int(11) DEFAULT '0',
  `version` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`uuid`,`property`)
);
CREATE TABLE IF NOT EXISTS `mg_user_strings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` BINARY(16) NOT NULL,
  `property` varchar(100) NOT NULL,
  `value` varchar(255) DEFAULT '0',
  `version` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`uuid`,`property`)
);
CREATE TABLE IF NOT EXISTS `mg_user_doubles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` BINARY(16) NOT NULL,
  `property` varchar(100) NOT NULL,
  `value` double DEFAULT '0',
  `version` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`uuid`,`property`)
);
CREATE TABLE IF NOT EXISTS `mg_user_score` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` binary(16) NOT NULL,
  `game` varchar(50) NOT NULL,
  `value` double DEFAULT NULL,
  `version` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index` (`uuid`,`game`)
);

CREATE TABLE IF NOT EXISTS mg_schema_version (
  version   INT(11)      NOT NULL,
  plugin    VARCHAR(50) NOT NULL,
  timestamp DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (version, plugin)
);

INSERT INTO mg_schema_version (version, plugin) VALUES (2, 'MinigamesBase');