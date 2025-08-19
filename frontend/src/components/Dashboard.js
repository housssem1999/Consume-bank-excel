import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Spin, Alert, DatePicker, Button, Tabs } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined, DollarOutlined, TransactionOutlined } from '@ant-design/icons';
import { dashboardAPI, formatCurrency } from '../services/api';
import ExpenseChart from './charts/ExpenseChart';
import IncomeChart from './charts/IncomeChart';
import MonthlyTrendChart from './charts/MonthlyTrendChart';
import SavingsRateChart from './charts/SavingsRateChart';
import BudgetComparisonChart from './charts/BudgetComparisonChart';
import BudgetManager from './BudgetManager';
import moment from 'moment';

const { RangePicker } = DatePicker;
const { TabPane } = Tabs;

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [summary, setSummary] = useState(null);
  const [budgetData, setBudgetData] = useState([]);
  const [budgetLoading, setBudgetLoading] = useState(false);
  const [dateRange, setDateRange] = useState([
    moment().startOf('year'),
    moment().endOf('year')
  ]);

  const fetchDashboardData = async (startDate, endDate) => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await dashboardAPI.getFinancialSummary(
        startDate ? startDate.format('YYYY-MM-DD') : null,
        endDate ? endDate.format('YYYY-MM-DD') : null
      );
      
      setSummary(response.data);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError('Failed to load dashboard data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fetchBudgetData = async (startDate, endDate) => {
    try {
      setBudgetLoading(true);
      let response;
      
      if (startDate && endDate) {
        response = await dashboardAPI.getBudgetComparisonForPeriod(
          startDate.format('YYYY-MM-DD'),
          endDate.format('YYYY-MM-DD')
        );
      } else {
        response = await dashboardAPI.getBudgetComparison();
      }
      
      setBudgetData(response.data);
    } catch (err) {
      console.error('Error fetching budget data:', err);
    } finally {
      setBudgetLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData(dateRange[0], dateRange[1]);
    fetchBudgetData(dateRange[0], dateRange[1]);
  }, []);

  const handleDateRangeChange = (dates) => {
    setDateRange(dates);
    if (dates && dates.length === 2) {
      fetchDashboardData(dates[0], dates[1]);
      fetchBudgetData(dates[0], dates[1]);
    }
  };

  const handleBudgetUpdate = () => {
    // Refresh budget data when budgets are updated
    fetchBudgetData(dateRange[0], dateRange[1]);
  };

  const handleQuickFilter = (period) => {
    let startDate, endDate;
    
    switch (period) {
      case 'currentMonth':
        startDate = moment().startOf('month');
        endDate = moment().endOf('month');
        break;
      case 'lastMonth':
        startDate = moment().subtract(1, 'month').startOf('month');
        endDate = moment().subtract(1, 'month').endOf('month');
        break;
      case 'currentYear':
        startDate = moment().startOf('year');
        endDate = moment().endOf('year');
        break;
      case 'lastYear':
        startDate = moment().subtract(1, 'year').startOf('year');
        endDate = moment().subtract(1, 'year').endOf('year');
        break;
      default:
        return;
    }
    
    setDateRange([startDate, endDate]);
    fetchDashboardData(startDate, endDate);
    fetchBudgetData(startDate, endDate);
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
        <p style={{ marginTop: 16 }}>Loading dashboard...</p>
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        message="Error"
        description={error}
        type="error"
        showIcon
        action={
          <Button size="small" danger onClick={() => fetchDashboardData(dateRange[0], dateRange[1])}>
            Retry
          </Button>
        }
      />
    );
  }

  return (
    <div>
      {/* Header with date filters */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <h1>Financial Dashboard</h1>
        </Col>
        <Col span={12} style={{ textAlign: 'right' }}>
          <div style={{ marginBottom: 8 }}>
            <Button size="small" onClick={() => handleQuickFilter('currentMonth')} style={{ marginRight: 8 }}>
              This Month
            </Button>
            <Button size="small" onClick={() => handleQuickFilter('lastMonth')} style={{ marginRight: 8 }}>
              Last Month
            </Button>
            <Button size="small" onClick={() => handleQuickFilter('currentYear')} style={{ marginRight: 8 }}>
              This Year
            </Button>
            <Button size="small" onClick={() => handleQuickFilter('lastYear')}>
              Last Year
            </Button>
          </div>
          <RangePicker
            value={dateRange}
            onChange={handleDateRangeChange}
            format="YYYY-MM-DD"
          />
        </Col>
      </Row>

      {/* Summary Statistics */}
      <Row gutter={[16, 16]} className="dashboard-stats">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Income"
              value={summary?.totalIncome || 0}
              precision={2}
              valueStyle={{ color: '#3f8600' }}
              prefix={<ArrowUpOutlined />}
              formatter={(value) => formatCurrency(value)}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Expenses"
              value={Math.abs(summary?.totalExpenses || 0)}
              precision={2}
              valueStyle={{ color: '#cf1322' }}
              prefix={<ArrowDownOutlined />}
              formatter={(value) => formatCurrency(value)}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Net Income"
              value={summary?.netIncome || 0}
              precision={2}
              valueStyle={{ 
                color: (summary?.netIncome || 0) >= 0 ? '#3f8600' : '#cf1322' 
              }}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(value)}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="Total Transactions"
              value={summary?.totalTransactions || 0}
              prefix={<TransactionOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Charts */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="Expenses by Category" className="chart-container">
            <ExpenseChart data={summary?.expensesByCategory || []} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Income by Category" className="chart-container">
            <IncomeChart data={summary?.incomeByCategory || []} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Monthly Trends" className="chart-container">
            <MonthlyTrendChart data={summary?.monthlyTrends || []} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Savings Rate Over Time" className="chart-container">
            <SavingsRateChart data={summary?.monthlyTrends || []} />
          </Card>
        </Col>
      </Row>

      {/* Budget Section */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Tabs defaultActiveKey="comparison" type="card">
            <TabPane tab="Budget vs Actual" key="comparison">
              <Card title="Budget vs Actual Spending" className="chart-container">
                <Spin spinning={budgetLoading}>
                  <BudgetComparisonChart data={budgetData} />
                </Spin>
              </Card>
            </TabPane>
            <TabPane tab="Budget Management" key="management">
              <BudgetManager onBudgetUpdate={handleBudgetUpdate} />
            </TabPane>
          </Tabs>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
