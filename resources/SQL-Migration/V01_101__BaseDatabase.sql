-- Removes tese foreign keys which were causing records to be deleted on cascade when we didn't want them to
ALTER TABLE `user` DROP FOREIGN KEY `user_ibfk_1`;
ALTER TABLE `program` DROP FOREIGN KEY `program_ibfk_1`;