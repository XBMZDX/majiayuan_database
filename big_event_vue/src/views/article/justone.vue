<template>
  <div ref="chartRef" class="chart"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let myChart = null

onMounted(() => {
  myChart = echarts.init(chartRef.value)

  myChart.showLoading()

  fetch('/gansu.json')
    .then(res => res.json())
    .then(GansuJson => {
      myChart.hideLoading()

      echarts.registerMap('Gansu', GansuJson)

      const option = {
        title: {
          text: '甘肃省市级示例数据',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
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
            map: 'Gansu',
            roam: true,
            layoutCenter: ['50%', '50%'],  // 居中
            layoutSize: '60%',             // 缩小到容器的60%
            emphasis: {
              label: { show: true }
            },
            data: [
              { name: '兰州市', value: 2189 },
              { name: '嘉峪关市', value: 2487 },
              { name: '金昌市', value: 12684 },
              { name: '白银市', value: 2502 },
              { name: '天水市', value: 3954 },
              { name: '酒泉市', value: 4256 },
              { name: '张掖市', value: 4567 },
              { name: '平凉市', value: 5678 },
              { name: '庆阳市', value: 6789 },
              { name: '定西市', value: 7890 },
              { name: '陇南市', value: 8901 },
              { name: '临夏回族自治州', value: 9012 },
              { name: '甘南藏族自治州', value: 10123 },
              { name: '武威市', value: 5650 }
            ]
          }
        ]
      }

      myChart.setOption(option)
    })

  // 自适应
  window.addEventListener('resize', resizeChart)
})

const resizeChart = () => {
  myChart && myChart.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  myChart && myChart.dispose()
})
</script>

<style scoped>
.chart {
  width: 100%;   /* 容器宽度改小 */
  height: 600px; /* 容器高度改小 */

}
</style>