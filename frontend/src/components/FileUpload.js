import React, { useState } from 'react';
import { Upload, Button, Card, Alert, Progress, Typography, Divider, List } from 'antd';
import { InboxOutlined, UploadOutlined, FileExcelOutlined } from '@ant-design/icons';
import { uploadAPI } from '../services/api';

const { Dragger } = Upload;
const { Title, Text, Paragraph } = Typography;

const FileUpload = () => {
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);
  const [error, setError] = useState(null);

  const handleUpload = async (file) => {
    setUploading(true);
    setError(null);
    setUploadResult(null);

    try {
      const response = await uploadAPI.uploadExcelFile(file);
      
      if (response.data.success) {
        setUploadResult(response.data);
      } else {
        setError(response.data.message || 'Upload failed');
      }
    } catch (err) {
      console.error('Upload error:', err);
      setError(err.response?.data?.message || 'Failed to upload file. Please try again.');
    } finally {
      setUploading(false);
    }

    return false; // Prevent default upload behavior
  };

  const uploadProps = {
    name: 'file',
    multiple: false,
    accept: '.xlsx,.xls',
    beforeUpload: handleUpload,
    showUploadList: false,
  };

  const expectedFormat = [
    { column: 'A', description: 'Date', example: '2024-01-15 or Excel date format' },
    { column: 'B', description: 'Description', example: 'Grocery Store Purchase' },
    { column: 'C', description: 'Amount', example: '-45.67 (negative for expenses, positive for income)' },
    { column: 'D', description: 'Reference (Optional)', example: 'TXN123456' },
  ];

  return (
    <div>
      <Title level={2}>Upload Bank Statement</Title>
      <Paragraph>
        Upload your bank statement in Excel format to automatically import and categorize your transactions.
      </Paragraph>

      <Card title="Upload Excel File" style={{ marginBottom: 24 }}>
        <Dragger {...uploadProps} style={{ padding: '40px' }}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined style={{ fontSize: '48px', color: '#1890ff' }} />
          </p>
          <p className="ant-upload-text">Click or drag Excel file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for .xlsx and .xls files. The file should contain your bank transaction data.
          </p>
        </Dragger>

        {uploading && (
          <div style={{ marginTop: 16 }}>
            <Progress percent={100} status="active" />
            <Text>Processing your file...</Text>
          </div>
        )}

        {error && (
          <Alert
            message="Upload Error"
            description={error}
            type="error"
            showIcon
            style={{ marginTop: 16 }}
          />
        )}

        {uploadResult && (
          <Alert
            message="Upload Successful!"
            description={`Successfully processed ${uploadResult.transactionsProcessed} transactions from your Excel file.`}
            type="success"
            showIcon
            style={{ marginTop: 16 }}
            action={
              <Button size="small" onClick={() => window.location.href = '/dashboard'}>
                View Dashboard
              </Button>
            }
          />
        )}
      </Card>

      <Card title="Expected Excel Format" icon={<FileExcelOutlined />}>
        <Paragraph>
          Your Excel file should have the following structure (first row should contain headers):
        </Paragraph>
        
        <List
          dataSource={expectedFormat}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={`Column ${item.column}: ${item.description}`}
                description={`Example: ${item.example}`}
              />
            </List.Item>
          )}
        />

        <Divider />

        <Title level={4}>Important Notes:</Title>
        <ul>
          <li>The first row should contain column headers</li>
          <li>Date should be in a recognizable format (YYYY-MM-DD preferred)</li>
          <li>Amount should be numeric (positive for income, negative for expenses)</li>
          <li>Transactions will be automatically categorized based on description</li>
          <li>Duplicate transactions will be handled automatically</li>
        </ul>

        <Divider />

        <Title level={4}>Sample Data:</Title>
        <div style={{ background: '#f5f5f5', padding: '16px', borderRadius: '4px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #d9d9d9' }}>
                <th style={{ padding: '8px', textAlign: 'left' }}>Date</th>
                <th style={{ padding: '8px', textAlign: 'left' }}>Description</th>
                <th style={{ padding: '8px', textAlign: 'left' }}>Amount</th>
                <th style={{ padding: '8px', textAlign: 'left' }}>Reference</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style={{ padding: '8px' }}>2024-01-15</td>
                <td style={{ padding: '8px' }}>Grocery Store</td>
                <td style={{ padding: '8px' }}>-85.50</td>
                <td style={{ padding: '8px' }}>TXN001</td>
              </tr>
              <tr>
                <td style={{ padding: '8px' }}>2024-01-16</td>
                <td style={{ padding: '8px' }}>Salary Deposit</td>
                <td style={{ padding: '8px' }}>3500.00</td>
                <td style={{ padding: '8px' }}>SAL001</td>
              </tr>
              <tr>
                <td style={{ padding: '8px' }}>2024-01-17</td>
                <td style={{ padding: '8px' }}>Gas Station</td>
                <td style={{ padding: '8px' }}>-45.20</td>
                <td style={{ padding: '8px' }}>TXN002</td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};

export default FileUpload;
