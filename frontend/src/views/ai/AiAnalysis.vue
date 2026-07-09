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
        <el-button :loading="summaryLoading" size="small" style="margin-top:12px;" @click="loadSummary">生成状态摘要</el-button>
        <p v-if="summaryNote" class="ai-card-note">辅助参考</p>
        <div v-if="summaryData" style="margin-top:12px;text-align:left;">
          <div style="padding:8px 12px;background:#f5f7fa;border-radius:4px;margin-bottom:8px;font-size:13px;color:#606266;">
            合计 {{ summaryData.totalCount }} 项，原值 ¥{{ summaryData.totalOriginalValue }}，净值 ¥{{ summaryData.totalNetValue }}
          </div>
          <el-table :data="summaryData.statusDistribution" size="small" stripe>
            <el-table-column prop="statusLabel" label="状态" />
            <el-table-column prop="count" label="数量" width="80" />
            <el-table-column prop="totalValue" label="净值合计" width="140">
              <template #default="{ row }">¥{{ row.totalValue }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#D97706"><Warning /></el-icon>
        </div>
        <h3 class="ai-card-title">异常资产提示</h3>
        <p class="ai-card-desc">根据资产使用年限、维修频次、报废记录和操作日志，识别长期闲置、超期未维保、状态异常的资产，生成异常资产提示与管理建议。</p>
        <el-button :loading="alertsLoading" size="small" style="margin-top:12px;" @click="loadAlerts">查看异常列表</el-button>
        <p v-if="alertsNote" class="ai-card-note">辅助参考</p>
        <div v-if="alertsData" style="margin-top:12px;text-align:left;">
          <div v-if="alertsData.idleAlerts?.length" style="margin-bottom:8px;">
            <p style="font-weight:600;font-size:13px;margin-bottom:4px;">长期闲置（{{ alertsData.idleAlerts.length }}）</p>
            <el-table :data="alertsData.idleAlerts" size="small" stripe>
              <el-table-column prop="assetName" label="资产名称" />
              <el-table-column prop="alertReason" label="原因" min-width="160">
                <template #default="{ row }"><span style="color:#E6A23C;">{{ row.alertReason }}</span></template>
              </el-table-column>
            </el-table>
          </div>
          <div v-if="alertsData.frequentRepairAlerts?.length" style="margin-bottom:8px;">
            <p style="font-weight:600;font-size:13px;margin-bottom:4px;">频繁维修（{{ alertsData.frequentRepairAlerts.length }}）</p>
            <el-table :data="alertsData.frequentRepairAlerts" size="small" stripe>
              <el-table-column prop="assetName" label="资产名称" />
              <el-table-column prop="alertReason" label="原因" min-width="160">
                <template #default="{ row }">
                  <span :style="{ color: row.severity === 'high' ? '#F56C6C' : '#E6A23C' }">{{ row.alertReason }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-if="alertsData.abnormalStatusAlerts?.length">
            <p style="font-weight:600;font-size:13px;margin-bottom:4px;">盘点异常（{{ alertsData.abnormalStatusAlerts.length }}）</p>
            <el-table :data="alertsData.abnormalStatusAlerts" size="small" stripe>
              <el-table-column prop="assetName" label="资产名称" />
              <el-table-column prop="alertReason" label="原因" min-width="160">
                <template #default="{ row }"><span style="color:#F56C6C;">{{ row.alertReason }}</span></template>
              </el-table-column>
            </el-table>
          </div>
          <div v-if="!alertsData.idleAlerts?.length && !alertsData.frequentRepairAlerts?.length && !alertsData.abnormalStatusAlerts?.length" style="color:#909399;font-size:13px;text-align:center;padding:12px 0;">
            暂无异常资产
          </div>
        </div>
      </div>

      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#4F8F7B"><Tools /></el-icon>
        </div>
        <h3 class="ai-card-title">维修/报废辅助建议</h3>
        <p class="ai-card-desc">基于维修记录和报废记录，分析资产维修频次与费用趋势，对高频维修或接近报废年限的资产提供维修或报废的辅助建议。</p>
        <el-button :loading="suggestionsLoading" size="small" style="margin-top:12px;" @click="loadSuggestions">查看建议列表</el-button>
        <p v-if="suggestionsNote" class="ai-card-note">辅助参考</p>
        <div v-if="suggestionsData" style="margin-top:12px;text-align:left;">
          <div v-if="suggestionsData.repairSuggestions?.length" style="margin-bottom:8px;">
            <p style="font-weight:600;font-size:13px;margin-bottom:4px;">建议维修（{{ suggestionsData.repairSuggestions.length }}）</p>
            <el-table :data="suggestionsData.repairSuggestions" size="small" stripe>
              <el-table-column prop="assetName" label="资产名称" />
              <el-table-column prop="repairCount" label="维修次数" width="80" />
              <el-table-column prop="suggestion" label="建议" min-width="140" />
            </el-table>
          </div>
          <div v-if="suggestionsData.scrapSuggestions?.length">
            <p style="font-weight:600;font-size:13px;margin-bottom:4px;">建议报废（{{ suggestionsData.scrapSuggestions.length }}）</p>
            <el-table :data="suggestionsData.scrapSuggestions" size="small" stripe>
              <el-table-column prop="assetName" label="资产名称" />
              <el-table-column prop="netValue" label="净值" width="100">
                <template #default="{ row }">¥{{ row.netValue }}</template>
              </el-table-column>
              <el-table-column prop="suggestion" label="建议" min-width="140" />
            </el-table>
          </div>
          <div v-if="!suggestionsData.repairSuggestions?.length && !suggestionsData.scrapSuggestions?.length" style="color:#909399;font-size:13px;text-align:center;padding:12px 0;">
            暂无维修/报废建议
          </div>
        </div>
      </div>

    </div>

    <!-- 管理报告辅助生成（重做） -->
    <div class="report-section">
      <div class="report-header">
        <div class="report-header-left">
          <h3 class="report-title">
            <el-icon :size="22" color="#1F4E79"><Document /></el-icon>
            AI 智能分析报告
          </h3>
          <div v-if="reportData" class="report-meta">
            <el-tag v-if="reportData.analysisMode === 'DEEPSEEK'" type="success" size="small" effect="light">DeepSeek 大模型分析</el-tag>
            <el-tag v-else type="warning" size="small" effect="light">规则引擎分析</el-tag>
            <span v-if="reportData.model" class="report-meta-item">模型：{{ reportData.model }}</span>
            <span v-if="reportData.generatedAt" class="report-meta-item">生成时间：{{ reportData.generatedAt }}</span>
          </div>
        </div>
        <div v-if="reportData" class="report-header-actions">
          <el-button type="primary" :loading="reportLoading" @click="loadReport">
            <el-icon><Refresh /></el-icon>重新生成报告
          </el-button>
          <el-button type="success" :loading="exporting" @click="handleExport">
            <el-icon><Download /></el-icon>导出报告
          </el-button>
        </div>
      </div>

      <el-alert
        v-if="reportData && reportData.analysisMode === 'DEEPSEEK'"
        type="info"
        show-icon
        :closable="false"
        title="本报告由 DeepSeek 大模型基于系统数据生成，仅供辅助决策"
        style="margin-top:16px;"
      />
      <el-alert
        v-else-if="reportData && reportData.analysisMode === 'RULE_FALLBACK'"
        type="warning"
        show-icon
        :closable="false"
        :title="reportData.fallbackReason || '当前未配置 DeepSeek API Key 或模型调用失败，系统已切换为规则引擎分析模式'"
        style="margin-top:16px;"
      />

      <!-- 空状态 / 首次加载 -->
      <div v-if="!reportData" v-loading="reportLoading" class="report-empty">
        <el-icon :size="48" color="#C0C4CC"><Document /></el-icon>
        <p class="report-empty-text">点击下方按钮生成 AI 智能分析报告</p>
        <el-button type="primary" :loading="reportLoading" @click="loadReport">
          <el-icon><Document /></el-icon>生成报告
        </el-button>
      </div>

      <!-- 报告内容 -->
      <div v-if="reportData" v-loading="reportLoading" class="report-content">
        <div v-if="reportData.summary" class="report-block">
          <div class="report-block-head">
            <el-icon :size="18" color="#1F4E79"><DataAnalysis /></el-icon>
            <span class="report-block-title">资产总体状态摘要</span>
          </div>
          <div class="report-block-body">{{ reportData.summary }}</div>
        </div>

        <div v-if="reportData.keyRisks" class="report-block">
          <div class="report-block-head">
            <el-icon :size="18" color="#D97706"><Warning /></el-icon>
            <span class="report-block-title">主要风险分析</span>
          </div>
          <div class="report-block-body">{{ reportData.keyRisks }}</div>
        </div>

        <div v-if="reportData.financialInsight" class="report-block">
          <div class="report-block-head">
            <el-icon :size="18" color="#4F8F7B"><Coin /></el-icon>
            <span class="report-block-title">财务与折旧分析</span>
          </div>
          <div class="report-block-body">{{ reportData.financialInsight }}</div>
        </div>

        <div v-if="reportData.operationInsight" class="report-block">
          <div class="report-block-head">
            <el-icon :size="18" color="#409EFF"><Operation /></el-icon>
            <span class="report-block-title">运营管理建议</span>
          </div>
          <div class="report-block-body">{{ reportData.operationInsight }}</div>
        </div>

        <div v-if="reportData.auditFocus" class="report-block">
          <div class="report-block-head">
            <el-icon :size="18" color="#6366F1"><View /></el-icon>
            <span class="report-block-title">审计关注点</span>
          </div>
          <div class="report-block-body">{{ reportData.auditFocus }}</div>
        </div>

        <div v-if="reportData.recommendations && reportData.recommendations.length" class="report-block">
          <div class="report-block-head">
            <el-icon :size="18" color="#1F4E79"><List /></el-icon>
            <span class="report-block-title">下一步处置建议</span>
          </div>
          <div class="report-block-body">
            <ol class="report-recommendations">
              <li v-for="(item, idx) in reportData.recommendations" :key="idx">{{ item }}</li>
            </ol>
          </div>
        </div>

        <div v-if="reportData.conclusion" class="report-block report-block-full">
          <div class="report-block-head">
            <el-icon :size="18" color="#67C23A"><Finished /></el-icon>
            <span class="report-block-title">管理结论</span>
          </div>
          <div class="report-block-body">{{ reportData.conclusion }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DataAnalysis, Warning, Tools, Document, Download, Refresh, Coin, Operation, View, List, Finished } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getAiSummary, getAiAlerts, getAiSuggestions, getAiReport } from '@/api/ai'
import type { SummaryData, AlertsData, SuggestionsData, ReportData } from '@/api/ai'
import { exportAiReport } from '@/api/export'
import { ElMessage } from 'element-plus'

const summaryLoading = ref(false)
const summaryData = ref<SummaryData | null>(null)
const summaryNote = ref(false)

const alertsLoading = ref(false)
const alertsData = ref<AlertsData | null>(null)
const alertsNote = ref(false)

const suggestionsLoading = ref(false)
const suggestionsData = ref<SuggestionsData | null>(null)
const suggestionsNote = ref(false)

const reportLoading = ref(false)
const reportData = ref<ReportData | null>(null)
const exporting = ref(false)

async function loadSummary() {
  summaryLoading.value = true
  summaryNote.value = false
  try {
    const res = await getAiSummary()
    summaryData.value = res.data
    summaryNote.value = true
  } catch {
    ElMessage.error('获取资产状态摘要失败')
  } finally {
    summaryLoading.value = false
  }
}

async function loadAlerts() {
  alertsLoading.value = true
  alertsNote.value = false
  try {
    const res = await getAiAlerts()
    alertsData.value = res.data
    alertsNote.value = true
  } catch {
    ElMessage.error('获取异常资产提示失败')
  } finally {
    alertsLoading.value = false
  }
}

async function loadSuggestions() {
  suggestionsLoading.value = true
  suggestionsNote.value = false
  try {
    const res = await getAiSuggestions()
    suggestionsData.value = res.data
    suggestionsNote.value = true
  } catch {
    ElMessage.error('获取维修/报废建议失败')
  } finally {
    suggestionsLoading.value = false
  }
}

async function loadReport() {
  reportLoading.value = true
  try {
    const res = await getAiReport()
    reportData.value = res.data
  } catch {
    ElMessage.error('生成管理报告失败')
  } finally {
    reportLoading.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    await exportAiReport()
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.ai-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 24px;
  text-align: center;
}
.ai-card-icon { margin-bottom: 12px; }
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
.ai-card-note {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 8px;
}

/* 报告区域 */
.report-section {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 24px;
  margin-top: 16px;
}
.report-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}
.report-header-left {
  flex: 1;
  min-width: 0;
}
.report-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1F4E79;
  margin: 0;
}
.report-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.report-meta-item {
  display: inline-flex;
  align-items: center;
}
.report-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.report-empty {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
}
.report-empty-text {
  margin: 12px 0 16px;
  font-size: 14px;
}
.report-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
  min-height: 80px;
}
.report-block {
  background: #f7f9fb;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 16px;
}
.report-block-full {
  grid-column: 1 / -1;
}
.report-block-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--color-border);
}
.report-block-title {
  font-size: 14px;
  font-weight: 600;
  color: #1F4E79;
}
.report-block-body {
  font-size: 13px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}
.report-recommendations {
  margin: 0;
  padding-left: 20px;
}
.report-recommendations li {
  margin-bottom: 6px;
  line-height: 1.7;
}
</style>
