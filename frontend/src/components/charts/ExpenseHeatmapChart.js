import React from 'react';
import { Chart as ChartJS, LinearScale, CategoryScale, Tooltip, Legend } from 'chart.js';
import { MatrixController, MatrixElement } from 'chartjs-chart-matrix';
import { Chart } from 'react-chartjs-2';
import { Empty } from 'antd';

ChartJS.register(LinearScale, CategoryScale, Tooltip, Legend, MatrixController, MatrixElement);

const ExpenseHeatmapChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="No expense heatmap data available" />;
  }

  // Days of week in order
  const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  
  // Get unique categories from data
  const categories = [...new Set(data.map(item => item.category))].sort();
  
  // Create a map for faster lookup
  const dataMap = new Map();
  data.forEach(item => {
    const key = `${item.category}-${item.dayOfWeek}`;
    dataMap.set(key, parseFloat(item.amount));
  });
  
  // Prepare data for matrix chart
  const chartData = [];
  let maxValue = 0;
  
  categories.forEach((category, categoryIndex) => {
    daysOfWeek.forEach((day, dayIndex) => {
      const key = `${category}-${day}`;
      const value = dataMap.get(key) || 0;
      maxValue = Math.max(maxValue, value);
      
      chartData.push({
        x: dayIndex,
        y: categoryIndex, 
        v: value
      });
    });
  });

  const chartConfig = {
    type: 'matrix',
    data: {
      datasets: [{
        label: 'Expenses',
        data: chartData,
        backgroundColor: function(ctx) {
          const value = ctx.parsed.v;
          const alpha = maxValue > 0 ? (value / maxValue) : 0;
          return `rgba(255, 107, 107, ${alpha})`;
        },
        borderColor: '#fff',
        borderWidth: 1,
        width: ({chart}) => (chart.chartArea || {}).width / daysOfWeek.length,
        height: ({chart}) => (chart.chartArea || {}).height / categories.length,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          type: 'linear',
          position: 'top',
          min: -0.5,
          max: daysOfWeek.length - 0.5,
          ticks: {
            callback: function(value) {
              return daysOfWeek[value] || '';
            },
            stepSize: 1
          },
          grid: {
            display: false
          }
        },
        y: {
          type: 'linear',
          min: -0.5,
          max: categories.length - 0.5,
          ticks: {
            callback: function(value) {
              return categories[value] || '';
            },
            stepSize: 1
          },
          grid: {
            display: false
          }
        }
      },
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          callbacks: {
            title: function() {
              return '';
            },
            label: function(context) {
              const categoryIndex = Math.round(context.parsed.y);
              const dayIndex = Math.round(context.parsed.x);
              const category = categories[categoryIndex];
              const day = daysOfWeek[dayIndex];
              const amount = context.parsed.v;
              return `${category} - ${day}: $${amount.toFixed(2)}`;
            }
          }
        }
      }
    }
  };

  return (
    <div style={{ height: '400px', position: 'relative' }}>
      <Chart {...chartConfig} />
    </div>
  );
};

export default ExpenseHeatmapChart;