import React from 'react';
import { Card, Typography, Space, Avatar, Divider, Button, message } from 'antd';
import { 
  LinkedinOutlined, 
  GithubOutlined, 
  UserOutlined,
  MailOutlined 
} from '@ant-design/icons';

const { Title, Paragraph, Text } = Typography;

const Contact = () => {
  const adminEmail = "mhoussem789@gmail.com";

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(adminEmail);
      message.success("Admin email copied to clipboard!");
    } catch (err) {
      message.error("Failed to copy email.");
    }
  };
  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>Contact & About</Title>
      <Paragraph>
        Get to know more about the developer and connect with me on social platforms.
      </Paragraph>

      <Card
        style={{ 
          maxWidth: 800, 
          margin: '0 auto',
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <Avatar 
            size={120} 
            icon={<UserOutlined />}
            style={{ 
              backgroundColor: '#1890ff',
              marginBottom: '16px'
            }}
          />
          <Title level={3} style={{ marginBottom: '8px' }}>
            Houssem
          </Title>
          <Text type="secondary" style={{ fontSize: '16px' }}>
            Full Stack Developer & Finance Technology Enthusiast
          </Text>
        </div>

        <Divider />

        <div style={{ marginBottom: '24px' }}>
          <Title level={4}>About Me</Title>
          <Paragraph>
            I'm a passionate developer focused on creating practical financial management solutions. 
            This Personal Finance Dashboard was built to help individuals take control of their finances 
            through automated transaction processing and insightful data visualization.
          </Paragraph>
          <Paragraph>
            The application leverages modern technologies including Spring Boot for the backend 
            and React with Ant Design for a seamless user experience. I believe in building 
            tools that make financial management accessible and efficient for everyone.
          </Paragraph>
        </div>

        <Divider />

        <div style={{ marginBottom: '24px' }}>
          <Title level={4}>Technical Skills</Title>
          <Paragraph>
            <Text strong>Backend:</Text> Java, Spring Boot, REST APIs, Database Design
          </Paragraph>
          <Paragraph>
            <Text strong>Frontend:</Text> React, JavaScript, HTML/CSS, Ant Design, Chart.js
          </Paragraph>
          <Paragraph>
            <Text strong>Tools & Technologies:</Text> Maven, Git, Excel Processing (Apache POI), Postgres Database
          </Paragraph>
        </div>

        <Divider />

        <div style={{ textAlign: 'center' }}>
          <Title level={4} style={{ marginBottom: '16px' }}>
            Connect With Me
          </Title>
          <Space size="large">
            <Button
              type="primary"
              icon={<LinkedinOutlined />}
              size="large"
              href="https://linkedin.com/in/houssem-moussa"
              target="_blank"
              rel="noopener noreferrer"
              style={{ 
                backgroundColor: '#0077b5',
                borderColor: '#0077b5'
              }}
            >
              LinkedIn Profile
            </Button>
            <Button
              icon={<GithubOutlined />}
              size="large"
              href="https://github.com/housssem1999"
              target="_blank"
              rel="noopener noreferrer"
              style={{
                backgroundColor: '#24292e',
                borderColor: '#24292e',
                color: 'white'
              }}
            >
              GitHub Profile
            </Button>
            <Button
              icon={<MailOutlined />}
              size="large"
              rel="noopener noreferrer"
              onClick={handleCopy}
              style={{
                backgroundColor: '#ea4335',
                borderColor: '#fbbc04',
                color: 'white'
              }}
            >
              Gmail
            </Button>
          </Space>
        </div>

        <Divider />

        <div style={{ textAlign: 'center', backgroundColor: '#f5f5f5', padding: '16px', borderRadius: '8px' }}>
          <Text type="secondary">
            <MailOutlined /> Feel free to reach out for collaboration opportunities or to discuss this project!
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default Contact;