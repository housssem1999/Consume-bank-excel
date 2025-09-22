import React, { useState, useEffect } from 'react';
import { Card, Table, InputNumber, message, Space } from 'antd';
import { categoriesAPI } from '../services/api';

const BudgetManager = ({ onBudgetUpdate }) => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [updatingBudgets, setUpdatingBudgets] = useState({});

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const response = await categoriesAPI.getAllCategories();
      setCategories(response.data);
    } catch (error) {
      message.error('Failed to fetch categories');
      console.error('Error fetching categories:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleBudgetUpdate = async (categoryId, budget) => {
    setUpdatingBudgets(prev => ({ ...prev, [categoryId]: true }));
    
    try {
      await categoriesAPI.updateCategoryBudget(categoryId, { monthlyBudget: budget });
      message.success('Budget updated successfully');
      
      // Update local state
      setCategories(prev => 
        prev.map(cat => 
          cat.id === categoryId 
            ? { ...cat, monthlyBudget: budget }
            : cat
        )
      );
      
      // Notify parent component
      if (onBudgetUpdate) {
        onBudgetUpdate();
      }
    } catch (error) {
      message.error('Failed to update budget');
      console.error('Error updating budget:', error);
    } finally {
      setUpdatingBudgets(prev => ({ ...prev, [categoryId]: false }));
    }
  };

  const columns = [
    {
      title: 'Category',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          <div
            style={{
              width: '12px',
              height: '12px',
              borderRadius: '50%',
              backgroundColor: record.color,
            }}
          />
          {text}
        </Space>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: 'Monthly Budget',
      dataIndex: 'monthlyBudget',
      key: 'monthlyBudget',
      render: (budget, record) => (
        <InputNumber
          value={budget}
          min={0}
          step={10}
          precision={2}
          formatter={value => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
          parser={value => value.replace(/\$\s?|(,*)/g, '')}
          onChange={(value) => handleBudgetUpdate(record.id, value || 0)}
          loading={updatingBudgets[record.id]}
          style={{ width: '150px' }}
        />
      ),
    },
  ];

  // Filter out Income, Transfer categories for budget setting
  const expenseCategories = categories.filter(cat => 
    !['Income', 'Transfer'].includes(cat.name)
  );

  return (
    <Card title="Budget Management" style={{ marginBottom: '24px' }}>
      <p style={{ marginBottom: '16px', color: '#666' }}>
        Set monthly budgets for your expense categories. Changes are saved automatically.
      </p>
      <Table
        columns={columns}
        dataSource={expenseCategories}
        rowKey="id"
        loading={loading}
        pagination={false}
        size="small"
      />
    </Card>
  );
};

export default BudgetManager;