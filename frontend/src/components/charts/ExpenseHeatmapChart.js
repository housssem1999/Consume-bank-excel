import React from 'react';
import { Chart as ChartJS, CategoryScale, LinearScale, Tooltip, Legend } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import { Empty, Table } from 'antd';

ChartJS.register(CategoryScale, LinearScale, Tooltip, Legend);

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
  
  // Create table data for better visualization
  const tableData = categories.map((category, index) => {
    const row = { key: index, category };
    daysOfWeek.forEach(day => {
      const key = `${category}-${day}`;
      const amount = dataMap.get(key) || 0;
      row[day] = amount > 0 ? `$${amount.toFixed(2)}` : '-';
    });
    return row;
  });

  const columns = [
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      fixed: 'left',
      width: 120,
    },
    ...daysOfWeek.map(day => ({
      title: day.substring(0, 3), // Show only first 3 letters
      dataIndex: day,
      key: day,
      width: 80,
      align: 'center',
      render: (value, record) => {
        const key = `${record.category}-${day}`;
        const amount = dataMap.get(key) || 0;
        if (amount === 0) return <span style={{ color: '#ccc' }}>-</span>;
        
        // Calculate intensity based on max value for color coding
        const maxAmount = Math.max(...Array.from(dataMap.values()));
        const intensity = amount / maxAmount;
        const backgroundColor = `rgba(255, 107, 107, ${intensity * 0.8 + 0.1})`;
        
        return (
          <div 
            style={{ 
              backgroundColor, 
              padding: '4px', 
              borderRadius: '4px',
              color: intensity > 0.5 ? 'white' : 'black',
              fontWeight: 'bold',
              fontSize: '12px'
            }}
          >
            ${amount.toFixed(0)}
          </div>
        );
      }
    }))
  ];

  return (
    <div style={{ height: '400px', position: 'relative' }}>
      <div style={{ marginBottom: '16px', fontSize: '14px', color: '#666' }}>
        Color intensity represents spending amount. Darker = Higher spending.
      </div>
      <Table
        columns={columns}
        dataSource={tableData}
        pagination={false}
        size="small"
        scroll={{ x: 800 }}
        style={{ fontSize: '12px' }}
      />
    </div>
  );
};

export default ExpenseHeatmapChart;