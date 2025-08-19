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
} from 'chart.js';
import { Empty, Card, Tag } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined, MinusOutlined } from '@ant-design/icons';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const NetWorthTrendChart = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="No net worth data available" />;
  }

  const sortedData = [...data].sort((a, b) => {
    if (a.year !== b.year) return a.year - b.year;
    return a.month - b.month;
  });

  const labels = sortedData.map(item => `${item.monthName} ${item.year}`);
  
  // Calculate trend indicators for the last few months
  const getTrendIcon = (direction) => {
    switch (direction) {
      case 'up':
        return <ArrowUpOutlined style={{ color: '#52c41a' }} />;
      case 'down':
        return <ArrowDownOutlined style={{ color: '#ff4d4f' }} />;
      default:
        return <MinusOutlined style={{ color: '#d9d9d9' }} />;
    }
  };

  const getTrendColor = (direction) => {
    switch (direction) {
      case 'up':
        return 'green';
      case 'down':
        return 'red';
      default:
        return 'default';
    }
  };

  const chartData = {
    labels,
    datasets: [
      {
        label: 'Net Worth',
        data: sortedData.map(item => item.cumulativeNetWorth || 0),
        borderColor: '#1890ff',
        backgroundColor: 'rgba(24, 144, 255, 0.1)',
        tension: 0.4,
        fill: true,
        pointRadius: 6,
        pointHoverRadius: 8,
        pointBackgroundColor: sortedData.map(item => {
          switch (item.trendDirection) {
            case 'up':
              return '#52c41a';
            case 'down':
              return '#ff4d4f';
            default:
              return '#d9d9d9';
          }
        }),
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
      }
    ]
  };

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
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const dataPoint = sortedData[context.dataIndex];
            const netWorth = context.parsed.y;
            const change = dataPoint.netWorthChange;
            const changeText = change >= 0 ? `+$${change.toFixed(2)}` : `-$${Math.abs(change).toFixed(2)}`;
            return [
              `Net Worth: $${netWorth.toFixed(2)}`,
              `Monthly Change: ${changeText}`
            ];
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
          text: 'Net Worth ($)'
        },
        ticks: {
          callback: function(value) {
            return '$' + value.toLocaleString();
          }
        },
        grid: {
          color: 'rgba(0, 0, 0, 0.1)',
        }
      }
    }
  };

  // Calculate overall trend for summary
  const currentNetWorth = sortedData[sortedData.length - 1]?.cumulativeNetWorth || 0;
  const previousNetWorth = sortedData[sortedData.length - 2]?.cumulativeNetWorth || 0;
  const overallChange = currentNetWorth - previousNetWorth;
  const overallDirection = overallChange > 0 ? 'up' : overallChange < 0 ? 'down' : 'flat';

  return (
    <Card 
      title="Net Worth Trend" 
      extra={
        <Tag color={getTrendColor(overallDirection)} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          {getTrendIcon(overallDirection)}
          <span>
            {overallChange >= 0 ? '+' : ''}${overallChange.toFixed(2)}
          </span>
        </Tag>
      }
    >
      <div style={{ height: '400px', position: 'relative' }}>
        <Line data={chartData} options={options} />
      </div>
      <div style={{ marginTop: '16px', textAlign: 'center', fontSize: '14px', color: '#666' }}>
        <strong>Current Net Worth: </strong>
        <span style={{ 
          color: currentNetWorth >= 0 ? '#52c41a' : '#ff4d4f',
          fontWeight: 'bold' 
        }}>
          ${currentNetWorth.toLocaleString()}
        </span>
      </div>
    </Card>
  );
};

export default NetWorthTrendChart;