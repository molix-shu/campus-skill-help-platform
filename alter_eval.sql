USE skill_help_db;
ALTER TABLE `evaluation` ADD COLUMN `score_speed` tinyint DEFAULT 5;
ALTER TABLE `evaluation` ADD COLUMN `score_attitude` tinyint DEFAULT 5;
ALTER TABLE `evaluation` ADD COLUMN `score_quality` tinyint DEFAULT 5;
ALTER TABLE `evaluation` ADD COLUMN `image_url` LONGTEXT;
