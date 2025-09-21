import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import { DashboardOutlined, UploadOutlined, BarChartOutlined, SettingOutlined } from '@ant-design/icons';
import { SpeedInsights } from '@vercel/speed-insights/react';
import { Analytics } from '@vercel/analytics/react';
import Dashboard from './components/Dashboard';
import FileUpload from './components/FileUpload';
import TransactionList from './components/TransactionList';
import './App.css';

const { Header, Content, Sider } = Layout;

function App() {
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
    }
  ];

  return (
    <Router>
      <SpeedInsights />
      <Analytics />
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
            defaultSelectedKeys={['1']}
            items={menuItems.map(item => ({
              key: item.key,
              icon: item.icon,
              label: (
                <a href={item.path} onClick={(e) => {
                  e.preventDefault();
                  window.location.href = item.path;
                }}>
                  {item.label}
                </a>
              )
            }))}
          />
        </Sider>
        <Layout>
          <Header style={{ 
            padding: 0, 
            background: '#fff',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
          }}>
            <div style={{ 
              padding: '0 24px',
              fontSize: '18px',
              fontWeight: 'bold',
              color: '#001529'
            }}>
              Personal Finance Dashboard
            </div>
          </Header>
          <Content style={{ margin: '24px 16px 0' }}>
            <div style={{ padding: 24, minHeight: 360, background: '#fff', borderRadius: 8 }}>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/upload" element={<FileUpload />} />
                <Route path="/transactions" element={<TransactionList />} />
              </Routes>
            </div>
          </Content>
        </Layout>
      </Layout>
    </Router>
  );
}

export default App;
