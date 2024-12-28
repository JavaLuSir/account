/*
Navicat MySQL Data Transfer

Source Server         : 81.70.76.218
Source Server Version : 50731
Source Host           : 81.70.76.218:3306
Source Database       : account

Target Server Type    : MYSQL
Target Server Version : 50731
File Encoding         : 65001

Date: 2021-10-19 09:27:22
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `T_HOLIDAY`
-- ----------------------------
DROP TABLE IF EXISTS `T_HOLIDAY`;
CREATE TABLE `T_HOLIDAY` (
  `ID` int(6) NOT NULL AUTO_INCREMENT,
  `BREAKDAY` varchar(255) DEFAULT NULL COMMENT '节假日表',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of T_HOLIDAY
-- ----------------------------
INSERT INTO `T_HOLIDAY` VALUES ('1', '2021-10-01');
