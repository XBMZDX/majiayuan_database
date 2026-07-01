# Relics模块新增功能测试说明

## 1. JSON请求示例

已在`RelicsAddExample.json`文件中提供了完整的新增遗迹请求示例。

## 2. 测试工具使用

### 2.1 使用ApiFox/Postman测试

1. **新建请求**：
   - 请求方法：`POST`
   - 请求URL：`http://localhost:8081/relics`

2. **配置请求头**：
   - `Content-Type: application/json`

3. **配置请求体**：
   - 选择`raw`格式
   - 粘贴`RelicsAddExample.json`中的内容

4. **发送请求**：
   - 点击发送按钮
   - 预期响应：`{"code": 1, "msg": "success", "data": null}`

### 2.2 使用curl命令测试

```bash
curl -X POST http://localhost:8081/relics -H "Content-Type: application/json" -d '{
  "relicCode": "RLC20230001",
  "siteId": 1,
  "siteName": "秦始皇陵",
  "name": "兵马俑一号坑",
  "type": "兵马俑坑",
  "positionWithinSite": "陵园东侧1.5公里",
  "excavationArea": "14260平方米",
  "excavationUnit": "陕西省考古研究院",
  "era": "秦代",
  "stratigraphy": "深约5米，分为四层",
  "structureDescription": "地下土木结构，东西长230米，南北宽62米",
  "dimensions": "长230m，宽62m，深5m",
  "orientation": "坐西向东",
  "burialDepth": 5.0,
  "preservationStatus": "良好",
  "functionPurpose": "秦始皇陪葬坑",
  "culturalFeatures": "陶俑、陶马、青铜兵器等",
  "relatedRelics": "二号坑、三号坑",
  "images": "http://localhost:8081/images/relics/t1.jpg",
  "drawings": "[{\"type\":\"平面图\",\"url\":\"http://localhost:8081/drawings/relics/t1-plan.jpg\"},{\"type\":\"剖面图\",\"url\":\"http://localhost:8081/drawings/relics/t1-section.jpg\"}]",
  "notes": "世界八大奇迹之一"
}'
```

## 3. 字段说明

| 字段名 | 类型 | 说明 | 是否必填 |
|-------|------|------|--------|
| relicCode | String | 遗迹编号 | 是 |
| siteId | Integer | 所属遗址ID | 是 |
| siteName | String | 遗址名称 | 是 |
| name | String | 遗迹名称 | 是 |
| type | String | 遗迹类型 | 否 |
| positionWithinSite | String | 在遗址中的位置 | 否 |
| excavationArea | String | 发掘区域 | 否 |
| excavationUnit | String | 发掘单位 | 否 |
| era | String | 所属时代 | 否 |
| stratigraphy | String | 地层关系 | 否 |
| structureDescription | String | 结构描述 | 否 |
| dimensions | String | 尺寸 | 否 |
| orientation | String | 方向 | 否 |
| burialDepth | Double | 埋藏深度 | 否 |
| preservationStatus | String | 保存状况 | 否 |
| functionPurpose | String | 功能用途 | 否 |
| culturalFeatures | String | 文化特征 | 否 |
| relatedRelics | String | 相关遗迹 | 否 |
| images | String | 遗迹图片URL（JSON数组） | 否 |
| drawings | String | 线图、剖面图等（JSON数组） | 否 |
| notes | String | 备注 | 否 |

**注意**：
- 创建时间、更新时间和创建人ID会由系统自动生成，无需在请求中提供
- images和drawings字段需要是有效的JSON格式字符串

## 4. 常见问题排查

1. **请求失败，返回401未授权**：
   - 请确保已经登录系统，获取到有效的JWT令牌
   - 在请求头中添加`Authorization: Bearer {token}`

2. **请求失败，返回400参数错误**：
   - 检查必填字段是否都已提供
   - 检查字段类型是否正确（如burialDepth必须是数字类型）
   - 检查drawings字段是否为有效的JSON格式字符串

3. **请求失败，返回500服务器错误**：
   - 检查数据库连接是否正常
   - 检查relics表结构是否与实体类一致
   - 查看服务器日志获取详细错误信息

## 5. 扩展建议

1. 可以根据实际业务需求调整必填字段
2. 可以添加数据验证注解，如`@NotNull`、`@NotEmpty`等
3. 可以添加文件上传功能，支持直接上传遗迹图片
4. 可以添加数据关联查询，如通过siteId查询遗址详细信息