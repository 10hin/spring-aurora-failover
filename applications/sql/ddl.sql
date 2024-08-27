CREATE TABLE IF NOT EXISTS `test` (
    `id` int AUTO_INCREMENT NOT NULL,
    `text_val` varchar(1024) NOT NULL DEFAULT '',
    `int_val` int NOT NULL DEFAULT 0,
    CONSTRAINT `test_id_pkc` PRIMARY KEY (`id`)
);
