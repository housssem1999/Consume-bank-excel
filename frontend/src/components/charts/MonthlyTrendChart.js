import React from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Empty } from 'antd';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const MonthlyTrendChart = ({ data, forecast }) => {
  if (!data || data.length === 0) {
    return <Empty description="No trend data available" />;
  }

  const sortedData = [...data].sort((a, b) => {
    if (a.year !== b.year) return a.year - b.year;
    return a.month - b.month;
  });

  // Create labels including forecast month if available
  const labels = sortedData.map(item => `${item.monthName} ${item.year}`);
  if (forecast) {
    labels.push(`${forecast.monthName} ${forecast.year}`);
  }
  
  // Helper function to create forecast datasets
  const createForecastData = (historicalData, forecastValue) => {
    const result = [...historicalData];
    if (forecast) {
      result.push(forecastValue);
    }
    return result;
  };
  
  const chartData = {
    labels,
    datasets: [
      {
        label: 'Income',
        data: createForecastData(
          sortedData.map(item => item.income || 0),
          forecast ? forecast.projectedIncome || 0 : null
        ),
        borderColor: '#52c41a',
        backgroundColor: 'rgba(82, 196, 26, 0.1)',
        tension: 0.4,
        fill: false,
      },
      {
        label: 'Expenses',
        data: createForecastData(
          sortedData.map(item => Math.abs(item.expenses || 0)),
          forecast ? Math.abs(forecast.projectedExpenses || 0) : null
        ),
        borderColor: '#ff4d4f',
        backgroundColor: 'rgba(255, 77, 79, 0.1)',
        tension: 0.4,
        fill: false,
      },
      {
        label: 'Net Income',
        data: createForecastData(
          sortedData.map(item => item.netAmount || 0),
          forecast ? forecast.projectedNetAmount || 0 : null
        ),
        borderColor: '#1890ff',
        backgroundColor: 'rgba(24, 144, 255, 0.1)',
        tension: 0.4,
        fill: false,
      }
    ]
  };

  // Add forecast datasets if forecast data is available
  if (forecast) {
    // Income forecast line (dotted)
    chartData.datasets.push({
      label: 'Income Forecast',
      data: [...Array(sortedData.length).fill(null), forecast.projectedIncome || 0],
      borderColor: '#52c41a',
      backgroundColor: 'rgba(82, 196, 26, 0.1)',
      tension: 0.4,
      fill: false,
      borderDash: [5, 5], // Dotted line
      pointRadius: 6,
      pointBackgroundColor: '#52c41a',
      pointBorderColor: '#ffffff',
      pointBorderWidth: 2,
    });

    // Expenses forecast line (dotted)
    chartData.datasets.push({
      label: 'Expenses Forecast',
      data: [...Array(sortedData.length).fill(null), Math.abs(forecast.projectedExpenses || 0)],
      borderColor: '#ff4d4f',
      backgroundColor: 'rgba(255, 77, 79, 0.1)',
      tension: 0.4,
      fill: false,
      borderDash: [5, 5], // Dotted line
      pointRadius: 6,
      pointBackgroundColor: '#ff4d4f',
      pointBorderColor: '#ffffff',
      pointBorderWidth: 2,
    });

    // Net Income forecast line (dotted)
    chartData.datasets.push({
      label: 'Net Income Forecast',
      data: [...Array(sortedData.length).fill(null), forecast.projectedNetAmount || 0],
      borderColor: '#1890ff',
      backgroundColor: 'rgba(24, 144, 255, 0.1)',
      tension: 0.4,
      fill: false,
      borderDash: [5, 5], // Dotted line
      pointRadius: 6,
      pointBackgroundColor: '#1890ff',
      pointBorderColor: '#ffffff',
      pointBorderWidth: 2,
    });

    // Confidence bounds for Net Income (shaded area)
    if (forecast.confidenceLowerBound !== undefined && forecast.confidenceUpperBound !== undefined) {
      chartData.datasets.push({
        label: 'Confidence Range',
        data: [...Array(sortedData.length).fill(null), forecast.confidenceUpperBound],
        borderColor: 'rgba(24, 144, 255, 0.2)',
        backgroundColor: 'rgba(24, 144, 255, 0.1)',
        tension: 0.4,
        fill: '+1', // Fill to next dataset
        pointRadius: 0,
        borderWidth: 1,
        borderDash: [2, 2],
      });

      chartData.datasets.push({
        label: 'Confidence Lower',
        data: [...Array(sortedData.length).fill(null), forecast.confidenceLowerBound],
        borderColor: 'rgba(24, 144, 255, 0.2)',
        backgroundColor: 'rgba(24, 144, 255, 0.1)',
        tension: 0.4,
        fill: false,
        pointRadius: 0,
        borderWidth: 1,
        borderDash: [2, 2],
      });
    }
  }

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: 'index',
      intersect: false,
    },
    plugins: {
      legend: {
        position: 'top',
        labels: {
          filter: function(legendItem, chartData) {
            // Hide the confidence lower bound from legend
            return legendItem.text !== 'Confidence Lower';
          }
        }
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const value = context.parsed.y;
            const label = context.dataset.label;
            
            // Format forecast labels specially
            if (label.includes('Forecast')) {
              return `${label}: $${value.toFixed(2)} (projected)`;
            } else if (label === 'Confidence Range') {
              return `Confidence Upper: $${value.toFixed(2)}`;
            } else if (label === 'Confidence Range Lower') {
              return `Confidence Lower: $${value.toFixed(2)}`;
            }
            
            return `${label}: $${value.toFixed(2)}`;
          },
          filter: function(tooltipItem) {
            // Hide the confidence lower bound from tooltip
            return tooltipItem.dataset.label !== 'Confidence Lower';
          }
        }
      }
    },
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: 'Month'
        }
      },
      y: {
        display: true,
        title: {
          display: true,
          text: 'Amount ($)'
        },
        ticks: {
          callback: function(value) {
            return '$' + value.toFixed(0);
          }
        }
      }
    }
  };

  return (
    <div style={{ height: '400px', position: 'relative' }}>
      <Line data={chartData} options={options} />
    </div>
  );
};

export default MonthlyTrendChart;
