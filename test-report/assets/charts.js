/* assets/charts.js — All ECharts visualizations for the test report */
(function () {
  var style = getComputedStyle(document.documentElement);
  var accent = style.getPropertyValue('--accent').trim();
  var accent2 = style.getPropertyValue('--accent2').trim();
  var ink = style.getPropertyValue('--ink').trim();
  var muted = style.getPropertyValue('--muted').trim();
  var rule = style.getPropertyValue('--rule').trim();
  var bg2 = style.getPropertyValue('--bg2').trim();

  // --- Chart 1: Test Results by Module (Bar) ---
  var chart1 = echarts.init(document.getElementById('chart-test-results'), null, { renderer: 'svg' });
  chart1.setOption({
    animation: false,
    tooltip: { trigger: 'axis', appendToBody: true },
    grid: { left: 60, right: 30, top: 20, bottom: 60 },
    xAxis: { type: 'category', data: ['登录认证', '驾驶舱', '资产台账', '生命周期', '审批流', '异常处理', '骨架页面'], axisLabel: { color: muted, fontSize: 11, rotate: 0 }, axisLine: { lineStyle: { color: rule } } },
    yAxis: { type: 'value', max: 20, axisLabel: { color: muted }, splitLine: { lineStyle: { color: rule } } },
    series: [{ type: 'bar', data: [6, 4, 16, 17, 18, 5, 3], itemStyle: { color: accent, borderRadius: [4, 4, 0, 0] }, label: { show: true, position: 'top', color: ink, fontSize: 12, fontWeight: 600 } }]
  });
  window.addEventListener('resize', function () { chart1.resize(); });

  // --- Chart 2: Test Pass/Fail Summary (Pie) ---
  var chart2 = echarts.init(document.getElementById('chart-pass-fail'), null, { renderer: 'svg' });
  chart2.setOption({
    animation: false,
    tooltip: { trigger: 'item', appendToBody: true, formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'],
      data: [
        { value: 74, name: '通过', itemStyle: { color: accent } },
        { value: 0, name: '失败', itemStyle: { color: '#e74c3c' } }
      ],
      label: { color: ink, fontSize: 13, formatter: '{b}: {c}' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' } }
    }]
  });
  window.addEventListener('resize', function () { chart2.resize(); });

  // --- Chart 3: Gantt-like Project Phase Timeline ---
  var chart3 = echarts.init(document.getElementById('chart-gantt'), null, { renderer: 'svg' });
  var phases = [
    '第一阶段：基础功能',
    '第二阶段：生命周期',
    '第三阶段·3.1：审批骨架',
    '第三阶段·3.2：审批服务',
    '第三阶段·3.3：审批流转',
    '3.3 收口修复',
    '全量回归测试'
  ];
  var start = [0, 1, 2, 3, 3.5, 4.5, 5];
  var dur =   [1, 1, 1, 0.5, 1, 0.5, 1];
  var colors = ['#173B57', '#2d6a9f', '#4096ff', '#69b1ff', '#91caff', '#b7d9ff', accent];
  var seriesData = phases.map(function (n, i) {
    return { name: n, value: [i, start[i], start[i] + dur[i]], itemStyle: { color: colors[i % colors.length], borderRadius: [3, 3, 3, 3] } };
  });
  chart3.setOption({
    animation: false,
    tooltip: { trigger: 'item', appendToBody: true, formatter: function (p) { return p.name + '<br/>' + p.value[1].toFixed(1) + ' → ' + p.value[2].toFixed(1) + ' (周)'; } },
    grid: { left: 160, right: 30, top: 10, bottom: 10 },
    xAxis: { type: 'value', min: 0, max: 6.5, axisLabel: { color: muted, formatter: '{value}周' }, splitLine: { lineStyle: { color: rule, type: 'dashed' } } },
    yAxis: { type: 'category', data: phases, axisLabel: { color: ink, fontSize: 11 }, axisLine: { show: false }, axisTick: { show: false } },
    series: [{
      type: 'bar', data: seriesData, barWidth: 18,
      label: { show: true, position: 'right', color: muted, fontSize: 10, formatter: function (p) { return p.value[1].toFixed(1) + 'w'; } },
      backgroundStyle: { color: bg2 }
    }]
  });
  window.addEventListener('resize', function () { chart3.resize(); });

  // --- Chart 4: Asset Status Distribution (Pie) ---
  var chart4 = echarts.init(document.getElementById('chart-asset-status'), null, { renderer: 'svg' });
  chart4.setOption({
    animation: false,
    tooltip: { trigger: 'item', appendToBody: true },
    series: [{
      type: 'pie', radius: ['30%', '60%'],
      data: [
        { value: 11, name: '使用中 (IN_USE)', itemStyle: { color: accent } },
        { value: 4, name: '闲置 (IDLE)', itemStyle: { color: accent2 } },
        { value: 3, name: '维修中 (REPAIRING)', itemStyle: { color: '#fa8c16' } },
        { value: 2, name: '已报废 (SCRAPPED)', itemStyle: { color: '#ff4d4f' } }
      ],
      label: { color: ink, fontSize: 12, formatter: '{b}\n{d}%' }
    }]
  });
  window.addEventListener('resize', function () { chart4.resize(); });
  
  // --- Chart 5: Approval Business Type Coverage (Bar) ---
  var chart5 = echarts.init(document.getElementById('chart-approval-coverage'), null, { renderer: 'svg' });
  chart5.setOption({
    animation: false,
    tooltip: { trigger: 'axis', appendToBody: true },
    grid: { left: 80, right: 30, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: ['领用\n(RECEIVE)', '调拨\n(TRANSFER)', '维修\n(REPAIR)', '报废\n(SCRAP)', '入库\n(INBOUND.跳过)'], axisLabel: { color: muted, fontSize: 11 }, axisLine: { lineStyle: { color: rule } } },
    yAxis: { type: 'value', max: 15, axisLabel: { color: muted }, splitLine: { lineStyle: { color: rule } } },
    series: [
      { name: '测试用例数', type: 'bar', data: [13, 5, 4, 5, 4], itemStyle: { color: accent, borderRadius: [4, 4, 0, 0] } },
      { name: '审批节点数', type: 'bar', data: [1, 1, 1, 2, 0], itemStyle: { color: accent2, borderRadius: [4, 4, 0, 0] } }
    ]
  });
  window.addEventListener('resize', function () { chart5.resize(); });
})();
