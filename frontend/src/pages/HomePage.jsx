import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import '../styles/HomePage.css';

function HomePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }

    const storedUser = authService.getStoredUser();
    setUser(storedUser);
    setLoading(false);
  }, [navigate]);

  const handleLogout = async () => {
    if (window.confirm('Bạn có chắc muốn đăng xuất?')) {
      await authService.logout();
      navigate('/login');
    }
  };

  if (loading) {
    return (
      <div className="home-container">
        <div className="loading">Đang tải...</div>
      </div>
    );
  }

  return (
    <div className="home-container">
      <header className="home-header">
        <div className="header-content">
          <h1>Hệ thống Quản lý Khoa CNTT</h1>
          <button onClick={handleLogout} className="logout-button">
            Đăng xuất
          </button>
        </div>
      </header>

      <main className="home-content">
        <div className="welcome-card">
          <h2>🎉 Đăng nhập thành công!</h2>
          <p className="welcome-text">
            Chào mừng bạn đến với Hệ thống Quản lý Khoa Công nghệ Thông tin
          </p>
        </div>

        {user && (
          <div className="info-card">
            <h3>Thông tin tài khoản</h3>
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">Họ tên:</span>
                <span className="info-value">{user.fullName}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Username:</span>
                <span className="info-value">{user.username}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Email:</span>
                <span className="info-value">{user.email}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Vai trò:</span>
                <span className="info-value role-badge">
                  {user.roleName}
                </span>
              </div>
            </div>
          </div>
        )}

        <div className="info-card">
          <h3>Thông tin Token (Testing)</h3>
          <div className="token-info">
            <p className="token-label">Access Token:</p>
            <div className="token-value">
              {localStorage.getItem('accessToken')?.substring(0, 50)}...
            </div>
            <p className="token-status">
              ✅ Token đang hoạt động và được gửi kèm mỗi API request
            </p>
          </div>
        </div>

        <div className="features-card">
          <h3>Chức năng sẽ có (Coming Soon)</h3>
          <div className="features-grid">
            <div className="feature-item disabled">
              <span className="feature-icon">👥</span>
              <span className="feature-name">Quản lý Users</span>
            </div>
            <div className="feature-item disabled">
              <span className="feature-icon">🎓</span>
              <span className="feature-name">Quản lý Sinh viên</span>
            </div>
            <div className="feature-item disabled">
              <span className="feature-icon">📚</span>
              <span className="feature-name">Đăng ký học</span>
            </div>
            <div className="feature-item disabled">
              <span className="feature-icon">📊</span>
              <span className="feature-name">Quản lý điểm</span>
            </div>
            <div className="feature-item disabled">
              <span className="feature-icon">📄</span>
              <span className="feature-name">Tài liệu</span>
            </div>
            <div className="feature-item disabled">
              <span className="feature-icon">💬</span>
              <span className="feature-name">Chat</span>
            </div>
          </div>
        </div>
      </main>

      <footer className="home-footer">
        <p>Giai đoạn 1: Authentication ✅ Hoàn thành</p>
      </footer>
    </div>
  );
}

export default HomePage;