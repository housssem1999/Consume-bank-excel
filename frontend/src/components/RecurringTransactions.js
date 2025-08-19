import React, { useState, useEffect } from 'react';
import { Table, Card, Alert, Tag, Spin, Button, Badge, Typography, Tabs } from 'antd';
import { ReloadOutlined, CalendarOutlined, BarChartOutlined } from '@ant-design/icons';
import { dashboardAPI, formatCurrency, formatDate } from '../services/api';
import RecurringTransactionsChart from './charts/RecurringTransactionsChart';

const { Title, Text } = Typography;

const RecurringTransactions = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [recurringTransactions, setRecurringTransactions] = useState([]);

  const fetchRecurringTransactions = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await dashboardAPI.getRecurringTransactions();
      setRecurringTransactions(response.data);
    } catch (err) {
      console.error('Error fetching recurring transactions:', err);
      setError('Failed to load recurring transactions. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecurringTransactions();
  }, []);

  const getFrequencyColor = (frequency) => {
    switch (frequency) {
      case 'WEEKLY':
        return 'blue';
      case 'MONTHLY':
        return 'green';
      case 'QUARTERLY':
        return 'orange';
      default:
        return 'default';
    }
  };

  const getFrequencyText = (frequency) => {
    switch (frequency) {
      case 'WEEKLY':
        return 'Weekly';
      case 'MONTHLY':
        return 'Monthly';
      case 'QUARTERLY':
        return 'Quarterly';
      default:
        return frequency;
    }
  };

  const isUpcoming = (nextExpectedDate) => {
    const today = new Date();
    const nextDate = new Date(nextExpectedDate);
    const diffTime = nextDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays >= 0;
  };

  const columns = [
    {
      title: 'Merchant',
      dataIndex: 'merchant',
      key: 'merchant',
      render: (text, record) => (
        <div>
          <Text strong>{text}</Text>
          <br />
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {record.categoryName}
          </Text>
        </div>
      ),
    },
    {
      title: 'Frequency',
      dataIndex: 'frequency',
      key: 'frequency',
      render: (frequency, record) => (
        <div>
          <Tag color={getFrequencyColor(frequency)}>
            {getFrequencyText(frequency)}
          </Tag>
          <br />
          <Text type="secondary" style={{ fontSize: '12px' }}>
            Every {record.intervalDays} days
          </Text>
        </div>
      ),
    },
    {
      title: 'Average Amount',
      dataIndex: 'averageAmount',
      key: 'averageAmount',
      render: (amount) => (
        <Text style={{ 
          color: amount < 0 ? '#ff4d4f' : '#52c41a',
          fontWeight: 'bold'
        }}>
          {formatCurrency(amount)}
        </Text>
      ),
      sorter: (a, b) => a.averageAmount - b.averageAmount,
    },
    {
      title: 'Count',
      dataIndex: 'transactionCount',
      key: 'transactionCount',
      render: (count) => (
        <Badge count={count} style={{ backgroundColor: '#52c41a' }} />
      ),
      sorter: (a, b) => a.transactionCount - b.transactionCount,
    },
    {
      title: 'Period',
      key: 'period',
      render: (_, record) => (
        <div>
          <Text style={{ fontSize: '12px' }}>
            {formatDate(record.firstTransaction)} - {formatDate(record.lastTransaction)}
          </Text>
        </div>
      ),
    },
    {
      title: 'Next Expected',
      dataIndex: 'nextExpectedDate',
      key: 'nextExpectedDate',
      render: (date) => (
        <div>
          <Text style={{ 
            color: isUpcoming(date) ? '#fa8c16' : 'inherit',
            fontWeight: isUpcoming(date) ? 'bold' : 'normal'
          }}>
            {formatDate(date)}
          </Text>
          {isUpcoming(date) && (
            <div>
              <Tag color="orange" size="small">
                Due Soon
              </Tag>
            </div>
          )}
        </div>
      ),
      sorter: (a, b) => new Date(a.nextExpectedDate) - new Date(b.nextExpectedDate),
    },
  ];

  if (loading) {
    return (
      <Card title="Recurring Transactions">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <p style={{ marginTop: 16 }}>Analyzing transaction patterns...</p>
        </div>
      </Card>
    );
  }

  if (error) {
    return (
      <Card title="Recurring Transactions">
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          action={
            <Button size="small" danger onClick={fetchRecurringTransactions}>
              Retry
            </Button>
          }
        />
      </Card>
    );
  }

  return (
    <Card 
      title={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={4} style={{ margin: 0 }}>
            <CalendarOutlined /> Recurring Transactions
          </Title>
          <Button 
            icon={<ReloadOutlined />} 
            onClick={fetchRecurringTransactions}
            size="small"
          >
            Refresh
          </Button>
        </div>
      }
      extra={
        <div style={{ textAlign: 'right' }}>
          <Text type="secondary">
            {recurringTransactions.length} patterns detected
          </Text>
        </div>
      }
    >
      {recurringTransactions.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <CalendarOutlined style={{ fontSize: '48px', color: '#d9d9d9' }} />
          <p style={{ marginTop: 16, color: '#666' }}>
            No recurring transaction patterns found.
          </p>
          <p style={{ color: '#999', fontSize: '14px' }}>
            Patterns are detected from transactions in the last 12 months with at least 3 occurrences.
          </p>
        </div>
      ) : (
        <>
          <div style={{ marginBottom: 16 }}>
            <Alert
              message="Recurring Transaction Patterns Detected"
              description={
                <div>
                  <p>These transactions appear to repeat on a regular schedule. 
                  Review them to identify subscriptions, bills, and other recurring expenses.</p>
                  <ul style={{ margin: 0, paddingLeft: 20 }}>
                    <li><strong>Weekly:</strong> Transactions repeating every 6-8 days</li>
                    <li><strong>Monthly:</strong> Transactions repeating every 28-35 days</li>
                    <li><strong>Quarterly:</strong> Transactions repeating every 84-95 days</li>
                  </ul>
                </div>
              }
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
          </div>
          
          <Tabs
            defaultActiveKey="table"
            items={[
              {
                key: 'table',
                label: (
                  <span>
                    <CalendarOutlined />
                    Table View
                  </span>
                ),
                children: (
                  <Table
                    columns={columns}
                    dataSource={recurringTransactions}
                    rowKey={(record) => `${record.merchant}-${record.frequency}`}
                    pagination={{
                      pageSize: 10,
                      showSizeChanger: true,
                      showTotal: (total, range) => 
                        `${range[0]}-${range[1]} of ${total} recurring patterns`,
                    }}
                    size="middle"
                    expandable={{
                      expandedRowRender: (record) => (
                        <div style={{ margin: 0 }}>
                          <p><strong>Description Pattern:</strong> {record.description}</p>
                          <p><strong>Transaction Dates:</strong></p>
                          <div style={{ marginLeft: 16 }}>
                            {record.transactionDates.map((date, index) => (
                              <Tag key={index} style={{ margin: '2px' }}>
                                {formatDate(date)}
                              </Tag>
                            ))}
                          </div>
                        </div>
                      ),
                      rowExpandable: (record) => record.transactionDates && record.transactionDates.length > 0,
                    }}
                  />
                ),
              },
              {
                key: 'chart',
                label: (
                  <span>
                    <BarChartOutlined />
                    Chart View
                  </span>
                ),
                children: (
                  <RecurringTransactionsChart data={recurringTransactions} />
                ),
              },
            ]}
          />
        </>
      )}
    </Card>
  );
};

export default RecurringTransactions;