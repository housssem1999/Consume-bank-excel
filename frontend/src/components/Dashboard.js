import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Spin, Alert, DatePicker, Button, Tabs } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined, DollarOutlined, TransactionOutlined, RobotOutlined } from '@ant-design/icons';
import { dashboardAPI, formatCurrency } from '../services/api';
import ExpenseChart from './charts/ExpenseChart';
import IncomeChart from './charts/IncomeChart';
import MonthlyTrendChart from './charts/MonthlyTrendChart';
import SavingsRateChart from './charts/SavingsRateChart';
import BudgetComparisonChart from './charts/BudgetComparisonChart';
import BudgetManager from './BudgetManager';
import ExpenseHeatmapChart from './charts/ExpenseHeatmapChart';
import moment from 'moment';

const { RangePicker } = DatePicker;
const { TabPane } = Tabs;

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [summary, setSummary] = useState(null);
  const [budgetData, setBudgetData] = useState([]);
  const [mlInsights, setMlInsights] = useState([]);
  const [mlLoading, setMlLoading] = useState(false);
  const [budgetLoading, setBudgetLoading] = useState(false);
  const [heatmapData, setHeatmapData] = useState([]);
  const [dateRange, setDateRange] = useState([
    moment().startOf('year'),
    moment().endOf('year')
  ]);

  const fetchDashboardData = async (startDate, endDate) => {
    try {
      setLoading(true);
      setError(null);
      
      const [summaryResponse, heatmapResponse] = await Promise.all([
        dashboardAPI.getFinancialSummary(
          startDate ? startDate.format('YYYY-MM-DD') : null,
          endDate ? endDate.format('YYYY-MM-DD') : null
        ),
        dashboardAPI.getExpenseHeatmap(
          startDate ? startDate.format('YYYY-MM-DD') : null,
          endDate ? endDate.format('YYYY-MM-DD') : null
        )
      ]);
      
      setSummary(summaryResponse.data);
      setHeatmapData(heatmapResponse.data);
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

  const fetchMLInsights = async () => {
    try {
      setMlLoading(true);
      const response = await fetch('/api/ml-analytics/insights/spending');
      if (response.ok) {
        const data = await response.json();
        setMlInsights(data.insights?.slice(0, 3) || []); // Show top 3 insights
      }
    } catch (err) {
      console.error('Error fetching ML insights:', err);
    } finally {
      setMlLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData(dateRange[0], dateRange[1]);
    fetchBudgetData(dateRange[0], dateRange[1]);
    fetchMLInsights();
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

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="Expense Heatmap by Day of Week" className="chart-container">
            <ExpenseHeatmapChart data={heatmapData} />
          </Card>
        </Col>
      </Row>

      {/* AI Insights Section */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card 
            title={
              <span>
                <RobotOutlined style={{ color: '#1890ff', marginRight: 8 }} />
                AI Insights & Recommendations
              </span>
            }
            extra={
              <Button 
                type="link" 
                onClick={() => window.location.href = '/ml-analytics'}
                icon={<RobotOutlined />}
              >
                View All AI Analytics
              </Button>
            }
            className="chart-container"
          >
            <Spin spinning={mlLoading}>
              {mlInsights.length > 0 ? (
                <Row gutter={[16, 16]}>
                  {mlInsights.map((insight, index) => (
                    <Col xs={24} md={8} key={index}>
                      <Card 
                        size="small" 
                        style={{ 
                          height: '100%',
                          borderLeft: `4px solid ${
                            insight.severity === 'HIGH' ? '#ff4d4f' : 
                            insight.severity === 'MEDIUM' ? '#faad14' : '#52c41a'
                          }`
                        }}
                      >
                        <div style={{ marginBottom: 8 }}>
                          <strong>{insight.title}</strong>
                        </div>
                        <div style={{ color: '#666', fontSize: '14px', marginBottom: 8 }}>
                          {insight.description}
                        </div>
                        <div style={{ fontSize: '12px', color: '#999' }}>
                          Type: {insight.insightType} | 
                          Confidence: {insight.confidence ? Math.round(insight.confidence * 100) + '%' : 'N/A'}
                        </div>
                      </Card>
                    </Col>
                  ))}
                </Row>
              ) : (
                <div style={{ textAlign: 'center', padding: '20px', color: '#999' }}>
                  <RobotOutlined style={{ fontSize: '32px', marginBottom: '8px' }} />
                  <div>AI insights will appear here as you add more transactions</div>
                </div>
              )}
            </Spin>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
