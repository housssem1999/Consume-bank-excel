import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, Space, Divider } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, UserAddOutlined } from '@ant-design/icons';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, Link } from 'react-router-dom';

const { Title, Text } = Typography;

const Register = () => {
  const [form] = Form.useForm();
  const { register, loading } = useAuth();
  const navigate = useNavigate();
  const [registerError, setRegisterError] = useState('');

  const onFinish = async (values) => {
    setRegisterError('');
    const result = await register({
      username: values.username,
      email: values.email,
      password: values.password,
      firstName: values.firstName,
      lastName: values.lastName
    });

    if (result.success) {
      navigate('/login');
    } else {
      setRegisterError(result.error);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      padding: '20px'
    }}>
      <Card
        style={{
          width: '100%',
          maxWidth: 500,
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
          borderRadius: '12px'
        }}
        bodyStyle={{ padding: '40px 32px' }}
      >
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <UserAddOutlined style={{ fontSize: '48px', color: '#1890ff', marginBottom: '16px' }} />
          <Title level={2} style={{ margin: '0 0 8px 0', color: '#001529' }}>
            Create Account
          </Title>
          <Text type="secondary">
            Join Personal Finance Dashboard today
          </Text>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={onFinish}
          layout="vertical"
          size="large"
        >
          <Space.Compact style={{ width: '100%', marginBottom: '16px' }}>
            <Form.Item
              name="firstName"
              label="First Name"
              style={{ width: '50%', marginRight: '8px' }}
              rules={[
                {
                  max: 50,
                  message: 'First name must be less than 50 characters!',
                },
              ]}
            >
              <Input placeholder="First Name" />
            </Form.Item>

            <Form.Item
              name="lastName"
              label="Last Name"
              style={{ width: '50%', marginLeft: '8px' }}
              rules={[
                {
                  max: 50,
                  message: 'Last name must be less than 50 characters!',
                },
              ]}
            >
              <Input placeholder="Last Name" />
            </Form.Item>
          </Space.Compact>

          <Form.Item
            name="username"
            label="Username"
            rules={[
              {
                required: true,
                message: 'Please input your username!',
              },
              {
                min: 3,
                max: 20,
                message: 'Username must be between 3 and 20 characters!',
              },
            ]}
          >
            <Input
              prefix={<UserOutlined className="site-form-item-icon" />}
              placeholder="Username"
            />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[
              {
                required: true,
                message: 'Please input your email!',
              },
              {
                type: 'email',
                message: 'Please enter a valid email address!',
              },
              {
                max: 50,
                message: 'Email must be less than 50 characters!',
              },
            ]}
          >
            <Input
              prefix={<MailOutlined className="site-form-item-icon" />}
              placeholder="Email"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="Password"
            rules={[
              {
                required: true,
                message: 'Please input your password!',
              },
              {
                min: 6,
                max: 40,
                message: 'Password must be between 6 and 40 characters!',
              },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined className="site-form-item-icon" />}
              placeholder="Password"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Confirm Password"
            dependencies={['password']}
            rules={[
              {
                required: true,
                message: 'Please confirm your password!',
              },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('The two passwords do not match!'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined className="site-form-item-icon" />}
              placeholder="Confirm Password"
            />
          </Form.Item>

          {registerError && (
            <div style={{ marginBottom: '16px' }}>
              <Text type="danger">{registerError}</Text>
            </div>
          )}

          <Form.Item style={{ marginBottom: '16px' }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              style={{
                height: '44px',
                borderRadius: '6px',
                fontSize: '16px',
                fontWeight: '500'
              }}
            >
              Create Account
            </Button>
          </Form.Item>

          <Divider plain>
            <Text type="secondary">Already have an account?</Text>
          </Divider>

          <div style={{ textAlign: 'center' }}>
            <Link to="/login">
              <Button type="link" style={{ padding: 0, fontSize: '16px' }}>
                Sign In
              </Button>
            </Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Register;