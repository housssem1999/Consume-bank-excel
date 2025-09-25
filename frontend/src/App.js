import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown, Avatar } from 'antd';
import { DashboardOutlined, UploadOutlined, BarChartOutlined, SettingOutlined, UserOutlined, LogoutOutlined, ContactsOutlined, RobotOutlined } from '@ant-design/icons';
import { SpeedInsights } from '@vercel/speed-insights/react';
import { Analytics } from '@vercel/analytics/react';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import FileUpload from './components/FileUpload';
import TransactionList from './components/TransactionList';
import CategoryManager from './components/CategoryManager';
import MLAnalytics from './components/MLAnalytics';
import Contact from './components/Contact';
import './App.css';

const { Header, Content, Sider } = Layout;

const AuthenticatedApp = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const menuItems = [
    {
      key: '1',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
      path: '/'
    },
    {
      key: '2',
      icon: <UploadOutlined />,
      label: 'Upload Data',
      path: '/upload'
    },
    {
      key: '3',
      icon: <BarChartOutlined />,
      label: 'Transactions',
      path: '/transactions'
    },
    {
      key: '4',
      icon: <RobotOutlined />,
      label: 'AI Analytics',
      path: '/ml-analytics'
    },
    {
      key: '5',
      icon: <SettingOutlined />,
      label: 'Categories',
      path: '/categories'
    },
    {
      key: '6',
      icon: <ContactsOutlined />,
      label: 'Contact',
      path: '/contact'
    }
  ];

  // Function to get the selected menu key based on current path
  const getSelectedKey = () => {
    const currentPath = location.pathname;
    const menuItem = menuItems.find(item => item.path === currentPath);
    return menuItem ? [menuItem.key] : ['1']; // Default to Dashboard if no match
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: logout
    }
  ];

  const handleMenuClick = (e) => {
    if (e.key === 'logout') {
      logout();
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        breakpoint="lg"
        collapsedWidth="0"
        onBreakpoint={(broken) => {
          console.log(broken);
        }}
        onCollapse={(collapsed, type) => {
          console.log(collapsed, type);
        }}
      >
        <div className="logo" style={{ 
          height: 32, 
          margin: 16, 
          background: 'rgba(255, 255, 255, 0.3)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontWeight: 'bold'
        }}>
          Finance Dashboard
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKey()}
          items={menuItems.map(item => ({
            key: item.key,
            icon: item.icon,
            label: (
              <span onClick={() => navigate(item.path)}>
                {item.label}
              </span>
            )
          }))}
        />
      </Sider>
      <Layout>
        <Header style={{ 
          padding: '0 24px', 
          background: '#fff',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div style={{ 
            fontSize: '18px',
            fontWeight: 'bold',
            color: '#001529'
          }}>
            Personal Finance Dashboard
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <span style={{ color: '#666' }}>
              Welcome, {user?.firstName || user?.username || 'User'}!
            </span>
            <Dropdown 
              menu={{ items: userMenuItems, onClick: handleMenuClick }}
              placement="bottomRight"
            >
              <Button 
                type="text" 
                style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '8px',
                  height: '40px'
                }}
              >
                <Avatar 
                  size="small" 
                  icon={<UserOutlined />} 
                  style={{ backgroundColor: '#1890ff' }}
                />
                <span>{user?.username}</span>
              </Button>
            </Dropdown>
          </div>
        </Header>
        <Content style={{ margin: '24px 16px 0' }}>
          <div style={{ padding: 24, minHeight: 360, background: '#fff', borderRadius: 8 }}>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/upload" element={<FileUpload />} />
              <Route path="/transactions" element={<TransactionList />} />
              <Route path="/ml-analytics" element={<MLAnalytics />} />
              <Route path="/categories" element={<CategoryManager />} />
              <Route path="/contact" element={<Contact />} />
            </Routes>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

function App() {
  return (
    <Router>
      <SpeedInsights />
      <Analytics />
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/*" element={
            <ProtectedRoute>
              <AuthenticatedApp />
            </ProtectedRoute>
          } />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
