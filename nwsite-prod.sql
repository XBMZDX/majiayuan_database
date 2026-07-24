-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: nwsite
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
-- Table structure for table `ai_chat_feedback`
--

DROP TABLE IF EXISTS `ai_chat_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `user_id` int NOT NULL,
  `rating` tinyint DEFAULT NULL,
  `feedback_text` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_feedback_message` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI回复反馈';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_feedback`
--

LOCK TABLES `ai_chat_feedback` WRITE;
/*!40000 ALTER TABLE `ai_chat_feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chat_feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_message`
--

DROP TABLE IF EXISTS `ai_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `user_id` int NOT NULL,
  `role` varchar(20) NOT NULL,
  `content` longtext NOT NULL,
  `content_format` varchar(20) DEFAULT 'text',
  `model_name` varchar(80) DEFAULT NULL,
  `token_count` int DEFAULT '0',
  `finish_reason` varchar(40) DEFAULT NULL,
  `latency_ms` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_session` (`session_id`,`deleted`),
  KEY `idx_ai_chat_message_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI聊天消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_message`
--

LOCK TABLES `ai_chat_message` WRITE;
/*!40000 ALTER TABLE `ai_chat_message` DISABLE KEYS */;
INSERT INTO `ai_chat_message` VALUES (23,3,1,'user','你好','text','local',0,'sent',0,'2026-07-22 10:03:06',0),(24,3,1,'assistant','你好，我在。\n\n我当前能结合的资料：\n业务模块：病害调查\n项目：车复原（CR-MRQ4J9PR）\n文物：M45C2\n\n如果你愿意，我可以继续按你的数据库内容帮你分析、总结、生成说明，或者直接回答你的具体问题。','text','local',0,'local',0,'2026-07-22 10:03:06',0);
/*!40000 ALTER TABLE `ai_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_reference`
--

DROP TABLE IF EXISTS `ai_chat_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_reference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `source_type` varchar(80) DEFAULT NULL,
  `source_id` bigint DEFAULT NULL,
  `source_title` varchar(255) DEFAULT NULL,
  `source_summary` text,
  `source_url` varchar(1000) DEFAULT NULL,
  `relevance_score` decimal(6,2) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_reference_message` (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI消息引用来源';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_reference`
--

LOCK TABLES `ai_chat_reference` WRITE;
/*!40000 ALTER TABLE `ai_chat_reference` DISABLE KEYS */;
INSERT INTO `ai_chat_reference` VALUES (6,24,'project',9,'车复原','','/conservation/overview',0.90,'2026-07-22 10:03:06');
/*!40000 ALTER TABLE `ai_chat_reference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_session`
--

DROP TABLE IF EXISTS `ai_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_code` varchar(60) NOT NULL,
  `user_id` int NOT NULL,
  `username` varchar(80) DEFAULT NULL,
  `session_title` varchar(255) NOT NULL,
  `context_module` varchar(80) DEFAULT NULL,
  `context_project_id` bigint DEFAULT NULL,
  `context_project_name` varchar(255) DEFAULT NULL,
  `context_resource_id` bigint DEFAULT NULL,
  `context_resource_name` varchar(255) DEFAULT NULL,
  `model_name` varchar(80) DEFAULT 'local',
  `system_prompt` text,
  `pinned` tinyint DEFAULT '0',
  `message_count` int DEFAULT '0',
  `last_message_preview` varchar(1000) DEFAULT NULL,
  `last_message_time` datetime DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_session_code` (`session_code`),
  KEY `idx_ai_chat_session_user` (`user_id`,`deleted`),
  KEY `idx_ai_chat_session_last_time` (`last_message_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI聊天会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_session`
--

LOCK TABLES `ai_chat_session` WRITE;
/*!40000 ALTER TABLE `ai_chat_session` DISABLE KEYS */;
INSERT INTO `ai_chat_session` VALUES (1,'AI-20260722093859-6542',1,'123456','新对话','conservation',NULL,'',NULL,'','local','',0,0,NULL,NULL,1,'2026-07-22 09:38:59','2026-07-22 10:02:49'),(2,'AI-20260722094458-2862',1,'123456','新对话','disease',9,'车复原',NULL,'','deepseek-chat','',0,22,'我明白你的意思：马家塬是什么。\n\n我当前能结合的资料：\n业务模块：病害调查\n项目：车复原（CR-MRQ4J9PR）\n文物：M45C2\n\n如果你愿意，我可以继续按你的数据库内容帮你分析、总结、生成说明，或者直接回答你的具体问题。','2026-07-22 10:02:12',1,'2026-07-22 09:44:58','2026-07-22 10:02:48'),(3,'AI-20260722100253-4692',1,'123456','新对话','disease',9,'车复原',NULL,'','local','',0,2,'你好，我在。\n\n我当前能结合的资料：\n业务模块：病害调查\n项目：车复原（CR-MRQ4J9PR）\n文物：M45C2\n\n如果你愿意，我可以继续按你的数据库内容帮你分析、总结、生成说明，或者直接回答你的具体问题。','2026-07-22 10:03:06',0,'2026-07-22 10:02:53','2026-07-22 10:03:06');
/*!40000 ALTER TABLE `ai_chat_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_template`
--

DROP TABLE IF EXISTS `ai_chat_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_code` varchar(80) NOT NULL,
  `template_name` varchar(255) NOT NULL,
  `scenario_name` varchar(255) DEFAULT NULL,
  `prompt_text` longtext NOT NULL,
  `context_module` varchar(80) DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `enabled` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_template_code` (`template_code`),
  KEY `idx_ai_chat_template_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI提示词模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_template`
--

LOCK TABLES `ai_chat_template` WRITE;
/*!40000 ALTER TABLE `ai_chat_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chat_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `analysis_results`
--

DROP TABLE IF EXISTS `analysis_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `analysis_results` (
  `id` int NOT NULL AUTO_INCREMENT,
  `detection_id` int NOT NULL COMMENT '关联detection_analysis.id',
  `artifact_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物编号（自动写入）',
  `artifact_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物名称（自动写入）',
  `sample_photo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样品照片（自动写入）',
  `experiment_method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验方法（自动写入=总览目的字段）',
  `detection_purpose` text COLLATE utf8mb4_unicode_ci COMMENT '检测目的（用户填写）',
  `instrument_model` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '仪器型号（用户填写）',
  `test_params` text COLLATE utf8mb4_unicode_ci COMMENT '测试参数（用户填写）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `analysis_results`
--

LOCK TABLES `analysis_results` WRITE;
/*!40000 ALTER TABLE `analysis_results` DISABLE KEYS */;
INSERT INTO `analysis_results` VALUES (1,97,'CX:1','盖弓帽','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/f50cc550-3832-4c5f-b658-111c542c1ac3.jpg','超景深显微镜',NULL,NULL,NULL,'2026-07-15 18:03:30','2026-07-15 18:03:30'),(2,98,'CX2','盖弓帽','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/951a79c7-5f57-4941-ba26-60b64cbc76c0.jpg','超景深显微镜',NULL,NULL,NULL,'2026-07-15 18:04:44','2026-07-15 18:04:44'),(7,99,'CX:3','盖弓帽','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/0670d352-44c1-4a7d-995c-0ed9af3bf685.jpg','超景深显微镜',NULL,NULL,NULL,'2026-07-16 10:02:44','2026-07-16 10:02:44'),(12,102,'M6：64','琉璃杯残片','','金相显微镜观察',NULL,NULL,NULL,'2026-07-22 14:48:33','2026-07-22 14:48:33'),(13,102,'M6：64','琉璃杯残片','','超景深显微镜',NULL,NULL,NULL,'2026-07-22 14:48:33','2026-07-22 14:48:33'),(16,103,'MD：9','坠饰','','金相显微镜观察',NULL,NULL,NULL,'2026-07-22 14:53:52','2026-07-22 14:53:52'),(17,103,'MD：9','坠饰','','超景深显微镜',NULL,NULL,NULL,'2026-07-22 14:53:52','2026-07-22 14:53:52'),(18,104,'M6:69','','','超景深显微镜',NULL,NULL,NULL,'2026-07-23 11:22:40','2026-07-23 11:22:40'),(27,111,'18','紫色陶珠',NULL,'超景深显微镜',NULL,NULL,NULL,'2026-07-23 12:59:48','2026-07-23 12:59:48'),(28,112,'18','紫色陶珠',NULL,'金相显微镜观察',NULL,NULL,NULL,'2026-07-23 12:59:48','2026-07-23 12:59:48'),(29,113,'18','紫色陶珠',NULL,'X射线荧光光谱分析(XRF)',NULL,NULL,NULL,'2026-07-23 12:59:48','2026-07-23 12:59:48');
/*!40000 ALTER TABLE `analysis_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifact_image`
--

DROP TABLE IF EXISTS `artifact_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artifact_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `artifact_id` int NOT NULL COMMENT '关联文物ID',
  `image_url` varchar(1000) NOT NULL COMMENT '图片URL',
  `image_name` varchar(255) DEFAULT NULL COMMENT '图片名称',
  `description` varchar(1000) DEFAULT NULL COMMENT '图片说明',
  `is_cover` tinyint DEFAULT '0' COMMENT '是否封面',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_artifact_image_artifact` (`artifact_id`),
  KEY `idx_artifact_image_cover` (`artifact_id`,`is_cover`),
  KEY `idx_artifact_image_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文物详情图片图库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifact_image`
--

LOCK TABLES `artifact_image` WRITE;
/*!40000 ALTER TABLE `artifact_image` DISABLE KEYS */;
INSERT INTO `artifact_image` VALUES (1,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/14f58fd6-6afe-4154-af6f-a0db80ddd2f6.jpg','测试点位图.jpg','',0,1,'2026-07-22 15:37:02','2026-07-22 15:38:21',1),(2,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/a47982c2-aee2-4687-af41-cbd88d730d33.jpg','jinxiang.jpg','',0,2,'2026-07-22 15:37:02','2026-07-22 15:38:23',1),(3,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/49477e4f-ec0e-4399-a7c5-6eca76672c57.jpg','xrf.jpg','',0,3,'2026-07-22 15:37:02','2026-07-22 15:38:24',1),(4,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/2112d3f9-3ebc-453b-a93b-a5b18da72151.jpg','BtDkjjWHQe0p52XAs-lnUIYhV4nadleku1uobSth3L35WxdAfOzDQcRkXJv_UYvTvryV7h81RNFbZuT2f0wfR71pejXimTsCcuF1EIC6iECE3xid5EymTmvU2vsQ15BFvE747oGBYSDHeGV9M_siWfiJS2ZgPy3-M3AxLUIZ-PFsmiIeNEaLdH1QTGnAEdVH.jpg','',0,4,'2026-07-22 15:37:02','2026-07-22 15:38:16',1),(5,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/57e53335-ed77-418a-97c1-9174104aa05b.jpg','637666117274685333931.jpg','',0,4,'2026-07-22 15:37:02','2026-07-22 15:38:19',1),(6,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/187d3cee-08a2-4def-acbc-c1671c1f1991.jpg','微信图片_20260703165825_12_34.jpg','',0,5,'2026-07-22 15:37:02','2026-07-22 15:38:26',1),(7,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/a2befafa-d93e-4b52-8ce7-1740cbb93902.jpg','能谱图.jpg','',0,6,'2026-07-22 15:37:02','2026-07-22 15:38:14',1),(8,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/929fc17d-0157-4939-bdf5-f3835babb5ee.jpg','123.jpg','',0,7,'2026-07-22 15:37:04','2026-07-23 13:55:17',1),(9,842,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/842/1295a271-2e50-4936-9153-09590da6e4bf.jpg','123.jpg','',1,1,'2026-07-23 10:55:29','2026-07-23 10:55:29',0),(10,555,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/555/86b4e701-da97-4498-9fce-3099495bded2.jpg','123.jpg','',1,1,'2026-07-23 13:50:32','2026-07-23 13:50:32',0),(11,553,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/80a03f4e-eb87-40b6-b029-fba9fe8e6ba8.jpg','123.jpg','',1,1,'2026-07-23 13:55:17','2026-07-23 13:55:17',0),(12,554,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/554/b451432b-eef8-4f5b-b7e0-1de5ae475ab3.jpg','123.jpg','',1,1,'2026-07-23 15:45:58','2026-07-23 15:45:58',0);
/*!40000 ALTER TABLE `artifact_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artifacts`
--

DROP TABLE IF EXISTS `artifacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artifacts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `serial_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_artifact_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物新编号',
  `new_artifact_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物新名称',
  `original_artifact_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物原始编号',
  `original_artifact_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物原名称',
  `material1` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `completeness` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `artifact_description` text COLLATE utf8mb4_unicode_ci,
  `quantity1` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity2` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dimensions` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `weight` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `excavation_relic` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出土遗迹',
  `excavation_position` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出土位置',
  `excavation_time` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出土时间',
  `storage_method` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '存放方式',
  `images` text COLLATE utf8mb4_unicode_ci,
  `transfer_process` text COLLATE utf8mb4_unicode_ci,
  `restoration_status` text COLLATE utf8mb4_unicode_ci,
  `photographer` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拍照人',
  `draftsperson` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绘图人',
  `text_describer` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文字描述人',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `grading_status` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '定级情况',
  `testing_status` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '科技检测情况',
  `created_by` int DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `material2` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `burial_id` int DEFAULT NULL COMMENT '所属墓葬ID',
  `coffin_id` int DEFAULT NULL COMMENT '所属棺ID',
  `chariot_id` int DEFAULT NULL COMMENT '所属车ID',
  `coffin_index` int DEFAULT '1' COMMENT '棺序号（1=棺1, 2=棺2...）',
  `chariot_index` int DEFAULT NULL COMMENT '车序号（1=车1, 2=车2...）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=846 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artifacts`
--

LOCK TABLES `artifacts` WRITE;
/*!40000 ALTER TABLE `artifacts` DISABLE KEYS */;
INSERT INTO `artifacts` VALUES (553,'1','','','18','紫色陶珠','人工硅酸盐','完整',NULL,'1颗','1颗','直径0.85、高0.64、孔径0.33','0.5','M6','1号盗洞','2007.11.24',NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/553/80a03f4e-eb87-40b6-b029-fba9fe8e6ba8.jpg','负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-23 13:55:17','釉砂（紫陶）',6,NULL,NULL,NULL,NULL),(554,'2','','','M6：64','琉璃杯残片','人工硅酸盐','残碎',NULL,'2件','2残块','长2.6、宽1.5、厚0.44','总重4.4','M6','墓室盗洞','2008.5.11',NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/554/b451432b-eef8-4f5b-b7e0-1de5ae475ab3.jpg','负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-23 15:45:58','玻璃',6,NULL,NULL,NULL,NULL),(555,'3','','','M6：69','琉璃杯残片','人工硅酸盐','残碎',NULL,'1件','2残块','长1.5、宽1.47、厚0.41','1.7','M6','墓室盗洞','2008.5.11',NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/artifact-images/555/86b4e701-da97-4498-9fce-3099495bded2.jpg','负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-23 13:50:32','玻璃',6,NULL,NULL,NULL,NULL),(556,'4','','','138','珠\r\n(扁状珠)','人工硅酸盐','完整',NULL,'1颗','1颗','长0.51、宽0.56、厚0.33、孔径0.16','约0.02','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（扁状珠）',6,NULL,NULL,NULL,NULL),(557,'5','','','139','珠\r\n(扁状珠)','人工硅酸盐','完整',NULL,'1颗','1颗','长0.55、宽0.57、厚0.31、孔径0.18','约0.02','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（扁状珠）',6,NULL,NULL,NULL,NULL),(558,'6','','','140','珠\r\n(扁状珠)','人工硅酸盐','完整',NULL,'1颗','1颗','长0.51、宽0.47、厚0.31、孔径0.14','约0.02','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（扁状珠）',6,NULL,NULL,NULL,NULL),(559,'7','','','141','珠\r\n(扁状珠)','人工硅酸盐','完整',NULL,'1颗','1颗','长0.57、宽0.59、厚0.34、孔径0.18','约0.02','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（扁状珠）',6,NULL,NULL,NULL,NULL),(560,'8','','','142','珠\r\n(扁状珠)','人工硅酸盐','残碎',NULL,'1颗','残为2半','长0.55','约0.02','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（扁状珠）',6,NULL,NULL,NULL,NULL),(561,'9','','','143','珠\r\n(扁状珠)','人工硅酸盐','完整',NULL,'1颗','1颗','长0.55、宽0.58、厚0.34、孔径0.2','约0.02','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（扁状珠）',6,NULL,NULL,NULL,NULL),(562,'10','','','144','小珠\r\n（汉紫）','人工硅酸盐','残渣',NULL,'1小袋','残渣','/','约0.01','M6','墓室盗洞','2008.5.11',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（汉紫）',6,NULL,NULL,NULL,NULL),(563,'11','','','147','玻璃珠','人工硅酸盐','残碎',NULL,'2件','2残块','长1.44、宽0.8、厚0.54','总1.7','M6','墓室盗洞','2008.5.12',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(564,'12','','','160','绿色小珠','人工硅酸盐','完整',NULL,'1颗','1颗','直径0.51、高0.36、孔径0.24','约0.05','M6','墓室盗洞','2008.5.16',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(565,'13','','','161','绿色小珠','人工硅酸盐','完整',NULL,'1小袋','6颗','直径0.51、高0.39、孔径0.22','0.4','M6','墓室盗洞','2008.5.17',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','标签为7颗\r\n实为6颗','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(566,'14','','','162','黑色小珠','人工硅酸盐','完整',NULL,'1颗','1件（炭精）','直径0.41、高0.32、孔径0.1','约0.05','M6','墓室盗洞','2008.5.18',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(567,'15','','','164','小珠','人工硅酸盐','完整',NULL,'2颗','2颗','方形7件','约0.05','M6','墓室盗洞','2008.5.20',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(568,'16','','','165','黑色小珠','人工硅酸盐','完整',NULL,'1颗','\r\n1颗\r\n','直径0.41、高0.28、孔径0.16','约0.06','M6','墓室盗洞','2008.5.21',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(569,'17','','','178','紫陶珠','人工硅酸盐','完整',NULL,'22颗','完整21颗\r\n残半1颗','直径0.9、高0.6、孔径0.34','总重11.1','M6','墓室盗洞','2008.5.28',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（紫陶）',6,NULL,NULL,NULL,NULL),(570,'18','','','180','串珠','人工硅酸盐','少量完整，余残碎',NULL,'1小袋','（数量不详）少量完整，余残碎','铅白 直径0.26、厚0.19、\r\n孔径0.15\r\n汉蓝 直径0.27、厚0.2、\r\n孔径0.15\r\n汉紫 直径0.26、厚0.19、\r\n孔径0.14','总重1.8','M6','墓室盗洞','2008.5.28',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（铅白、汉蓝、汉紫）',6,NULL,NULL,NULL,NULL),(571,'19','','','181','串珠','人工硅酸盐','残',NULL,'1小袋','完整4件，余残碎','直径0.48、厚0.29、孔径0.17','总重1.5','M6','墓室盗洞','2008.5.28',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（陶珠）',6,NULL,NULL,NULL,NULL),(572,'20','','','193','玻璃珠','人工硅酸盐','残碎',NULL,'3件','2残半\r\n1颗于2008年9月13日陈建林老师借走做检测，谢炎签字','长1.93、宽0.82、孔径0.27','2.8','M6','墓室盗洞','2008.5.24',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(573,'21','','','09TZMM6：200','蓝色蜻蜓眼珠','人工硅酸盐','完整',NULL,'5颗','5颗','直径0.87～0.97、高0.56～0.74、口径约0.3','总重3.45','M6','墓室盗洞','',NULL,NULL,'2026.3.26二楼整理室定级文物柜','','现状照：陆君苗','','','','已定级（三级）','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','蜻蜓眼',6,NULL,NULL,NULL,NULL),(574,'22','','','205','碎陶珠','人工硅酸盐','残碎',NULL,'1小袋','数量不详','','总重9.6','M6','墓道盗洞','2008.5.28',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（铅白、汉蓝、汉紫）',6,NULL,NULL,NULL,NULL),(575,'23','','','207','残铁饰','金属、人工硅酸盐','铁：残段、\r\n釉砂：完整',NULL,'8件','铁3件、\r\n釉砂5颗','铁：衡面饰通长8.41、通宽6.82、厚度约0.4；条形通长7.44、通宽2.94、厚度约0.41；\r\n釉砂：直径0.73、高0.6、孔径0.28','铁：92.9、\r\n釉砂：2','M6','墓道盗洞','',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','铁金银、釉砂（汉紫）',6,NULL,NULL,NULL,NULL),(576,'24','','','208','紫陶珠','人工硅酸盐','完整14颗、\r\n残半1颗',NULL,'15颗','完整14颗、\r\n残半1颗','直径0.85、高0.63、孔径0.31','7.9','M6','盗洞','2008.5.24',NULL,NULL,'负8库\r\n→24.12.8一楼清棺室\r\n→202501二楼整理室\r\n→2025.12清点二楼整理室','','现状照：卢凯','','','','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂（紫陶）',6,NULL,NULL,NULL,NULL),(577,'25','','','72','玻璃珠','','残',NULL,NULL,'2件（其中1件残）','','','M6','墓室盗洞','',NULL,NULL,'未见','','','','','未见\r\n张川县博暂存清单上登记数量为1件','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(578,'26','','','199','玻璃珠','','残',NULL,NULL,'3件','','','M6','墓室盗洞','',NULL,NULL,'未见','','','','','未见\r\n蜻蜓眼\r\n张川县博暂存清单上有登记','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(579,'27','','','201','玻璃珠','','残',NULL,NULL,'1件','','','M6','墓室盗洞','',NULL,NULL,'未见','','','','','未见\r\n张川县博暂存清单上有登记','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','玻璃',6,NULL,NULL,NULL,NULL),(580,'28','','','220','蜻蜓眼珠','','',NULL,NULL,'5颗','','','M6','','',NULL,NULL,'未见','','','','','未见\r\n张川县博暂存清单上有登记','','',1,'2026-07-06 15:45:11','2026-07-06 15:45:11','釉砂',6,NULL,NULL,NULL,NULL),(581,'29','','','MD：9','坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'9件，汉蓝、铅白陶珠若干','9件，汉蓝、铅白陶珠若干','通高3.15，宽2.9，孔外径0.8、内径0.4','28.17','M16','墓道车北轸下','2020.7.27',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','铜、釉砂（铅蓝，汉白，汉紫）',11,NULL,NULL,NULL,NULL),(582,'30','','','MD：10','坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'29件，汉蓝、铅白、汉紫陶珠若干','29件，汉蓝、铅白、汉紫陶珠若干','通高3.15，宽2.9，孔外径0.8、内径0.4','86.57','M16','墓道车厢底部','2020.7.27',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','铜、釉砂（铅蓝，汉白，汉紫）',11,NULL,NULL,NULL,NULL),(583,'31','','','MDCX：3','后轸坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'1套','12件','通高3.15，宽2.9，孔外径0.8、内径0.4','总重28.15','M16','后轸偏北处，由北向南依次编为3-1~3-10，3-1铜饰距北轸12、后坠7、墓底20','2020.7.25',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','铜、釉砂（铅蓝，汉白，汉紫）',11,NULL,NULL,NULL,NULL),(584,'32','','','MDCX：4','车轸坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'1套','23件','通高3.15，宽2.9，孔外径0.8、内径0.4','铜饰件54.07,珠子16,12','M16','前轸下分布，由北向南第十串单独提取','2020.7.26',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','铜、釉砂（铅蓝，汉白，汉紫）',11,NULL,NULL,NULL,NULL),(585,'33','','','M62G:TB1','头部顶端料珠','玉石、人工硅酸盐','红玉髓完整，料珠有残破，呈碎渣状',NULL,'样杯4个','1-1汉蓝珠28颗在橙色盖样杯；1-2铅白珠17颗在橙色盖样杯；1-3红玉髓珠1颗，在橙色盖样杯；汉紫珠4颗，在橙色盖样杯','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；1-3红玉髓珠：外径0.47、孔径0.1、高0.33','0.02','M16','8-13～10,12-28～11两个坐标点之间往头骨顶部上端','23.08.24',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(586,'34','','','M62G:TB2','头部左侧珠子','人工硅酸盐','料珠有整有残。残破的呈碎渣状',NULL,'样杯3个','2-1汉蓝珠32颗在橙色盖样杯；2-2铅白珠3在橙色盖样杯；2-3红玉髓珠2颗，在橙色盖样杯；汉紫珠10颗','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；1-3红玉髓珠：外径0.47、孔径0.1、高0.33','0.02','M16','头骨左侧上端最外到37-2～11,都顾左侧下端靠内到31-21～11之间区域','23.08.24',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(587,'35','','','M62G:TB3','头部右侧珠子','人工硅酸盐','料珠有整有残。残破的呈碎渣状',NULL,'样杯3个','3-1汉蓝珠54颗在橙色盖样杯；3-2铅白珠67颗在橙色盖样杯；在 汉蓝、铅白珠中各由有1串排列规律的，用鱼线穿起来，开始观察可能是纯色的，提取串号后，水洗发现汉蓝珠、铅白珠都有','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；',' ','M16','头骨右侧上端最外测到13-5～11,头骨右侧下端内侧到25-11～11两个点之间往头骨顶部上端','23.08.25',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(588,'36','','','M62G:TB4','头（面）骨上珠子','人工硅酸盐','料珠有整有残。残破的呈碎渣状',NULL,'样杯2个','汉蓝珠6颗在橙色盖样杯；铅白珠5颗在橙色盖样杯；','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；','0.02','M16','头骨上梨状孔上端到额骨，左侧颌骨处之间（头部第一层料珠）17-20-8,','23.08.28',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(589,'37','','','M62G:TB6','项饰','金属、玉石、人工硅酸盐','',NULL,NULL,NULL,'','','M16','','23.08-24.10',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','金、绿松石、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(590,'38','','','M62G:TB6-6','汉蓝小圆珠','人工硅酸盐','提取粉末',NULL,'样杯1','1件','','','M16','左下颌骨外侧约3-7厘米处，中心位置在27-27～12，周边2\r\n厘米左右，','23.08.31',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(591,'39','','','M62G:TB6-14','汉蓝管珠（粉末）','人工硅酸盐','完全粉末状，提取粉末',NULL,NULL,'1件','','','M16','A2探方东侧，下颌骨下端约1-3厘米处，中心位置在27-27～12，周边2\r\n厘米左右，','23.09.12',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(592,'40','','','M62G:TB6-16','汉蓝管珠','人工硅酸盐','粉末状，提取粉末',NULL,'样杯1','1件','可能有3节，总高约6厘米，核算每节2厘米','','M16','金栅栏形饰右上端','24.10.29',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(593,'41','','','M62G:TB6-22','汉蓝小圆珠','人工硅酸盐','粉末状',NULL,'样杯1','1件','','','M16','','24.10.29',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(594,'42','','','M62G:TB7','料珠（铅白、汉蓝、汉紫圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：\r\n汉蓝：\r\n汉紫：','外径0.29、孔径0.2、高0.25','0.02','M16','颅腔内散落（头部第二层料珠）','24.11.5～11.7',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(595,'43','','','M62G:TB7-1','料珠（铅白、汉蓝、汉紫圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：32颗\r\n汉蓝：112颗\r\n汉紫：12颗','外径0.29、孔径0.2、高0.25','0.02','M16','','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(596,'44','','','M62G:TB7-2','料珠（铅白、汉蓝、汉紫圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：16颗\r\n汉蓝：36颗+残\r\n汉紫：11颗+残3','外径0.29、孔径0.2、高0.25','0.02','M16','','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(597,'45','','','M62G:TB8','料珠（铅白、汉蓝圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：7颗\r\n汉蓝：9颗\r\n汉紫：7颗','外径0.29、孔径0.2、高0.25','0.02','M16','顶骨、枕骨外侧及上端（头部第三层料珠）','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(598,'46','','','M62G:TB8-1','料珠（铅白、汉蓝圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：4颗\r\n汉蓝：43颗\r\n汉紫：51颗','外径0.29、孔径0.2、高0.25','0.02','M16','头骨顶端左半侧    TA1：19.8-10.2～13.8   TA1：6.6-16.4～13.3    TA1：11.8-14.4～13    TA1：7.7-12～11.3      TA1：14-11.9～13.3     TA1：11.8-10～11.6     TA1：17-11.3～12.6','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(599,'47','','','M62G:TB8-2','料珠（铅白、汉蓝圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：45颗\r\n汉蓝：217颗\r\n汉紫：30颗','外径0.29、孔径0.2、高0.25','0.02','M16','头骨顶端右半侧         1 TA1：18.1-29.8～10.9    TA1：17.9-26.8～11.9   2 TB1：12.2-1.1～11.2  TA1：13.7-27.1～11.5   3 TA1：2.2-28.3～11.0  TA1：11.0-23.2～11.5   4 TA1：5-20.7～10.5    TA1：10.3-20.9～11.8  ','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(600,'48','','','M62G:TB9-1','面饰（红玉髓圆珠、蓝玻璃珠）','玉石、人工硅酸盐','完整',NULL,'1组','红玉髓圆珠（舌面）6颗，红玉髓圆珠19颗，蓝玻璃珠8颗','红玉髓圆珠：外径0.36-0.56，高0.26-0.35；蓝玻璃珠：外径0.59-0.68，内径0.25-0.31，高0.39-0.41','红玉髓圆珠：0.03-0.15蓝玻璃珠：0.12-0.13','M16','下颌内外端左半侧    1:TA1 25.2-17.6～12.2   2:TA1 21-16.7～13.5     3:TA1 27.5-12.7～12     4:TA2 3.3-11.6～12.5    5:TA2 2.8-13.1～12.7    6:TA2 0.8-15～12.5','24.10.16',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','红玉髓、玻璃',11,NULL,NULL,NULL,NULL),(601,'49','','','M62G:TB9-2-2','面饰（蓝玻璃珠）','人工硅酸盐','完整',NULL,NULL,'7颗','外径0.53-0.64，内径0.21-0.28，高0.29-0.41','0.09-0.17','M16','左耳坠左侧及下端   TA1：27.6-19.5～12.9 TA1：28.8-14.7～12.5','24.10.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','玻璃',11,NULL,NULL,NULL,NULL),(602,'50','','','M62G:TB10-1','面饰（红玉髓圆珠、蓝玻璃珠）','玉石、人工硅酸盐','完整',NULL,'1组','红玉髓圆珠26颗，蓝玻璃珠6颗','红玉髓圆珠：外径0.33-0.52，高0.22-0.35；蓝玻璃珠：外径0.58-0.63，内径0.28-0.30，高0.33-0.45','红玉髓圆珠：0.01-0.13；蓝玻璃珠：0.12-0.16','M16','下颌内外端右半侧    1:TA1 25.4-18.2～12.4   2:TA1 24.3-22.9～11.3     3:TA1 25.8-27.9～11.6     4:TA2 25-5.6～11.7    5:TA2 7.4-18.6～12    6:TA2 2.2-18.3～12.1','24.10.16',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','红玉髓、玻璃',11,NULL,NULL,NULL,NULL),(603,'51','','','M62G:TB10-2-2','面饰（蓝玻璃珠）','人工硅酸盐','完整',NULL,NULL,'8颗','10-2-2-1:外径0.59，内径0.28，高0.43；10-2-2-2:外径0.58，内径0.22，高0.38；10-2-2-3:外径0.65，内径0.31，高0.39；10-2-2-4:外径0.59，内径0.26，高0.33；10-2-2-5:外径0.56，内径0.24，高0.33；10-2-2-6:外径0.58，内径0.29，高0.37；','10-2-2-1:0.14；10-2-2-2:0.13；10-2-2-3:0.16；10-2-21-4:0.12；10-2-2-5:0.1；10-2-2-6:0.13；','M16','胸椎上端          TA2：7.8-20.7～13 TA2：3-24.3～13   TA2：8-26.2～12.7','24.10.28',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','玻璃',11,NULL,NULL,NULL,NULL),(604,'52','','','M62GYB','腰部装饰','金属、人工硅酸盐','',NULL,'1组','铁板带钩1件、银格里芬纹腰带饰9片、料珠片饰5片共952颗：汉蓝656、汉紫239、铅白57颗','','','M16','','25.03.17-25.07.10',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','银、铁、釉砂',11,NULL,NULL,NULL,NULL),(605,'53','','','M62G:YB2','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共251颗：汉蓝172、汉紫59、铅白20','径约0.33，高约0.24','','M16','2025.T.Z.M.M62G髋骨右上侧近小臂处','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(606,'54','','','M62G:YB5','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共246颗：汉蓝168、汉紫60、铅白18','径约0.33，高约0.24','','M16','2025.T.Z.M.M62G左小臂右侧近腰椎处','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(607,'55','','','M62G:YB8','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共214颗：汉蓝141、汉紫59、铅白14','径约0.33，高约0.24','','M16','2025.T.Z.M.M62G左小臂右侧近腰椎处','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(608,'56','','','M62G:YB11','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共241颗：汉蓝175、汉紫61、铅白5','径约0.33，高约0.24','','M16','2025.T.Z.M.M62G左前臂下端上层','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(609,'57','','','M62G:YB14','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,NULL,'片饰长4.8-5、宽3.2-3.5cm\r\n料珠径约0.33，高约0.24','','M16','2025.T.Z.M.M62G盆骨左外侧上层\r\n','暂未提取',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','釉砂',11,NULL,NULL,NULL,NULL),(610,'58','','','M62GYT','右腿装饰物','金属、玉石、人工硅酸盐','完整',NULL,'1套2组','铁环1枚、料珠共1137颗、大蓝珠34颗、小蜻蜓眼43颗、大蜻蜓眼3颗、费昂丝管珠49颗、红玉髓小圆珠11颗、红玉髓碟形珠13颗、红玉髓扁管珠1颗、红玉髓五棱管珠7颗、红玉髓圆管珠10颗、铜月牙形饰8枚','','','M16','','25.03.18-25.07.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:45','2026-07-06 15:46:45','铜、铁、釉砂、红玉髓',11,NULL,NULL,NULL,NULL),(611,'59','','','','右腿垂坠饰','金属、玉石、人工硅酸盐','',NULL,'2组','大蓝珠34颗、小蜻蜓眼43颗、大蜻蜓眼3颗、费昂丝管珠49颗、红玉髓小圆珠11颗、红玉髓碟形珠13颗、红玉髓扁管珠1颗、红玉髓五棱管珠7颗、红玉髓圆管珠10颗、铜月牙形饰8枚','整体长约39.5、宽约4-4.5','','M16','右股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.03.17-25.04.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','铜、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(612,'60','','','M62G:YT2','右腿垂坠饰右侧串','金属、玉石、人工硅酸盐','',NULL,'1组','大蓝珠8颗、小蜻蜓眼22颗、大蜻蜓眼2颗、费昂丝管珠25颗、红玉髓小圆珠9颗、红玉髓碟形珠8颗、红玉髓五棱管珠5颗、红玉髓圆管珠3颗、铜月牙形饰4枚','整体长约39.5、宽约4-4.5','','M16','右股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.03.17-25.04.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','铜、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(613,'61','','','M62G:YT2-1—7','大蓝珠','人工硅酸盐','完整',NULL,NULL,'7颗','2-1：高0.5、径0.68；2-2：高0.71、径0.97；2-3：高0.65、径0.87；2-4：高0.71、径0.87；2-5：高0.64、径0.87；2-6：高0.7、径0.91；2-7：高0.61、径8.6','0.22；0.61；0.2；0.49；0.46；0.59；0.43','M16','右股骨上端外侧，铁环内及铁环下端\r\nTA3：8.4-27.2～6.8\r\nTA3：12.1-26.5～8.9','25.03.17-25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(614,'62','','','M62G:YT2-8','小蜻蜓眼','人工硅酸盐','残渣',NULL,NULL,'1颗','','','M16','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(615,'63','','','M62G:YT2-19—20','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','2-19：高0.56、径0.65；2-20高0.54、径0.71','0.26；0.29','M16','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(616,'64','','','M62G:YT2-21—22','大蓝珠','人工硅酸盐','完整',NULL,NULL,'2颗','2-21：高7.29、径9.07；2-20：高0.7、径0.88','0.58；0.57','M16','右股骨上段外侧','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(617,'65','','','M62G:YT2-23—24','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','2-23：高0.5、径0，73；2-24：高0.54、径0.7','0.18；0.27','M16','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(618,'66','','','M62G:YT2-31','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高0.54、径0.7','0.23','M16','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(619,'67','','','M62G:YT2-36','大蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高2.2，径2.3，孔径0.048','11.73','M16','右股骨外侧中下段','25.03.19',NULL,NULL,'一楼清棺室→2026.04.13兰州大学（陈玥寒）','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(620,'68','','','M62G:YT2-37','大蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高2.5，径2.2，孔径0.05','9.51','M16','右股骨外侧中下段','25.03.19',NULL,NULL,'一楼清棺室→2026.04.13兰州大学（陈玥寒）','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(621,'69','','','M62G:YT2-38-1-1—3','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','2-38-1-1：高0.88、径0.59；2-38-1-2：高1.78、径0.7、孔径0.3；2-38-1-3：高1.57、径0.6、孔径0.35','0.3；1.43；0.62','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(622,'70','','','M62G:YT2-38-1-4—5','小蜻蜓眼','人工硅酸盐','1整1残',NULL,NULL,'2颗','高0.55、径0.67','0.23','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(623,'71','','','M62G:YT2-38-1-8—9','小蜻蜓眼','人工硅酸盐','1整1残',NULL,NULL,'2颗','高0.56、径0.67','0.24','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(624,'72','','','M62G:YT2-38-1-10—13','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'4颗','2-38-1-10：高1.07、径0.68；2-38-1-11：高1.1、径0.74；2-38-1-12：高1.1、径0.7；2-38-1-13：高1.2、径0.8','0.69；0.77；0.71；0.93','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(625,'73','','','M62G:YT2-38-2-1—3','费昂斯管珠','人工硅酸盐','2-38-2-1残',NULL,NULL,'3颗','2-38-2-2：高1.69，径0.63，孔径0.27；2-38-2-3：高1.77，径0.68，孔径0.29；','2-38-2-2：0.79；2-38-2-3：1.1','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(626,'74','','','M62G:YT2-38-2-4—5','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','高0.65、径0.51，孔径0.018','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(627,'75','','','M62G:YT2-38-2-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','高0.65、径0.51，孔径0.018','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(628,'76','','','M62G:YT2-38-2-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','2-38-2-10：高1.88，径0.67，孔径0.27；2-38-2-11：高1.73，径0.7，孔径0.28;2-38-2-12：高1.09，径0.66，孔径0.26','2-38-2-10：0.38；2-38-2-11：1.1；2-38-2-12：0.53','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(629,'77','','','M62G:YT2-38-3-1—3','费昂斯管珠','人工硅酸盐','2-38-3-1残',NULL,NULL,'3颗','2-38-3-2：高1.89，径0.69，孔径0.22-0.30；2-38-3-3：高1.82，径0.69，孔径0.32；','2-38-3-2：1.28；2-38-3-3：1.21','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(630,'78','','','M62G:YT2-38-3-4—5','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','高0.65、径0.51，孔径0.018','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(631,'79','','','M62G:YT2-38-3-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','2-38-3-8：高0.57，径0.76，孔径0.18-0.21；2-38-3-9：高0.54，径0.78，孔径0.17-0.20','2-38-3-6：0.31；2-38-3-7：0.30','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(632,'80','','','M62G:YT2-38-3-10—12','费昂斯管珠','人工硅酸盐','2-38-3-10残',NULL,NULL,'3颗','2-38-3-11：高1.53，径0.75，孔径0.31-0.36;2-38-3-12：高1.54，径0.68，孔径0.32-0.36','2-38-3-11：1.03；2-38-3-12：1.01','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(633,'81','','','M62G:YT2-38-4-1—3','费昂斯管珠','人工硅酸盐','2-38-4-1残',NULL,NULL,'3颗','2-38-4-2：高1.86，径0.69，孔径0.28；2-38-4-3：高1.64，径0.65，孔径0.33；','2-38-4-4：1.30；2-38-4-5：1.01','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(634,'82','','','M62G:YT2-38-4-4—5','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','2-38-4-4：高0.5，径0.73，孔径0.19；2-38-4-5：高0.544，径0.68，孔径0.20；','2-38-4-2：0.26；2-38-4-3：0.22','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(635,'83','','','M62G:YT2-38-4-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(636,'84','','','M62G:YT2-38-4-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','2-38-4-10：高1.53，径0.68，孔径0.29；2-38-4-11：高1.25，径0.76，孔径0.34;2-38-4-12：高1.34，径0.73，孔径0.36','2-38-4-10：1.15；2-38-4-11：0.77；2-38-4-12：0.7','M16','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(637,'85','','','M62G:YT3','右腿垂坠饰左侧串','金属、玉石、人工硅酸盐','',NULL,'1组','大蓝珠24颗、小蜻蜓眼21颗、大蜻蜓眼1颗、费昂丝管珠24颗、红玉髓碟形珠3颗、红玉髓五棱管珠5颗、红玉髓圆管珠2颗、红玉髓扁管珠2颗、铜月牙形饰7枚','整体长约39.5、宽约4-4.5','','M16','右股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.03.17-25.04.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','铜、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(638,'86','','','M62G:YT3-1—7','大蓝珠','人工硅酸盐','完整',NULL,NULL,'7颗','3-1：高0.73，径0.92，孔径0.25；3-2：高0.73，径1.04，孔径0.25;3-3：高0.61，径0.89，孔径0.27；3-4：高0.72，径0.87，孔径0.26-0.28；3-5：高0.64，径0.78，孔径0.27-0.29；3-6：高0.68，径0.83，孔径0.26-0.28；3-7：高0.69，径0.82，孔径0.29','3-1：0.26；3-2：0.75;3-3：0.46；3-4：0.66；3-5：0.50；3-6：0.55；3-7：0.53','M16','右股骨上端外侧，铁环内及铁环下端','25.03.17-25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(639,'87','','','M62G:YT3-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','3-8：高0.54，径0.58，孔径0.17-0.19；3-9：高0.52，径0.55，孔径0.18','3-7：0.31；3-8：0.27','M16','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(640,'88','','','M62G:YT3-14—15','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M16','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(641,'89','','','M62G:YT3-16—27','大蓝珠','人工硅酸盐','完整',NULL,NULL,'12颗','3-16：高0.7，径0.98，孔径0.28-0.32；3-17：高0.63，径0.87，孔径0.27；3-18：高0.66，径0.85，孔径0.28-0.32；3-19：高0.69，径0.96，孔径0.28-0.31;3-20：高0.76，径0.91，孔径0.3-0.31；3-21：高0.65，径0.92，孔径0.28-0.30；3-22：高0.66，径0.90，孔径0.29-0.33；3-23：高0.75，径0.92，孔径0.29-0.30；3-24：高0.62，径0.94，孔径0.27-0.29；3-25：高0.74，径0.95，孔径0.29-0.31；3-26：高0.68，径0.92，孔径0.27；3-27：高0.64，径0.91，孔径0.29-0.30','3-16：0.61；3-17：0.47;3-18：0.46；3-19：0.58；3-20：0.52；3-21：0.50;3-22：0.49；3-23：0.62；3-24：0.50；3-25：0.55;3-26：0.51；3-27：0.51','M16','右股骨上端外侧','25.03.19',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(642,'90','','','M62G:YT3-28','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1颗','','','M16','右股骨上段大蓝珠与红骨髓间','25.03.19',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(643,'91','','','M62G:YT3-29—34','大蓝珠','人工硅酸盐','完整',NULL,NULL,'6颗','3-29：高0.70，径0.95，孔径0.28；3-30：高0.71，径0.95，孔径0.30-0.31；3-31：高0.66，径0.93，孔径0.27；3-32：高0.74，径0.96，孔径0.27-0.28;3-33：高0.71，径0.99，孔径0.31-0.33；3-34：高0.66，径0.91，孔径0.25-0.26','3-29：0.58；3-30：0.53;3-31：0.57；3-32：0.61；3-33：0.59;3-34：0.51','M16','右股骨上端外侧','25.03.19',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(644,'92','','','M62G:YT3-37','大蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高2.0，径2.23，孔径0.55','7.75','M16','右股骨外侧中下段','25.03.19',NULL,NULL,'一楼清棺室→2026.04.13兰州大学（陈玥寒）','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(645,'93','','','M62G:YT3-38-1-1—3','费昂斯管珠','人工硅酸盐','3-38-1-1残',NULL,NULL,'3颗','3-38-1-2:高1.60，径0.66，孔径0.27-0.28；3-38-1-3:高1.82，径0.67，孔径0.31','3-38-1-2:1.11；3-38-1-3:1.39','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(646,'94','','','M62G:YT3-38-1-4—5','小蜻蜓眼','人工硅酸盐','3-38-1-5残',NULL,NULL,'2颗','3-38-1-4:高0.52，径0.62，孔径0.19','3-38-1-4:0.17','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(647,'95','','','M62G:YT3-38-1-8—9','小蜻蜓眼','人工硅酸盐','3-38-1-8残',NULL,NULL,'2颗','3-38-1-9:高0.58，径0.65，孔径0.21-0.23','0.29','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(648,'96','','','M62G:YT3-38-1-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','3-38-1-10:高1.91，径0.72，孔径0.29','3-38-1-10:1.39','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(649,'97','','','M62G:YT3-38-2-1—3','费昂斯管珠','人工硅酸盐','3-38-2-1残',NULL,NULL,'3颗','3-38-2-2:高1.55，径0.67，孔径0.32；3-38-2-3:高1.52，径0.70，孔径0.3','3-38-2-2:0.94；3-38-2-3:1.13','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(650,'98','','','M62G:YT3-38-2-4—5','小蜻蜓眼','人工硅酸盐','3-38-2-5残',NULL,NULL,'2颗','3-38-2-4:高0.5，径0.64，孔径0.17','3-38-2-4:0.23','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(651,'99','','','M62G:YT3-38-2-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(652,'100','','','M62G:YT3-38-2-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','3-38-2-10:高1.12，径0.67，孔径0.38-0.40；3-38-2-11:高13.9，径0.69，孔径0.32；3-38-2-12:高1.13，径0.67，孔径0.2-0.25','3-38-2-10:0.47；3-38-2-11:1.07；3-38-2-12:0.79','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(653,'101','','','M62G:YT3-38-3-1—2','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'2颗','3-38-3-1:高1.73，径0.68，孔径0.3；3-38-3-2:高1.80，径0.73，孔径0.27-0.32','3-38-3-1:1.32；3-38-3-2：0.99','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(654,'102','','','M62G:YT3-38-3-3—4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(655,'103','','','M62G:YT3-38-3-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(656,'104','','','M62G:YT3-38-3-10—13','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'4颗','3-38-3-10:高1.15，径0.71，孔径0.27-0.28；3-38-3-11:高1.11，径0.73，孔径0.34；3-38-3-12:高1.16，径0.67，孔径0.34-0.36；3-38-3-13:高1.03，径0.64，孔径0.38-0.42','3-38-3-10:0.56；3-38-3-11：0.62；3-38-3-12：0.59；3-38-3-13：0.39','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(657,'105','','','M62G:YT3-38-4-1—2','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'2颗','3-38-4-1:高1.76，径0.68，孔径0.33；3-38-4-2:高1.56，径0.66，孔径0.33-0.35','3-38-4-1:1.27；3-38-4-2：1.01','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(658,'106','','','M62G:YT3-38-4-3—4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(659,'107','','','M62G:YT3-38-4-7—8','小蜻蜓眼','人工硅酸盐','3-38-4-7残',NULL,NULL,'2颗','3-38-4-8:高0.55，径0.72，孔径0.18','3-38-4-8：0.25','M16','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(660,'108','','','M62G:YT3-38-4-9—12','费昂斯管珠','人工硅酸盐',';',NULL,NULL,'4颗','3-38-4-9:高1.05，径0.75，孔径0.39-0.41；3-38-4-10:高1.14，径0.72，孔径0.33-0.36；3-38-4-11:高1.31，径0.73，孔径0.27；3-38-4-12:高1.17，径0.69，孔径0.24-0.28','3-38-4-9：0.58；3-38-4-10:0.60；3-38-4-11：0.69；3-38-4-12：0.71','M16','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(661,'109','','','M62G:YT4','右腿料珠片饰','人工硅酸盐','完整',NULL,NULL,'1127颗：汉蓝805、汉紫268、铅白54颗','','','M16','盆骨右下侧铁环下层至大蜻蜓眼处','25.03.18-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(662,'110','','','M62G:YT4-1','右腿第一层料珠片饰','人工硅酸盐','完整',NULL,'1组','779颗：汉蓝507、汉紫189颗、铅白64颗','长15.4、宽3.2-4.8\r\n料珠高0.25、径0.28','','M16','髋骨右侧及右股骨外侧\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(663,'111','','','M62G:YT4-1-1','右腿第一层料珠片饰上半段','人工硅酸盐','完整',NULL,NULL,'420颗：汉蓝293、汉紫80颗、铅白47颗','长约8、宽3.2-4.8\r\n料珠高0.25、径0.28','','M16','右股骨外侧，铁环下端至左侧可见第5颗大蓝珠处\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\n\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.20',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(664,'112','','','M62G:YT4-1-2','右腿第一层料珠片饰下半段','人工硅酸盐','完整',NULL,NULL,'359颗：汉蓝214、汉紫100颗、铅白17颗','长约7.6、宽约4.8\r\n料珠高0.25、径0.28','','M16','右股骨外侧，左侧可见第5颗大蓝珠至大蜻蜓眼处\r\nTA3：19.8-28.6～7.4\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1','25.03.20-25.03.21',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(665,'113','','','M62G:YT4-2','右腿第二层料珠片饰','人工硅酸盐','完整',NULL,NULL,'385颗：汉蓝306、汉紫79颗','长15.4、宽3.2-4.8\r\n料珠高0.25、径0.28','','M16','髋骨右侧及右股骨外侧\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(666,'114','','','M62G:YT4-2-1','右腿第二层料珠片饰上半段','人工硅酸盐','完整',NULL,NULL,'13364颗：汉蓝112、汉紫77颗、铅白10','长约8、宽3.2-4.8\r\n料珠高0.25、径0.28','','M16','右股骨外侧，铁环下端至左侧可见第5颗大蓝珠处\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\n\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(667,'115','','','M62G:YT4-2-2','右腿第二层料珠片饰下半段','人工硅酸盐','完整',NULL,NULL,'223颗：汉蓝186、汉紫37颗','长约7.6、宽约4.8\r\n料珠高0.25、径0.28','','M16','右股骨外侧，左侧可见第5颗大蓝珠至大蜻蜓眼处\r\nTA3：19.8-28.6～7.4\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1','25.03.20-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(668,'116','','','','左腿垂坠饰','金属、玉石、人工硅酸盐','完整',NULL,'1套2组','铁环1枚、贴金铜盒1件、费昂丝管珠40枚、红玉髓管珠17枚碟形珠11枚、小蜻蜓眼14枚、铜月牙形饰9枚、料珠共1041颗','整体长约29.7、宽约3.4','','M16','左股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','金、铜、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(669,'117','','','M62G:ZT2','左腿右侧垂坠饰','金属、玉石、人工硅酸盐','',NULL,'1组','贴金铜盒1件、铜月牙形饰4枚、费昂斯管珠40枚、小蜻蜓眼14枚、红玉髓六棱管珠9枚碟形珠1枚','整体长约25、宽约3.4','','M16','左股骨上端铁环下至右股骨外侧下端近髌骨处','25.06.10-25.09.25',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','金、铜、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(670,'118','','','M62G:ZT2-1—6','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'6枚','2-1：高1.8、径0.7；2-2：高1.61、径0.79；2-3：高1.52、径0.77；2-4：高1.07、径0.74；2-5：高1.06、径0.66；2-6：高1.0、径0.73；','0.6；1.6；1.32；0.64；0.43；0.59','M16','分布于贴金铜盒上端一周、铁环右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(671,'119','','','M62G:ZT2-7','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','','','M16','铁环右上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(672,'120','','','M62G:ZT2-8—13','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'6枚','2-8：高1.09、径0.66；2-9：高0.93、径0.6；2-10：高1.0、径0.71；2-11：高1.14、径0.7；2-12：高1.18、径0.7；2-13：高1.05、径0.7','0.45；0.36；0.37；0.55；0.76；0.58','M16','分布于贴金铜盒上端一周、铁环右侧、右上侧及上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(673,'121','','','M62G:ZT2-14','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','高0.55、径0.78','0.32','M16','铁环右上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(674,'122','','','M62G:ZT2-15—18','费昂丝管珠','人工硅酸盐','3整1残',NULL,NULL,'4枚','2-15：高1.45、径0.8；2-16：高1.44、径0.71；2-17：高1.65、径0.77','1.51；1.47；1.59','M16','分布于贴金铜盒上端一周、铁环右上侧及上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(675,'123','','','M62G:ZT2-21-1-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-1-1：高0.93、径0.65、孔径0.34；2-21-1-2：高1.63、径0.77；2-21-1-3：高1.58、径0.75','0.35；1.49；1.44','M16','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(676,'124','','','M62G:ZT2-21-1-4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','','','M16','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(677,'125','','','M62G:ZT2-21-1-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2枚','','','M16','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(678,'126','','','M62G:ZT2-21-1-10—12','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-1-10：高1.37、径0.78；2-21-1-11：高1.4、径0.87；2-21-1-13：高1.35、径0.83','0.87；1.06；0.96','M16','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(679,'127','','','M62G:ZT2-21-2-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-2-1：高1.03、径0.69；2-21-2-2：高1.75、径0.74、孔径0.34；2-21-2-3：高1.27、径0.8','0.43；1.05；0.79','M16','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(680,'128','','','M62G:ZT2-21-2-4','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1枚','','','M16','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(681,'129','','','M62G:ZT2-21-2-7—8','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2枚','','','M16','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(682,'130','','','M62G:ZT2-21-2-9—11','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-2-9：高1.27、径0.64；2-21-2-10：高1.22、径0.87；2-21-2-11：高1.83、径0.77','0.85；0.94；1.96','M16','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(683,'131','','','M62G:ZT2-21-3-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-3-1：高1.35、径0.75；2-21-3-2：高1.44、径0.7；2-21-3-3：高1.43、径0.74','0.88；0.94；0.98','M16','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(684,'132','','','M62G:ZT2-21-3-4','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1枚','高0.5、径0.68','0.25','M16','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(685,'133','','','M62G:ZT2-21-3-7—8','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2枚','高0.65、径0.51，孔径0.018','','M16','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(686,'134','','','M62G:ZT2-21-3-9—11','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-3-9：高1.36、径0.8；2-21-3-10：高1.26、径0.8；2-21-3-11：高1.32、径0.83','1.04；1.04；1.1','M16','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(687,'135','','','M62G:ZT2-21-4-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-4-1：高1.17、径0.76；2-21-4-2：高1.14、径0.7；2-21-4-3：高1.75、径0.39','0.76；0.69；0.86','M16','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(688,'136','','','M62G:ZT2-21-4-4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','','','M16','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(689,'137','','','M62G:ZT2-21-4-7—8','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2枚','2-21-4-7：高0.52、径0.63；2-21-4-8：高0.5、径0.64','0.21；0.25','M16','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','蜻蜓眼',11,NULL,NULL,NULL,NULL),(690,'138','','','M62G:ZT2-21-4-9—11','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-4-9：高1.48、径0.81；2-21-4-10：高1.63、径0.84','1.25；1.75','M16','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂（费昂斯）',11,NULL,NULL,NULL,NULL),(691,'139','','','M62G:ZT3','左腿左侧垂坠饰','金属、玉石、人工硅酸盐','完整',NULL,'1组','红玉髓碟形珠10枚、大蓝珠4枚、红玉髓枣核型管珠8枚、铜镜1枚','','','M16','整体堆积于贴金铜盒上端及铁环内、叠压于铜镜下','25.06.10-25.09.25',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','铜、红玉髓、釉砂',11,NULL,NULL,NULL,NULL),(692,'140','','','M62G:ZT3-3—6','大蓝珠','人工硅酸盐','完整',NULL,NULL,'4枚','3-3：高0.49、径0.58、孔径0.24；3-4：高0.51、径0.63、孔径0.24；3-5：高0.48、径0.59、孔径0.25；3-6：高0.45、径0.64','0.13；0.15；0.13；0.15','M16','盆骨内侧左下端，贴金铜盒右侧、铁环右下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(693,'141','','','M62GZ:ZT4','料珠饰','人工硅酸盐','基本完整',NULL,'1组','共1019颗：汉蓝541、汉紫392、铅白86颗，铜月牙形饰5枚','整体长约29.7、宽约3.4','','M16','左股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.06.10-25.09.25',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(694,'142','','','M62G:ZT4-1','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共821颗：汉蓝454、汉紫302、铅白65颗','整体长约16、宽约3.4','','M16','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(695,'143','','','M62G:ZT4-1-1','1层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共38颗：汉蓝17、汉紫21','','','M16','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(696,'144','','','M62G:ZT4-1-2','2层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共59颗：汉蓝33、汉紫26','','','M16','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(697,'145','','','M62G:ZT4-1-3','3层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共41颗：汉蓝25、汉紫11、铅白6颗','','','M16','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(698,'146','','','M62G:ZT4-1-4','4层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共27颗：汉蓝9、汉紫14、铅白4颗','','','M16','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(699,'147','','','M62G:ZT4-1-5','5层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共18颗：汉蓝8、汉紫14、铅白6颗','','','M16','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(700,'148','','','M62G:ZT4-1-6','各部位收集并归入料珠片饰','人工硅酸盐','基本完整',NULL,NULL,NULL,'','','M16','左股骨上端、髋骨左下侧背面、髋骨左侧铜镜下、拆棺散落','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(701,'149','','','M62G:ZT4-2','料珠串饰','金属、人工硅酸盐','基本完整',NULL,'1组5串','串饰共5串，铜月牙形饰5枚、料珠共198颗：汉蓝87、汉紫90、铅白21','整体长约13.5','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','铜、釉砂',11,NULL,NULL,NULL,NULL),(702,'150','','','M62G:ZT4-2-1-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','汉蓝5颗','','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(703,'151','','','M62G:ZT4-2-2-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共36颗，汉蓝15、汉紫20、铅白1颗','','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(704,'152','','','M62G:ZT4-2-3-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共24颗：汉紫21、铅白23颗','','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(705,'153','','','M62G:ZT4-2-4-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共48颗，汉蓝33、汉紫13、铅白2颗','','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(706,'154','','','M62G:ZT4-2-5-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共31颗，汉蓝17、汉紫14','','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(707,'155','','','M62G:ZT4-2-6','料珠串饰（散落）','人工硅酸盐','基本完整',NULL,'1串','共53颗，汉蓝20、汉紫13、铅白9颗','','','M16','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(708,'156','','','','脚部装饰','玉石、人工硅酸盐','完整',NULL,'1套2组','料珠937颗：汉蓝583、汉紫339、铅白51\r\n红玉髓珠30颗\r\n绿松石片珠10颗','','','M16','脚部及周围','2026.01.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','红玉髓、绿松石、釉砂',11,NULL,NULL,NULL,NULL),(709,'157','','','M62G:YJ1','料珠片饰','人工硅酸盐','基本完整',NULL,'1片','共293颗：汉蓝293、汉紫189、铅白35颗','','','M16','脚部右侧处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(710,'158','','','M62G:ZJ1','料珠片饰','人工硅酸盐','基本完整',NULL,'1片','共290颗：汉蓝290、汉紫150、铅白16颗','','','M16','集中于脚部左侧处向右侧散落','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:46:46','2026-07-06 15:46:46','釉砂',11,NULL,NULL,NULL,NULL),(711,'159','','','MD：9','坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'9件，汉蓝、铅白陶珠若干','9件，汉蓝、铅白陶珠若干','通高3.15，宽2.9，孔外径0.8、内径0.4','28.17','M62','墓道车北轸下','2020.7.27',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、釉砂（铅蓝，汉白，汉紫）',7,NULL,NULL,NULL,NULL),(712,'160','','','MD：10','坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'29件，汉蓝、铅白、汉紫陶珠若干','29件，汉蓝、铅白、汉紫陶珠若干','通高3.15，宽2.9，孔外径0.8、内径0.4','86.57','M62','墓道车厢底部','2020.7.27',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、釉砂（铅蓝，汉白，汉紫）',7,NULL,NULL,NULL,NULL),(713,'161','','','MDCX：3','后轸坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'1套','12件','通高3.15，宽2.9，孔外径0.8、内径0.4','总重28.15','M62','后轸偏北处，由北向南依次编为3-1~3-10，3-1铜饰距北轸12、后坠7、墓底20','2020.7.25',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、釉砂（铅蓝，汉白，汉紫）',7,NULL,NULL,NULL,NULL),(714,'162','','','MDCX：4','车轸坠饰','金属，人工硅酸盐','铜坠饰基本完整，釉砂珠残碎',NULL,'1套','23件','通高3.15，宽2.9，孔外径0.8、内径0.4','铜饰件54.07,珠子16,12','M62','前轸下分布，由北向南第十串单独提取','2020.7.26',NULL,NULL,'2025.12.1负8库\r\n→25.12核对存负8库','','现状照：陆君苗','','','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、釉砂（铅蓝，汉白，汉紫）',7,NULL,NULL,NULL,NULL),(715,'163','','','M62G:TB1','头部顶端料珠','玉石、人工硅酸盐','红玉髓完整，料珠有残破，呈碎渣状',NULL,'样杯4个','1-1汉蓝珠28颗在橙色盖样杯；1-2铅白珠17颗在橙色盖样杯；1-3红玉髓珠1颗，在橙色盖样杯；汉紫珠4颗，在橙色盖样杯','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；1-3红玉髓珠：外径0.47、孔径0.1、高0.33','0.02','M62','8-13～10,12-28～11两个坐标点之间往头骨顶部上端','23.08.24',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(716,'164','','','M62G:TB2','头部左侧珠子','人工硅酸盐','料珠有整有残。残破的呈碎渣状',NULL,'样杯3个','2-1汉蓝珠32颗在橙色盖样杯；2-2铅白珠3在橙色盖样杯；2-3红玉髓珠2颗，在橙色盖样杯；汉紫珠10颗','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；1-3红玉髓珠：外径0.47、孔径0.1、高0.33','0.02','M62','头骨左侧上端最外到37-2～11,都顾左侧下端靠内到31-21～11之间区域','23.08.24',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(717,'165','','','M62G:TB3','头部右侧珠子','人工硅酸盐','料珠有整有残。残破的呈碎渣状',NULL,'样杯3个','3-1汉蓝珠54颗在橙色盖样杯；3-2铅白珠67颗在橙色盖样杯；在 汉蓝、铅白珠中各由有1串排列规律的，用鱼线穿起来，开始观察可能是纯色的，提取串号后，水洗发现汉蓝珠、铅白珠都有','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；',' ','M62','头骨右侧上端最外测到13-5～11,头骨右侧下端内侧到25-11～11两个点之间往头骨顶部上端','23.08.25',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(718,'166','','','M62G:TB4','头（面）骨上珠子','人工硅酸盐','料珠有整有残。残破的呈碎渣状',NULL,'样杯2个','汉蓝珠6颗在橙色盖样杯；铅白珠5颗在橙色盖样杯；','1-1汉蓝珠：外径0.29、孔径0.2、高0.25；1-2铅白珠：外径0.33、孔径0.2、高0.25；','0.02','M62','头骨上梨状孔上端到额骨，左侧颌骨处之间（头部第一层料珠）17-20-8,','23.08.28',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(719,'167','','','M62G:TB6','项饰','金属、玉石、人工硅酸盐','',NULL,NULL,NULL,'','','M62','','23.08-24.10',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','金、绿松石、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(720,'168','','','M62G:TB6-6','汉蓝小圆珠','人工硅酸盐','提取粉末',NULL,'样杯1','1件','','','M62','左下颌骨外侧约3-7厘米处，中心位置在27-27～12，周边2\r\n厘米左右，','23.08.31',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(721,'169','','','M62G:TB6-14','汉蓝管珠（粉末）','人工硅酸盐','完全粉末状，提取粉末',NULL,NULL,'1件','','','M62','A2探方东侧，下颌骨下端约1-3厘米处，中心位置在27-27～12，周边2\r\n厘米左右，','23.09.12',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(722,'170','','','M62G:TB6-16','汉蓝管珠','人工硅酸盐','粉末状，提取粉末',NULL,'样杯1','1件','可能有3节，总高约6厘米，核算每节2厘米','','M62','金栅栏形饰右上端','24.10.29',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(723,'171','','','M62G:TB6-22','汉蓝小圆珠','人工硅酸盐','粉末状',NULL,'样杯1','1件','','','M62','','24.10.29',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(724,'172','','','M62G:TB7','料珠（铅白、汉蓝、汉紫圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：\r\n汉蓝：\r\n汉紫：','外径0.29、孔径0.2、高0.25','0.02','M62','颅腔内散落（头部第二层料珠）','24.11.5～11.7',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(725,'173','','','M62G:TB7-1','料珠（铅白、汉蓝、汉紫圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：32颗\r\n汉蓝：112颗\r\n汉紫：12颗','外径0.29、孔径0.2、高0.25','0.02','M62','','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(726,'174','','','M62G:TB7-2','料珠（铅白、汉蓝、汉紫圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：16颗\r\n汉蓝：36颗+残\r\n汉紫：11颗+残3','外径0.29、孔径0.2、高0.25','0.02','M62','','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(727,'175','','','M62G:TB8','料珠（铅白、汉蓝圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：7颗\r\n汉蓝：9颗\r\n汉紫：7颗','外径0.29、孔径0.2、高0.25','0.02','M62','顶骨、枕骨外侧及上端（头部第三层料珠）','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(728,'176','','','M62G:TB8-1','料珠（铅白、汉蓝圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：4颗\r\n汉蓝：43颗\r\n汉紫：51颗','外径0.29、孔径0.2、高0.25','0.02','M62','头骨顶端左半侧    TA1：19.8-10.2～13.8   TA1：6.6-16.4～13.3    TA1：11.8-14.4～13    TA1：7.7-12～11.3      TA1：14-11.9～13.3     TA1：11.8-10～11.6     TA1：17-11.3～12.6','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(729,'177','','','M62G:TB8-2','料珠（铅白、汉蓝圆珠）','人工硅酸盐','部分完整，部分残（渣）',NULL,'1组','铅白：45颗\r\n汉蓝：217颗\r\n汉紫：30颗','外径0.29、孔径0.2、高0.25','0.02','M62','头骨顶端右半侧         1 TA1：18.1-29.8～10.9    TA1：17.9-26.8～11.9   2 TB1：12.2-1.1～11.2  TA1：13.7-27.1～11.5   3 TA1：2.2-28.3～11.0  TA1：11.0-23.2～11.5   4 TA1：5-20.7～10.5    TA1：10.3-20.9～11.8  ','24.11.5～11.7',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(730,'178','','','M62G:TB9-1','面饰（红玉髓圆珠、蓝玻璃珠）','玉石、人工硅酸盐','完整',NULL,'1组','红玉髓圆珠（舌面）6颗，红玉髓圆珠19颗，蓝玻璃珠8颗','红玉髓圆珠：外径0.36-0.56，高0.26-0.35；蓝玻璃珠：外径0.59-0.68，内径0.25-0.31，高0.39-0.41','红玉髓圆珠：0.03-0.15蓝玻璃珠：0.12-0.13','M62','下颌内外端左半侧    1:TA1 25.2-17.6～12.2   2:TA1 21-16.7～13.5     3:TA1 27.5-12.7～12     4:TA2 3.3-11.6～12.5    5:TA2 2.8-13.1～12.7    6:TA2 0.8-15～12.5','24.10.16',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','红玉髓、玻璃',7,NULL,NULL,NULL,NULL),(731,'179','','','M62G:TB9-2-2','面饰（蓝玻璃珠）','人工硅酸盐','完整',NULL,NULL,'7颗','外径0.53-0.64，内径0.21-0.28，高0.29-0.41','0.09-0.17','M62','左耳坠左侧及下端   TA1：27.6-19.5～12.9 TA1：28.8-14.7～12.5','24.10.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','玻璃',7,NULL,NULL,NULL,NULL),(732,'180','','','M62G:TB10-1','面饰（红玉髓圆珠、蓝玻璃珠）','玉石、人工硅酸盐','完整',NULL,'1组','红玉髓圆珠26颗，蓝玻璃珠6颗','红玉髓圆珠：外径0.33-0.52，高0.22-0.35；蓝玻璃珠：外径0.58-0.63，内径0.28-0.30，高0.33-0.45','红玉髓圆珠：0.01-0.13；蓝玻璃珠：0.12-0.16','M62','下颌内外端右半侧    1:TA1 25.4-18.2～12.4   2:TA1 24.3-22.9～11.3     3:TA1 25.8-27.9～11.6     4:TA2 25-5.6～11.7    5:TA2 7.4-18.6～12    6:TA2 2.2-18.3～12.1','24.10.16',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','红玉髓、玻璃',7,NULL,NULL,NULL,NULL),(733,'181','','','M62G:TB10-2-2','面饰（蓝玻璃珠）','人工硅酸盐','完整',NULL,NULL,'8颗','10-2-2-1:外径0.59，内径0.28，高0.43；10-2-2-2:外径0.58，内径0.22，高0.38；10-2-2-3:外径0.65，内径0.31，高0.39；10-2-2-4:外径0.59，内径0.26，高0.33；10-2-2-5:外径0.56，内径0.24，高0.33；10-2-2-6:外径0.58，内径0.29，高0.37；','10-2-2-1:0.14；10-2-2-2:0.13；10-2-2-3:0.16；10-2-21-4:0.12；10-2-2-5:0.1；10-2-2-6:0.13；','M62','胸椎上端          TA2：7.8-20.7～13 TA2：3-24.3～13   TA2：8-26.2～12.7','24.10.28',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','玻璃',7,NULL,NULL,NULL,NULL),(734,'182','','','M62GYB','腰部装饰','金属、人工硅酸盐','',NULL,'1组','铁板带钩1件、银格里芬纹腰带饰9片、料珠片饰5片共952颗：汉蓝656、汉紫239、铅白57颗','','','M62','','25.03.17-25.07.10',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','银、铁、釉砂',7,NULL,NULL,NULL,NULL),(735,'183','','','M62G:YB2','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共251颗：汉蓝172、汉紫59、铅白20','径约0.33，高约0.24','','M62','2025.T.Z.M.M62G髋骨右上侧近小臂处','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(736,'184','','','M62G:YB5','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共246颗：汉蓝168、汉紫60、铅白18','径约0.33，高约0.24','','M62','2025.T.Z.M.M62G左小臂右侧近腰椎处','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(737,'185','','','M62G:YB8','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共214颗：汉蓝141、汉紫59、铅白14','径约0.33，高约0.24','','M62','2025.T.Z.M.M62G左小臂右侧近腰椎处','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(738,'186','','','M62G:YB11','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共241颗：汉蓝175、汉紫61、铅白5','径约0.33，高约0.24','','M62','2025.T.Z.M.M62G左前臂下端上层','2025.07.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(739,'187','','','M62G:YB14','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,NULL,'片饰长4.8-5、宽3.2-3.5cm\r\n料珠径约0.33，高约0.24','','M62','2025.T.Z.M.M62G盆骨左外侧上层\r\n','暂未提取',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(740,'188','','','M62GYT','右腿装饰物','金属、玉石、人工硅酸盐','完整',NULL,'1套2组','铁环1枚、料珠共1137颗、大蓝珠34颗、小蜻蜓眼43颗、大蜻蜓眼3颗、费昂丝管珠49颗、红玉髓小圆珠11颗、红玉髓碟形珠13颗、红玉髓扁管珠1颗、红玉髓五棱管珠7颗、红玉髓圆管珠10颗、铜月牙形饰8枚','','','M62','','25.03.18-25.07.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、铁、釉砂、红玉髓',7,NULL,NULL,NULL,NULL),(741,'189','','','','右腿垂坠饰','金属、玉石、人工硅酸盐','',NULL,'2组','大蓝珠34颗、小蜻蜓眼43颗、大蜻蜓眼3颗、费昂丝管珠49颗、红玉髓小圆珠11颗、红玉髓碟形珠13颗、红玉髓扁管珠1颗、红玉髓五棱管珠7颗、红玉髓圆管珠10颗、铜月牙形饰8枚','整体长约39.5、宽约4-4.5','','M62','右股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.03.17-25.04.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(742,'190','','','M62G:YT2','右腿垂坠饰右侧串','金属、玉石、人工硅酸盐','',NULL,'1组','大蓝珠8颗、小蜻蜓眼22颗、大蜻蜓眼2颗、费昂丝管珠25颗、红玉髓小圆珠9颗、红玉髓碟形珠8颗、红玉髓五棱管珠5颗、红玉髓圆管珠3颗、铜月牙形饰4枚','整体长约39.5、宽约4-4.5','','M62','右股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.03.17-25.04.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(743,'191','','','M62G:YT2-1—7','大蓝珠','人工硅酸盐','完整',NULL,NULL,'7颗','2-1：高0.5、径0.68；2-2：高0.71、径0.97；2-3：高0.65、径0.87；2-4：高0.71、径0.87；2-5：高0.64、径0.87；2-6：高0.7、径0.91；2-7：高0.61、径8.6','0.22；0.61；0.2；0.49；0.46；0.59；0.43','M62','右股骨上端外侧，铁环内及铁环下端\r\nTA3：8.4-27.2～6.8\r\nTA3：12.1-26.5～8.9','25.03.17-25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(744,'192','','','M62G:YT2-8','小蜻蜓眼','人工硅酸盐','残渣',NULL,NULL,'1颗','','','M62','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(745,'193','','','M62G:YT2-19—20','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','2-19：高0.56、径0.65；2-20高0.54、径0.71','0.26；0.29','M62','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(746,'194','','','M62G:YT2-21—22','大蓝珠','人工硅酸盐','完整',NULL,NULL,'2颗','2-21：高7.29、径9.07；2-20：高0.7、径0.88','0.58；0.57','M62','右股骨上段外侧','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(747,'195','','','M62G:YT2-23—24','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','2-23：高0.5、径0，73；2-24：高0.54、径0.7','0.18；0.27','M62','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(748,'196','','','M62G:YT2-31','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高0.54、径0.7','0.23','M62','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(749,'197','','','M62G:YT2-36','大蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高2.2，径2.3，孔径0.048','11.73','M62','右股骨外侧中下段','25.03.19',NULL,NULL,'一楼清棺室→2026.04.13兰州大学（陈玥寒）','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(750,'198','','','M62G:YT2-37','大蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高2.5，径2.2，孔径0.05','9.51','M62','右股骨外侧中下段','25.03.19',NULL,NULL,'一楼清棺室→2026.04.13兰州大学（陈玥寒）','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(751,'199','','','M62G:YT2-38-1-1—3','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','2-38-1-1：高0.88、径0.59；2-38-1-2：高1.78、径0.7、孔径0.3；2-38-1-3：高1.57、径0.6、孔径0.35','0.3；1.43；0.62','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(752,'200','','','M62G:YT2-38-1-4—5','小蜻蜓眼','人工硅酸盐','1整1残',NULL,NULL,'2颗','高0.55、径0.67','0.23','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(753,'201','','','M62G:YT2-38-1-8—9','小蜻蜓眼','人工硅酸盐','1整1残',NULL,NULL,'2颗','高0.56、径0.67','0.24','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(754,'202','','','M62G:YT2-38-1-10—13','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'4颗','2-38-1-10：高1.07、径0.68；2-38-1-11：高1.1、径0.74；2-38-1-12：高1.1、径0.7；2-38-1-13：高1.2、径0.8','0.69；0.77；0.71；0.93','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(755,'203','','','M62G:YT2-38-2-1—3','费昂斯管珠','人工硅酸盐','2-38-2-1残',NULL,NULL,'3颗','2-38-2-2：高1.69，径0.63，孔径0.27；2-38-2-3：高1.77，径0.68，孔径0.29；','2-38-2-2：0.79；2-38-2-3：1.1','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(756,'204','','','M62G:YT2-38-2-4—5','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','高0.65、径0.51，孔径0.018','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(757,'205','','','M62G:YT2-38-2-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','高0.65、径0.51，孔径0.018','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(758,'206','','','M62G:YT2-38-2-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','2-38-2-10：高1.88，径0.67，孔径0.27；2-38-2-11：高1.73，径0.7，孔径0.28;2-38-2-12：高1.09，径0.66，孔径0.26','2-38-2-10：0.38；2-38-2-11：1.1；2-38-2-12：0.53','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(759,'207','','','M62G:YT2-38-3-1—3','费昂斯管珠','人工硅酸盐','2-38-3-1残',NULL,NULL,'3颗','2-38-3-2：高1.89，径0.69，孔径0.22-0.30；2-38-3-3：高1.82，径0.69，孔径0.32；','2-38-3-2：1.28；2-38-3-3：1.21','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(760,'208','','','M62G:YT2-38-3-4—5','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','高0.65、径0.51，孔径0.018','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(761,'209','','','M62G:YT2-38-3-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','2-38-3-8：高0.57，径0.76，孔径0.18-0.21；2-38-3-9：高0.54，径0.78，孔径0.17-0.20','2-38-3-6：0.31；2-38-3-7：0.30','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(762,'210','','','M62G:YT2-38-3-10—12','费昂斯管珠','人工硅酸盐','2-38-3-10残',NULL,NULL,'3颗','2-38-3-11：高1.53，径0.75，孔径0.31-0.36;2-38-3-12：高1.54，径0.68，孔径0.32-0.36','2-38-3-11：1.03；2-38-3-12：1.01','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(763,'211','','','M62G:YT2-38-4-1—3','费昂斯管珠','人工硅酸盐','2-38-4-1残',NULL,NULL,'3颗','2-38-4-2：高1.86，径0.69，孔径0.28；2-38-4-3：高1.64，径0.65，孔径0.33；','2-38-4-4：1.30；2-38-4-5：1.01','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(764,'212','','','M62G:YT2-38-4-4—5','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2颗','2-38-4-4：高0.5，径0.73，孔径0.19；2-38-4-5：高0.544，径0.68，孔径0.20；','2-38-4-2：0.26；2-38-4-3：0.22','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(765,'213','','','M62G:YT2-38-4-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','蜻蜓眼',7,NULL,NULL,NULL,NULL),(766,'214','','','M62G:YT2-38-4-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','2-38-4-10：高1.53，径0.68，孔径0.29；2-38-4-11：高1.25，径0.76，孔径0.34;2-38-4-12：高1.34，径0.73，孔径0.36','2-38-4-10：1.15；2-38-4-11：0.77；2-38-4-12：0.7','M62','右股骨外侧中下段','25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(767,'215','','','M62G:YT3','右腿垂坠饰左侧串','金属、玉石、人工硅酸盐','',NULL,'1组','大蓝珠24颗、小蜻蜓眼21颗、大蜻蜓眼1颗、费昂丝管珠24颗、红玉髓碟形珠3颗、红玉髓五棱管珠5颗、红玉髓圆管珠2颗、红玉髓扁管珠2颗、铜月牙形饰7枚','整体长约39.5、宽约4-4.5','','M62','右股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.03.17-25.04.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','铜、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(768,'216','','','M62G:YT3-1—7','大蓝珠','人工硅酸盐','完整',NULL,NULL,'7颗','3-1：高0.73，径0.92，孔径0.25；3-2：高0.73，径1.04，孔径0.25;3-3：高0.61，径0.89，孔径0.27；3-4：高0.72，径0.87，孔径0.26-0.28；3-5：高0.64，径0.78，孔径0.27-0.29；3-6：高0.68，径0.83，孔径0.26-0.28；3-7：高0.69，径0.82，孔径0.29','3-1：0.26；3-2：0.75;3-3：0.46；3-4：0.66；3-5：0.50；3-6：0.55；3-7：0.53','M62','右股骨上端外侧，铁环内及铁环下端','25.03.17-25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:00','2026-07-06 15:47:00','釉砂',7,NULL,NULL,NULL,NULL),(769,'217','','','M62G:YT3-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','3-8：高0.54，径0.58，孔径0.17-0.19；3-9：高0.52，径0.55，孔径0.18','3-7：0.31；3-8：0.27','M62','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(770,'218','','','M62G:YT3-14—15','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M62','右股骨上段大蓝珠与红骨髓间','25.03.18',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(771,'219','','','M62G:YT3-16—27','大蓝珠','人工硅酸盐','完整',NULL,NULL,'12颗','3-16：高0.7，径0.98，孔径0.28-0.32；3-17：高0.63，径0.87，孔径0.27；3-18：高0.66，径0.85，孔径0.28-0.32；3-19：高0.69，径0.96，孔径0.28-0.31;3-20：高0.76，径0.91，孔径0.3-0.31；3-21：高0.65，径0.92，孔径0.28-0.30；3-22：高0.66，径0.90，孔径0.29-0.33；3-23：高0.75，径0.92，孔径0.29-0.30；3-24：高0.62，径0.94，孔径0.27-0.29；3-25：高0.74，径0.95，孔径0.29-0.31；3-26：高0.68，径0.92，孔径0.27；3-27：高0.64，径0.91，孔径0.29-0.30','3-16：0.61；3-17：0.47;3-18：0.46；3-19：0.58；3-20：0.52；3-21：0.50;3-22：0.49；3-23：0.62；3-24：0.50；3-25：0.55;3-26：0.51；3-27：0.51','M62','右股骨上端外侧','25.03.19',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(772,'220','','','M62G:YT3-28','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1颗','','','M62','右股骨上段大蓝珠与红骨髓间','25.03.19',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(773,'221','','','M62G:YT3-29—34','大蓝珠','人工硅酸盐','完整',NULL,NULL,'6颗','3-29：高0.70，径0.95，孔径0.28；3-30：高0.71，径0.95，孔径0.30-0.31；3-31：高0.66，径0.93，孔径0.27；3-32：高0.74，径0.96，孔径0.27-0.28;3-33：高0.71，径0.99，孔径0.31-0.33；3-34：高0.66，径0.91，孔径0.25-0.26','3-29：0.58；3-30：0.53;3-31：0.57；3-32：0.61；3-33：0.59;3-34：0.51','M62','右股骨上端外侧','25.03.19',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(774,'222','','','M62G:YT3-37','大蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1颗','高2.0，径2.23，孔径0.55','7.75','M62','右股骨外侧中下段','25.03.19',NULL,NULL,'一楼清棺室→2026.04.13兰州大学（陈玥寒）','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(775,'223','','','M62G:YT3-38-1-1—3','费昂斯管珠','人工硅酸盐','3-38-1-1残',NULL,NULL,'3颗','3-38-1-2:高1.60，径0.66，孔径0.27-0.28；3-38-1-3:高1.82，径0.67，孔径0.31','3-38-1-2:1.11；3-38-1-3:1.39','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(776,'224','','','M62G:YT3-38-1-4—5','小蜻蜓眼','人工硅酸盐','3-38-1-5残',NULL,NULL,'2颗','3-38-1-4:高0.52，径0.62，孔径0.19','3-38-1-4:0.17','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(777,'225','','','M62G:YT3-38-1-8—9','小蜻蜓眼','人工硅酸盐','3-38-1-8残',NULL,NULL,'2颗','3-38-1-9:高0.58，径0.65，孔径0.21-0.23','0.29','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(778,'226','','','M62G:YT3-38-1-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','3-38-1-10:高1.91，径0.72，孔径0.29','3-38-1-10:1.39','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(779,'227','','','M62G:YT3-38-2-1—3','费昂斯管珠','人工硅酸盐','3-38-2-1残',NULL,NULL,'3颗','3-38-2-2:高1.55，径0.67，孔径0.32；3-38-2-3:高1.52，径0.70，孔径0.3','3-38-2-2:0.94；3-38-2-3:1.13','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(780,'228','','','M62G:YT3-38-2-4—5','小蜻蜓眼','人工硅酸盐','3-38-2-5残',NULL,NULL,'2颗','3-38-2-4:高0.5，径0.64，孔径0.17','3-38-2-4:0.23','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(781,'229','','','M62G:YT3-38-2-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(782,'230','','','M62G:YT3-38-2-10—12','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'3颗','3-38-2-10:高1.12，径0.67，孔径0.38-0.40；3-38-2-11:高13.9，径0.69，孔径0.32；3-38-2-12:高1.13，径0.67，孔径0.2-0.25','3-38-2-10:0.47；3-38-2-11:1.07；3-38-2-12:0.79','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(783,'231','','','M62G:YT3-38-3-1—2','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'2颗','3-38-3-1:高1.73，径0.68，孔径0.3；3-38-3-2:高1.80，径0.73，孔径0.27-0.32','3-38-3-1:1.32；3-38-3-2：0.99','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(784,'232','','','M62G:YT3-38-3-3—4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(785,'233','','','M62G:YT3-38-3-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(786,'234','','','M62G:YT3-38-3-10—13','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'4颗','3-38-3-10:高1.15，径0.71，孔径0.27-0.28；3-38-3-11:高1.11，径0.73，孔径0.34；3-38-3-12:高1.16，径0.67，孔径0.34-0.36；3-38-3-13:高1.03，径0.64，孔径0.38-0.42','3-38-3-10:0.56；3-38-3-11：0.62；3-38-3-12：0.59；3-38-3-13：0.39','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(787,'235','','','M62G:YT3-38-4-1—2','费昂斯管珠','人工硅酸盐','完整',NULL,NULL,'2颗','3-38-4-1:高1.76，径0.68，孔径0.33；3-38-4-2:高1.56，径0.66，孔径0.33-0.35','3-38-4-1:1.27；3-38-4-2：1.01','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(788,'236','','','M62G:YT3-38-4-3—4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2颗','','','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(789,'237','','','M62G:YT3-38-4-7—8','小蜻蜓眼','人工硅酸盐','3-38-4-7残',NULL,NULL,'2颗','3-38-4-8:高0.55，径0.72，孔径0.18','3-38-4-8：0.25','M62','下段费昂丝管珠与红玉髓管珠间','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(790,'238','','','M62G:YT3-38-4-9—12','费昂斯管珠','人工硅酸盐',';',NULL,NULL,'4颗','3-38-4-9:高1.05，径0.75，孔径0.39-0.41；3-38-4-10:高1.14，径0.72，孔径0.33-0.36；3-38-4-11:高1.31，径0.73，孔径0.27；3-38-4-12:高1.17，径0.69，孔径0.24-0.28','3-38-4-9：0.58；3-38-4-10:0.60；3-38-4-11：0.69；3-38-4-12：0.71','M62','右股骨外侧中下段','25.03.24',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(791,'239','','','M62G:YT4','右腿料珠片饰','人工硅酸盐','完整',NULL,NULL,'1127颗：汉蓝805、汉紫268、铅白54颗','','','M62','盆骨右下侧铁环下层至大蜻蜓眼处','25.03.18-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(792,'240','','','M62G:YT4-1','右腿第一层料珠片饰','人工硅酸盐','完整',NULL,'1组','779颗：汉蓝507、汉紫189颗、铅白64颗','长15.4、宽3.2-4.8\r\n料珠高0.25、径0.28','','M62','髋骨右侧及右股骨外侧\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(793,'241','','','M62G:YT4-1-1','右腿第一层料珠片饰上半段','人工硅酸盐','完整',NULL,NULL,'420颗：汉蓝293、汉紫80颗、铅白47颗','长约8、宽3.2-4.8\r\n料珠高0.25、径0.28','','M62','右股骨外侧，铁环下端至左侧可见第5颗大蓝珠处\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\n\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.20',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(794,'242','','','M62G:YT4-1-2','右腿第一层料珠片饰下半段','人工硅酸盐','完整',NULL,NULL,'359颗：汉蓝214、汉紫100颗、铅白17颗','长约7.6、宽约4.8\r\n料珠高0.25、径0.28','','M62','右股骨外侧，左侧可见第5颗大蓝珠至大蜻蜓眼处\r\nTA3：19.8-28.6～7.4\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1','25.03.20-25.03.21',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(795,'243','','','M62G:YT4-2','右腿第二层料珠片饰','人工硅酸盐','完整',NULL,NULL,'385颗：汉蓝306、汉紫79颗','长15.4、宽3.2-4.8\r\n料珠高0.25、径0.28','','M62','髋骨右侧及右股骨外侧\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(796,'244','','','M62G:YT4-2-1','右腿第二层料珠片饰上半段','人工硅酸盐','完整',NULL,NULL,'13364颗：汉蓝112、汉紫77颗、铅白10','长约8、宽3.2-4.8\r\n料珠高0.25、径0.28','','M62','右股骨外侧，铁环下端至左侧可见第5颗大蓝珠处\r\nTB3：10.4-0.1～9.1\r\nTA3：8.6-25.4～9.8\r\n\r\nTA3：19.8-28.6～7.4','25.03.18-25.03.20',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(797,'245','','','M62G:YT4-2-2','右腿第二层料珠片饰下半段','人工硅酸盐','完整',NULL,NULL,'223颗：汉蓝186、汉紫37颗','长约7.6、宽约4.8\r\n料珠高0.25、径0.28','','M62','右股骨外侧，左侧可见第5颗大蓝珠至大蜻蜓眼处\r\nTA3：19.8-28.6～7.4\r\nTB4：11.5-0.2～7.2\r\nTB4：7.6-0.9～7.1','25.03.20-25.03.21',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(798,'246','','','','左腿垂坠饰','金属、玉石、人工硅酸盐','完整',NULL,'1套2组','铁环1枚、贴金铜盒1件、费昂丝管珠40枚、红玉髓管珠17枚碟形珠11枚、小蜻蜓眼14枚、铜月牙形饰9枚、料珠共1041颗','整体长约29.7、宽约3.4','','M62','左股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','金、铜、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(799,'247','','','M62G:ZT2','左腿右侧垂坠饰','金属、玉石、人工硅酸盐','',NULL,'1组','贴金铜盒1件、铜月牙形饰4枚、费昂斯管珠40枚、小蜻蜓眼14枚、红玉髓六棱管珠9枚碟形珠1枚','整体长约25、宽约3.4','','M62','左股骨上端铁环下至右股骨外侧下端近髌骨处','25.06.10-25.09.25',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','金、铜、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(800,'248','','','M62G:ZT2-1—6','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'6枚','2-1：高1.8、径0.7；2-2：高1.61、径0.79；2-3：高1.52、径0.77；2-4：高1.07、径0.74；2-5：高1.06、径0.66；2-6：高1.0、径0.73；','0.6；1.6；1.32；0.64；0.43；0.59','M62','分布于贴金铜盒上端一周、铁环右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(801,'249','','','M62G:ZT2-7','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','','','M62','铁环右上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(802,'250','','','M62G:ZT2-8—13','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'6枚','2-8：高1.09、径0.66；2-9：高0.93、径0.6；2-10：高1.0、径0.71；2-11：高1.14、径0.7；2-12：高1.18、径0.7；2-13：高1.05、径0.7','0.45；0.36；0.37；0.55；0.76；0.58','M62','分布于贴金铜盒上端一周、铁环右侧、右上侧及上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(803,'251','','','M62G:ZT2-14','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','高0.55、径0.78','0.32','M62','铁环右上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(804,'252','','','M62G:ZT2-15—18','费昂丝管珠','人工硅酸盐','3整1残',NULL,NULL,'4枚','2-15：高1.45、径0.8；2-16：高1.44、径0.71；2-17：高1.65、径0.77','1.51；1.47；1.59','M62','分布于贴金铜盒上端一周、铁环右上侧及上端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(805,'253','','','M62G:ZT2-21-1-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-1-1：高0.93、径0.65、孔径0.34；2-21-1-2：高1.63、径0.77；2-21-1-3：高1.58、径0.75','0.35；1.49；1.44','M62','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(806,'254','','','M62G:ZT2-21-1-4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','','','M62','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(807,'255','','','M62G:ZT2-21-1-8—9','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2枚','','','M62','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(808,'256','','','M62G:ZT2-21-1-10—12','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-1-10：高1.37、径0.78；2-21-1-11：高1.4、径0.87；2-21-1-13：高1.35、径0.83','0.87；1.06；0.96','M62','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(809,'257','','','M62G:ZT2-21-2-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-2-1：高1.03、径0.69；2-21-2-2：高1.75、径0.74、孔径0.34；2-21-2-3：高1.27、径0.8','0.43；1.05；0.79','M62','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(810,'258','','','M62G:ZT2-21-2-4','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1枚','','','M62','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(811,'259','','','M62G:ZT2-21-2-7—8','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'2枚','','','M62','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(812,'260','','','M62G:ZT2-21-2-9—11','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-2-9：高1.27、径0.64；2-21-2-10：高1.22、径0.87；2-21-2-11：高1.83、径0.77','0.85；0.94；1.96','M62','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(813,'261','','','M62G:ZT2-21-3-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-3-1：高1.35、径0.75；2-21-3-2：高1.44、径0.7；2-21-3-3：高1.43、径0.74','0.88；0.94；0.98','M62','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(814,'262','','','M62G:ZT2-21-3-4','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'1枚','高0.5、径0.68','0.25','M62','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(815,'263','','','M62G:ZT2-21-3-7—8','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2枚','高0.65、径0.51，孔径0.018','','M62','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(816,'264','','','M62G:ZT2-21-3-9—11','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-3-9：高1.36、径0.8；2-21-3-10：高1.26、径0.8；2-21-3-11：高1.32、径0.83','1.04；1.04；1.1','M62','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(817,'265','','','M62G:ZT2-21-4-1—3','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-4-1：高1.17、径0.76；2-21-4-2：高1.14、径0.7；2-21-4-3：高1.75、径0.39','0.76；0.69；0.86','M62','分布于贴金铜盒下端右侧','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(818,'266','','','M62G:ZT2-21-4-4','小蜻蜓眼','人工硅酸盐','残',NULL,NULL,'1枚','','','M62','左股骨上端外侧、费昂斯管珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(819,'267','','','M62G:ZT2-21-4-7—8','小蜻蜓眼','人工硅酸盐','完整',NULL,NULL,'2枚','2-21-4-7：高0.52、径0.63；2-21-4-8：高0.5、径0.64','0.21；0.25','M62','左股骨上端外侧、红玉髓碟形珠下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','蜻蜓眼',7,NULL,NULL,NULL,NULL),(820,'268','','','M62G:ZT2-21-4-9—11','费昂丝管珠','人工硅酸盐','完整',NULL,NULL,'3枚','2-21-4-9：高1.48、径0.81；2-21-4-10：高1.63、径0.84','1.25；1.75','M62','左股骨中端外侧、小蜻蜓眼下端','',NULL,NULL,'一楼清棺室→2025.07.18天津科荟→2026.04.07一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂（费昂斯）',7,NULL,NULL,NULL,NULL),(821,'269','','','M62G:ZT3','左腿左侧垂坠饰','金属、玉石、人工硅酸盐','完整',NULL,'1组','红玉髓碟形珠10枚、大蓝珠4枚、红玉髓枣核型管珠8枚、铜镜1枚','','','M62','整体堆积于贴金铜盒上端及铁环内、叠压于铜镜下','25.06.10-25.09.25',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','铜、红玉髓、釉砂',7,NULL,NULL,NULL,NULL),(822,'270','','','M62G:ZT3-3—6','大蓝珠','人工硅酸盐','完整',NULL,NULL,'4枚','3-3：高0.49、径0.58、孔径0.24；3-4：高0.51、径0.63、孔径0.24；3-5：高0.48、径0.59、孔径0.25；3-6：高0.45、径0.64','0.13；0.15；0.13；0.15','M62','盆骨内侧左下端，贴金铜盒右侧、铁环右下端','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(823,'271','','','M62GZ:ZT4','料珠饰','人工硅酸盐','基本完整',NULL,'1组','共1019颗：汉蓝541、汉紫392、铅白86颗，铜月牙形饰5枚','整体长约29.7、宽约3.4','','M62','左股骨上端铁环下至右股骨外侧下端近髌骨处\r\nTA3：12.1-26.5～6.8\r\nTA4：15.3-1.0～8.2','25.06.10-25.09.25',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(824,'272','','','M62G:ZT4-1','料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共821颗：汉蓝454、汉紫302、铅白65颗','整体长约16、宽约3.4','','M62','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(825,'273','','','M62G:ZT4-1-1','1层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共38颗：汉蓝17、汉紫21','','','M62','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(826,'274','','','M62G:ZT4-1-2','2层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共59颗：汉蓝33、汉紫26','','','M62','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(827,'275','','','M62G:ZT4-1-3','3层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共41颗：汉蓝25、汉紫11、铅白6颗','','','M62','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(828,'276','','','M62G:ZT4-1-4','4层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共27颗：汉蓝9、汉紫14、铅白4颗','','','M62','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(829,'277','','','M62G:ZT4-1-5','5层料珠片饰','人工硅酸盐','基本完整',NULL,NULL,'共18颗：汉蓝8、汉紫14、铅白6颗','','','M62','盆骨左外侧腰带下端至左股骨中段','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(830,'278','','','M62G:ZT4-1-6','各部位收集并归入料珠片饰','人工硅酸盐','基本完整',NULL,NULL,NULL,'','','M62','左股骨上端、髋骨左下侧背面、髋骨左侧铜镜下、拆棺散落','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(831,'279','','','M62G:ZT4-2','料珠串饰','金属、人工硅酸盐','基本完整',NULL,'1组5串','串饰共5串，铜月牙形饰5枚、料珠共198颗：汉蓝87、汉紫90、铅白21','整体长约13.5','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','铜、釉砂',7,NULL,NULL,NULL,NULL),(832,'280','','','M62G:ZT4-2-1-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','汉蓝5颗','','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(833,'281','','','M62G:ZT4-2-2-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共36颗，汉蓝15、汉紫20、铅白1颗','','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(834,'282','','','M62G:ZT4-2-3-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共24颗：汉紫21、铅白23颗','','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(835,'283','','','M62G:ZT4-2-4-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共48颗，汉蓝33、汉紫13、铅白2颗','','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(836,'284','','','M62G:ZT4-2-5-1','料珠串饰','人工硅酸盐','基本完整',NULL,'1串','共31颗，汉蓝17、汉紫14','','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(837,'285','','','M62G:ZT4-2-6','料珠串饰（散落）','人工硅酸盐','基本完整',NULL,'1串','共53颗，汉蓝20、汉紫13、铅白9颗','','','M62','左股骨中段外侧至下端近髌骨处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(838,'286','','','','脚部装饰','玉石、人工硅酸盐','完整',NULL,'1套2组','料珠937颗：汉蓝583、汉紫339、铅白51\r\n红玉髓珠30颗\r\n绿松石片珠10颗','','','M62','脚部及周围','2026.01.09',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','红玉髓、绿松石、釉砂',7,NULL,NULL,NULL,NULL),(839,'287','','','M62G:YJ1','料珠片饰','人工硅酸盐','基本完整',NULL,'1片','共293颗：汉蓝293、汉紫189、铅白35颗','','','M62','脚部右侧处','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(840,'288','','','M62G:ZJ1','料珠片饰','人工硅酸盐','基本完整',NULL,'1片','共290颗：汉蓝290、汉紫150、铅白16颗','','','M62','集中于脚部左侧处向右侧散落','',NULL,NULL,'一楼清棺室','','卢凯','殷茹','杨欣、王立莹','','','',1,'2026-07-06 15:47:01','2026-07-06 15:47:01','釉砂',7,NULL,NULL,NULL,NULL),(841,'289','CX:1','盖弓帽','','','','','',NULL,NULL,'','','M31','','',NULL,NULL,'','','','','','','','',1,'2026-07-23 09:52:23','2026-07-23 10:49:57','',9,NULL,NULL,NULL,NULL),(845,'290','1','1','','','','','',NULL,NULL,'','','M16/G1','','',NULL,NULL,'','','','','','','',NULL,1,'2026-07-23 11:29:43','2026-07-23 11:29:43','',11,NULL,NULL,1,NULL);
/*!40000 ALTER TABLE `artifacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `burial`
--

DROP TABLE IF EXISTS `burial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `burial` (
  `id` int NOT NULL AUTO_INCREMENT,
  `burial_no` varchar(50) DEFAULT NULL COMMENT '墓葬编号',
  `name` varchar(200) DEFAULT NULL,
  `site_id` int DEFAULT NULL COMMENT '所属遗址ID',
  `site_name` varchar(200) DEFAULT NULL COMMENT '所属遗址',
  `era` varchar(100) DEFAULT NULL COMMENT '年代',
  `burial_type` varchar(100) DEFAULT NULL COMMENT '墓葬形制',
  `orientation` varchar(100) DEFAULT NULL COMMENT '朝向',
  `burial_style` varchar(100) DEFAULT NULL COMMENT '葬式',
  `robber_hole` varchar(10) DEFAULT NULL COMMENT '盗洞：有/无',
  `excavation_date` date DEFAULT NULL COMMENT '发掘时间',
  `has_coffin` tinyint DEFAULT '0' COMMENT '有棺',
  `has_chariot` tinyint DEFAULT '0' COMMENT '有车',
  `coffin_count` int DEFAULT '0' COMMENT '棺数',
  `coffin_material` varchar(100) DEFAULT NULL COMMENT '棺材质',
  `coffin_decoration` varchar(200) DEFAULT NULL COMMENT '棺饰',
  `skeleton_status` varchar(50) DEFAULT NULL COMMENT '棺内人骨',
  `chariot_count` int DEFAULT '0' COMMENT '车数',
  `horse_count` int DEFAULT '0' COMMENT '马数',
  `chariot_decoration` varchar(200) DEFAULT NULL COMMENT '车饰',
  `chariot_type` varchar(200) DEFAULT NULL COMMENT '车类型',
  `artifact_count` int DEFAULT '0' COMMENT '随葬品数量',
  `bone_preservation` varchar(50) DEFAULT NULL COMMENT '人骨保存',
  `status` varchar(20) DEFAULT '待发掘' COMMENT '状态',
  `notes` text COMMENT '备注',
  `created_by` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='墓葬信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `burial`
--

LOCK TABLES `burial` WRITE;
/*!40000 ALTER TABLE `burial` DISABLE KEYS */;
INSERT INTO `burial` VALUES (1,'M60','',NULL,'张家川','战国中期','',NULL,NULL,NULL,'2026-07-05',1,0,1,'','','',5,4,'','',1,'完整','待发掘','',1,'2026-07-03 15:50:45','2026-07-04 16:38:53'),(2,'M40','',NULL,'','战国晚期','',NULL,NULL,NULL,'2026-07-07',1,0,1,'','','',1,4,'','',0,'完整','待发掘','',1,'2026-07-03 15:56:42','2026-07-04 16:38:17'),(3,'M39','',NULL,'','战国晚期','',NULL,NULL,NULL,'2026-07-27',1,0,1,'','','',2,4,'','',0,'残','待发掘','发掘很好',1,'2026-07-03 16:11:50','2026-07-04 16:37:55'),(4,'M28','',NULL,'','','',NULL,NULL,NULL,NULL,1,0,1,'','','',0,0,'','',0,'','发掘中','',1,'2026-07-03 16:21:20','2026-07-04 16:37:42'),(5,'M25','',NULL,'','','',NULL,NULL,NULL,NULL,1,0,1,'','','',0,0,'','',0,'','发掘中','',1,'2026-07-03 16:21:24','2026-07-04 16:37:30'),(6,'M6','',NULL,'','','',NULL,NULL,NULL,NULL,0,0,0,'','','',0,0,'','',0,'','待发掘','',1,'2026-07-03 16:38:53','2026-07-04 15:08:13'),(7,'M62','M62',NULL,'','','',NULL,NULL,NULL,NULL,1,1,1,'','','',2,0,'','',0,'','待发掘','',1,'2026-07-04 16:42:05','2026-07-11 14:54:58'),(8,'M69','M69',NULL,'','','',NULL,NULL,NULL,NULL,1,0,1,'','','',0,0,'','',0,'','待发掘','',1,'2026-07-04 16:42:16','2026-07-04 16:42:16'),(9,'M31','M31',NULL,'','','',NULL,NULL,NULL,NULL,0,1,0,'','','',1,0,'','',0,'','待发掘','',1,'2026-07-04 16:42:55','2026-07-04 16:42:55'),(10,'M45','M45',NULL,'','','',NULL,NULL,NULL,NULL,1,1,1,'','','',2,0,'','',0,'','待发掘','',1,'2026-07-04 16:43:06','2026-07-06 15:28:52'),(11,'M16','M16',NULL,'','','',NULL,NULL,NULL,NULL,1,0,1,'','','',0,0,'','',0,'','已发掘','',1,'2026-07-06 15:28:44','2026-07-16 15:41:48');
/*!40000 ALTER TABLE `burial` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chariot_workflow_media`
--

DROP TABLE IF EXISTS `chariot_workflow_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chariot_workflow_media` (
  `id` int NOT NULL AUTO_INCREMENT,
  `timeline_id` int NOT NULL,
  `media_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chariot_workflow_media`
--

LOCK TABLES `chariot_workflow_media` WRITE;
/*!40000 ALTER TABLE `chariot_workflow_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `chariot_workflow_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chariot_workflow_note`
--

DROP TABLE IF EXISTS `chariot_workflow_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chariot_workflow_note` (
  `id` int NOT NULL AUTO_INCREMENT,
  `timeline_id` int NOT NULL,
  `note_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chariot_workflow_note`
--

LOCK TABLES `chariot_workflow_note` WRITE;
/*!40000 ALTER TABLE `chariot_workflow_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `chariot_workflow_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chariot_workflow_timeline`
--

DROP TABLE IF EXISTS `chariot_workflow_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chariot_workflow_timeline` (
  `id` int NOT NULL AUTO_INCREMENT,
  `burial_id` int NOT NULL,
  `chariot_index` int NOT NULL DEFAULT '1' COMMENT '车序号',
  `flow_id` int DEFAULT NULL,
  `event_date` date DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chariot_workflow_timeline`
--

LOCK TABLES `chariot_workflow_timeline` WRITE;
/*!40000 ALTER TABLE `chariot_workflow_timeline` DISABLE KEYS */;
/*!40000 ALTER TABLE `chariot_workflow_timeline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chariot_workflow_tree`
--

DROP TABLE IF EXISTS `chariot_workflow_tree`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chariot_workflow_tree` (
  `id` int NOT NULL AUTO_INCREMENT,
  `burial_id` int NOT NULL,
  `chariot_index` int NOT NULL DEFAULT '1' COMMENT '车序号（1=车1, 2=车2...）',
  `label` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chariot_workflow_tree`
--

LOCK TABLES `chariot_workflow_tree` WRITE;
/*!40000 ALTER TABLE `chariot_workflow_tree` DISABLE KEYS */;
/*!40000 ALTER TABLE `chariot_workflow_tree` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coffin`
--

DROP TABLE IF EXISTS `coffin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coffin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `burial_id` int DEFAULT NULL COMMENT '所属墓葬ID',
  `coffin_no` varchar(50) DEFAULT NULL COMMENT '棺编号',
  `coffin_count` int DEFAULT '1' COMMENT '棺数',
  `material` varchar(200) DEFAULT NULL COMMENT '棺材质',
  `decoration` varchar(200) DEFAULT NULL COMMENT '棺饰',
  `skeleton_status` varchar(50) DEFAULT NULL COMMENT '人骨保存',
  `notes` text COMMENT '备注',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='棺信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coffin`
--

LOCK TABLES `coffin` WRITE;
/*!40000 ALTER TABLE `coffin` DISABLE KEYS */;
/*!40000 ALTER TABLE `coffin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coffin_workflow_media`
--

DROP TABLE IF EXISTS `coffin_workflow_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coffin_workflow_media` (
  `id` int NOT NULL AUTO_INCREMENT,
  `timeline_id` int NOT NULL,
  `media_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coffin_workflow_media`
--

LOCK TABLES `coffin_workflow_media` WRITE;
/*!40000 ALTER TABLE `coffin_workflow_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `coffin_workflow_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coffin_workflow_note`
--

DROP TABLE IF EXISTS `coffin_workflow_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coffin_workflow_note` (
  `id` int NOT NULL AUTO_INCREMENT,
  `timeline_id` int NOT NULL,
  `note_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coffin_workflow_note`
--

LOCK TABLES `coffin_workflow_note` WRITE;
/*!40000 ALTER TABLE `coffin_workflow_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `coffin_workflow_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coffin_workflow_timeline`
--

DROP TABLE IF EXISTS `coffin_workflow_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coffin_workflow_timeline` (
  `id` int NOT NULL AUTO_INCREMENT,
  `coffin_id` int NOT NULL,
  `flow_id` int DEFAULT NULL,
  `event_date` date DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coffin_workflow_timeline`
--

LOCK TABLES `coffin_workflow_timeline` WRITE;
/*!40000 ALTER TABLE `coffin_workflow_timeline` DISABLE KEYS */;
/*!40000 ALTER TABLE `coffin_workflow_timeline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coffin_workflow_tree`
--

DROP TABLE IF EXISTS `coffin_workflow_tree`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coffin_workflow_tree` (
  `id` int NOT NULL AUTO_INCREMENT,
  `coffin_id` int NOT NULL,
  `label` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coffin_workflow_tree`
--

LOCK TABLES `coffin_workflow_tree` WRITE;
/*!40000 ALTER TABLE `coffin_workflow_tree` DISABLE KEYS */;
/*!40000 ALTER TABLE `coffin_workflow_tree` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `completeness_categories`
--

DROP TABLE IF EXISTS `completeness_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `completeness_categories` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` int DEFAULT '0' COMMENT '父级ID，0表示一级分类',
  `name` varchar(50) NOT NULL COMMENT '完整度名称',
  `level` tinyint NOT NULL COMMENT '层级: 1/2',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='完整度分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `completeness_categories`
--

LOCK TABLES `completeness_categories` WRITE;
/*!40000 ALTER TABLE `completeness_categories` DISABLE KEYS */;
INSERT INTO `completeness_categories` VALUES (1,0,'完整',1,1),(2,0,'基本完整',1,2),(3,0,'残可复原',1,3),(4,0,'残余不可复原',1,4),(5,0,'残',1,5),(6,3,'缺',2,1),(7,3,'断',2,2),(8,3,'裂',2,3),(9,5,'朽',2,1),(10,5,'碎',2,2),(11,5,'渣',2,3),(12,5,'粉',2,4);
/*!40000 ALTER TABLE `completeness_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_archive`
--

DROP TABLE IF EXISTS `conservation_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_archive` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `artifact_id` int DEFAULT NULL,
  `archive_code` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `archive_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `archive_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'comprehensive',
  `compiler` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reviewer` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `approval_person` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `archive_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'compiling',
  `current_version` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'V1.0',
  `executive_summary` text COLLATE utf8mb4_unicode_ci,
  `protection_goal` text COLLATE utf8mb4_unicode_ci,
  `conservation_basis` text COLLATE utf8mb4_unicode_ci,
  `final_conclusion` text COLLATE utf8mb4_unicode_ci,
  `source_survey_id` bigint DEFAULT NULL,
  `source_survey_version` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `compiled_date` date DEFAULT NULL,
  `completeness_rate` int DEFAULT '0',
  `workspace_json` json DEFAULT NULL,
  `submitted_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_archive_project` (`project_id`),
  UNIQUE KEY `uk_archive_code` (`archive_code`),
  KEY `idx_archive_status` (`archive_status`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_archive`
--

LOCK TABLES `conservation_archive` WRITE;
/*!40000 ALTER TABLE `conservation_archive` DISABLE KEYS */;
INSERT INTO `conservation_archive` VALUES (32,57,554,'CA-M664-057','琉璃杯残片保护修复档案','comprehensive','陈玥寒',NULL,NULL,'compiling','V1.0',NULL,'','',NULL,NULL,NULL,'2026-07-23',30,'{\"plan\": {\"compiler\": \"\", \"planCode\": \"\", \"planGoal\": \"\", \"planName\": \"\", \"planStatus\": \"draft\", \"compiledDate\": \"\", \"riskAnalysis\": \"\", \"expectedResult\": \"\", \"selectedMethod\": \"\", \"technicalBasis\": \"\", \"emergencyMeasures\": \"\", \"safetyRequirements\": \"\", \"environmentRequirements\": \"\", \"compatibilityDescription\": \"\", \"reversibilityDescription\": \"\"}, \"goals\": {\"overall\": \"\", \"longTerm\": \"\", \"appearance\": \"\", \"displayUse\": \"\", \"diseaseControl\": \"\", \"structuralStability\": \"\", \"informationRetention\": \"\"}, \"tools\": [], \"advice\": {\"display\": \"\", \"handling\": \"\", \"lighting\": \"\", \"packaging\": \"\", \"airQuality\": \"\", \"shockproof\": \"\", \"reviewCycle\": \"\", \"humidityRange\": \"\", \"followUpAdvice\": \"\", \"monitorDiseases\": \"\", \"temperatureRange\": \"\", \"warningConditions\": \"\", \"monitoringIndicators\": \"\"}, \"survey\": {\"id\": 22, \"status\": \"submitted\", \"summary\": \"整体保存状况较差\", \"surveyor\": \"王飞洋\", \"projectId\": 57, \"surveyCode\": \"DS-57-20260723\", \"surveyDate\": \"2026-07-23\", \"surveyLocation\": \"马家塬车马清理室\", \"overallRiskLevel\": \"low\", \"environmentSummary\": \"环境合适\", \"preservationStatus\": \"fair\", \"structuralStability\": \"stable\"}, \"evaluation\": {\"evaluator\": \"\", \"retestIds\": [], \"colorChange\": \"\", \"glossChange\": \"\", \"diseaseControl\": \"\", \"evaluationDate\": \"\", \"hasSideEffects\": \"pending\", \"goalAchievement\": \"\", \"remainingIssues\": \"\", \"surfaceStrength\": \"\", \"structuralChange\": \"\", \"acceptanceConclusion\": \"\", \"materialCompatibility\": \"\", \"appearanceCoordination\": \"\"}, \"principles\": {\"notes\": {}, \"custom\": \"\", \"options\": [\"最小干预原则\", \"保持原状原则\", \"历史真实性原则\", \"可辨识性原则\", \"可再处理性原则\", \"兼容性原则\", \"安全性原则\", \"预防性保护原则\"], \"selected\": []}, \"attachments\": [], \"comparisons\": [], \"materialTab\": \"materials\", \"planMaterials\": [], \"retestOptions\": [], \"diseaseRecords\": [{\"id\": 33, \"side\": null, \"media\": [], \"partName\": null, \"severity\": \"minor\", \"surveyId\": 22, \"emergency\": false, \"projectId\": 57, \"sortOrder\": 0, \"extentUnit\": null, \"mediaCount\": 0, \"morphology\": null, \"diseaseName\": \"破损\", \"diseaseType\": \"\", \"extentValue\": null, \"causeFactors\": [], \"causeAnalysis\": null, \"diseaseTypeId\": null, \"diseaseCategory\": \"\", \"structuralImpact\": \"none\", \"developmentStatus\": \"stable\", \"recommendedAction\": null, \"positionDescription\": null}, {\"id\": 34, \"side\": null, \"media\": [], \"partName\": null, \"severity\": \"minor\", \"surveyId\": 22, \"emergency\": false, \"projectId\": 57, \"sortOrder\": 1, \"extentUnit\": null, \"mediaCount\": 0, \"morphology\": null, \"diseaseName\": \"破损\", \"diseaseType\": \"\", \"extentValue\": null, \"causeFactors\": [], \"causeAnalysis\": null, \"diseaseTypeId\": null, \"diseaseCategory\": \"\", \"structuralImpact\": \"none\", \"developmentStatus\": \"stable\", \"recommendedAction\": null, \"positionDescription\": null}], \"processSummary\": {\"steps\": [], \"pending\": 0, \"planned\": 0, \"completed\": 0, \"processing\": 0}, \"planDiseaseList\": [], \"processParameters\": {\"humidity\": \"\", \"dryingTime\": \"\", \"temperature\": \"\", \"operationTimes\": \"\", \"parameterLimit\": \"\", \"qualityControl\": \"\", \"operationMethod\": \"\", \"applicationOrder\": \"\", \"emergencyRequirement\": \"\", \"materialConcentration\": \"\"}, \"materialDictionary\": [], \"restorationResults\": [], \"detectionReferences\": [], \"surveyReferenceUpdated\": false}',NULL,'2026-07-23 15:24:13','2026-07-23 16:21:54',0);
/*!40000 ALTER TABLE `conservation_archive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_archive_advice`
--

DROP TABLE IF EXISTS `conservation_archive_advice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_archive_advice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `temperature_range` varchar(100) DEFAULT NULL,
  `humidity_range` varchar(100) DEFAULT NULL,
  `lighting` varchar(255) DEFAULT NULL,
  `air_quality` varchar(255) DEFAULT NULL,
  `packaging` varchar(255) DEFAULT NULL,
  `handling` varchar(255) DEFAULT NULL,
  `shockproof` varchar(255) DEFAULT NULL,
  `review_cycle` varchar(255) DEFAULT NULL,
  `monitor_diseases` text,
  `monitoring_indicators` text,
  `follow_up_advice` text,
  `warning_conditions` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_archive_advice_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='保护修复档案后续保存建议';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_archive_advice`
--

LOCK TABLES `conservation_archive_advice` WRITE;
/*!40000 ALTER TABLE `conservation_archive_advice` DISABLE KEYS */;
INSERT INTO `conservation_archive_advice` VALUES (78,57,'','','','','','','','','','','','','2026-07-23 16:21:54','2026-07-23 16:21:54');
/*!40000 ALTER TABLE `conservation_archive_advice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_archive_attachment`
--

DROP TABLE IF EXISTS `conservation_archive_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_archive_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `archive_id` bigint NOT NULL,
  `project_id` int NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content_type` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_data` longblob,
  `source_module` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `section_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version_no` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `uploaded_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `file_url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `oss_object_key` varchar(600) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_archive_attachment_parent` (`archive_id`),
  KEY `idx_archive_attachment_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_archive_attachment`
--

LOCK TABLES `conservation_archive_attachment` WRITE;
/*!40000 ALTER TABLE `conservation_archive_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_archive_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_archive_revision`
--

DROP TABLE IF EXISTS `conservation_archive_revision`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_archive_revision` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `archive_id` bigint NOT NULL,
  `version_no` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `revision_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'manual',
  `revision_description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operator` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `archive_snapshot_json` json DEFAULT NULL,
  `workspace_snapshot_json` json DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_archive_revision_parent` (`archive_id`),
  KEY `idx_archive_revision_version` (`archive_id`,`version_no`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_archive_revision`
--

LOCK TABLES `conservation_archive_revision` WRITE;
/*!40000 ALTER TABLE `conservation_archive_revision` DISABLE KEYS */;
INSERT INTO `conservation_archive_revision` VALUES (35,32,'V1.0','initial','建立保护修复档案并初始化业务引用','陈玥寒','{\"archiveCode\": \"CA-M664-057\", \"archiveTitle\": \"琉璃杯残片保护修复档案\"}','{\"plan\": {\"compiler\": \"\", \"planCode\": \"\", \"planGoal\": \"\", \"planName\": \"\", \"planStatus\": \"draft\", \"compiledDate\": \"\", \"riskAnalysis\": \"\", \"expectedResult\": \"\", \"selectedMethod\": \"\", \"technicalBasis\": \"\", \"emergencyMeasures\": \"\", \"safetyRequirements\": \"\", \"environmentRequirements\": \"\", \"compatibilityDescription\": \"\", \"reversibilityDescription\": \"\"}, \"goals\": {\"overall\": \"\", \"longTerm\": \"\", \"appearance\": \"\", \"displayUse\": \"\", \"diseaseControl\": \"\", \"structuralStability\": \"\", \"informationRetention\": \"\"}, \"tools\": [], \"advice\": {\"display\": \"\", \"handling\": \"\", \"lighting\": \"\", \"packaging\": \"\", \"airQuality\": \"\", \"shockproof\": \"\", \"reviewCycle\": \"\", \"humidityRange\": \"\", \"followUpAdvice\": \"\", \"monitorDiseases\": \"\", \"temperatureRange\": \"\", \"warningConditions\": \"\", \"monitoringIndicators\": \"\"}, \"survey\": {\"id\": 22, \"status\": \"submitted\", \"summary\": null, \"surveyor\": \"王飞洋\", \"projectId\": 57, \"surveyCode\": \"DS-57-20260723\", \"surveyDate\": \"2026-07-23\", \"surveyLocation\": null, \"overallRiskLevel\": \"low\", \"environmentSummary\": null, \"preservationStatus\": \"fair\", \"structuralStability\": \"stable\"}, \"evaluation\": {\"evaluator\": \"\", \"retestIds\": [], \"colorChange\": \"\", \"glossChange\": \"\", \"diseaseControl\": \"\", \"evaluationDate\": \"\", \"hasSideEffects\": \"pending\", \"goalAchievement\": \"\", \"remainingIssues\": \"\", \"surfaceStrength\": \"\", \"structuralChange\": \"\", \"acceptanceConclusion\": \"\", \"materialCompatibility\": \"\", \"appearanceCoordination\": \"\"}, \"principles\": {\"notes\": {}, \"custom\": \"\", \"options\": [\"最小干预原则\", \"保持原状原则\", \"历史真实性原则\", \"可辨识性原则\", \"可再处理性原则\", \"兼容性原则\", \"安全性原则\", \"预防性保护原则\"], \"selected\": []}, \"attachments\": [], \"comparisons\": [], \"materialTab\": \"materials\", \"planMaterials\": [], \"retestOptions\": [], \"diseaseRecords\": [{\"id\": 33, \"side\": null, \"media\": [], \"partName\": null, \"severity\": \"minor\", \"surveyId\": 22, \"emergency\": false, \"projectId\": 57, \"sortOrder\": 0, \"extentUnit\": null, \"mediaCount\": 0, \"morphology\": null, \"diseaseName\": \"破损\", \"diseaseType\": \"\", \"extentValue\": null, \"causeFactors\": [], \"causeAnalysis\": null, \"diseaseTypeId\": null, \"diseaseCategory\": \"\", \"structuralImpact\": \"none\", \"developmentStatus\": \"stable\", \"recommendedAction\": null, \"positionDescription\": null}, {\"id\": 34, \"side\": null, \"media\": [], \"partName\": null, \"severity\": \"minor\", \"surveyId\": 22, \"emergency\": false, \"projectId\": 57, \"sortOrder\": 1, \"extentUnit\": null, \"mediaCount\": 0, \"morphology\": null, \"diseaseName\": \"破损\", \"diseaseType\": \"\", \"extentValue\": null, \"causeFactors\": [], \"causeAnalysis\": null, \"diseaseTypeId\": null, \"diseaseCategory\": \"\", \"structuralImpact\": \"none\", \"developmentStatus\": \"stable\", \"recommendedAction\": null, \"positionDescription\": null}], \"processSummary\": {\"steps\": [], \"pending\": 0, \"planned\": 0, \"completed\": 0, \"processing\": 0}, \"planDiseaseList\": [], \"processParameters\": {\"humidity\": \"\", \"dryingTime\": \"\", \"temperature\": \"\", \"operationTimes\": \"\", \"parameterLimit\": \"\", \"qualityControl\": \"\", \"operationMethod\": \"\", \"applicationOrder\": \"\", \"emergencyRequirement\": \"\", \"materialConcentration\": \"\"}, \"materialDictionary\": [], \"restorationResults\": [], \"detectionReferences\": [], \"surveyReferenceUpdated\": false}','2026-07-23 15:24:13');
/*!40000 ALTER TABLE `conservation_archive_revision` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_comparison`
--

DROP TABLE IF EXISTS `conservation_comparison`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_comparison` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `artifact_id` int DEFAULT NULL,
  `process_id` bigint DEFAULT NULL,
  `step_id` bigint DEFAULT NULL,
  `comparison_code` varchar(60) DEFAULT NULL,
  `comparison_title` varchar(255) NOT NULL,
  `comparison_type` varchar(40) DEFAULT 'before_after',
  `target_part` varchar(150) DEFAULT NULL,
  `shooting_position` varchar(150) DEFAULT NULL,
  `before_summary` text,
  `after_summary` text,
  `comparison_description` text,
  `overall_effect` varchar(40) DEFAULT NULL,
  `remaining_issue` text,
  `monitoring_review_part` varchar(150) DEFAULT NULL,
  `monitoring_notes` text,
  `evaluator` varchar(100) DEFAULT NULL,
  `evaluation_date` date DEFAULT NULL,
  `comparison_status` varchar(30) DEFAULT 'draft',
  `overall_comparison` tinyint DEFAULT '0',
  `no_applicable_metrics` tinyint DEFAULT '0',
  `selected_for_archive` tinyint DEFAULT '0',
  `selected_for_restoration` tinyint DEFAULT '0',
  `selected_as_monitoring_baseline` tinyint DEFAULT '0',
  `comparison_json` json DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comparison_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=98017 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='修复前后对比监测来源';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_comparison`
--

LOCK TABLES `conservation_comparison` WRITE;
/*!40000 ALTER TABLE `conservation_comparison` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_comparison` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_comparison_media`
--

DROP TABLE IF EXISTS `conservation_comparison_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_comparison_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comparison_id` bigint NOT NULL,
  `source_media_id` bigint DEFAULT NULL,
  `image_stage` varchar(30) DEFAULT NULL,
  `image_role` varchar(30) DEFAULT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_data` longblob,
  `target_part` varchar(150) DEFAULT NULL,
  `shooting_position` varchar(150) DEFAULT NULL,
  `shooting_time` datetime DEFAULT NULL,
  `photographer` varchar(100) DEFAULT NULL,
  `description` text,
  `sequence_no` int DEFAULT '0',
  `is_primary` tinyint DEFAULT '0',
  `source_module` varchar(100) DEFAULT NULL,
  `file_url` varchar(1000) DEFAULT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comparison_media_parent` (`comparison_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7249196403226266159 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='前后对比影像';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_comparison_media`
--

LOCK TABLES `conservation_comparison_media` WRITE;
/*!40000 ALTER TABLE `conservation_comparison_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_comparison_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_comparison_metric`
--

DROP TABLE IF EXISTS `conservation_comparison_metric`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_comparison_metric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comparison_id` bigint NOT NULL,
  `metric_name` varchar(150) NOT NULL,
  `metric_category` varchar(50) DEFAULT NULL,
  `before_value` decimal(15,4) DEFAULT NULL,
  `after_value` decimal(15,4) DEFAULT NULL,
  `value_unit` varchar(30) DEFAULT NULL,
  `expected_direction` varchar(30) DEFAULT NULL,
  `result_status` varchar(30) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comparison_metric_parent` (`comparison_id`)
) ENGINE=InnoDB AUTO_INCREMENT=98012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='前后对比量化指标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_comparison_metric`
--

LOCK TABLES `conservation_comparison_metric` WRITE;
/*!40000 ALTER TABLE `conservation_comparison_metric` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_comparison_metric` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_disease_media`
--

DROP TABLE IF EXISTS `conservation_disease_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_disease_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `disease_record_id` int NOT NULL,
  `survey_id` int NOT NULL,
  `project_id` int NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `content_type` varchar(120) NOT NULL,
  `file_size` bigint NOT NULL,
  `file_data` longblob,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `annotations_json` json DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `file_url` varchar(1000) DEFAULT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_disease_media_record` (`disease_record_id`),
  KEY `idx_disease_media_survey` (`survey_id`),
  KEY `idx_disease_media_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病害调查影像与标注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_disease_media`
--

LOCK TABLES `conservation_disease_media` WRITE;
/*!40000 ALTER TABLE `conservation_disease_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_disease_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_disease_record`
--

DROP TABLE IF EXISTS `conservation_disease_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_disease_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `survey_id` int NOT NULL COMMENT '所属病害调查ID',
  `project_id` int NOT NULL COMMENT '关联保护修复项目ID',
  `disease_type_id` int DEFAULT NULL COMMENT '病害类型ID',
  `disease_type` varchar(100) DEFAULT NULL COMMENT '用户输入的病害类型',
  `disease_name` varchar(100) DEFAULT NULL COMMENT '病害名称',
  `disease_category` varchar(30) DEFAULT NULL COMMENT '病害类别',
  `severity` varchar(20) DEFAULT NULL COMMENT '严重程度：minor/moderate/severe/critical',
  `development_status` varchar(30) DEFAULT NULL COMMENT '发展状态',
  `extent_value` decimal(10,2) DEFAULT NULL COMMENT '病害范围值',
  `extent_unit` varchar(20) DEFAULT NULL COMMENT '范围单位',
  `part_name` varchar(100) DEFAULT NULL COMMENT '部位名称',
  `side` varchar(30) DEFAULT NULL COMMENT '方位',
  `position_description` varchar(500) DEFAULT NULL COMMENT '位置描述',
  `morphology` text COMMENT '表观形态',
  `cause_factors_json` json DEFAULT NULL COMMENT '成因因素多选结果',
  `cause_analysis` text COMMENT '成因分析',
  `structural_impact` varchar(30) DEFAULT NULL COMMENT '结构影响',
  `emergency` tinyint DEFAULT '0' COMMENT '是否紧急',
  `recommended_action` text COMMENT '初步处理建议',
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_disease_survey` (`survey_id`),
  KEY `idx_disease_project` (`project_id`),
  KEY `idx_disease_type` (`disease_type_id`),
  KEY `idx_disease_severity` (`severity`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病害明细记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_disease_record`
--

LOCK TABLES `conservation_disease_record` WRITE;
/*!40000 ALTER TABLE `conservation_disease_record` DISABLE KEYS */;
INSERT INTO `conservation_disease_record` VALUES (33,22,57,NULL,NULL,'破损',NULL,'minor','stable',NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL,'none',0,NULL,0,'2026-07-23 15:24:03','2026-07-23 15:24:03',0),(34,22,57,NULL,NULL,'破损',NULL,'minor','stable',NULL,NULL,NULL,NULL,NULL,NULL,'[]',NULL,'none',0,NULL,1,'2026-07-23 15:24:03','2026-07-23 15:24:03',0);
/*!40000 ALTER TABLE `conservation_disease_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_disease_survey`
--

DROP TABLE IF EXISTS `conservation_disease_survey`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_disease_survey` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL COMMENT '关联保护修复项目ID',
  `survey_code` varchar(50) DEFAULT NULL COMMENT '调查编号',
  `survey_date` date DEFAULT NULL COMMENT '调查日期',
  `surveyor` varchar(100) DEFAULT NULL COMMENT '调查人',
  `survey_location` varchar(200) DEFAULT NULL COMMENT '调查地点',
  `preservation_status` varchar(30) DEFAULT NULL COMMENT '整体保存状态：good/fair/poor/critical',
  `structural_stability` varchar(30) DEFAULT NULL COMMENT '结构稳定性：stable/partially_unstable/unstable/dangerous',
  `environment_summary` text COMMENT '环境摘要',
  `overall_risk_level` varchar(20) DEFAULT NULL COMMENT '综合风险等级',
  `summary` text COMMENT '调查总结',
  `status` varchar(20) DEFAULT 'draft' COMMENT '调查状态：draft/submitted/reviewed/returned',
  `reviewer` varchar(100) DEFAULT NULL COMMENT '审核人',
  `review_opinion` text COMMENT '审核意见',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_survey_project` (`project_id`),
  KEY `idx_survey_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病害调查主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_disease_survey`
--

LOCK TABLES `conservation_disease_survey` WRITE;
/*!40000 ALTER TABLE `conservation_disease_survey` DISABLE KEYS */;
INSERT INTO `conservation_disease_survey` VALUES (22,57,'DS-57-20260723','2026-07-23','王飞洋','马家塬车马清理室','fair','stable','环境合适','low','整体保存状况较差','submitted',NULL,NULL,NULL,'2026-07-23 15:24:03','2026-07-23 15:46:57',0);
/*!40000 ALTER TABLE `conservation_disease_survey` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_disease_type`
--

DROP TABLE IF EXISTS `conservation_disease_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_disease_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '类型编码',
  `name` varchar(100) NOT NULL COMMENT '病害名称',
  `category` varchar(30) DEFAULT NULL COMMENT '类别：physical/chemical/biological/structural',
  `applicable_material` varchar(500) DEFAULT NULL COMMENT '适用材质',
  `description` text COMMENT '描述',
  `default_risk_weight` int DEFAULT '1' COMMENT '默认风险权重',
  `enabled` tinyint DEFAULT '1',
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病害类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_disease_type`
--

LOCK TABLES `conservation_disease_type` WRITE;
/*!40000 ALTER TABLE `conservation_disease_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_disease_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_process`
--

DROP TABLE IF EXISTS `conservation_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_process` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `archive_id` bigint DEFAULT NULL,
  `process_code` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `process_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `process_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'not_started',
  `execution_mode` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'formal',
  `supervisor` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `expected_end_date` date DEFAULT NULL,
  `actual_end_date` date DEFAULT NULL,
  `total_steps` int DEFAULT '0',
  `completed_steps` int DEFAULT '0',
  `progress` int DEFAULT '0',
  `execution_summary` text COLLATE utf8mb4_unicode_ci,
  `final_result` text COLLATE utf8mb4_unicode_ci,
  `pause_json` json DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_project` (`project_id`),
  UNIQUE KEY `uk_process_code` (`process_code`),
  KEY `idx_process_status` (`process_status`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_process`
--

LOCK TABLES `conservation_process` WRITE;
/*!40000 ALTER TABLE `conservation_process` DISABLE KEYS */;
INSERT INTO `conservation_process` VALUES (29,57,32,'CP-M664-057','琉璃杯残片保护修复执行过程','not_started','draft_plan','陈玥寒',NULL,'2026-07-30',NULL,0,0,0,NULL,NULL,NULL,'2026-07-23 16:21:55','2026-07-23 16:21:55',0);
/*!40000 ALTER TABLE `conservation_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_process_media`
--

DROP TABLE IF EXISTS `conservation_process_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_process_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `process_id` bigint NOT NULL,
  `step_id` bigint NOT NULL,
  `project_id` int NOT NULL,
  `media_stage` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_data` longblob,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_time` datetime DEFAULT NULL,
  `shooting_position` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_part` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `photographer` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `selected_for_comparison` tinyint DEFAULT '0',
  `selected_for_archive` tinyint DEFAULT '0',
  `selected_for_restoration` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `file_url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `oss_object_key` varchar(600) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_process_media_step` (`step_id`),
  KEY `idx_process_media_parent` (`process_id`),
  KEY `idx_process_media_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_process_media`
--

LOCK TABLES `conservation_process_media` WRITE;
/*!40000 ALTER TABLE `conservation_process_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_process_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_process_step`
--

DROP TABLE IF EXISTS `conservation_process_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_process_step` (
  `id` bigint NOT NULL,
  `process_id` bigint NOT NULL,
  `project_id` int NOT NULL,
  `step_code` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `step_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `step_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `step_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `sequence_no` int DEFAULT '0',
  `progress_weight` int DEFAULT '10',
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_part` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `planned_start_time` datetime DEFAULT NULL,
  `planned_end_time` datetime DEFAULT NULL,
  `actual_start_time` datetime DEFAULT NULL,
  `actual_end_time` datetime DEFAULT NULL,
  `completion_rate` int DEFAULT '0',
  `temporary_step` tinyint DEFAULT '0',
  `requires_monitoring` tinyint DEFAULT '0',
  `step_json` json NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_process_step_parent` (`process_id`),
  KEY `idx_process_step_project` (`project_id`),
  KEY `idx_process_step_status` (`step_status`),
  KEY `idx_process_step_sequence` (`process_id`,`sequence_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_process_step`
--

LOCK TABLES `conservation_process_step` WRITE;
/*!40000 ALTER TABLE `conservation_process_step` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_process_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_project`
--

DROP TABLE IF EXISTS `conservation_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_project` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_code` varchar(50) DEFAULT NULL COMMENT '项目编号',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称',
  `artifact_id` int DEFAULT NULL COMMENT '关联文物ID',
  `artifact_code` varchar(100) DEFAULT NULL COMMENT '用户录入的文物编号',
  `artifact_name` varchar(200) DEFAULT NULL COMMENT '用户录入的文物名称',
  `project_type` varchar(20) DEFAULT '综合' COMMENT '项目类型：保护/修复/复原/综合',
  `status` varchar(20) DEFAULT 'draft' COMMENT '项目状态：draft/active/completed/suspended/archived',
  `current_stage` varchar(30) DEFAULT 'pendingSurvey' COMMENT '当前阶段',
  `risk_level` varchar(20) DEFAULT 'medium' COMMENT '风险等级：high/medium/low',
  `progress` int DEFAULT '0' COMMENT '项目进度 0-100',
  `principal` varchar(100) DEFAULT NULL COMMENT '项目负责人',
  `department` varchar(100) DEFAULT NULL COMMENT '执行部门',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `expected_end_date` date DEFAULT NULL COMMENT '预计完成日期',
  `actual_end_date` date DEFAULT NULL COMMENT '实际完成日期',
  `summary` text COMMENT '项目摘要',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `source_project_id` int DEFAULT NULL COMMENT '来源保护修复项目ID',
  `source_alert_id` bigint DEFAULT NULL COMMENT '来源监测预警ID',
  `source_monitoring_record_id` bigint DEFAULT NULL COMMENT '来源监测记录ID',
  `record_mode` varchar(20) DEFAULT 'standard' COMMENT '建档模式：quick/standard/full',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='保护修复项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_project`
--

LOCK TABLES `conservation_project` WRITE;
/*!40000 ALTER TABLE `conservation_project` DISABLE KEYS */;
INSERT INTO `conservation_project` VALUES (54,'CR-MRX5RGWC','珠饰修复',NULL,'紫色陶珠','18','综合','draft','quick_record','medium',0,'','',NULL,NULL,NULL,'','2026-07-23 14:56:55','2026-07-23 15:07:48',1,NULL,NULL,NULL,'quick'),(56,'zstz-18-xf','珠饰修复',553,'18','紫色陶珠','综合','draft','quick_record','low',0,'陈玥寒','马家塬办公室','2026-07-14','2026-07-22',NULL,'','2026-07-23 15:22:17','2026-07-23 15:22:17',0,NULL,NULL,NULL,'quick'),(57,'M664-LLBCP-XF','玻璃修复',554,'M6：64','琉璃杯残片','综合','active','planning','low',30,'陈玥寒','马家塬实验室','2026-07-14','2026-07-30',NULL,'','2026-07-23 15:23:26','2026-07-23 15:24:03',0,NULL,NULL,NULL,'standard');
/*!40000 ALTER TABLE `conservation_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_quick_record`
--

DROP TABLE IF EXISTS `conservation_quick_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_quick_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `issue_description` text,
  `treatment_method` text,
  `operator_name` varchar(100) DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `conclusion` text,
  `remark` text,
  `record_status` varchar(20) DEFAULT 'draft',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quick_record_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_quick_record`
--

LOCK TABLES `conservation_quick_record` WRITE;
/*!40000 ALTER TABLE `conservation_quick_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_quick_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_quick_record_media`
--

DROP TABLE IF EXISTS `conservation_quick_record_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_quick_record_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quick_record_id` bigint NOT NULL,
  `media_role` varchar(20) NOT NULL,
  `original_name` varchar(255) NOT NULL,
  `content_type` varchar(120) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_url` varchar(1000) NOT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_quick_record_media_record` (`quick_record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_quick_record_media`
--

LOCK TABLES `conservation_quick_record_media` WRITE;
/*!40000 ALTER TABLE `conservation_quick_record_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_quick_record_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_restoration_media`
--

DROP TABLE IF EXISTS `conservation_restoration_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_restoration_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL,
  `part_id` bigint DEFAULT NULL,
  `source_media_id` bigint DEFAULT NULL,
  `source_business_type` varchar(50) DEFAULT 'restoration',
  `source_business_id` bigint DEFAULT NULL,
  `media_stage` varchar(30) DEFAULT NULL,
  `media_type` varchar(30) DEFAULT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_data` longblob,
  `title` varchar(255) DEFAULT NULL,
  `description` text,
  `is_primary` tinyint DEFAULT '0',
  `selected_for_archive` tinyint DEFAULT '0',
  `selected_as_monitoring_baseline` tinyint DEFAULT '0',
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `file_url` varchar(1000) DEFAULT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_restoration_media_result` (`result_id`),
  KEY `idx_restoration_media_source` (`source_business_type`,`source_media_id`)
) ENGINE=InnoDB AUTO_INCREMENT=97019 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复原成果影像';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_restoration_media`
--

LOCK TABLES `conservation_restoration_media` WRITE;
/*!40000 ALTER TABLE `conservation_restoration_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_restoration_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_restoration_model`
--

DROP TABLE IF EXISTS `conservation_restoration_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_restoration_model` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL,
  `model_name` varchar(255) DEFAULT NULL,
  `model_type` varchar(40) DEFAULT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_data` longblob,
  `file_format` varchar(30) DEFAULT NULL,
  `scale_unit` varchar(30) DEFAULT NULL,
  `coordinate_system` varchar(100) DEFAULT NULL,
  `polygon_count` bigint DEFAULT NULL,
  `texture_count` int DEFAULT NULL,
  `model_stage` varchar(30) DEFAULT NULL,
  `model_description` text,
  `supports_layer` tinyint DEFAULT '0',
  `supports_annotation` tinyint DEFAULT '0',
  `is_primary` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `file_url` varchar(1000) DEFAULT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_restoration_model_result` (`result_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_restoration_model`
--

LOCK TABLES `conservation_restoration_model` WRITE;
/*!40000 ALTER TABLE `conservation_restoration_model` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_restoration_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_restoration_part`
--

DROP TABLE IF EXISTS `conservation_restoration_part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_restoration_part` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL,
  `part_code` varchar(60) DEFAULT NULL,
  `part_name` varchar(255) NOT NULL,
  `part_type` varchar(40) DEFAULT NULL,
  `target_location` varchar(255) DEFAULT NULL,
  `scope_description` text,
  `material_name` varchar(255) DEFAULT NULL,
  `technique` varchar(255) DEFAULT NULL,
  `evidence_level` varchar(20) DEFAULT NULL,
  `confidence_level` varchar(20) DEFAULT NULL,
  `removable` tinyint DEFAULT '0',
  `reversible` tinyint DEFAULT '0',
  `reversible_description` text,
  `distinguishable` tinyint DEFAULT '1',
  `display_style` varchar(100) DEFAULT NULL,
  `annotation_text` text,
  `percentage_value` decimal(5,2) DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `model_layer` tinyint DEFAULT '0',
  `selected_for_monitoring` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `model_object_name` varchar(255) DEFAULT NULL,
  `layer_visible` tinyint DEFAULT '1',
  `annotation_position_json` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_restoration_part_result` (`result_id`),
  KEY `idx_restoration_part_type` (`part_type`)
) ENGINE=InnoDB AUTO_INCREMENT=97012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复原成果组成部分';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_restoration_part`
--

LOCK TABLES `conservation_restoration_part` WRITE;
/*!40000 ALTER TABLE `conservation_restoration_part` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_restoration_part` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_restoration_result`
--

DROP TABLE IF EXISTS `conservation_restoration_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_restoration_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `artifact_id` int DEFAULT NULL,
  `archive_id` bigint DEFAULT NULL,
  `process_id` bigint DEFAULT NULL,
  `comparison_id` bigint DEFAULT NULL,
  `result_code` varchar(60) DEFAULT NULL,
  `result_name` varchar(255) NOT NULL,
  `restoration_type` varchar(40) DEFAULT NULL,
  `restoration_category` varchar(50) DEFAULT NULL,
  `target_part` varchar(150) DEFAULT NULL,
  `restoration_scope` text,
  `restoration_purpose` text,
  `basis_summary` text,
  `method_summary` text,
  `result_summary` text,
  `uncertainty_summary` text,
  `evidence_level` varchar(20) DEFAULT NULL,
  `confidence_level` varchar(20) DEFAULT NULL,
  `result_status` varchar(30) DEFAULT NULL,
  `completion_date` date DEFAULT NULL,
  `current_version` varchar(30) DEFAULT NULL,
  `completion_rate` decimal(5,2) DEFAULT '0.00',
  `overall_score` decimal(5,2) DEFAULT NULL,
  `evaluation_conclusion` text,
  `final_conclusion` text,
  `selected_for_archive` tinyint DEFAULT '0',
  `recommended_result` tinyint DEFAULT '0',
  `requires_monitoring` tinyint DEFAULT '0',
  `principal` varchar(100) DEFAULT NULL,
  `participant_names` varchar(500) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `monitoring_indicators` text,
  `monitoring_cycle` varchar(100) DEFAULT NULL,
  `monitoring_baseline_id` bigint DEFAULT NULL,
  `warning_conditions` text,
  `monitoring_note` text,
  `step_ids_json` json DEFAULT NULL,
  `comparison_ids_json` json DEFAULT NULL,
  `disease_ids_json` json DEFAULT NULL,
  `detection_ids_json` json DEFAULT NULL,
  `method_parameters_json` json DEFAULT NULL,
  `evaluation_json` json DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_restoration_result_code` (`result_code`),
  KEY `idx_restoration_project` (`project_id`),
  KEY `idx_restoration_status` (`result_status`),
  KEY `idx_restoration_type` (`restoration_type`)
) ENGINE=InnoDB AUTO_INCREMENT=97015 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文物复原成果监测来源';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_restoration_result`
--

LOCK TABLES `conservation_restoration_result` WRITE;
/*!40000 ALTER TABLE `conservation_restoration_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_restoration_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_restoration_source`
--

DROP TABLE IF EXISTS `conservation_restoration_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_restoration_source` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL,
  `part_id` bigint DEFAULT NULL,
  `source_type` varchar(40) DEFAULT NULL,
  `source_title` varchar(255) DEFAULT NULL,
  `business_type` varchar(50) DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  `support_description` text,
  `reliability` varchar(20) DEFAULT NULL,
  `evidence_level` varchar(20) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_url` varchar(500) DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_restoration_source_result` (`result_id`),
  KEY `idx_restoration_source_part` (`part_id`),
  KEY `idx_restoration_source_business` (`business_type`,`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=97013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_restoration_source`
--

LOCK TABLES `conservation_restoration_source` WRITE;
/*!40000 ALTER TABLE `conservation_restoration_source` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_restoration_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conservation_restoration_version`
--

DROP TABLE IF EXISTS `conservation_restoration_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conservation_restoration_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL,
  `version_no` varchar(30) DEFAULT NULL,
  `version_name` varchar(255) DEFAULT NULL,
  `version_type` varchar(30) DEFAULT NULL,
  `change_description` text,
  `evidence_level` varchar(20) DEFAULT NULL,
  `confidence_level` varchar(20) DEFAULT NULL,
  `is_current` tinyint DEFAULT '0',
  `is_recommended` tinyint DEFAULT '0',
  `archived` tinyint DEFAULT '0',
  `snapshot_json` json DEFAULT NULL,
  `creator` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_restoration_version_result` (`result_id`),
  KEY `idx_restoration_version_no` (`version_no`)
) ENGINE=InnoDB AUTO_INCREMENT=97017 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conservation_restoration_version`
--

LOCK TABLES `conservation_restoration_version` WRITE;
/*!40000 ALTER TABLE `conservation_restoration_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `conservation_restoration_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detection_analysis`
--

DROP TABLE IF EXISTS `detection_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detection_analysis` (
  `id` int NOT NULL AUTO_INCREMENT,
  `serial_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '序号',
  `artifact_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物编号',
  `artifact_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物名称',
  `excavation_relic` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出土遗迹',
  `sample_position` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分析/取样部位',
  `sample_material` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样品材质',
  `sample_status` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样品状态',
  `sample_quantity` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样品数量',
  `sample_method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取样方法',
  `purpose` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目的',
  `instrument_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '仪器名称',
  `instrument_model` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '仪器型号',
  `test_params` text COLLATE utf8mb4_unicode_ci COMMENT '测试参数',
  `storage_location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '存放位置',
  `departure_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出库时间',
  `destination` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '去处',
  `sample_photo` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取样照片',
  `analysis_data` text COLLATE utf8mb4_unicode_ci COMMENT '分析数据',
  `analysis_report` text COLLATE utf8mb4_unicode_ci COMMENT '分析报告',
  `manager` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文物管理人',
  `sampler` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取样人',
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注（归还）',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检测分析表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detection_analysis`
--

LOCK TABLES `detection_analysis` WRITE;
/*!40000 ALTER TABLE `detection_analysis` DISABLE KEYS */;
INSERT INTO `detection_analysis` VALUES (96,'1','CX:1','盖弓帽','M31/墓葬出土','器物表面','青铜镀锡','完整','1','不取样','超景深显微镜/X射线荧光光谱分析(XRF)',NULL,NULL,NULL,'马车二楼','2026/06/23','所内实验室','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/f50cc550-3832-4c5f-b658-111c542c1ac3.jpg','','','','','','2026-07-15 18:02:09','2026-07-16 09:47:20'),(98,'2','CX2','盖弓帽','M31/墓葬出土','表面','青铜镀锡','完整','1','不取样','超景深显微镜',NULL,NULL,NULL,'马车二楼','','','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/951a79c7-5f57-4941-ba26-60b64cbc76c0.jpg','','','','','','2026-07-15 18:04:44','2026-07-15 18:04:44'),(99,'3','CX:3','盖弓帽','M31/墓葬出土','表面','青铜镀锡','完整','1','不取样','超景深显微镜',NULL,NULL,NULL,'马车二楼','2026/06/23','','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/0670d352-44c1-4a7d-995c-0ed9af3bf685.jpg','','','','','','2026-07-16 09:52:16','2026-07-16 10:02:45'),(102,'5','M6：64','琉璃杯残片','M6/墓葬出土','','','','','','金相显微镜观察/超景深显微镜',NULL,NULL,NULL,'','','','','','','','','','2026-07-22 14:48:15','2026-07-22 14:48:34'),(103,'6','MD：9','坠饰','M16/墓葬出土','','','','','','金相显微镜观察/超景深显微镜',NULL,NULL,NULL,'','','','','','','','','','2026-07-22 14:53:15','2026-07-22 14:53:53'),(104,'162','M6:69','','','','','已完成','','','超景深显微镜','','','','','','','','','','','','','2026-07-23 11:22:40','2026-07-23 13:49:21'),(111,'7','18','紫色陶珠','M6','','人工硅酸盐','待检测',NULL,NULL,'超景深显微镜',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'','','','2026-07-23 12:59:48','2026-07-23 13:52:43'),(112,'8','18','紫色陶珠','M6','','人工硅酸盐','待检测',NULL,NULL,'金相显微镜观察',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'','','','2026-07-23 12:59:48','2026-07-23 12:59:48'),(113,'9','18','紫色陶珠','M6','','人工硅酸盐','已完成',NULL,NULL,'X射线荧光光谱分析(XRF)',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'','','','2026-07-23 12:59:48','2026-07-23 13:57:08');
/*!40000 ALTER TABLE `detection_analysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_compilation_draft`
--

DROP TABLE IF EXISTS `digital_compilation_draft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_compilation_draft` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `archive_id` bigint NOT NULL,
  `draft_content` longtext,
  `updated_by` varchar(100) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_digital_compilation_draft_archive` (`archive_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='档案编研草稿';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_compilation_draft`
--

LOCK TABLES `digital_compilation_draft` WRITE;
/*!40000 ALTER TABLE `digital_compilation_draft` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_compilation_draft` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_compilation_export`
--

DROP TABLE IF EXISTS `digital_compilation_export`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_compilation_export` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `archive_id` bigint NOT NULL,
  `export_name` varchar(255) DEFAULT NULL,
  `export_format` varchar(30) DEFAULT NULL,
  `file_url` varchar(1200) DEFAULT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  `export_status` varchar(30) NOT NULL DEFAULT 'completed',
  `created_by` varchar(100) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_digital_compilation_export_archive` (`archive_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='档案编研导出记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_compilation_export`
--

LOCK TABLES `digital_compilation_export` WRITE;
/*!40000 ALTER TABLE `digital_compilation_export` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_compilation_export` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_media_collection`
--

DROP TABLE IF EXISTS `digital_media_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_media_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `collection_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collection_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collection_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'custom',
  `object_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_id` bigint DEFAULT NULL,
  `object_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_part` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_resource_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `item_count` int DEFAULT '0',
  `created_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_code` (`collection_code`),
  KEY `idx_object` (`object_type`,`object_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_media_collection`
--

LOCK TABLES `digital_media_collection` WRITE;
/*!40000 ALTER TABLE `digital_media_collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_media_collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_media_collection_item`
--

DROP TABLE IF EXISTS `digital_media_collection_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_media_collection_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `collection_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `display_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `caption` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_resource` (`collection_id`,`resource_id`),
  KEY `idx_collection_id` (`collection_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_media_collection_item`
--

LOCK TABLES `digital_media_collection_item` WRITE;
/*!40000 ALTER TABLE `digital_media_collection_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_media_collection_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_media_comparison`
--

DROP TABLE IF EXISTS `digital_media_comparison`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_media_comparison` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comparison_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `comparison_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_id` bigint DEFAULT NULL,
  `object_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_part` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `before_resource_id` bigint NOT NULL,
  `after_resource_id` bigint NOT NULL,
  `before_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `after_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `comparison_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'side_by_side',
  `comparison_description` text COLLATE utf8mb4_unicode_ci,
  `result_summary` text COLLATE utf8mb4_unicode_ci,
  `deleted` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comparison_code` (`comparison_code`),
  KEY `idx_object` (`object_type`,`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_media_comparison`
--

LOCK TABLES `digital_media_comparison` WRITE;
/*!40000 ALTER TABLE `digital_media_comparison` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_media_comparison` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_media_marker`
--

DROP TABLE IF EXISTS `digital_media_marker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_media_marker` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `marker_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `marker_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `position_x` decimal(10,6) DEFAULT NULL,
  `position_y` decimal(10,6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_media_marker`
--

LOCK TABLES `digital_media_marker` WRITE;
/*!40000 ALTER TABLE `digital_media_marker` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_media_marker` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_media_metadata`
--

DROP TABLE IF EXISTS `digital_media_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_media_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `media_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_subtype` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_object_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_object_id` bigint DEFAULT NULL,
  `primary_object_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_object_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_part` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_angle` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_date` datetime DEFAULT NULL,
  `photographer` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shooting_device` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `has_scale` tinyint DEFAULT '0',
  `scale_unit` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `magnification` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_width` int DEFAULT NULL,
  `image_height` int DEFAULT NULL,
  `duration_seconds` decimal(12,3) DEFAULT NULL,
  `quality_level` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'unchecked',
  `metadata_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'incomplete',
  `is_key_media` tinyint DEFAULT '0',
  `is_comparison_candidate` tinyint DEFAULT '0',
  `sort_time` datetime DEFAULT NULL,
  `media_description` text COLLATE utf8mb4_unicode_ci,
  `created_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_id` (`resource_id`),
  KEY `idx_media_type` (`media_type`),
  KEY `idx_media_stage` (`media_stage`),
  KEY `idx_primary_object` (`primary_object_type`,`primary_object_id`),
  KEY `idx_shooting_date` (`shooting_date`),
  KEY `idx_sort_time` (`sort_time`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_media_metadata`
--

LOCK TABLES `digital_media_metadata` WRITE;
/*!40000 ALTER TABLE `digital_media_metadata` DISABLE KEYS */;
INSERT INTO `digital_media_metadata` VALUES (4,4,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 17:12:48',NULL,'2026-07-23 17:12:48'),(5,5,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 17:58:05',NULL,'2026-07-23 17:58:05'),(6,6,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 18:00:03',NULL,'2026-07-23 18:00:03'),(7,7,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 18:04:49',NULL,'2026-07-23 18:04:49'),(8,10,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 19:00:37',NULL,'2026-07-23 19:00:37'),(9,11,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 19:12:06',NULL,'2026-07-23 19:12:06'),(10,14,'image',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,'unchecked','incomplete',0,0,NULL,NULL,NULL,'2026-07-23 19:12:07',NULL,'2026-07-23 19:12:07');
/*!40000 ALTER TABLE `digital_media_metadata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_model_collection`
--

DROP TABLE IF EXISTS `digital_model_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_model_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `collection_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collection_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collection_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'custom',
  `object_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_id` bigint DEFAULT NULL,
  `object_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover_resource_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `item_count` int DEFAULT '0',
  `deleted` tinyint DEFAULT '0',
  `created_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_code` (`collection_code`),
  KEY `idx_object` (`object_type`,`object_id`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_model_collection`
--

LOCK TABLES `digital_model_collection` WRITE;
/*!40000 ALTER TABLE `digital_model_collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_model_collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_model_collection_item`
--

DROP TABLE IF EXISTS `digital_model_collection_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_model_collection_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `collection_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `display_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_resource` (`collection_id`,`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_model_collection_item`
--

LOCK TABLES `digital_model_collection_item` WRITE;
/*!40000 ALTER TABLE `digital_model_collection_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_model_collection_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_model_metadata`
--

DROP TABLE IF EXISTS `digital_model_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_model_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `model_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'scan',
  `model_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'raw',
  `model_format` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_object_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_object_id` bigint DEFAULT NULL,
  `primary_object_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `primary_object_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coordinate_system` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `scale_unit` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `real_scale` tinyint DEFAULT '0',
  `scale_factor` decimal(20,8) DEFAULT NULL,
  `vertex_count` bigint DEFAULT NULL,
  `face_count` bigint DEFAULT NULL,
  `triangle_count` bigint DEFAULT NULL,
  `point_count` bigint DEFAULT NULL,
  `material_count` int DEFAULT NULL,
  `texture_count` int DEFAULT NULL,
  `layer_count` int DEFAULT '0',
  `acquisition_method` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `acquisition_device` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `acquisition_date` datetime DEFAULT NULL,
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `processing_software` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `processing_description` text COLLATE utf8mb4_unicode_ci,
  `model_description` text COLLATE utf8mb4_unicode_ci,
  `preview_resource_id` bigint DEFAULT NULL,
  `authenticity_level` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `evidence_description` text COLLATE utf8mb4_unicode_ci,
  `uncertainty_description` text COLLATE utf8mb4_unicode_ci,
  `confidence_score` decimal(5,2) DEFAULT NULL,
  `quality_level` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'unchecked',
  `metadata_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'incomplete',
  `preview_status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `is_key_model` tinyint DEFAULT '0',
  `created_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_id` (`resource_id`),
  KEY `idx_model_type` (`model_type`),
  KEY `idx_primary_object` (`primary_object_type`,`primary_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_model_metadata`
--

LOCK TABLES `digital_model_metadata` WRITE;
/*!40000 ALTER TABLE `digital_model_metadata` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_model_metadata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_resource`
--

DROP TABLE IF EXISTS `digital_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_code` varchar(80) DEFAULT NULL,
  `resource_name` varchar(255) DEFAULT NULL,
  `original_file_name` varchar(255) DEFAULT NULL,
  `resource_type` varchar(40) DEFAULT NULL,
  `resource_subtype` varchar(80) DEFAULT NULL,
  `mime_type` varchar(120) DEFAULT NULL,
  `file_extension` varchar(30) DEFAULT NULL,
  `file_size` bigint DEFAULT '0',
  `storage_type` varchar(40) DEFAULT 'oss',
  `storage_bucket` varchar(100) DEFAULT NULL,
  `storage_path` varchar(500) DEFAULT NULL,
  `file_url` varchar(1000) DEFAULT NULL,
  `preview_url` varchar(1000) DEFAULT NULL,
  `thumbnail_url` varchar(1000) DEFAULT NULL,
  `file_hash` varchar(128) DEFAULT NULL,
  `hash_algorithm` varchar(20) DEFAULT 'SHA-256',
  `source_module` varchar(60) DEFAULT NULL,
  `source_business_type` varchar(80) DEFAULT NULL,
  `source_business_id` bigint DEFAULT NULL,
  `resource_status` varchar(40) DEFAULT 'normal',
  `preview_status` varchar(30) DEFAULT 'pending',
  `data_status` varchar(40) DEFAULT 'unchecked',
  `current_version` varchar(30) DEFAULT 'V1.0',
  `version_count` int DEFAULT '1',
  `title` varchar(255) DEFAULT NULL,
  `description` text,
  `keywords` varchar(500) DEFAULT NULL,
  `downloaded_count` bigint DEFAULT '0',
  `viewed_count` bigint DEFAULT '0',
  `creator` varchar(120) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `uploaded_by` varchar(120) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(120) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `deleted_by` varchar(100) DEFAULT NULL,
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_digital_resource_type` (`resource_type`),
  KEY `idx_digital_resource_source` (`source_module`,`source_business_type`,`source_business_id`),
  KEY `idx_digital_resource_status` (`resource_status`,`data_status`),
  KEY `idx_digital_resource_upload_time` (`upload_time`),
  KEY `idx_digital_resource_deleted` (`deleted`),
  KEY `idx_digital_resource_hash` (`file_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数字档案资源主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_resource`
--

LOCK TABLES `digital_resource` WRITE;
/*!40000 ALTER TABLE `digital_resource` DISABLE KEYS */;
INSERT INTO `digital_resource` VALUES (4,'DR-1784797951944','123','123.jpg','image',NULL,NULL,'.jpg',3333693,'oss',NULL,NULL,'','','',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'123','','',0,0,NULL,NULL,'123456','2026-07-23 17:12:48','2026-07-23 17:12:48',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(5,'DR-1784800683746','123','123.jpg','image',NULL,NULL,'.jpg',3333693,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/84f618c1-d82b-4ee9-9a7f-f76d829f9dee.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/84f618c1-d82b-4ee9-9a7f-f76d829f9dee.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/84f618c1-d82b-4ee9-9a7f-f76d829f9dee.jpg',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'123','','',0,0,NULL,NULL,'123456','2026-07-23 17:58:05','2026-07-23 17:58:05',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(6,'DR-1784800802808','download','download.pdf','image',NULL,NULL,'.pdf',246308,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/245a84b5-0772-46fc-94d0-a93b3ea97be7.pdf','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/245a84b5-0772-46fc-94d0-a93b3ea97be7.pdf','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/245a84b5-0772-46fc-94d0-a93b3ea97be7.pdf',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'download','','',0,0,NULL,NULL,'123456','2026-07-23 18:00:03','2026-07-23 18:00:03',NULL,'2026-07-23 19:45:48',1,'user','2026-07-23 19:45:48'),(7,'DR-1784801087840','123','123.jpg','image',NULL,NULL,'.jpg',3333693,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/822d4149-8c0e-4a13-a3b7-b5679e44561a.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/822d4149-8c0e-4a13-a3b7-b5679e44561a.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/822d4149-8c0e-4a13-a3b7-b5679e44561a.jpg',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'123','','',0,0,NULL,NULL,'123456','2026-07-23 18:04:49','2026-07-23 18:04:49',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(8,'DR-1784801105292','download','download.pdf','document',NULL,NULL,'.pdf',246308,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/6711e110-ad86-4040-b3c9-c08dc4df7ca8.pdf',NULL,NULL,NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'download','','',0,0,NULL,NULL,'123456','2026-07-23 18:05:06','2026-07-23 18:05:06',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(9,'DR-1784803409700','download','download.pdf','document',NULL,NULL,'.pdf',246308,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/67704a1a-fb4b-4c70-89a6-e0ed690fb659.pdf',NULL,NULL,NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'download','','',0,0,NULL,NULL,'123456','2026-07-23 18:43:31','2026-07-23 18:43:31',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(10,'DR-1784804435717','测试点位图','测试点位图.jpg','image',NULL,NULL,'.jpg',10420,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/4d4767e4-110d-4559-8859-70942b6a7343.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/4d4767e4-110d-4559-8859-70942b6a7343.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/4d4767e4-110d-4559-8859-70942b6a7343.jpg',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'测试点位图','123','金相测试点位',0,0,NULL,NULL,'123456','2026-07-23 19:00:37','2026-07-23 19:00:37',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(11,'DR-1784805123713-E263EB','123','123.jpg','image',NULL,NULL,'.jpg',3333693,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/85dd0445-7660-4b73-9a00-8100ff159f06.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/85dd0445-7660-4b73-9a00-8100ff159f06.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/85dd0445-7660-4b73-9a00-8100ff159f06.jpg',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'123','','',0,0,NULL,NULL,'123456','2026-07-23 19:12:06','2026-07-23 19:12:06',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(12,'DR-1784805126106-AA5F4F','1234','1234.txt','document',NULL,NULL,'.txt',7948,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/2e2e204f-76c8-40cc-b62a-032e8968eed9.txt',NULL,NULL,NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'1234','','',0,0,NULL,NULL,'123456','2026-07-23 19:12:06','2026-07-23 19:12:06',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(13,'DR-1784805126355-B9F94E','20260409-七棺三车分析工作进展-更新汇总版','20260409-七棺三车分析工作进展-更新汇总版.xlsx','spreadsheet',NULL,NULL,'.xlsx',31984,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/6bc5a394-b6cc-4feb-b101-6abf9863522b.xlsx',NULL,NULL,NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'20260409-七棺三车分析工作进展-更新汇总版','','',0,0,NULL,NULL,'123456','2026-07-23 19:12:06','2026-07-23 19:12:06',NULL,'2026-07-23 19:40:37',0,NULL,NULL),(14,'DR-1784805126775-85A537','637666117274685333931','637666117274685333931.jpg','image',NULL,NULL,'.jpg',539548,'oss',NULL,NULL,'http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/0b6ecc41-263d-4eed-aea0-47aae5e6a701.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/0b6ecc41-263d-4eed-aea0-47aae5e6a701.jpg','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/0b6ecc41-263d-4eed-aea0-47aae5e6a701.jpg',NULL,'SHA-256','manual',NULL,NULL,'normal','pending','incomplete','V1.0',1,'637666117274685333931','','',0,0,NULL,NULL,'123456','2026-07-23 19:12:07','2026-07-23 19:12:07',NULL,'2026-07-23 19:40:37',0,NULL,NULL);
/*!40000 ALTER TABLE `digital_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_resource_operation_log`
--

DROP TABLE IF EXISTS `digital_resource_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_resource_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint DEFAULT NULL,
  `operation_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `operation_description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operation_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_log_resource` (`resource_id`),
  KEY `idx_log_type` (`operation_type`),
  KEY `idx_log_time` (`operation_time`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_resource_operation_log`
--

LOCK TABLES `digital_resource_operation_log` WRITE;
/*!40000 ALTER TABLE `digital_resource_operation_log` DISABLE KEYS */;
INSERT INTO `digital_resource_operation_log` VALUES (1,4,'create','新增资源',NULL,'2026-07-23 17:12:48'),(2,5,'create','新增资源',NULL,'2026-07-23 17:58:05'),(3,5,'update_tags','更新标签',NULL,'2026-07-23 17:59:23'),(4,6,'create','新增资源',NULL,'2026-07-23 18:00:03'),(5,6,'delete','移入回收站',NULL,'2026-07-23 18:04:24'),(6,5,'delete','移入回收站',NULL,'2026-07-23 18:04:26'),(7,4,'delete','移入回收站',NULL,'2026-07-23 18:04:27'),(8,7,'create','新增资源',NULL,'2026-07-23 18:04:49'),(9,8,'create','新增资源',NULL,'2026-07-23 18:05:06'),(10,9,'create','新增资源',NULL,'2026-07-23 18:43:31'),(11,10,'create','新增资源',NULL,'2026-07-23 19:00:37'),(12,11,'create','新增资源',NULL,'2026-07-23 19:12:06'),(13,12,'create','新增资源',NULL,'2026-07-23 19:12:06'),(14,13,'create','新增资源',NULL,'2026-07-23 19:12:06'),(15,14,'create','新增资源',NULL,'2026-07-23 19:12:07'),(16,6,'batch_delete','批量删除资源',NULL,'2026-07-23 19:45:48');
/*!40000 ALTER TABLE `digital_resource_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_resource_relation`
--

DROP TABLE IF EXISTS `digital_resource_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_resource_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `relation_type` varchar(80) DEFAULT NULL,
  `relation_id` bigint DEFAULT NULL,
  `relation_code` varchar(120) DEFAULT NULL,
  `relation_name` varchar(255) DEFAULT NULL,
  `relation_role` varchar(80) DEFAULT NULL,
  `relation_description` text,
  `is_primary` tinyint DEFAULT '0',
  `created_by` varchar(120) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource_relation_resource` (`resource_id`),
  KEY `idx_resource_relation_business` (`relation_type`,`relation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数字资源业务关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_resource_relation`
--

LOCK TABLES `digital_resource_relation` WRITE;
/*!40000 ALTER TABLE `digital_resource_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_resource_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_resource_tag`
--

DROP TABLE IF EXISTS `digital_resource_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_resource_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(80) NOT NULL,
  `tag_type` varchar(60) DEFAULT NULL,
  `color` varchar(30) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_digital_resource_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数字资源标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_resource_tag`
--

LOCK TABLES `digital_resource_tag` WRITE;
/*!40000 ALTER TABLE `digital_resource_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_resource_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_resource_tag_relation`
--

DROP TABLE IF EXISTS `digital_resource_tag_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_resource_tag_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_tag_relation` (`resource_id`,`tag_id`),
  KEY `idx_resource_tag_relation_resource` (`resource_id`),
  KEY `idx_resource_tag_relation_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数字资源标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_resource_tag_relation`
--

LOCK TABLES `digital_resource_tag_relation` WRITE;
/*!40000 ALTER TABLE `digital_resource_tag_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `digital_resource_tag_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `digital_resource_version`
--

DROP TABLE IF EXISTS `digital_resource_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `digital_resource_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `version_no` varchar(30) NOT NULL,
  `version_name` varchar(255) DEFAULT NULL,
  `file_url` varchar(1000) DEFAULT NULL,
  `preview_url` varchar(1000) DEFAULT NULL,
  `file_size` bigint DEFAULT '0',
  `file_hash` varchar(128) DEFAULT NULL,
  `change_description` text,
  `created_by` varchar(120) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_current` tinyint DEFAULT '0',
  `version_type` varchar(30) DEFAULT NULL,
  `original_file_name` varchar(255) DEFAULT NULL,
  `file_extension` varchar(30) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `version_status` varchar(30) DEFAULT 'current',
  PRIMARY KEY (`id`),
  KEY `idx_resource_version_resource` (`resource_id`),
  KEY `idx_resource_version_current` (`resource_id`,`is_current`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数字资源版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `digital_resource_version`
--

LOCK TABLES `digital_resource_version` WRITE;
/*!40000 ALTER TABLE `digital_resource_version` DISABLE KEYS */;
INSERT INTO `digital_resource_version` VALUES (1,4,'V1.0','123','',NULL,3333693,NULL,NULL,NULL,'2026-07-23 17:12:48',0,'original','123.jpg','.jpg','image/jpeg','current'),(2,5,'V1.0','123','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/84f618c1-d82b-4ee9-9a7f-f76d829f9dee.jpg',NULL,3333693,NULL,NULL,NULL,'2026-07-23 17:58:05',0,'original','123.jpg','.jpg','image/jpeg','current'),(3,6,'V1.0','download','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/245a84b5-0772-46fc-94d0-a93b3ea97be7.pdf',NULL,246308,NULL,NULL,NULL,'2026-07-23 18:00:03',0,'original','download.pdf','.pdf','application/pdf','current'),(4,7,'V1.0','123','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/822d4149-8c0e-4a13-a3b7-b5679e44561a.jpg',NULL,3333693,NULL,NULL,NULL,'2026-07-23 18:04:49',0,'original','123.jpg','.jpg','image/jpeg','current'),(5,8,'V1.0','download','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/6711e110-ad86-4040-b3c9-c08dc4df7ca8.pdf',NULL,246308,NULL,NULL,NULL,'2026-07-23 18:05:06',0,'original','download.pdf','.pdf','application/pdf','current'),(6,9,'V1.0','download','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/67704a1a-fb4b-4c70-89a6-e0ed690fb659.pdf',NULL,246308,NULL,NULL,NULL,'2026-07-23 18:43:31',0,'original','download.pdf','.pdf','application/pdf','current'),(7,10,'V1.0','测试点位图','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/4d4767e4-110d-4559-8859-70942b6a7343.jpg',NULL,10420,NULL,NULL,NULL,'2026-07-23 19:00:37',0,'original','测试点位图.jpg','.jpg','image/jpeg','current'),(8,11,'V1.0','123','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/85dd0445-7660-4b73-9a00-8100ff159f06.jpg',NULL,3333693,NULL,NULL,NULL,'2026-07-23 19:12:06',0,'original','123.jpg','.jpg','image/jpeg','current'),(9,12,'V1.0','1234','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/2e2e204f-76c8-40cc-b62a-032e8968eed9.txt',NULL,7948,NULL,NULL,NULL,'2026-07-23 19:12:06',0,'original','1234.txt','.txt','text/plain','current'),(10,13,'V1.0','20260409-七棺三车分析工作进展-更新汇总版','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/6bc5a394-b6cc-4feb-b101-6abf9863522b.xlsx',NULL,31984,NULL,NULL,NULL,'2026-07-23 19:12:06',0,'original','20260409-七棺三车分析工作进展-更新汇总版.xlsx','.xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet','current'),(11,14,'V1.0','637666117274685333931','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/digital-resource/0b6ecc41-263d-4eed-aea0-47aae5e6a701.jpg',NULL,539548,NULL,NULL,NULL,'2026-07-23 19:12:07',0,'original','637666117274685333931.jpg','.jpg','image/jpeg','current');
/*!40000 ALTER TABLE `digital_resource_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experiment_results`
--

DROP TABLE IF EXISTS `experiment_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `experiment_results` (
  `id` int NOT NULL AUTO_INCREMENT,
  `detection_id` int NOT NULL,
  `experiment_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验名称',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT '待检测' COMMENT '待检测/检测中/待审核/已完成',
  `result_data` text COLLATE utf8mb4_unicode_ci COMMENT '实验结果数据JSON',
  `images` text COLLATE utf8mb4_unicode_ci COMMENT '图片URL列表JSON数组',
  `attachments` text COLLATE utf8mb4_unicode_ci COMMENT '附件列表JSON数组',
  `notes` text COLLATE utf8mb4_unicode_ci COMMENT '工作备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experiment_results`
--

LOCK TABLES `experiment_results` WRITE;
/*!40000 ALTER TABLE `experiment_results` DISABLE KEYS */;
INSERT INTO `experiment_results` VALUES (3,102,'金相显微镜观察','待检测',NULL,NULL,NULL,NULL,'2026-07-22 14:48:33','2026-07-22 14:48:33'),(4,102,'超景深显微镜','待检测',NULL,NULL,NULL,NULL,'2026-07-22 14:48:33','2026-07-22 14:48:33'),(7,103,'金相显微镜观察','待检测',NULL,NULL,NULL,NULL,'2026-07-22 14:53:52','2026-07-22 14:53:52'),(8,103,'超景深显微镜','待检测',NULL,NULL,NULL,NULL,'2026-07-22 14:53:52','2026-07-22 14:53:52'),(9,104,'超景深显微镜','待检测','{\"fields\":[],\"rows\":[]}','[{\"title\":\"显微照片\",\"category\":\"显微照片\",\"description\":\"\",\"url\":\"http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/a71534c9-5089-457c-aee8-d00821895155.jpg\",\"id\":1784780536897,\"uploadTime\":\"2026-07-23T04:22:16.897Z\"}]','[{\"name\":\"download.pdf\",\"type\":\"PDF\",\"description\":\"\",\"url\":\"http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/9a1db107-ec67-475d-b94a-21e8019b0e77.pdf\",\"size\":\"0.23 MB\",\"id\":1784780782607,\"uploadTime\":\"2026-07-23T04:26:22.607Z\"}]','{\"conclusion\":\"ok\"}','2026-07-23 11:22:40','2026-07-23 13:48:21'),(18,111,'超景深显微镜','待检测',NULL,NULL,NULL,NULL,'2026-07-23 12:59:48','2026-07-23 12:59:48'),(19,112,'金相显微镜观察','待检测',NULL,NULL,NULL,NULL,'2026-07-23 12:59:48','2026-07-23 12:59:48'),(20,113,'X射线荧光光谱分析(XRF)','待检测',NULL,NULL,NULL,NULL,'2026-07-23 12:59:48','2026-07-23 12:59:48');
/*!40000 ALTER TABLE `experiment_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `heritage_sites`
--

DROP TABLE IF EXISTS `heritage_sites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `heritage_sites` (
  `id` int NOT NULL AUTO_INCREMENT,
  `site_code` varchar(50) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `alias` varchar(200) DEFAULT NULL,
  `location_province` varchar(50) DEFAULT NULL,
  `location_city` varchar(50) DEFAULT NULL,
  `location_detail` varchar(500) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `era` varchar(100) DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `protection_level` varchar(50) DEFAULT NULL,
  `discovery_year` int DEFAULT NULL,
  `excavation_year` int DEFAULT NULL,
  `area_size` double DEFAULT NULL,
  `description` text,
  `cultural_value` text,
  `current_status` varchar(50) DEFAULT NULL,
  `cover_image` varchar(500) DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `heritage_sites`
--

LOCK TABLES `heritage_sites` WRITE;
/*!40000 ALTER TABLE `heritage_sites` DISABLE KEYS */;
/*!40000 ALTER TABLE `heritage_sites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lab_instruments`
--

DROP TABLE IF EXISTS `lab_instruments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_instruments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实验名字',
  `image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验图片URL',
  `scope` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验范围',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验地点',
  `model` text COLLATE utf8mb4_unicode_ci COMMENT '仪器型号',
  `conditions` text COLLATE utf8mb4_unicode_ci COMMENT '实验条件',
  `method` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验方法文档URL',
  `method_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实验方法文档名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `applicable_materials` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `research_purposes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `non_destructive` tinyint(1) NOT NULL DEFAULT '0',
  `requires_sampling` tinyint(1) NOT NULL DEFAULT '0',
  `main_outputs` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_instruments`
--

LOCK TABLES `lab_instruments` WRITE;
/*!40000 ALTER TABLE `lab_instruments` DISABLE KEYS */;
INSERT INTO `lab_instruments` VALUES (1,'超景深显微镜','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/1e68cf45-9966-405d-a3d5-88bd5589ea23.jpg','金属/非金属/有机物','甘肃省所213','实验采用的超景深显微镜设备为基恩士（日本）VHX-5000型超景深三维视频显微系统，设备配备有两个可替换镜头，分别可实现20～200、200～1000倍的放大效果。此外，设备还可切换为消除反光模式，可以在拍摄光滑金属基体表面时有效减轻其表面眩光。','实验中，根据样品的具体情况选择不同的拍摄倍数，通常使用20～200倍镜头进行样品微观形貌观察及图像采集。 ','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/f3ff8e21-93b7-491c-9065-084accd830d3.docx','f3ff8e21-93b7-491c-9065-084accd830d3.docx','2026-07-14 15:48:50','2026-07-14 15:48:50',NULL,NULL,0,0,NULL),(2,'金相显微镜观察','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/617f9fff-9ed5-49e0-9b3f-85ea7aa58cab.jpg','金属','甘肃所223','实验所采用的金相显微镜为徕卡（德国）生产的Leica DM4000M型配有LED照明的多功能立式金相显微镜，设备配置有5倍、10倍、20倍、50倍、100倍一组共计5枚物镜镜头，可实现50～1000倍的放大倍率变化。仪器可实现明场、暗场、偏光、干涉等多种不同的观察方式。','实验中，根据样品金属种类、尺寸大小的不同，选择不同的放大倍率进行观察、拍摄。所有金相照片均在金相显微镜的明场模式下采集。','','','2026-07-14 15:50:39','2026-07-14 15:50:39',NULL,NULL,0,0,NULL),(3,'X射线荧光光谱分析(XRF)','http://nwuwfy202421145.oss-cn-beijing.aliyuncs.com/ca8de371-7a0f-4619-b824-74c84a9dc95a.jpg','金属','甘肃所213','实验中共使用到了两台不同型号的X射线荧光光谱仪，均为手持式X射线荧光光谱仪（p-XRF），仪器型号分别为：由布鲁克公司（美国）生产的S1 TITAN型手持X射线荧光光谱仪（Bruker Corporation Handheld XRF Spectrometers S1 TITAN），以及由热电公司（美国）生产的Niton™ XL3t型X射线荧光光谱分析器（ThermoFisher Scientific Niton™ XL3t XRF）。具体实验采用的设备将会在实验数据处进行标注。','布鲁克公司生产的S1 TITAN型手持X射线荧光光谱仪配有Alloys 2、GeoExploration、GeoMining、Shavings and Turnings 2、Spectrometer Mode五种不同的测试模式，对于本次实验中涉及到的所有金属文物样品，均采用仪器配备的Alloys 2，也即合金模式进行测试。\n热电公司（美国）生产的Niton™ XL3t型X射线荧光光谱分析器在金属模式下，存在贵金属模式、常见金属模式等不同模式，本次实验中，对于金、银两种材质的金属样品，我们采用贵金属模式进行测试，其他材质的金属则统一在常见金属模式下检测。\n','','','2026-07-14 15:56:19','2026-07-14 15:56:19','金属',NULL,1,1,NULL);
/*!40000 ALTER TABLE `lab_instruments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_categories`
--

DROP TABLE IF EXISTS `material_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_categories` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` int DEFAULT '0' COMMENT '父级ID，0表示一级分类',
  `name` varchar(50) NOT NULL COMMENT '材质名称',
  `level` tinyint NOT NULL COMMENT '层级: 1/2/3',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='材质分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_categories`
--

LOCK TABLES `material_categories` WRITE;
/*!40000 ALTER TABLE `material_categories` DISABLE KEYS */;
INSERT INTO `material_categories` VALUES (1,0,'金属',1,1),(2,0,'人工硅酸盐',1,2),(3,0,'玉石',1,3),(4,0,'陶制',1,4),(5,0,'动物源有机质',1,5),(6,0,'植物源有机质',1,6),(7,0,'漆器',1,7),(8,1,'金',2,1),(9,1,'银',2,2),(10,1,'铜',2,3),(11,1,'铁',2,4),(12,1,'铅',2,5),(13,1,'锡',2,6),(14,1,'铁金',2,7),(15,1,'铁银',2,8),(16,1,'铁金银',2,9),(17,1,'铜金',2,10),(18,1,'铜锡',2,11),(19,2,'蜻蜓眼',2,1),(20,2,'玻璃',2,2),(21,2,'釉砂',2,3),(22,21,'铅白',3,1),(23,21,'汉蓝',3,2),(24,21,'汉紫',3,3),(25,21,'费昂斯',3,4),(26,21,'紫陶',3,5),(27,3,'绿松石',2,1),(28,3,'红玉髓',2,2),(29,3,'煤精',2,3),(30,3,'水晶',2,4),(31,4,'夹砂红陶',2,1),(32,4,'夹砂灰陶',2,2),(33,4,'泥质红陶',2,3),(34,4,'泥质灰陶',2,4),(35,5,'骨',2,1),(36,5,'贝',2,2),(37,5,'皮',2,3),(38,6,'木',2,1),(39,6,'丝',2,2),(40,6,'棉',2,3),(41,6,'麻',2,4),(42,7,'漆',2,1);
/*!40000 ALTER TABLE `material_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_alert`
--

DROP TABLE IF EXISTS `monitoring_alert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_alert` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `plan_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `record_id` bigint DEFAULT NULL,
  `target_id` bigint NOT NULL,
  `indicator_id` bigint DEFAULT NULL,
  `alert_code` varchar(60) NOT NULL,
  `alert_level` varchar(30) DEFAULT NULL,
  `alert_title` varchar(255) DEFAULT NULL,
  `alert_description` text,
  `trigger_type` varchar(50) DEFAULT NULL,
  `trigger_value` varchar(100) DEFAULT NULL,
  `threshold_description` varchar(500) DEFAULT NULL,
  `alert_status` varchar(30) DEFAULT 'new',
  `discovered_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `confirmed_time` datetime DEFAULT NULL,
  `confirmed_by` varchar(100) DEFAULT NULL,
  `immediate_action` text,
  `treatment_advice` text,
  `requires_recheck` tinyint DEFAULT '0',
  `requires_disease_survey` tinyint DEFAULT '0',
  `requires_intervention` tinyint DEFAULT '0',
  `requires_new_project` tinyint DEFAULT '0',
  `resolved_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_project_id` int DEFAULT NULL COMMENT '由预警创建的保护修复项目ID',
  `project_created_time` datetime DEFAULT NULL COMMENT '保护修复项目创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_monitoring_alert_code` (`alert_code`),
  KEY `idx_monitoring_alert_project` (`project_id`),
  KEY `idx_monitoring_alert_plan` (`plan_id`),
  KEY `idx_monitoring_alert_status` (`alert_status`)
) ENGINE=InnoDB AUTO_INCREMENT=99009 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测风险预警';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_alert`
--

LOCK TABLES `monitoring_alert` WRITE;
/*!40000 ALTER TABLE `monitoring_alert` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_baseline`
--

DROP TABLE IF EXISTS `monitoring_baseline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_baseline` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `indicator_id` bigint DEFAULT NULL,
  `source_business_type` varchar(50) DEFAULT NULL,
  `source_business_id` bigint DEFAULT NULL,
  `baseline_date` date DEFAULT NULL,
  `baseline_value` decimal(15,4) DEFAULT NULL,
  `baseline_unit` varchar(30) DEFAULT NULL,
  `baseline_status` varchar(255) DEFAULT NULL,
  `baseline_description` text,
  `baseline_media_id` bigint DEFAULT NULL,
  `version_no` varchar(30) DEFAULT 'V1.0',
  `is_current` tinyint DEFAULT '1',
  `created_by` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_monitoring_baseline_target` (`target_id`),
  KEY `idx_monitoring_baseline_current` (`target_id`,`is_current`)
) ENGINE=InnoDB AUTO_INCREMENT=99005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='版本化监测基准';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_baseline`
--

LOCK TABLES `monitoring_baseline` WRITE;
/*!40000 ALTER TABLE `monitoring_baseline` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_baseline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_indicator`
--

DROP TABLE IF EXISTS `monitoring_indicator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_indicator` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `indicator_code` varchar(60) DEFAULT NULL,
  `indicator_name` varchar(150) NOT NULL,
  `indicator_category` varchar(50) DEFAULT NULL,
  `data_type` varchar(30) DEFAULT 'number',
  `value_unit` varchar(30) DEFAULT NULL,
  `baseline_value` decimal(15,4) DEFAULT NULL,
  `normal_min` decimal(15,4) DEFAULT NULL,
  `normal_max` decimal(15,4) DEFAULT NULL,
  `warning_min` decimal(15,4) DEFAULT NULL,
  `warning_max` decimal(15,4) DEFAULT NULL,
  `critical_min` decimal(15,4) DEFAULT NULL,
  `critical_max` decimal(15,4) DEFAULT NULL,
  `change_warning_value` decimal(15,4) DEFAULT NULL,
  `change_warning_rate` decimal(10,2) DEFAULT NULL,
  `expected_direction` varchar(30) DEFAULT NULL,
  `observation_method` varchar(255) DEFAULT NULL,
  `instrument_name` varchar(255) DEFAULT NULL,
  `required_flag` tinyint DEFAULT '1',
  `sort_order` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_monitoring_indicator_target` (`target_id`),
  KEY `idx_monitoring_indicator_plan` (`plan_id`)
) ENGINE=InnoDB AUTO_INCREMENT=99004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测指标与阈值';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_indicator`
--

LOCK TABLES `monitoring_indicator` WRITE;
/*!40000 ALTER TABLE `monitoring_indicator` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_indicator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_media`
--

DROP TABLE IF EXISTS `monitoring_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `plan_id` bigint DEFAULT NULL,
  `task_id` bigint DEFAULT NULL,
  `record_id` bigint NOT NULL,
  `target_id` bigint DEFAULT NULL,
  `media_role` varchar(30) DEFAULT 'current',
  `original_name` varchar(255) NOT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_data` longblob,
  `shooting_position` varchar(150) DEFAULT NULL,
  `shooting_time` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` text,
  `created_by` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `file_url` varchar(1000) DEFAULT NULL,
  `oss_object_key` varchar(600) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_monitoring_media_record` (`record_id`),
  KEY `idx_monitoring_media_target` (`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测影像与附件（文件存入MySQL）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_media`
--

LOCK TABLES `monitoring_media` WRITE;
/*!40000 ALTER TABLE `monitoring_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_plan`
--

DROP TABLE IF EXISTS `monitoring_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `artifact_id` int DEFAULT NULL,
  `archive_id` bigint DEFAULT NULL,
  `plan_code` varchar(60) NOT NULL,
  `plan_name` varchar(255) NOT NULL,
  `plan_type` varchar(40) DEFAULT 'comprehensive',
  `plan_status` varchar(30) DEFAULT 'draft',
  `monitoring_purpose` text,
  `monitoring_scope` text,
  `overall_strategy` text,
  `responsible_person` varchar(100) DEFAULT NULL,
  `participant_names` varchar(255) DEFAULT NULL,
  `monitoring_location` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `expected_end_date` date DEFAULT NULL,
  `next_monitoring_date` date DEFAULT NULL,
  `default_frequency_value` int DEFAULT '1',
  `default_frequency_unit` varchar(20) DEFAULT 'month',
  `auto_generate_task` tinyint DEFAULT '1',
  `alert_enabled` tinyint DEFAULT '1',
  `execution_count` int DEFAULT '0',
  `overdue_count` int DEFAULT '0',
  `completion_rate` decimal(6,2) DEFAULT '0.00',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_monitoring_plan_code` (`plan_code`),
  KEY `idx_monitoring_plan_project` (`project_id`),
  KEY `idx_monitoring_plan_status` (`plan_status`)
) ENGINE=InnoDB AUTO_INCREMENT=99002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后续监测计划';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_plan`
--

LOCK TABLES `monitoring_plan` WRITE;
/*!40000 ALTER TABLE `monitoring_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_record`
--

DROP TABLE IF EXISTS `monitoring_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `project_id` int NOT NULL,
  `record_code` varchar(60) NOT NULL,
  `monitoring_date` datetime DEFAULT NULL,
  `monitor_person` varchar(100) DEFAULT NULL,
  `monitoring_location` varchar(255) DEFAULT NULL,
  `overall_status` varchar(30) DEFAULT NULL,
  `comparison_result` varchar(30) DEFAULT NULL,
  `observation_description` text,
  `change_description` text,
  `result_conclusion` text,
  `requires_recheck` tinyint DEFAULT '0',
  `requires_intervention` tinyint DEFAULT '0',
  `requires_new_disease_survey` tinyint DEFAULT '0',
  `requires_new_project` tinyint DEFAULT '0',
  `next_monitoring_date` date DEFAULT NULL,
  `monitoring_status` varchar(30) DEFAULT 'draft',
  `submitted_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_monitoring_record_code` (`record_code`),
  KEY `idx_monitoring_record_task` (`task_id`),
  KEY `idx_monitoring_record_target` (`target_id`),
  KEY `idx_monitoring_record_date` (`monitoring_date`)
) ENGINE=InnoDB AUTO_INCREMENT=99007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测执行记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_record`
--

LOCK TABLES `monitoring_record` WRITE;
/*!40000 ALTER TABLE `monitoring_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_target`
--

DROP TABLE IF EXISTS `monitoring_target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_target` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `project_id` int NOT NULL,
  `target_type` varchar(40) NOT NULL,
  `target_name` varchar(255) NOT NULL,
  `source_business_type` varchar(50) DEFAULT NULL,
  `source_business_id` bigint DEFAULT NULL,
  `target_part` varchar(150) DEFAULT NULL,
  `target_location` varchar(500) DEFAULT NULL,
  `risk_level` varchar(20) DEFAULT 'medium',
  `priority_level` varchar(20) DEFAULT 'medium',
  `monitoring_reason` text,
  `current_status` varchar(100) DEFAULT NULL,
  `requires_image` tinyint DEFAULT '0',
  `shooting_position` varchar(150) DEFAULT NULL,
  `enabled` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_monitoring_target_plan` (`plan_id`),
  KEY `idx_monitoring_target_project` (`project_id`),
  KEY `idx_monitoring_target_source` (`source_business_type`,`source_business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=99003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测对象';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_target`
--

LOCK TABLES `monitoring_target` WRITE;
/*!40000 ALTER TABLE `monitoring_target` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_target` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_task`
--

DROP TABLE IF EXISTS `monitoring_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `project_id` int NOT NULL,
  `task_code` varchar(60) NOT NULL,
  `task_name` varchar(255) NOT NULL,
  `task_type` varchar(30) DEFAULT 'routine',
  `task_status` varchar(30) DEFAULT 'pending',
  `planned_date` date DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `actual_start_time` datetime DEFAULT NULL,
  `actual_end_time` datetime DEFAULT NULL,
  `responsible_person` varchar(100) DEFAULT NULL,
  `participant_names` varchar(255) DEFAULT NULL,
  `target_count` int DEFAULT '0',
  `completed_target_count` int DEFAULT '0',
  `completion_rate` decimal(6,2) DEFAULT '0.00',
  `overall_result` varchar(30) DEFAULT NULL,
  `summary` text,
  `generated_automatically` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_monitoring_task_code` (`task_code`),
  KEY `idx_monitoring_task_plan` (`plan_id`),
  KEY `idx_monitoring_task_status` (`task_status`)
) ENGINE=InnoDB AUTO_INCREMENT=99006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测周期任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_task`
--

LOCK TABLES `monitoring_task` WRITE;
/*!40000 ALTER TABLE `monitoring_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitoring_value`
--

DROP TABLE IF EXISTS `monitoring_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitoring_value` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_id` bigint NOT NULL,
  `indicator_id` bigint NOT NULL,
  `indicator_name` varchar(150) DEFAULT NULL,
  `value_number` decimal(15,4) DEFAULT NULL,
  `value_text` text,
  `value_unit` varchar(30) DEFAULT NULL,
  `baseline_value` decimal(15,4) DEFAULT NULL,
  `previous_value` decimal(15,4) DEFAULT NULL,
  `change_value` decimal(15,4) DEFAULT NULL,
  `change_rate` decimal(10,2) DEFAULT NULL,
  `result_level` varchar(30) DEFAULT NULL,
  `result_description` varchar(500) DEFAULT NULL,
  `manually_confirmed` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_monitoring_value_record_indicator` (`record_id`,`indicator_id`),
  KEY `idx_monitoring_value_indicator` (`indicator_id`)
) ENGINE=InnoDB AUTO_INCREMENT=99008 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监测指标实测值';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitoring_value`
--

LOCK TABLES `monitoring_value` WRITE;
/*!40000 ALTER TABLE `monitoring_value` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitoring_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `relics`
--

DROP TABLE IF EXISTS `relics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `relics` (
  `id` int NOT NULL AUTO_INCREMENT,
  `relic_code` varchar(50) DEFAULT NULL,
  `site_id` int DEFAULT NULL,
  `site_name` varchar(200) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `position_within_site` varchar(200) DEFAULT NULL,
  `excavation_area` double DEFAULT NULL,
  `excavation_unit` varchar(200) DEFAULT NULL,
  `era` varchar(100) DEFAULT NULL,
  `stratigraphy` varchar(500) DEFAULT NULL,
  `structure_description` text,
  `dimensions` varchar(200) DEFAULT NULL,
  `orientation` varchar(50) DEFAULT NULL,
  `burial_depth` double DEFAULT NULL,
  `preservation_status` varchar(50) DEFAULT NULL,
  `function_purpose` varchar(200) DEFAULT NULL,
  `cultural_features` text,
  `related_relics` text,
  `images` text,
  `drawings` text,
  `notes` text,
  `created_by` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `relics`
--

LOCK TABLES `relics` WRITE;
/*!40000 ALTER TABLE `relics` DISABLE KEYS */;
/*!40000 ALTER TABLE `relics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nickname` varchar(10) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `user_pic` varchar(500) DEFAULT NULL COMMENT '鐢ㄦ埛澶村儚鍦板潃',
  `token_version` int DEFAULT '0' COMMENT 'JWT token鐗堟湰鍙凤紝淇?敼瀵嗙爜鍚?1',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'123456','$2a$10$IIXFlhSzmkW0zBj27/c4H.nNZVeaZBC8I1bINe7CbKHVpit.D/u2O','wfy','695394526@qq.com',NULL,0,'2026-07-01 17:15:23','2026-07-24 10:48:24','2026-07-24 10:48:24');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_security_log`
--

DROP TABLE IF EXISTS `user_security_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_security_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `event_type` varchar(50) NOT NULL,
  `event_detail` varchar(255) DEFAULT NULL,
  `ip_address` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_security_log_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_security_log`
--

LOCK TABLES `user_security_log` WRITE;
/*!40000 ALTER TABLE `user_security_log` DISABLE KEYS */;
INSERT INTO `user_security_log` VALUES (1,1,'login','登录成功','0:0:0:0:0:0:0:1','2026-07-23 19:35:12'),(2,1,'login','登录成功','0:0:0:0:0:0:0:1','2026-07-24 09:33:33'),(3,1,'login','登录成功','0:0:0:0:0:0:0:1','2026-07-24 10:48:24');
/*!40000 ALTER TABLE `user_security_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL,
  `user_id` int NOT NULL,
  `login_ip` varchar(64) DEFAULT NULL,
  `user_agent` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_active_time` datetime DEFAULT NULL,
  `logout_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_id` (`session_id`),
  KEY `idx_user_session_user_status` (`user_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_session`
--

LOCK TABLES `user_session` WRITE;
/*!40000 ALTER TABLE `user_session` DISABLE KEYS */;
INSERT INTO `user_session` VALUES (1,'dcf84c6f-94ea-41e8-b1c1-7d54a999f5f5',1,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0','active','2026-07-23 19:35:12','2026-07-23 19:45:48',NULL),(2,'53f98ead-69fa-41af-b021-15cf37ead385',1,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0','active','2026-07-24 09:33:33','2026-07-24 10:04:58',NULL),(3,'6bf3da87-4abd-4ce0-b590-1dcf58234b2d',1,'0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36 Edg/150.0.0.0','active','2026-07-24 10:48:24','2026-07-24 10:48:24',NULL);
/*!40000 ALTER TABLE `user_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workflow_media`
--

DROP TABLE IF EXISTS `workflow_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_media` (
  `id` int NOT NULL AUTO_INCREMENT,
  `timeline_id` int NOT NULL COMMENT '关联时间轴ID',
  `media_type` varchar(50) DEFAULT NULL COMMENT '媒体类型',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件URL',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流媒体表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workflow_media`
--

LOCK TABLES `workflow_media` WRITE;
/*!40000 ALTER TABLE `workflow_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `workflow_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workflow_note`
--

DROP TABLE IF EXISTS `workflow_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_note` (
  `id` int NOT NULL AUTO_INCREMENT,
  `timeline_id` int NOT NULL COMMENT '关联时间轴ID',
  `note_type` varchar(50) DEFAULT NULL COMMENT '备注类型',
  `content` text COMMENT '内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流备注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workflow_note`
--

LOCK TABLES `workflow_note` WRITE;
/*!40000 ALTER TABLE `workflow_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `workflow_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workflow_timeline`
--

DROP TABLE IF EXISTS `workflow_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_timeline` (
  `id` int NOT NULL AUTO_INCREMENT,
  `flow_id` int NOT NULL COMMENT '关联流程树ID',
  `event_date` date DEFAULT NULL COMMENT '日期',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `burial_id` int DEFAULT NULL COMMENT '关联墓葬ID',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: done/doing/pending',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=290 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流时间轴表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workflow_timeline`
--

LOCK TABLES `workflow_timeline` WRITE;
/*!40000 ALTER TABLE `workflow_timeline` DISABLE KEYS */;
INSERT INTO `workflow_timeline` VALUES (247,69,'2006-07-01','整理资料','2026-07-11 11:17:09',NULL,'pending'),(248,70,'2007-04-10','实地调查','2026-07-11 11:17:09',NULL,'pending'),(249,69,'2008-07-01','中期汇报','2026-07-11 11:17:09',NULL,'pending'),(250,69,'2009-01-01','完成整理','2026-07-11 11:17:09',NULL,'pending'),(251,70,'2009-06-24','踏查','2026-07-11 11:17:09',NULL,'pending'),(252,70,'2009-08-25','结束','2026-07-11 11:17:09',NULL,'pending'),(253,71,'2010-03-15','预发掘','2026-07-11 11:17:09',NULL,'pending'),(254,71,'2011-09-14','清理地表','2026-07-11 11:17:09',NULL,'pending'),(255,72,'2013-07-01','发掘','2026-07-11 11:17:09',NULL,'pending'),(256,72,'2013-08-01','清理地层②','2026-07-11 11:17:09',NULL,'pending'),(257,73,'2020-07-01','墓葬清理','2026-07-11 11:17:09',NULL,'pending'),(258,72,'2020-12-01','完成发掘','2026-07-11 11:17:09',NULL,'pending'),(259,70,'2026-07-01','遗迹记录','2026-07-11 11:17:09',NULL,'pending'),(263,90,'2021-07-01','前期资料整理','2026-07-14 11:43:11',4,'pending'),(264,90,'2026-07-13','完成资料整理','2026-07-14 11:43:11',4,'done'),(285,103,'2012-07-01','资料整理','2026-07-22 16:24:43',11,'doing'),(286,103,'2026-07-13','资料整理','2026-07-22 16:24:43',11,'doing'),(287,103,'2026-07-14','资料整理2','2026-07-22 16:24:43',11,'done'),(288,104,'2026-07-14','资料整理','2026-07-22 16:24:43',11,'doing'),(289,103,'2026-07-15','资料整理','2026-07-22 16:24:43',11,'done');
/*!40000 ALTER TABLE `workflow_timeline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `workflow_tree`
--

DROP TABLE IF EXISTS `workflow_tree`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_tree` (
  `id` int NOT NULL AUTO_INCREMENT,
  `label` varchar(100) NOT NULL COMMENT '流程名称',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `burial_id` int DEFAULT NULL COMMENT '关联墓葬ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作流树表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `workflow_tree`
--

LOCK TABLES `workflow_tree` WRITE;
/*!40000 ALTER TABLE `workflow_tree` DISABLE KEYS */;
INSERT INTO `workflow_tree` VALUES (69,'前期调查',1,'2026-07-11 10:29:54',NULL),(70,'考古勘探',2,'2026-07-11 10:29:54',NULL),(71,'发掘准备',3,'2026-07-11 10:29:54',NULL),(72,'墓葬发掘',4,'2026-07-11 10:29:54',NULL),(73,'墓葬清理',5,'2026-07-11 10:29:54',NULL),(74,'遗迹记录',6,'2026-07-11 10:29:54',NULL),(75,'随葬品提取',7,'2026-07-11 10:29:54',NULL),(76,'棺椁处理',8,'2026-07-11 10:29:54',NULL),(77,'车马器处理',9,'2026-07-11 10:29:54',NULL),(78,'整体提取',10,'2026-07-11 10:29:54',NULL),(79,'现场清理',11,'2026-07-11 10:29:54',NULL),(90,'前期调查',1,'2026-07-13 14:48:18',4),(91,'考古勘探',2,'2026-07-13 14:48:18',4),(92,'发掘准备',3,'2026-07-13 14:48:18',4),(93,'墓葬发掘',4,'2026-07-13 14:48:18',4),(94,'墓葬清理',5,'2026-07-13 14:48:18',4),(95,'遗迹记录',6,'2026-07-13 14:48:18',4),(96,'随葬品提取',7,'2026-07-13 14:48:18',4),(97,'棺椁处理',8,'2026-07-13 14:48:18',4),(98,'车处理',9,'2026-07-13 14:48:18',4),(99,'整体提取',10,'2026-07-13 14:48:18',4),(100,'现场工作结束',11,'2026-07-13 14:48:18',4),(103,'资料整理',1,'2026-07-22 16:06:13',11),(104,'田野调查',2,'2026-07-22 16:06:13',11);
/*!40000 ALTER TABLE `workflow_tree` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'nwsite'
--

--
-- Dumping routines for database 'nwsite'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-24 14:31:25
