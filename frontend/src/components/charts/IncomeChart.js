import React from 'react';
import { Pie } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Empty } from 'antd';

ChartJS.register(ArcElement, Tooltip, Legend);

const IncomeChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="No income data available" />;
  }

  // Generate colors for income categories
  const colors = [
    '#82E0AA', '#AED6F1', '#F7DC6F', '#DDA0DD', '#98D8C8',
    '#FFEAA7', '#96CEB4', '#45B7D1', '#4ECDC4', '#FF6B6B'
  ];

  const chartData = {
    labels: data.map(item => item.categoryName),
    datasets: [
      {
        data: data.map(item => Math.abs(item.totalAmount)),
        backgroundColor: colors.slice(0, data.length),
        borderColor: colors.slice(0, data.length).map(color => color + '80'),
        borderWidth: 2,
        hoverOffset: 4
      }
    ]
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          padding: 20,
          usePointStyle: true,
          font: {
            size: 12
          }
        }
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const value = context.parsed;
            const total = context.dataset.data.reduce((a, b) => a + b, 0);
            const percentage = ((value / total) * 100).toFixed(1);
            return `${context.label}: $${value.toFixed(2)} (${percentage}%)`;
          }
        }
      }
    }
  };

  return (
    <div style={{ height: '300px', position: 'relative' }}>
      <Pie data={chartData} options={options} />
    </div>
  );
};

export default IncomeChart;
