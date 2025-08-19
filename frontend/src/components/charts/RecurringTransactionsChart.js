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

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const RecurringTransactionsChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <div style={{ textAlign: 'center', padding: '50px', color: '#999' }}>No recurring transactions to display</div>;
  }

  // Prepare chart data
  const chartData = {
    labels: data.map(item => item.merchant),
    datasets: [
      {
        label: 'Monthly Amount',
        data: data.map(item => {
          // Convert to monthly amount regardless of frequency
          const amount = Math.abs(item.averageAmount);
          if (item.frequency === 'WEEKLY') {
            return amount * 4.33; // Weekly to monthly (52 weeks / 12 months)
          } else if (item.frequency === 'QUARTERLY') {
            return amount / 3; // Quarterly to monthly
          }
          return amount; // Already monthly
        }),
        backgroundColor: data.map(item => {
          if (item.frequency === 'WEEKLY') return '#52c41a';
          if (item.frequency === 'MONTHLY') return '#1890ff';
          if (item.frequency === 'QUARTERLY') return '#fa8c16';
          return '#d9d9d9';
        }),
        borderColor: data.map(item => {
          if (item.frequency === 'WEEKLY') return '#389e0d';
          if (item.frequency === 'MONTHLY') return '#096dd9';
          if (item.frequency === 'QUARTERLY') return '#d46b08';
          return '#bfbfbf';
        }),
        borderWidth: 1,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      title: {
        display: true,
        text: 'Monthly Recurring Expenses',
        font: {
          size: 16,
        },
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const item = data[context.dataIndex];
            const amount = context.parsed.y;
            return [
              `Monthly Amount: $${amount.toFixed(2)}`,
              `Frequency: ${item.frequency}`,
              `Original Amount: $${Math.abs(item.averageAmount).toFixed(2)}`,
              `Transaction Count: ${item.transactionCount}`,
            ];
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function(value) {
            return '$' + value.toFixed(2);
          },
        },
        title: {
          display: true,
          text: 'Monthly Amount ($)',
        },
      },
      x: {
        title: {
          display: true,
          text: 'Merchant / Service',
        },
        ticks: {
          maxRotation: 45,
          minRotation: 0,
        },
      },
    },
  };

  return (
    <div style={{ height: '400px' }}>
      <Bar data={chartData} options={options} />
    </div>
  );
};

export default RecurringTransactionsChart;