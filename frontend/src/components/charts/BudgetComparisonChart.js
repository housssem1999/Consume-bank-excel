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

const BudgetComparisonChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="No budget data available" />;
  }

  // Filter out categories with no budget set
  const budgetedCategories = data.filter(item => 
    item.budgetAmount && item.budgetAmount > 0
  );
  
  if (budgetedCategories.length === 0) {
    return <Empty description="No budgets set for any categories" />;
  }

  const chartData = {
    labels: budgetedCategories.map(item => item.categoryName),
    datasets: [
      {
        label: 'Budget',
        data: budgetedCategories.map(item => Math.abs(item.budgetAmount)),
        backgroundColor: 'rgba(54, 162, 235, 0.6)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
      },
      {
        label: 'Actual Spending',
        data: budgetedCategories.map(item => Math.abs(item.actualAmount)),
        backgroundColor: budgetedCategories.map(item => item.statusColor + '99'), // Add transparency
        borderColor: budgetedCategories.map(item => item.statusColor),
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
        labels: {
          padding: 20,
          font: {
            size: 12
          }
        }
      },
      title: {
        display: true,
        text: 'Budget vs Actual Spending',
        font: {
          size: 16,
          weight: 'bold'
        }
      },
      tooltip: {
        callbacks: {
          afterLabel: function(context) {
            const dataIndex = context.dataIndex;
            const item = budgetedCategories[dataIndex];
            const percentage = item.percentageDifference;
            const status = item.overBudget ? 'Over Budget' : 'Under Budget';
            const diff = Math.abs(percentage).toFixed(1);
            return [
              `${status}: ${diff}%`,
              `Difference: $${Math.abs(item.actualAmount - item.budgetAmount).toFixed(2)}`
            ];
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
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
      <Bar data={chartData} options={options} />
      
      {/* Status indicators */}
      <div style={{ marginTop: '16px', display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
        {budgetedCategories.map((item, index) => (
          <div
            key={index}
            style={{
              display: 'flex',
              alignItems: 'center',
              padding: '4px 8px',
              borderRadius: '4px',
              backgroundColor: item.statusColor + '20',
              border: `1px solid ${item.statusColor}`,
              fontSize: '12px'
            }}
          >
            <span style={{ color: item.statusColor, fontWeight: 'bold' }}>
              {item.categoryName}
            </span>
            <span style={{ marginLeft: '8px' }}>
              {item.overBudget ? '+' : ''}{item.percentageDifference.toFixed(1)}%
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BudgetComparisonChart;