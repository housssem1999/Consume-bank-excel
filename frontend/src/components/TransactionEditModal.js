import React, { useState, useEffect } from 'react';
import { 
  Modal, 
  Form, 
  Input, 
  InputNumber, 
  Select, 
  DatePicker, 
  Button, 
  Alert, 
  Space,
  Spin,
  notification
} from 'antd';
import { ExclamationCircleOutlined, SaveOutlined, CloseOutlined } from '@ant-design/icons';
import { transactionsAPI, categoriesAPI } from '../services/api';
import moment from 'moment';

const { Option } = Select;
const { TextArea } = Input;

const TransactionEditModal = ({ 
  visible, 
  onCancel, 
  onSuccess, 
  transaction = null,
  mode = 'edit' // 'edit' or 'create'
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [categories, setCategories] = useState([]);
  const [fieldErrors, setFieldErrors] = useState({});
  const [retryCount, setRetryCount] = useState(0);
  const maxRetries = 3;

  // Initialize form when transaction changes
  useEffect(() => {
    if (visible) {
      if (mode === 'edit' && transaction) {
        form.setFieldsValue({
          date: transaction.date ? moment(transaction.date) : null,
          description: transaction.description || '',
          amount: transaction.amount || '',
          type: transaction.type || '',
          categoryId: transaction.category?.id || null,
          reference: transaction.reference || ''
        });
      } else if (mode === 'create') {
        form.resetFields();
        form.setFieldsValue({
          date: moment(),
          type: 'EXPENSE' // Default to expense
        });
      }
      fetchCategories();
      setError(null);
      setFieldErrors({});
      setRetryCount(0);
    }
  }, [visible, transaction, mode, form]);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const response = await categoriesAPI.getAllCategories();
      setCategories(response.data || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
      setError('Failed to load categories. You can still save the transaction.');
      // Don't block the modal if categories fail to load
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values) => {
    try {
      setSaving(true);
      setError(null);
      setFieldErrors({});

      // Prepare transaction data
      const transactionData = {
        date: values.date ? values.date.format('YYYY-MM-DD') : null,
        description: values.description?.trim() || '',
        amount: values.amount?.toString() || '',
        type: values.type || '',
        categoryId: values.categoryId || null,
        reference: values.reference?.trim() || ''
      };

      let response;
      if (mode === 'edit' && transaction?.id) {
        response = await transactionsAPI.updateTransaction(transaction.id, transactionData);
      } else {
        response = await transactionsAPI.createTransaction(transactionData);
      }

      if (response.data.success) {
        notification.success({
          message: 'Success',
          description: response.data.message || `Transaction ${mode}d successfully`,
          duration: 3
        });
        
        // Reset retry count on success
        setRetryCount(0);
        
        if (onSuccess) {
          onSuccess(response.data.transaction);
        }
        handleCancel();
      } else {
        handleApiError(response.data);
      }

    } catch (err) {
      handleRequestError(err);
    } finally {
      setSaving(false);
    }
  };

  const handleApiError = (errorData) => {
    if (errorData.errors) {
      // Field-specific errors
      setFieldErrors(errorData.errors);
      setError('Please fix the highlighted errors and try again.');
    } else {
      setError(errorData.message || 'An error occurred while saving the transaction.');
    }
  };

  const handleRequestError = (err) => {
    console.error('Transaction save error:', err);
    
    if (err.response?.status === 400 && err.response?.data?.errors) {
      // Validation errors
      setFieldErrors(err.response.data.errors);
      setError('Please fix the highlighted errors and try again.');
    } else if (err.response?.status === 404) {
      setError('Transaction not found. It may have been deleted by another user.');
    } else if (err.response?.status === 409) {
      setError('This transaction has been modified by another user. Please refresh and try again.');
    } else if (err.response?.status >= 500) {
      setError('Server error occurred. Please try again or contact support if the problem persists.');
    } else if (err.code === 'NETWORK_ERROR' || !navigator.onLine) {
      setError('Network connection lost. Please check your internet connection and try again.');
    } else {
      setError(err.response?.data?.message || 'An unexpected error occurred. Please try again.');
    }
  };

  const handleRetry = () => {
    if (retryCount < maxRetries) {
      setRetryCount(prev => prev + 1);
      form.submit();
    } else {
      notification.error({
        message: 'Maximum Retries Exceeded',
        description: 'Please check your inputs and try again later.',
      });
    }
  };

  const handleCancel = () => {
    setError(null);
    setFieldErrors({});
    setRetryCount(0);
    form.resetFields();
    if (onCancel) {
      onCancel();
    }
  };

  const getFieldError = (fieldName) => {
    return fieldErrors[fieldName];
  };

  const validateAmount = (_, value) => {
    if (value === undefined || value === null || value === '') {
      return Promise.reject(new Error('Amount is required'));
    }
    
    const num = parseFloat(value);
    if (isNaN(num)) {
      return Promise.reject(new Error('Please enter a valid number'));
    }
    
    // Check decimal places
    const decimalPlaces = (value.toString().split('.')[1] || '').length;
    if (decimalPlaces > 2) {
      return Promise.reject(new Error('Amount cannot have more than 2 decimal places'));
    }
    
    return Promise.resolve();
  };

  const validateDescription = (_, value) => {
    if (!value || value.trim().length === 0) {
      return Promise.reject(new Error('Description is required'));
    }
    
    if (value.length > 500) {
      return Promise.reject(new Error('Description cannot exceed 500 characters'));
    }
    
    return Promise.resolve();
  };

  return (
    <Modal
      title={
        <Space>
          {mode === 'edit' ? 'Edit Transaction' : 'Create New Transaction'}
          {saving && <Spin size="small" />}
        </Space>
      }
      open={visible}
      onCancel={handleCancel}
      width={600}
      footer={null}
      maskClosable={!saving}
      closable={!saving}
    >
      {error && (
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
          action={
            retryCount < maxRetries && (
              <Button size="small" danger onClick={handleRetry}>
                Retry ({maxRetries - retryCount} left)
              </Button>
            )
          }
        />
      )}

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        disabled={saving}
      >
        <Form.Item
          name="date"
          label="Date"
          rules={[{ required: true, message: 'Date is required' }]}
          validateStatus={getFieldError('date') ? 'error' : ''}
          help={getFieldError('date')}
        >
          <DatePicker 
            style={{ width: '100%' }} 
            format="YYYY-MM-DD"
            placeholder="Select date"
          />
        </Form.Item>

        <Form.Item
          name="description"
          label="Description"
          rules={[{ validator: validateDescription }]}
          validateStatus={getFieldError('description') ? 'error' : ''}
          help={getFieldError('description')}
        >
          <TextArea 
            rows={3} 
            placeholder="Enter transaction description"
            showCount
            maxLength={500}
          />
        </Form.Item>

        <Form.Item
          name="amount"
          label="Amount"
          rules={[{ validator: validateAmount }]}
          validateStatus={getFieldError('amount') ? 'error' : ''}
          help={getFieldError('amount')}
        >
          <InputNumber
            style={{ width: '100%' }}
            placeholder="Enter amount"
            precision={2}
            step={0.01}
            formatter={value => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
            parser={value => value.replace(/\$\s?|(,*)/g, '')}
          />
        </Form.Item>

        <Form.Item
          name="type"
          label="Type"
          rules={[{ required: true, message: 'Transaction type is required' }]}
          validateStatus={getFieldError('type') ? 'error' : ''}
          help={getFieldError('type')}
        >
          <Select placeholder="Select transaction type">
            <Option value="INCOME">Income</Option>
            <Option value="EXPENSE">Expense</Option>
            <Option value="TRANSFER">Transfer</Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="categoryId"
          label="Category"
          validateStatus={getFieldError('categoryId') ? 'error' : ''}
          help={getFieldError('categoryId')}
        >
          <Select 
            placeholder="Select category (optional)"
            allowClear
            loading={loading}
            notFoundContent={loading ? 'Loading categories...' : 'No categories found'}
          >
            {categories.map(category => (
              <Option key={category.id} value={category.id}>
                {category.name}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="reference"
          label="Reference"
          validateStatus={getFieldError('reference') ? 'error' : ''}
          help={getFieldError('reference')}
        >
          <Input placeholder="Enter reference (optional)" />
        </Form.Item>

        <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
          <Space>
            <Button 
              onClick={handleCancel}
              disabled={saving}
              icon={<CloseOutlined />}
            >
              Cancel
            </Button>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={saving}
              icon={<SaveOutlined />}
            >
              {saving ? 'Saving...' : (mode === 'edit' ? 'Update' : 'Create')}
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default TransactionEditModal;