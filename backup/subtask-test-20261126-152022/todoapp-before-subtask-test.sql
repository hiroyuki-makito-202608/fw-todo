-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: todoapp
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `todoapp`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `todoapp` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `todoapp`;

--
-- Table structure for table `todo_attachments`
--

DROP TABLE IF EXISTS `todo_attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `todo_attachments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `todo_id` bigint NOT NULL,
  `original_file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `content_type` varchar(255) DEFAULT NULL,
  `file_size` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_todo_attachments_stored_file_name` (`stored_file_name`),
  KEY `fk_todo_attachments_todo` (`todo_id`),
  CONSTRAINT `fk_todo_attachments_todo` FOREIGN KEY (`todo_id`) REFERENCES `todos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `todo_attachments`
--

LOCK TABLES `todo_attachments` WRITE;
/*!40000 ALTER TABLE `todo_attachments` DISABLE KEYS */;
INSERT INTO `todo_attachments` VALUES (1,25,'キャライノベイトヒアリング.txt','b1bd3c95-0e0e-4dc0-8fc4-c920b56780b9.txt','text/plain',5871,'2026-11-26 10:35:12');
/*!40000 ALTER TABLE `todo_attachments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `todo_categories`
--

DROP TABLE IF EXISTS `todo_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `todo_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_todo_categories_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `todo_categories`
--

LOCK TABLES `todo_categories` WRITE;
/*!40000 ALTER TABLE `todo_categories` DISABLE KEYS */;
INSERT INTO `todo_categories` VALUES (1,'就職活動','2026-11-26 11:07:15'),(2,'プログラミング','2026-11-26 11:07:15'),(3,'資格','2026-11-26 11:07:15'),(4,'マーケティング','2026-11-26 11:07:15'),(5,'デザイン','2026-11-26 11:07:15'),(8,'掃除','2026-11-26 11:19:47');
/*!40000 ALTER TABLE `todo_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `todo_subtasks`
--

DROP TABLE IF EXISTS `todo_subtasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `todo_subtasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `todo_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `position` int NOT NULL DEFAULT '0',
  `completed` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_todo_subtasks_todo_id_position` (`todo_id`,`position`),
  CONSTRAINT `fk_todo_subtasks_todo` FOREIGN KEY (`todo_id`) REFERENCES `todos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `todo_subtasks`
--

LOCK TABLES `todo_subtasks` WRITE;
/*!40000 ALTER TABLE `todo_subtasks` DISABLE KEYS */;
INSERT INTO `todo_subtasks` VALUES (17,24,'1',0,1,'2026-11-26 12:32:39'),(18,24,'2',1,1,'2026-11-26 12:32:39'),(19,24,'3',2,1,'2026-11-26 12:32:39'),(20,24,'4',3,1,'2026-11-26 12:32:39'),(23,25,'まとめ１',0,1,'2026-11-26 13:42:28'),(24,25,'まとめ２',1,1,'2026-11-26 13:42:28'),(25,23,'1',0,1,'2026-11-26 14:10:24'),(26,23,'2',1,1,'2026-11-26 14:10:24'),(27,23,'3',2,1,'2026-11-26 14:10:24'),(28,23,'4',3,0,'2026-11-26 14:10:24'),(32,29,'1',0,0,'2026-11-26 14:54:33'),(33,29,'3',1,0,'2026-11-26 14:54:33');
/*!40000 ALTER TABLE `todo_subtasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `todos`
--

DROP TABLE IF EXISTS `todos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `todos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `category` varchar(255) NOT NULL,
  `category_id` bigint DEFAULT NULL,
  `priority` int NOT NULL DEFAULT '2',
  `due_date` date DEFAULT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'TODO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_todos_category` (`category_id`),
  CONSTRAINT `fk_todos_category` FOREIGN KEY (`category_id`) REFERENCES `todo_categories` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `todos`
--

LOCK TABLES `todos` WRITE;
/*!40000 ALTER TABLE `todos` DISABLE KEYS */;
INSERT INTO `todos` VALUES (2,'競合サイトを3つ調べる','','就職活動',1,2,'2026-11-26',0,'TODO','2026-11-06 15:08:31','2026-11-26 11:07:15'),(3,'FW演習の課題を仕上げる','','プログラミング',2,1,'2026-11-11',1,'DONE','2026-11-06 15:08:31','2026-11-26 11:07:15'),(4,'ITパスポートの過去問を解く','','資格',3,2,'2026-12-15',0,'TODO','2026-11-06 15:08:31','2026-11-26 11:07:15'),(5,'職務経歴書を書き直す','','就職活動',1,3,'2027-01-05',1,'DONE','2026-11-06 15:08:31','2026-11-26 11:07:15'),(7,'競合サイトを3つ調べる','','マーケティング',4,1,'2026-11-18',0,'TODO','2026-11-09 02:43:47','2026-11-26 11:07:15'),(8,'ITパスポートの過去問を解く','かきかわった？メモ','資格',3,2,'2026-11-19',0,'IN_PROGRESS','2026-11-09 02:51:31','2026-11-26 11:07:15'),(16,'うううう','ｒｒｒｒ','資格',3,3,'2026-11-19',1,'DONE','2026-11-11 01:30:12','2026-11-26 11:07:15'),(21,'テスト1117-1','テスト','デザイン',5,1,'2026-11-18',0,'IN_PROGRESS','2026-11-17 05:41:41','2026-11-26 15:06:18'),(22,'11111','11111','デザイン',5,1,NULL,1,'DONE','2026-11-17 06:02:59','2026-11-26 11:07:15'),(23,'22222','222222','マーケティング',4,2,NULL,1,'COMPLETED','2026-11-17 06:03:11','2026-11-26 14:13:52'),(24,'3333333','3333333','掃除',8,2,NULL,1,'COMPLETED','2026-11-17 06:03:23','2026-11-26 12:32:20'),(25,'キャライノベイトヒアリングをまとめる','444444','デザイン',5,1,'2026-12-03',0,'IN_PROGRESS','2026-11-17 06:18:11','2026-11-26 12:38:26'),(26,'競合サイトを3つ調べる',NULL,'マーケティング',4,2,'2026-12-01',0,'TODO','2026-11-25 05:09:32','2026-11-26 11:07:15'),(27,'履歴書の自己PRを見直す',NULL,'就職活動',1,2,'2026-11-04',0,'IN_PROGRESS','2026-11-25 15:01:23','2026-11-26 14:01:35'),(28,'面接で聞かれそうな質問を準備する',NULL,'就職活動',1,1,'2026-11-04',0,'TODO','2026-11-25 15:01:27','2026-11-26 11:07:15'),(29,'応募先企業の事業内容を調べる','','就職活動',1,3,'2026-11-04',0,'TODO','2026-11-25 15:01:31','2026-11-26 14:12:56'),(30,'応募する',NULL,'就職活動',1,3,'2026-11-04',0,'IN_PROGRESS','2026-11-25 15:02:32','2026-11-26 11:42:24'),(31,'aaaa','aaaaa','デザイン',5,2,'2026-11-27',0,'TODO','2026-11-26 12:04:56','2026-11-26 12:06:06');
/*!40000 ALTER TABLE `todos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'todoapp'
--

--
-- Dumping routines for database 'todoapp'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-26 15:20:47
