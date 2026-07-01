# Artifacts添加数据接口使用说明

## 接口说明
本接口用于向系统中添加新的遗物数据，支持遗物的所有字段信息录入。

## 请求信息
- **请求URL**: `http://localhost:8081/artifacts`
- **请求方法**: POST
- **请求头**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (需要有效的登录令牌)

## 请求体示例
```json
{
  "artifactCode": "ART-2025-001",
  "siteId": 1,
  "siteName": "西安秦始皇兵马俑",
  "relicId": 1,
  "relicName": "兵马俑一号坑",
  "name": "青铜剑",
  "category": "武器",
  "subCategory": "剑",
  "material": "青铜",
  "era": "秦朝",
  "size": "长91.3厘米，宽3.1厘米",
  "weight": 1.2,
  "color": "青绿色",
  "texture": "光滑",
  "decoration": "无",
  "inscription": "相邦吕不韦造",
  "productionTechnique": "铸造",
  "usageFunction": "作战武器",
  "discoveryContext": "1974年出土于秦始皇兵马俑一号坑",
  "preservationCondition": "良好",
  "restorationInfo": "未修复",
  "currentLocation": "秦始皇兵马俑博物馆",
  "museumNumber": "M00123",
  "images": "[{\"url\":\"http://example.com/artifact1.jpg\",\"description\":\"青铜剑正面\"},{\"url\":\"http://example.com/artifact2.jpg\",\"description\":\"青铜剑侧面\"}]",
  "model3dUrl": "http://example.com/artifact3d.glb",
  "researchNotes": "此剑保存完好，剑身上的铬盐氧化层技术比西方早了近两千年",
  "bibliography": "《秦始皇兵马俑发掘报告》",
  "culturalValue": "具有极高的历史、艺术和科学价值，是中国古代青铜铸造技术的杰出代表"
}
```

## 响应示例
```json
{
  "code": 1,
  "msg": "success",
  "data": null
}
```

## 使用示例
### 使用curl命令发送请求
```bash
curl -X POST "http://localhost:8081/artifacts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {你的登录令牌}" \
  -d '{
    "artifactCode": "ART-2025-001",
    "siteId": 1,
    "siteName": "西安秦始皇兵马俑",
    "relicId": 1,
    "relicName": "兵马俑一号坑",
    "name": "青铜剑",
    "category": "武器",
    "subCategory": "剑",
    "material": "青铜",
    "era": "秦朝",
    "size": "长91.3厘米，宽3.1厘米",
    "weight": 1.2,
    "color": "青绿色",
    "texture": "光滑",
    "decoration": "无",
    "inscription": "相邦吕不韦造",
    "productionTechnique": "铸造",
    "usageFunction": "作战武器",
    "discoveryContext": "1974年出土于秦始皇兵马俑一号坑",
    "preservationCondition": "良好",
    "restorationInfo": "未修复",
    "currentLocation": "秦始皇兵马俑博物馆",
    "museumNumber": "M00123",
    "images": "[{\"url\":\"http://example.com/artifact1.jpg\",\"description\":\"青铜剑正面\"},{\"url\":\"http://example.com/artifact2.jpg\",\"description\":\"青铜剑侧面\"}]",
    "model3dUrl": "http://example.com/artifact3d.glb",
    "researchNotes": "此剑保存完好，剑身上的铬盐氧化层技术比西方早了近两千年",
    "bibliography": "《秦始皇兵马俑发掘报告》",
    "culturalValue": "具有极高的历史、艺术和科学价值，是中国古代青铜铸造技术的杰出代表"
  }'
```

### 使用Postman发送请求
1. 打开Postman，创建一个新的请求
2. 设置请求方法为POST
3. 输入请求URL: `http://localhost:8081/artifacts`
4. 在Headers选项卡中添加:
   - Key: `Content-Type`, Value: `application/json`
   - Key: `Authorization`, Value: `Bearer {你的登录令牌}`
5. 在Body选项卡中选择raw -> JSON格式
6. 粘贴上述请求体JSON数据
7. 点击Send按钮发送请求

## 注意事项
1. 接口需要有效的登录令牌才能访问，请先登录获取令牌
2. 以下字段为必填项:
   - artifactCode (遗物编号)
   - name (遗物名称)
   - category (遗物类别)
   - material (材质)
   - era (时代)
3. 其他字段为选填项，可以根据实际情况填写
4. 系统会自动填充以下字段:
   - id (遗物ID，自动生成)
   - createdBy (创建人ID，从登录令牌中获取)
   - verificationStatus (审核状态，默认值为"pending")
   - createTime (创建时间，默认值为当前时间)
   - updateTime (更新时间，默认值为当前时间)
5. images字段需要是一个包含url和description属性的JSON数组字符串格式

## 错误处理
如果请求失败，系统会返回相应的错误信息，例如:

```json
{
  "code": 0,
  "msg": "遗物编号不能为空",
  "data": null
}
```

请根据错误信息调整请求参数后重新发送请求。