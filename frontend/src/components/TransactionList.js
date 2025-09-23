import React, { useState, useEffect } from 'react';
import { Table, Card, DatePicker, Button, Tag, Space, Alert, Tooltip, Popconfirm, message } from 'antd';
import { ReloadOutlined, EditOutlined, DeleteOutlined, PlusOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { dashboardAPI, formatCurrency, formatDate, transactionsAPI } from '../services/api';
import TransactionEditModal from './TransactionEditModal';
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
  const [sorting, setSorting] = useState({
    field: null,
    order: null,
  });
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [editModalMode, setEditModalMode] = useState('edit');
  const [deleteLoading, setDeleteLoading] = useState({});

  const fetchTransactions = async (startDate, endDate, page = 0, size = 50, sortBy = null, sortDir = 'desc') => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await dashboardAPI.getTransactions(
        startDate ? startDate.format('YYYY-MM-DD') : null,
        endDate ? endDate.format('YYYY-MM-DD') : null,
        page,
        size,
        sortBy,
        sortDir
      );
      
      setTransactions(response.data.data);
      setPagination(prev => ({
        ...prev,
        total: response.data.total,
        current: response.data.page + 1, // Convert 0-based to 1-based
        pageSize: response.data.size,
      }));
    } catch (err) {
      console.error('Error fetching transactions:', err);
      setError('Failed to load transactions. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions(dateRange[0], dateRange[1], 0, pagination.pageSize);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleDateRangeChange = (dates) => {
    setDateRange(dates);
    if (dates && dates.length === 2) {
      // Reset pagination when date range changes
      setPagination(prev => ({
        ...prev,
        current: 1,
      }));
      setSorting({ field: null, order: null });
      fetchTransactions(dates[0], dates[1], 0, pagination.pageSize);
    }
  };

  const handleRefresh = () => {
    // Reset pagination and sorting on refresh
    setPagination(prev => ({
      ...prev,
      current: 1,
    }));
    setSorting({ field: null, order: null });
    fetchTransactions(dateRange[0], dateRange[1], 0, pagination.pageSize);
  };

  const handleTableChange = (paginationConfig, filters, sorter) => {
    const { current, pageSize } = paginationConfig;
    
    // Update pagination state
    setPagination({
      current,
      pageSize,
      total: pagination.total,
    });

    // Update sorting state
    let sortBy = null;
    let sortDir = 'desc';
    
    if (sorter && sorter.field && sorter.order) {
      sortBy = sorter.field;
      sortDir = sorter.order === 'ascend' ? 'asc' : 'desc';
      setSorting({
        field: sorter.field,
        order: sorter.order,
      });
    } else {
      setSorting({ field: null, order: null });
    }

    // Fetch data with new parameters (convert to 0-based page index)
    fetchTransactions(
      dateRange[0], 
      dateRange[1], 
      current - 1, 
      pageSize, 
      sortBy, 
      sortDir
    );
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

  const handleEdit = (transaction) => {
    setEditingTransaction(transaction);
    setEditModalMode('edit');
    setEditModalVisible(true);
  };

  const handleCreate = () => {
    setEditingTransaction(null);
    setEditModalMode('create');
    setEditModalVisible(true);
  };

  const handleEditModalCancel = () => {
    setEditModalVisible(false);
    setEditingTransaction(null);
  };

  const handleEditModalSuccess = (updatedTransaction) => {
    // Refresh the transaction list to show updated data
    handleRefresh();
    message.success(`Transaction ${editModalMode}d successfully`);
  };

  const handleDelete = async (transaction) => {
    try {
      setDeleteLoading(prev => ({ ...prev, [transaction.id]: true }));
      
      const response = await transactionsAPI.deleteTransaction(transaction.id);
      
      if (response.data.success) {
        message.success('Transaction deleted successfully');
        // Refresh the transaction list
        handleRefresh();
      } else {
        message.error(response.data.message || 'Failed to delete transaction');
      }
    } catch (error) {
      console.error('Error deleting transaction:', error);
      
      if (error.response?.status === 404) {
        message.error('Transaction not found. It may have already been deleted.');
        // Refresh to update the list
        handleRefresh();
      } else if (error.response?.status >= 500) {
        message.error('Server error occurred. Please try again.');
      } else if (error.code === 'NETWORK_ERROR' || !navigator.onLine) {
        message.error('Network connection lost. Please check your internet connection.');
      } else {
        message.error(error.response?.data?.message || 'Failed to delete transaction');
      }
    } finally {
      setDeleteLoading(prev => ({ ...prev, [transaction.id]: false }));
    }
  };

  const columns = [
    {
      title: 'Date',
      dataIndex: 'date',
      key: 'date',
      render: (date) => formatDate(date),
      sorter: true,
      sortOrder: sorting.field === 'date' ? sorting.order : null,
      width: 120,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      sorter: true,
      sortOrder: sorting.field === 'description' ? sorting.order : null,
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
      sorter: true,
      sortOrder: sorting.field === 'amount' ? sorting.order : null,
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
    {
      title: 'Actions',
      key: 'actions',
      width: 100,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="Edit Transaction">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
              size="small"
            />
          </Tooltip>
          <Tooltip title="Delete Transaction">
            <Popconfirm
              title="Delete Transaction"
              description={`Are you sure you want to delete this transaction?`}
              onConfirm={() => handleDelete(record)}
              okText="Yes"
              cancelText="No"
              icon={<ExclamationCircleOutlined style={{ color: 'red' }} />}
            >
              <Button
                type="text"
                icon={<DeleteOutlined />}
                danger
                size="small"
                loading={deleteLoading[record.id]}
              />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
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
            <Button 
              type="primary"
              icon={<PlusOutlined />} 
              onClick={handleCreate}
            >
              Add Transaction
            </Button>
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
          onChange={handleTableChange}
          scroll={{ x: 1000 }}
          size="middle"
        />
      </Card>

      <TransactionEditModal
        visible={editModalVisible}
        onCancel={handleEditModalCancel}
        onSuccess={handleEditModalSuccess}
        transaction={editingTransaction}
        mode={editModalMode}
      />
    </div>
  );
};

export default TransactionList;
