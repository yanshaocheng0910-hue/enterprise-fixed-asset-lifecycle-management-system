<template>
  <div>
    <PageHeader title="AI 辅助分析" description="基于资产台账、生命周期单据和操作日志生成管理建议，辅助资产决策。" />
    <el-alert
      title="AI 辅助分析说明"
      type="info"
      show-icon
      :closable="false"
      description="本模块基于系统内部资产数据、生命周期单据、维修记录、报废记录和操作日志进行分析，生成资产状态摘要、异常资产提示、维修/报废辅助建议和管理报告辅助内容。AI 输出仅作为辅助参考，不直接修改资产状态和业务单据。"
      style="margin-bottom: 16px;"
    />
    <div style="display:grid;grid-template-columns:repeat(2,1fr);gap:16px;">
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#173B57"><DataAnalysis /></el-icon>
        </div>
        <h3 class="ai-card-title">资产状态摘要</h3>
        <p class="ai-card-desc">基于资产台账数据，汇总各状态（闲置、使用中、维修中、待报废、已报废）的资产数量与价值分布，生成资产整体状态摘要。</p>
        <el-button disabled size="small" style="margin-top:12px;">生成状态摘要</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#D97706"><Warning /></el-icon>
        </div>
        <h3 class="ai-card-title">异常资产提示</h3>
        <p class="ai-card-desc">根据资产使用年限、维修频次、报废记录和操作日志，识别长期闲置、超期未维保、状态异常的资产，生成异常资产提示与管理建议。</p>
        <el-button disabled size="small" style="margin-top:12px;">查看异常列表</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#4F8F7B"><Tools /></el-icon>
        </div>
        <h3 class="ai-card-title">维修/报废辅助建议</h3>
        <p class="ai-card-desc">基于维修记录和报废记录，分析资产维修频次与费用趋势，对高频维修或接近报废年限的资产提供维修或报废的辅助建议。</p>
        <el-button disabled size="small" style="margin-top:12px;">查看建议列表</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#6366F1"><Document /></el-icon>
        </div>
        <h3 class="ai-card-title">管理报告辅助生成</h3>
        <p class="ai-card-desc">基于系统内部数据，辅助生成资产管理报告内容，包括资产总体情况、异常资产分析、维修/报废建议等，供管理人员参考。</p>
        <el-button disabled size="small" style="margin-top:12px;">生成报告</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { DataAnalysis, Warning, Tools, Document } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
</script>

<style scoped>
.ai-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 24px;
  text-align: center;
}
.ai-card-icon {
  margin-bottom: 12px;
}
.ai-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}
.ai-card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
.ai-card-status {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 8px;
}
</style>
