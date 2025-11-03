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

const SavingsRateChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="No savings rate data available" />;
  }

  const sortedData = [...data].sort((a, b) => {
    // Sort by year first, then by month number
    if (a.year !== b.year) return a.year - b.year;
    return (a.monthNum || a.month) - (b.monthNum || b.month);
  });

  const labels = sortedData.map(item => {
    // Support both old format (monthName + year) and new format
    if (item.monthName && item.year) {
      return `${item.monthName} ${item.year}`;
    }
    // Fallback: parse from month string like "2025-01"
    if (item.month && typeof item.month === 'string' && item.month.includes('-')) {
      const [year, monthNum] = item.month.split('-');
      const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      return `${monthNames[parseInt(monthNum) - 1]} ${year}`;
    }
    return item.month || 'Unknown';
  });
  
  // Calculate savings rate and determine colors
  const savingsRateData = sortedData.map(item => {
    if (!item.income || item.income === 0) return 0;
    return ((item.netAmount || 0) / item.income) * 100;
  });

  // Color bars: green for positive savings rate, red for negative
  const backgroundColors = savingsRateData.map(rate => 
    rate >= 0 ? 'rgba(82, 196, 26, 0.6)' : 'rgba(255, 77, 79, 0.6)'
  );
  
  const borderColors = savingsRateData.map(rate => 
    rate >= 0 ? '#52c41a' : '#ff4d4f'
  );

  const chartData = {
    labels,
    datasets: [
      {
        label: 'Savings Rate (%)',
        data: savingsRateData,
        backgroundColor: backgroundColors,
        borderColor: borderColors,
        borderWidth: 1,
      }
    ]
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const value = context.parsed.y;
            return `Savings Rate: ${value.toFixed(2)}%`;
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
          text: 'Savings Rate (%)'
        },
        ticks: {
          callback: function(value) {
            return value.toFixed(0) + '%';
          }
        },
        grid: {
          color: function(context) {
            // Add a horizontal line at 0%
            if (context.tick.value === 0) {
              return 'rgba(0, 0, 0, 0.3)';
            }
            return 'rgba(0, 0, 0, 0.1)';
          }
        }
      }
    }
  };

  return (
    <div style={{ height: '400px', position: 'relative' }}>
      <Bar data={chartData} options={options} />
    </div>
  );
};

export default SavingsRateChart;