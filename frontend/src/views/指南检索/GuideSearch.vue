<template>
  <div class="guide-search">
    <div class="search-header">
      <el-input v-model="searchQuery" placeholder="搜索临床指南..." prefix-icon="Search" style="width: 400px;" @keyup.enter="handleSearch" />
      <el-select v-model="guideTab" placeholder="分类">
        <el-option label="全部" value="all" />
        <el-option label="心血管" value="cardio" />
        <el-option label="内分泌" value="endo" />
        <el-option label="呼吸" value="resp" />
        <el-option label="感染" value="infec" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <div class="quick-tags">
      <span class="tag-label">快捷标签：</span>
      <el-tag v-for="tag in quickTags" :key="tag" size="small" @click="searchQuery = tag">{{ tag }}</el-tag>
    </div>

    <div class="main-content">
      <div class="guide-list">
        <el-card v-for="guide in filteredGuides" :key="guide.id" class="guide-card" @click="selectGuide(guide)">
          <div class="guide-title">{{ guide.title }}</div>
          <div class="guide-meta">
            <span>{{ guide.source }}</span>
            <span>{{ guide.publishDate }}</span>
            <el-tag size="small" :type="getMatchType(guide.matchScore)">{{ guide.matchScore }}%匹配</el-tag>
          </div>
        </el-card>
      </div>

      <div class="guide-detail" v-if="selectedGuide">
        <el-card>
          <template #header>
            <div class="guide-detail-header">
              <h3>{{ selectedGuide.title }}</h3>
              <el-button type="text" @click="selectedGuide = null">关闭</el-button>
            </div>
          </template>
          <div class="guide-detail-content">
            <div class="detail-info">
              <p><strong>来源：</strong>{{ selectedGuide.source }}</p>
              <p><strong>发布时间：</strong>{{ selectedGuide.publishDate }}</p>
              <p><strong>匹配度：</strong><el-tag :type="getMatchType(selectedGuide.matchScore)">{{ selectedGuide.matchScore }}%</el-tag></p>
            </div>
            <div class="detail-section">
              <h4>指南摘要</h4>
              <p>本指南基于最新的临床研究证据，为{{ selectedGuide.type }}疾病的诊断与治疗提供标准化建议...</p>
            </div>
            <div class="detail-section">
              <h4>推荐意见</h4>
              <ul>
                <li>推荐使用XX药物进行初始治疗</li>
                <li>建议定期监测相关指标</li>
                <li>高危患者应加强随访管理</li>
              </ul>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const searchQuery = ref('')
const guideTab = ref('all')
const selectedGuide = ref(null)

const quickTags = ['高血压', '糖尿病', '冠心病', '发热', '咳嗽', '胸痛']

const guideList = [
  { id: 'guide001', title: '中国急性冠状动脉综合征诊断与治疗指南', source: '中华医学会', publishDate: '2023年', matchScore: 92, type: '心血管' },
  { id: 'guide002', title: '高血压防治指南', source: '国家心血管病中心', publishDate: '2022年', matchScore: 78, type: '心血管' },
  { id: 'guide003', title: '冠心病患者长期管理专家共识', source: '中华心血管病杂志', publishDate: '2023年', matchScore: 85, type: '心血管' },
  { id: 'guide004', title: '2型糖尿病防治指南', source: '中华医学会内分泌分会', publishDate: '2022年', matchScore: 65, type: '内分泌' },
  { id: 'guide005', title: '慢性阻塞性肺疾病诊治指南', source: '中华医学会呼吸分会', publishDate: '2021年', matchScore: 58, type: '呼吸' },
  { id: 'guide006', title: '发热待查诊断与治疗指南', source: '中华医学会感染分会', publishDate: '2023年', matchScore: 72, type: '感染' }
]

const filteredGuides = computed(() => {
  let result = guideList
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(g => g.title.toLowerCase().includes(query) || g.type.includes(searchQuery.value))
  }
  return result
})

const handleSearch = () => {
  selectedGuide.value = null
}

const selectGuide = (guide) => {
  selectedGuide.value = guide
}

const getMatchType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'info'
}
</script>

<style scoped>
.guide-search {
  padding: 20px;
}

.search-header {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 20px;
}

.quick-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.tag-label {
  color: #909399;
  font-size: 14px;
}

.main-content {
  display: flex;
  gap: 20px;
}

.guide-list {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.guide-card {
  cursor: pointer;
  transition: all 0.2s;
}

.guide-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.guide-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.guide-meta {
  display: flex;
  gap: 15px;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.guide-detail {
  width: 400px;
}

.guide-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.guide-detail-content {
  padding-top: 15px;
}

.detail-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #e4e7ed;
}

.detail-info p {
  margin: 8px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  font-size: 15px;
  margin-bottom: 10px;
  color: #606266;
}

.detail-section p {
  line-height: 1.8;
  color: #606266;
}

.detail-section ul {
  padding-left: 20px;
}

.detail-section li {
  margin-bottom: 8px;
  color: #606266;
}
</style>