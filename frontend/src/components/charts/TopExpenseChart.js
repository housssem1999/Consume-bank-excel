import React from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Empty } from 'antd';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const TopExpenseChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="No expense data available" />;
  }

  // Generate colors for categories - using expense-themed colors
  const colors = [
    '#FF6B6B', '#FF8E8E', '#FFB3B3', '#FFC2C2', '#FFD1D1'
  ];

  const chartData = {
    labels: data.map(item => item.categoryName),
    datasets: [
      {
        label: 'Total Spent',
        data: data.map(item => Math.abs(item.totalAmount)),
        backgroundColor: colors.slice(0, data.length),
        borderColor: colors.slice(0, data.length).map(color => color + '80'),
        borderWidth: 1,
      }
    ]
  };

  const options = {
    indexAxis: 'y', // This makes it horizontal
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false, // Hide legend since we only have one dataset
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const value = context.parsed.x;
            return `Total Spent: $${value.toFixed(2)}`;
          }
        }
      },
      datalabels: {
        anchor: 'end',
        align: 'right',
        formatter: function(value) {
          return '$' + value.toFixed(0);
        },
        font: {
          weight: 'bold'
        }
      }
    },
    scales: {
      x: {
        beginAtZero: true,
        ticks: {
          callback: function(value) {
            return '$' + value.toFixed(0);
          }
        },
        title: {
          display: true,
          text: 'Amount ($)'
        }
      },
      y: {
        title: {
          display: true,
          text: 'Categories'
        }
      }
    },
    layout: {
      padding: {
        right: 80 // Add padding for value labels
      }
    }
  };

  return (
    <div style={{ height: '400px', position: 'relative' }}>
      <Bar data={chartData} options={options} />
    </div>
  );
};

export default TopExpenseChart;