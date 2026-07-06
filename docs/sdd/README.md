# SDD — Spec-Driven Development

本项目采用 **SDD（Spec-Driven Development，规格驱动开发）** 方式组织后续所有功能开发。

## 核心理念

> **没有 Spec，不允许写代码。**
> **没有 Design，不允许建表和写接口。**
> **没有 Acceptance，不允许说完成。**

## 完整开发流程

```
1. Scope      —— 明确阶段边界
2. Spec       —— 写清楚需求规格
3. Design     —— 设计数据库、接口、状态流转、页面结构
4. Tasks      —— 拆分开发任务
5. Impl       —— 按任务实现代码
6. Acceptance —— 按验收用例测试
7. Review     —— 输出验收报告
8. Commit     —— 通过后再提交代码
```

## 关键规则

1. 每个新阶段必须先在 `docs/sdd/` 下创建独立的 spec 文档。
2. 每个功能在 spec 中必须明确「做什么」和「不做什么」。
3. 没有 Design 文档，不允许建数据库表和写接口代码。
4. 没有验收测试通过，不允许标记阶段为「已完成」。
5. 不允许随意重构已稳定的一阶段和二阶段主链路。

## 目录说明

| 文件 | 说明 |
|---|---|
| `00-project-scope.md` | 项目总边界与阶段划分 |
| `01-domain-model.md` | 核心领域模型与关系 |
| `02-development-process.md` | 后续开发流程规范 |
| `03-spec-template.md` | 需求规格模板 |
| `04-design-template.md` | 设计文档模板 |
| `05-task-template.md` | 任务拆分模板 |
| `06-acceptance-template.md` | 验收报告模板 |
| `phase-1-core-ledger-spec.md` | 第一阶段规格说明 |
| `phase-2-lifecycle-flow-spec.md` | 第二阶段规格说明 |
| `phase-3-approval-flow-draft.md` | 第三阶段审批流草案 |
