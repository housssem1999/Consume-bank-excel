import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Spin,
  Alert,
  Tabs,
  Button,
  Input,
  Form,
  List,
  Tag,
  Progress,
  Statistic,
  Typography,
  Space,
  Divider,
  Table,
  Tooltip
} from 'antd';
import {
  RobotOutlined,
  BulbOutlined,
  RiseOutlined,
  WarningOutlined,
  SearchOutlined,
  DollarOutlined,
  CalendarOutlined,
  BarChartOutlined
} from '@ant-design/icons';
import axios from 'axios';

const { Title, Text, Paragraph } = Typography;
const { TabPane } = Tabs;
const { TextArea } = Input;

const MLAnalytics = () => {
  const [loading, setLoading] = useState(false);
  const [predictions, setPredictions] = useState([]);
  const [insights, setInsights] = useState([]);
  const [anomalies, setAnomalies] = useState([]);
  const [transactionAnalysis, setTransactionAnalysis] = useState(null);
  const [analysisLoading, setAnalysisLoading] = useState(false);
  const [error, setError] = useState(null);

  const [form] = Form.useForm();

  useEffect(() => {
    loadMLAnalytics();
  }, []);

  const loadMLAnalytics = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Load comprehensive ML analysis
      const response = await axios.get('/api/ml-analytics/analysis/comprehensive?predictionDays=30');
      const data = response.data;
      
      setPredictions(data.expensePredictions || []);
      setInsights(data.spendingInsights || []);
      setAnomalies(data.recentAnomalies || []);
    } catch (error) {
      console.error('Error loading ML analytics:', error);
      setError('Failed to load ML analytics data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const analyzeTransaction = async (values) => {
    if (!values.description || values.description.trim() === '') {
      return;
    }

    setAnalysisLoading(true);
    try {
      const response = await axios.post('/api/ml-analytics/analyze/transaction', {
        description: values.description
      });
      setTransactionAnalysis(response.data);
    } catch (error) {
      console.error('Error analyzing transaction:', error);
      setError('Failed to analyze transaction. Please try again.');
    } finally {
      setAnalysisLoading(false);
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity?.toLowerCase()) {
      case 'high': return 'red';
      case 'medium': return 'orange';
      case 'low': return 'green';
      default: return 'blue';
    }
  };

  const getSeverityIcon = (severity) => {
    switch (severity?.toLowerCase()) {
      case 'high': return <WarningOutlined />;
      case 'medium': return <BulbOutlined />;
      case 'low': return <RiseOutlined />;
      default: return <BarChartOutlined />;
    }
  };

  const predictionColumns = [
    {
      title: 'Category',
      dataIndex: 'categoryName',
      key: 'categoryName',
      render: (text) => <Text strong>{text}</Text>
    },
    {
      title: 'Predicted Amount',
      dataIndex: 'predictedAmount',
      key: 'predictedAmount',
      render: (amount) => (
        <Statistic
          value={amount}
          precision={2}
          prefix="$"
          valueStyle={{ fontSize: '14px' }}
        />
      )
    },
    {
      title: 'Confidence',
      dataIndex: 'confidence',
      key: 'confidence',
      render: (confidence) => (
        <Progress 
          percent={Math.round(confidence * 100)} 
          size="small"
          status={confidence > 0.7 ? 'success' : confidence > 0.5 ? 'normal' : 'exception'}
        />
      )
    },
    {
      title: 'Model',
      dataIndex: 'predictionModel',
      key: 'predictionModel',
      render: (model) => <Text type="secondary" style={{ fontSize: '12px' }}>{model}</Text>
    }
  ];

  const anomalyColumns = [
    {
      title: 'Date',
      dataIndex: 'date',
      key: 'date',
      render: (date) => new Date(date).toLocaleDateString()
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      render: (text) => (
        <Tooltip title={text}>
          <Text>{text.length > 50 ? text.substring(0, 50) + '...' : text}</Text>
        </Tooltip>
      )
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount) => (
        <Text strong style={{ color: Math.abs(amount) > 1000 ? 'red' : 'inherit' }}>
          ${Math.abs(amount).toFixed(2)}
        </Text>
      )
    },
    {
      title: 'Category',
      dataIndex: ['category', 'name'],
      key: 'category',
      render: (category) => <Tag>{category || 'Other'}</Tag>
    }
  ];

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
        <Title level={4} style={{ marginTop: 16 }}>
          <RobotOutlined /> Loading AI Analytics...
        </Title>
        <Text type="secondary">Processing your financial data with machine learning models</Text>
      </div>
    );
  }

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ marginBottom: 24 }}>
        <Title level={2}>
          <RobotOutlined style={{ color: '#1890ff' }} /> AI-Powered Analytics
        </Title>
        <Paragraph>
          Advanced financial insights powered by Hugging Face machine learning models. 
          Get predictions, detect anomalies, and receive intelligent recommendations for your spending.
        </Paragraph>
        <Button 
          type="primary" 
          onClick={loadMLAnalytics}
          loading={loading}
          icon={<RobotOutlined />}
        >
          Refresh AI Analysis
        </Button>
      </div>

      {error && (
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          closable
          style={{ marginBottom: 16 }}
          onClose={() => setError(null)}
        />
      )}

      <Tabs defaultActiveKey="predictions" type="card">
        {/* Expense Predictions */}
        <TabPane tab={
          <span>
            <RiseOutlined />
            Expense Predictions
          </span>
        } key="predictions">
          <Row gutter={[16, 16]}>
            <Col span={24}>
              <Card 
                title="30-Day Expense Predictions"
                extra={<Tag color="blue">ML Powered</Tag>}
              >
                {predictions.length > 0 ? (
                  <Table
                    dataSource={predictions}
                    columns={predictionColumns}
                    rowKey="categoryName"
                    pagination={{ pageSize: 10 }}
                    size="middle"
                  />
                ) : (
                  <div style={{ textAlign: 'center', padding: '40px' }}>
                    <BarChartOutlined style={{ fontSize: '48px', color: '#ccc' }} />
                    <Title level={4} type="secondary">No Predictions Available</Title>
                    <Text type="secondary">
                      Add more transactions to generate AI-powered expense predictions
                    </Text>
                  </div>
                )}
              </Card>
            </Col>
            
            {/* Prediction Summary */}
            {predictions.length > 0 && (
              <Col span={24}>
                <Card title="Prediction Summary">
                  <Row gutter={[16, 16]}>
                    <Col xs={24} sm={8}>
                      <Statistic
                        title="Total Predicted Expenses"
                        value={predictions.reduce((sum, p) => sum + parseFloat(p.predictedAmount || 0), 0)}
                        precision={2}
                        prefix="$"
                        valueStyle={{ color: '#cf1322' }}
                      />
                    </Col>
                    <Col xs={24} sm={8}>
                      <Statistic
                        title="High Confidence Predictions"
                        value={predictions.filter(p => p.confidence > 0.7).length}
                        suffix={`/ ${predictions.length}`}
                        valueStyle={{ color: '#3f8600' }}
                      />
                    </Col>
                    <Col xs={24} sm={8}>
                      <Statistic
                        title="Categories Analyzed"
                        value={predictions.length}
                        valueStyle={{ color: '#1890ff' }}
                      />
                    </Col>
                  </Row>
                </Card>
              </Col>
            )}
          </Row>
        </TabPane>

        {/* Spending Insights */}
        <TabPane tab={
          <span>
            <BulbOutlined />
            AI Insights
          </span>
        } key="insights">
          <Card title="Intelligent Spending Insights">
            {insights.length > 0 ? (
              <List
                dataSource={insights}
                renderItem={(insight) => (
                  <List.Item>
                    <Card 
                      size="small" 
                      style={{ width: '100%' }}
                      title={
                        <Space>
                          {getSeverityIcon(insight.severity)}
                          <Text strong>{insight.title}</Text>
                          <Tag color={getSeverityColor(insight.severity)}>
                            {insight.severity}
                          </Tag>
                        </Space>
                      }
                    >
                      <Paragraph>{insight.description}</Paragraph>
                      
                      {insight.recommendations && insight.recommendations.length > 0 && (
                        <div>
                          <Text strong>Recommendations:</Text>
                          <ul style={{ marginTop: 8 }}>
                            {insight.recommendations.map((rec, index) => (
                              <li key={index}>{rec}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                      
                      <div style={{ marginTop: 12 }}>
                        <Space split={<Divider type="vertical" />}>
                          <Text type="secondary">Type: {insight.insightType}</Text>
                          {insight.confidence && (
                            <Text type="secondary">
                              Confidence: {Math.round(insight.confidence * 100)}%
                            </Text>
                          )}
                          {insight.impact && (
                            <Text type="secondary">
                              Impact: ${parseFloat(insight.impact).toFixed(2)}
                            </Text>
                          )}
                        </Space>
                      </div>
                    </Card>
                  </List.Item>
                )}
              />
            ) : (
              <div style={{ textAlign: 'center', padding: '40px' }}>
                <BulbOutlined style={{ fontSize: '48px', color: '#ccc' }} />
                <Title level={4} type="secondary">No Insights Available</Title>
                <Text type="secondary">
                  More transaction data needed to generate AI insights
                </Text>
              </div>
            )}
          </Card>
        </TabPane>

        {/* Anomaly Detection */}
        <TabPane tab={
          <span>
            <WarningOutlined />
            Anomalies ({anomalies.length})
          </span>
        } key="anomalies">
          <Card 
            title="Spending Anomalies"
            extra={<Tag color="orange">AI Detected</Tag>}
          >
            {anomalies.length > 0 ? (
              <Table
                dataSource={anomalies}
                columns={anomalyColumns}
                rowKey="id"
                pagination={{ pageSize: 10 }}
                size="middle"
              />
            ) : (
              <div style={{ textAlign: 'center', padding: '40px' }}>
                <WarningOutlined style={{ fontSize: '48px', color: '#52c41a' }} />
                <Title level={4} style={{ color: '#52c41a' }}>No Anomalies Detected</Title>
                <Text type="secondary">
                  Your spending patterns look normal for the analyzed period
                </Text>
              </div>
            )}
          </Card>
        </TabPane>

        {/* Transaction Analysis */}
        <TabPane tab={
          <span>
            <SearchOutlined />
            Transaction Analyzer
          </span>
        } key="analyzer">
          <Card title="AI Transaction Analyzer">
            <Form form={form} onFinish={analyzeTransaction} layout="vertical">
              <Form.Item
                label="Transaction Description"
                name="description"
                rules={[{ required: true, message: 'Please enter a transaction description' }]}
              >
                <TextArea
                  rows={3}
                  placeholder="e.g., 'Coffee at Starbucks downtown', 'Grocery shopping at Walmart', 'Netflix subscription'"
                />
              </Form.Item>
              <Form.Item>
                <Button 
                  type="primary" 
                  htmlType="submit"
                  loading={analysisLoading}
                  icon={<RobotOutlined />}
                >
                  Analyze with AI
                </Button>
              </Form.Item>
            </Form>

            {transactionAnalysis && (
              <Card 
                title="Analysis Results"
                style={{ marginTop: 16 }}
                type="inner"
              >
                <Row gutter={[16, 16]}>
                  <Col xs={24} sm={12}>
                    <Card size="small" title="Suggested Category">
                      <Space direction="vertical" style={{ width: '100%' }}>
                        <Tag color="blue" style={{ fontSize: '14px', padding: '4px 8px' }}>
                          {transactionAnalysis.suggestedCategory}
                        </Tag>
                        <Progress 
                          percent={Math.round(transactionAnalysis.confidence * 100)}
                          status={transactionAnalysis.confidence > 0.7 ? 'success' : 'normal'}
                        />
                      </Space>
                    </Card>
                  </Col>
                  <Col xs={24} sm={12}>
                    <Card size="small" title="Sentiment Analysis">
                      <Space direction="vertical" style={{ width: '100%' }}>
                        <Tag color={
                          transactionAnalysis.sentiment === 'positive' ? 'green' :
                          transactionAnalysis.sentiment === 'negative' ? 'red' : 'blue'
                        }>
                          {transactionAnalysis.sentiment?.toUpperCase()}
                        </Tag>
                        <Progress 
                          percent={Math.round(transactionAnalysis.sentimentScore * 100)}
                          status="normal"
                        />
                      </Space>
                    </Card>
                  </Col>
                </Row>

                {transactionAnalysis.extractedKeywords && transactionAnalysis.extractedKeywords.length > 0 && (
                  <div style={{ marginTop: 16 }}>
                    <Text strong>Extracted Keywords:</Text>
                    <div style={{ marginTop: 8 }}>
                      {transactionAnalysis.extractedKeywords.map((keyword, index) => (
                        <Tag key={index} style={{ margin: '2px' }}>{keyword}</Tag>
                      ))}
                    </div>
                  </div>
                )}

                {transactionAnalysis.alternativeCategories && transactionAnalysis.alternativeCategories.length > 0 && (
                  <div style={{ marginTop: 16 }}>
                    <Text strong>Alternative Categories:</Text>
                    <div style={{ marginTop: 8 }}>
                      {transactionAnalysis.alternativeCategories.map((category, index) => (
                        <Tag key={index} color="orange" style={{ margin: '2px' }}>{category}</Tag>
                      ))}
                    </div>
                  </div>
                )}
              </Card>
            )}
          </Card>
        </TabPane>
      </Tabs>
    </div>
  );
};

export default MLAnalytics;