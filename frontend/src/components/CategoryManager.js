import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Table, 
  Button, 
  Modal, 
  Form, 
  Input, 
  message, 
  Space, 
  Popconfirm, 
  Tooltip
} from 'antd';
import { 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  ExclamationCircleOutlined 
} from '@ant-design/icons';
import { categoriesAPI } from '../services/api';

const CategoryManager = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [form] = Form.useForm();

  // Predefined color options for categories
  const colorOptions = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', 
    '#DDA0DD', '#98D8C8', '#F7DC6F', '#82E0AA', '#7DCEA0',
    '#76D7C4', '#85C1E9', '#F8C471', '#D5A6BD', '#AED6F1',
    '#D5DBDB', '#F1948A', '#85929E', '#F8D7DA', '#D1F2EB'
  ];

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

  const handleCreate = () => {
    setEditingCategory(null);
    form.resetFields();
    form.setFieldsValue({ color: colorOptions[0] }); // Set default color
    setModalVisible(true);
  };

  const handleEdit = (category) => {
    setEditingCategory(category);
    form.setFieldsValue({
      name: category.name,
      description: category.description,
      color: category.color
    });
    setModalVisible(true);
  };

  const handleDelete = async (category) => {
    try {
      // First check if category has transactions
      const countResponse = await categoriesAPI.getCategoryTransactionCount(category.id);
      const transactionCount = countResponse.data.transactionCount;
      
      if (transactionCount > 0) {
        message.error(`Cannot delete category '${category.name}' because it has ${transactionCount} associated transactions.`);
        return;
      }

      await categoriesAPI.deleteCategory(category.id);
      message.success('Category deleted successfully');
      fetchCategories();
    } catch (error) {
      message.error(error.response?.data?.message || 'Failed to delete category');
      console.error('Error deleting category:', error);
    }
  };

  const handleSubmit = async (values) => {
    try {
      if (editingCategory) {
        await categoriesAPI.updateCategory(editingCategory.id, values);
        message.success('Category updated successfully');
      } else {
        await categoriesAPI.createCategory(values);
        message.success('Category created successfully');
      }
      
      setModalVisible(false);
      fetchCategories();
    } catch (error) {
      message.error(error.response?.data?.message || 'Failed to save category');
      console.error('Error saving category:', error);
    }
  };

  const handleCancel = () => {
    setModalVisible(false);
    setEditingCategory(null);
    form.resetFields();
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          <div
            style={{
              width: '16px',
              height: '16px',
              borderRadius: '50%',
              backgroundColor: record.color,
              border: '1px solid #d9d9d9'
            }}
          />
          <strong>{text}</strong>
        </Space>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Color',
      dataIndex: 'color',
      key: 'color',
      width: 100,
      render: (color) => (
        <Space>
          <div
            style={{
              width: '20px',
              height: '20px',
              backgroundColor: color,
              border: '1px solid #d9d9d9',
              borderRadius: '4px'
            }}
          />
          <code style={{ fontSize: '11px', color: '#666' }}>{color}</code>
        </Space>
      ),
    },
    {
      title: 'Budget',
      dataIndex: 'monthlyBudget',
      key: 'monthlyBudget',
      width: 120,
      render: (budget) => budget ? `$${budget.toLocaleString()}` : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (text, record) => (
        <Space>
          <Tooltip title="Edit Category">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
              size="small"
            />
          </Tooltip>
          <Tooltip title="Delete Category">
            <Popconfirm
              title="Delete Category"
              description={`Are you sure you want to delete "${record.name}"?`}
              onConfirm={() => handleDelete(record)}
              okText="Yes"
              cancelText="No"
              icon={<ExclamationCircleOutlined style={{ color: 'red' }} />}
              disabled={record.name === 'Other'} // Prevent deletion of default category
            >
              <Button
                type="text"
                icon={<DeleteOutlined />}
                danger
                size="small"
                disabled={record.name === 'Other'}
              />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card 
        title="Category Management" 
        extra={
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={handleCreate}
          >
            Add Category
          </Button>
        }
        style={{ marginBottom: '24px' }}
      >
        <p style={{ marginBottom: '16px', color: '#666' }}>
          Manage your transaction categories. Create, edit, or delete categories to better organize your financial data.
          Categories with transactions cannot be deleted.
        </p>
        
        <Table
          columns={columns}
          dataSource={categories}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          size="middle"
        />
      </Card>

      <Modal
        title={editingCategory ? 'Edit Category' : 'Create New Category'}
        open={modalVisible}
        onCancel={handleCancel}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ color: colorOptions[0] }}
        >
          <Form.Item
            name="name"
            label="Category Name"
            rules={[
              { required: true, message: 'Please enter category name' },
              { min: 2, message: 'Name must be at least 2 characters' },
              { max: 50, message: 'Name must be less than 50 characters' }
            ]}
          >
            <Input placeholder="Enter category name" />
          </Form.Item>

          <Form.Item
            name="description"
            label="Description"
            rules={[
              { required: true, message: 'Please enter category description' },
              { max: 200, message: 'Description must be less than 200 characters' }
            ]}
          >
            <Input.TextArea 
              placeholder="Enter category description" 
              rows={3}
            />
          </Form.Item>

          <Form.Item
            name="color"
            label="Color"
            rules={[{ required: true, message: 'Please select a color' }]}
          >
            <div>
              <div style={{ marginBottom: '8px' }}>
                <Input 
                  placeholder="#FF6B6B" 
                  style={{ width: '120px', marginRight: '8px' }}
                  onChange={(e) => form.setFieldsValue({ color: e.target.value })}
                />
                <span style={{ color: '#666', fontSize: '12px' }}>
                  Or select from options below:
                </span>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                {colorOptions.map((color) => (
                  <div
                    key={color}
                    style={{
                      width: '24px',
                      height: '24px',
                      backgroundColor: color,
                      border: form.getFieldValue('color') === color ? '2px solid #1890ff' : '1px solid #d9d9d9',
                      borderRadius: '4px',
                      cursor: 'pointer'
                    }}
                    onClick={() => form.setFieldsValue({ color })}
                  />
                ))}
              </div>
            </div>
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={handleCancel}>
                Cancel
              </Button>
              <Button type="primary" htmlType="submit">
                {editingCategory ? 'Update' : 'Create'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CategoryManager;