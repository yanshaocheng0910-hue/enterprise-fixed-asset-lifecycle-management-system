<template>
  <div>
    <PageHeader title="AI 智能分析" description="利用人工智能技术辅助资产管理，提升资产分类、故障预测和盘点效率。" />
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#173B57"><Camera /></el-icon>
        </div>
        <h3 class="ai-card-title">AI 资产照片自动分类</h3>
        <p class="ai-card-desc">上传资产照片后，系统自动识别资产类别，例如电脑、打印机、空调、服务器等。支持批量识别与导入。</p>
        <el-button disabled size="small" style="margin-top:12px;">上传照片识别</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#D97706"><Warning /></el-icon>
        </div>
        <h3 class="ai-card-title">AI 设备故障风险预测</h3>
        <p class="ai-card-desc">根据资产使用年限、维修次数、资产类别及同类型资产故障率，预测设备在未来的故障风险等级。</p>
        <el-button disabled size="small" style="margin-top:12px;">查看风险列表</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
      <div class="ai-card">
        <div class="ai-card-icon">
          <el-icon :size="36" color="#4F8F7B"><MapLocation /></el-icon>
        </div>
        <h3 class="ai-card-title">AI 最优盘点路线推荐</h3>
        <p class="ai-card-desc">根据楼栋、楼层、房间和资产数量，智能生成最优盘点路线，减少盘点人员跑动距离，提升盘点效率。</p>
        <el-button disabled size="small" style="margin-top:12px;">生成盘点路线</el-button>
        <p class="ai-card-status">功能开发中，暂不可用</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Camera, Warning, MapLocation } from '@element-plus/icons-vue'
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
