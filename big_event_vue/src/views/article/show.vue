<template>
  <div>
    <div class="buttons">
      <button @click="switchMap('甘肃省')">甘肃省</button>
      <button @click="switchMap('陕西省')">陕西省</button>
      <button @click="switchMap('新疆省')">新疆省</button>
      <button @click="switchMap('青海省')">青海省</button>
      <button @click="switchMap('宁夏省')">宁夏省</button>
    </div>

    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let myChart = null
let currentData = []  // 当前显示的示例值

// 初始化地图数据
const mapFiles = {
  '甘肃省': '/gansu.json',
  '陕西省': '/shanxi1.json',
  '新疆省': '/xinjiang.json',
  '青海省': '/qinghai.json',
  '宁夏省': '/ningxia.json'
}

const exampleValues = {
  Gansu: [
    { name: '兰州市', value: 2189 },
    { name: '嘉峪关市', value: 2487 },
    { name: '金昌市', value: 12684 }
    // ...其他市
  ],
  Shaanxi: [
    { name: '西安市', value: 5000 },
    { name: '咸阳市', value: 3200 }
    // ...其他市
  ],
  Northwest: [
    { name: '甘肃', value: 2502 },
    { name: '陕西', value: 3954 },
    { name: '宁夏', value: 1200 },
    { name: '青海', value: 800 },
    { name: '新疆', value: 3000 }
  ]
}

onMounted(() => {
  myChart = echarts.init(chartRef.value)
  loadMap('甘肃省')  // 默认显示甘肃

  window.addEventListener('resize', resizeChart)
})

const resizeChart = () => {
  myChart && myChart.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  myChart && myChart.dispose()
})

// 切换地图函数
const switchMap = (mapName) => {
  loadMap(mapName)
}

// 加载地图
const loadMap = (mapName) => {
  myChart.showLoading()
  fetch(mapFiles[mapName])
    .then(res => res.json())
    .then(mapJson => {
      myChart.hideLoading()
      echarts.registerMap(mapName, mapJson)
      currentData = exampleValues[mapName] || []

      const option = {
        title: {
          text: `${mapName}示例数据`,
          left: 'center'
        },
        tooltip: { trigger: 'item' },
        visualMap: {
          min: 0,
          max: 15000,
          left: 'left',
          bottom: '10%',
          text: ['高', '低'],
          calculable: true
        },
        series: [
          {
            name: '示例值',
            type: 'map',
            map: mapName,
            roam: true,
            layoutCenter: ['50%', '50%'],
            layoutSize: '60%',
            emphasis: { label: { show: true } },
            data: currentData
          }
        ]
      }
      myChart.setOption(option)
    })
}
</script>

<style scoped>
.chart {
  width: 80%;
  height: 400px;
  margin: 0 auto;
}
.buttons {
  text-align: center;
  margin-bottom: 10px;
}
.buttons button {
  margin: 0 10px;
  padding: 5px 15px;
  cursor: pointer;
}
</style>