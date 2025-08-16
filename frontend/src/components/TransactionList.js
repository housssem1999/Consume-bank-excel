import React, { useState, useEffect } from 'react';
import { Table, Card, DatePicker, Button, Tag, Space, Spin, Alert } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import { dashboardAPI, formatCurrency, formatDate } from '../services/api';
import moment from 'moment';

const { RangePicker } = DatePicker;

const TransactionList = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [dateRange, setDateRange] = useState([
    moment().subtract(3, 'months'),
    moment()
  ]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 50,
    total: 0,
  });

  const fetchTransactions = async (startDate, endDate, page = 0, size = 50) => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await dashboardAPI.getTransactions(
        startDate ? startDate.format('YYYY-MM-DD') : null,
        endDate ? endDate.format('YYYY-MM-DD') : null,
        page,
        size
      );
      
      setTransactions(response.data);
      setPagination(prev => ({
        ...prev,
        total: response.total, // Use the total count from the server response
      }));
    } catch (err) {
      console.error('Error fetching transactions:', err);
      setError('Failed to load transactions. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions(dateRange[0], dateRange[1]);
  }, []);

  const handleDateRangeChange = (dates) => {
    setDateRange(dates);
    if (dates && dates.length === 2) {
      fetchTransactions(dates[0], dates[1]);
    }
  };

  const handleRefresh = () => {
    fetchTransactions(dateRange[0], dateRange[1]);
  };

  const getTransactionTypeColor = (type) => {
    switch (type) {
      case 'INCOME':
        return 'green';
      case 'EXPENSE':
        return 'red';
      case 'TRANSFER':
        return 'blue';
      default:
        return 'default';
    }
  };

  const getCategoryColor = (categoryName) => {
    // Simple hash function to generate consistent colors
    let hash = 0;
    for (let i = 0; i < categoryName.length; i++) {
      hash = categoryName.charCodeAt(i) + ((hash << 5) - hash);
    }
    const colors = ['magenta', 'red', 'volcano', 'orange', 'gold', 'lime', 'green', 'cyan', 'blue', 'geekblue', 'purple'];
    return colors[Math.abs(hash) % colors.length];
  };

  const columns = [
    {
      title: 'Date',
      dataIndex: 'date',
      key: 'date',
      render: (date) => formatDate(date),
      sorter: (a, b) => new Date(a.date) - new Date(b.date),
      width: 120,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      width: 300,
    },
    {
      title: 'Category',
      dataIndex: ['category', 'name'],
      key: 'category',
      render: (categoryName) => (
        <Tag color={getCategoryColor(categoryName || 'Other')}>
          {categoryName || 'Other'}
        </Tag>
      ),
      width: 150,
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: (type) => (
        <Tag color={getTransactionTypeColor(type)}>
          {type}
        </Tag>
      ),
      filters: [
        { text: 'Income', value: 'INCOME' },
        { text: 'Expense', value: 'EXPENSE' },
        { text: 'Transfer', value: 'TRANSFER' },
      ],
      onFilter: (value, record) => record.type === value,
      width: 100,
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount) => (
        <span className={amount >= 0 ? 'positive-amount' : 'negative-amount'}>
          {formatCurrency(amount)}
        </span>
      ),
      sorter: (a, b) => a.amount - b.amount,
      width: 120,
      align: 'right',
    },
    {
      title: 'Reference',
      dataIndex: 'reference',
      key: 'reference',
      ellipsis: true,
      width: 150,
    },
  ];

  if (error) {
    return (
      <Alert
        message="Error"
        description={error}
        type="error"
        showIcon
        action={
          <Button size="small" danger onClick={handleRefresh}>
            Retry
          </Button>
        }
      />
    );
  }

  return (
    <div>
      <Card
        title="Transaction History"
        extra={
          <Space>
            <RangePicker
              value={dateRange}
              onChange={handleDateRangeChange}
              format="YYYY-MM-DD"
            />
            <Button 
              icon={<ReloadOutlined />} 
              onClick={handleRefresh}
              loading={loading}
            >
              Refresh
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={transactions}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} of ${total} transactions`,
          }}
          scroll={{ x: 1000 }}
          size="middle"
        />
      </Card>
    </div>
  );
};

export default TransactionList;
