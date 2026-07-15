# 马家塬考古数据库管理系统

考古遗址出土文物的数字化管理系统，支持遗址、遗迹、墓葬、棺/车、文物的录入检索，以及墓葬与棺的工作流程管理、文物检测分析等功能。

## 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 前端框架 | Vue 3 + Vite | 3.5 / 7.2 |
| UI 组件库 | Element Plus | 2.12 |
| 状态管理 | Pinia | 3.0 |
| 图表 | ECharts | 6.0 |
| 后端框架 | Spring Boot | 3.5.7 |
| ORM | MyBatis | 3.0.4 |
| 数据库 | MySQL | 8.0 |
| 对象存储 | 阿里云 OSS | — |
| 认证 | JWT (Auth0) | 4.0 |
| Excel | Apache POI | 5.2.4 |

## 项目结构

```
├── big_event_vue/              # 前端项目 (Vue 3 + Vite)
│   └── src/
│       ├── api/                # API 接口模块
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia 状态管理
│       ├── utils/              # 工具函数（axios 实例等）
│       └── views/
│           ├── article/        # 核心业务页面
│           │   ├── TombManage.vue      # 墓葬总览
│           │   ├── TombWorkflow.vue    # 墓葬工作流程
│           │   ├── TombExcavation.vue  # 墓葬出土文物
│           │   ├── CoffinBasic.vue     # 棺工作流程
│           │   ├── CoffinArtifacts.vue # 棺出土文物
│           │   ├── ArtifactsManage.vue # 文物总览
│           │   ├── DetectionAnalysis.vue # 文物检测分析
│           │   └── ...                 # 其他页面
│           └── user/           # 个人中心
│
├── bigevent_nwu/               # 后端项目 (Spring Boot)
│   └── src/main/java/com/itheima/bigevent/
│       ├── controller/         # 控制器（含内联 Mapper）
│       ├── mapper/             # MyBatis Mapper 接口
│       ├── pojo/               # 实体类
│       ├── service/            # 服务层
│       └── utils/              # 工具类（OSS 上传等）
│
├── init.sql                    # 数据库初始化脚本（17 张表）
└── README.md
```

## 功能模块

- **遗址管理** — 遗址信息录入、检索、批量导入
- **遗迹管理** — 遗迹信息录入、与遗址关联
- **墓葬管理** — 墓葬信息 CRUD、棺/车配置
- **工作流程** — 流程树 + 时间轴 + 完整档案（工作记录、图片、附件、文件预览）
- **出土文物** — 文物录入、Excel 批量导入导出、按墓葬/棺/车分类筛选
- **检测实验** — 仪器库管理（卡片总览、照片/文档上传）
- **检测分析** — 文物取样、检测数据、分析报告管理
- **分析结果** — 联动总览自动生成结果卡片，支持编辑检测目的/仪器/参数
- **分类字典** — 材质分类、完整度分类
- **用户认证** — JWT 登录、权限拦截

## 快速开始

### 环境要求

- JDK 21+
- Node.js 20+
- MySQL 8.0+
- Maven 3.6+

### 1. 初始化数据库

```bash
mysql -u root -p < init.sql
```

### 2. 配置阿里云 OSS（文件上传）

设置系统环境变量：

```
OSS_ACCESS_KEY_ID=你的AccessKey ID
OSS_ACCESS_KEY_SECRET=你的AccessKey Secret
```

### 3. 启动后端

```bash
cd bigevent_nwu
mvn spring-boot:run
# 或通过 IDE 运行 BigeventApplication.java
```

后端运行在 `http://localhost:8081`

### 4. 启动前端

```bash
cd big_event_vue
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，API 请求自动代理到后端。

### 5. 访问系统

浏览器打开 `http://localhost:5173`，注册账号后登录使用。

## 数据库表

| 模块 | 表名 | 说明 |
|------|------|------|
| 系统 | `user` | 用户表 |
| 遗址 | `heritage_sites` | 遗址表 |
| 遗迹 | `relics` | 遗迹表 |
| 墓葬 | `burial` | 墓葬信息表 |
| 棺 | `coffin` | 棺信息表 |
| 文物 | `artifacts` | 出土文物表 |
| 分类 | `material_categories` | 材质分类 |
| 分类 | `completeness_categories` | 完整度分类 |
| 检测 | `detection_analysis` | 检测分析表 |
| 工作流 | `workflow_tree/timeline/note/media` | 墓葬工作流 4 表 |
| 工作流 | `coffin_workflow_tree/timeline/note/media` | 棺工作流 4 表 |
| 工作流 | `chariot_workflow_tree/timeline/note/media` | 车工作流 4 表 |
| 仪器 | `lab_instruments` | 检测仪器表 |
| 分析 | `analysis_results` | 分析结果表 |
| 实验 | `experiment_results` | 实验结果表 |

## 配置说明

- **数据库连接**：`bigevent_nwu/src/main/resources/application.yml`
- **后端端口**：8081
- **上传文件**：通过阿里云 OSS 存储，Bucket 为 `nwuwfy202421145`，Region 为 `cn-beijing`
