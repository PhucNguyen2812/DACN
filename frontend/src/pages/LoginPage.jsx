import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import '../styles/LoginPage.css';

function LoginPage() {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');

  useEffect(() => {
    if (authService.isAuthenticated()) {
      navigate('/home');
    }
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!formData.username.trim() || !formData.password.trim()) {
      setError('Vui lòng nhập đầy đủ thông tin');
      setLoading(false);
      return;
    }

    try {
      const response = await authService.login(formData.username, formData.password);
      
      if (response.success) {
        navigate('/home');
      } else {
        setError(response.message || 'Đăng nhập thất bại');
      }
    } catch (err) {
      setError(err.message || 'Username hoặc password không đúng');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    if (!forgotEmail.trim()) {
      alert('Vui lòng nhập email');
      return;
    }
    
    alert('Chức năng đặt lại mật khẩu đang được phát triển.\nVui lòng liên hệ admin để được hỗ trợ.');
    setShowForgotPassword(false);
    setForgotEmail('');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>Đăng Nhập</h1>
          <p>Hệ thống Quản lý Khoa CNTT</p>
        </div>

        {error && (
          <div className="error-message">
            <span>⚠️ {error}</span>
          </div>
        )}

        {!showForgotPassword ? (
          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-group">
              <label htmlFor="username">Tên đăng nhập</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Nhập username hoặc mã sinh viên"
                disabled={loading}
                autoFocus
              />
            </div>

            <div className="form-group">
              <label htmlFor="password">Mật khẩu</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Nhập mật khẩu"
                disabled={loading}
              />
            </div>

            <div className="form-footer">
              <button
                type="button"
                className="forgot-password-link"
                onClick={() => setShowForgotPassword(true)}
              >
                Quên mật khẩu?
              </button>
            </div>

            <button type="submit" className="login-button" disabled={loading}>
              {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleForgotPassword} className="login-form">
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
                value={forgotEmail}
                onChange={(e) => setForgotEmail(e.target.value)}
                placeholder="Nhập email của bạn"
                autoFocus
              />
            </div>

            <div className="forgot-buttons">
              <button type="submit" className="login-button">
                Gửi yêu cầu
              </button>
              <button
                type="button"
                className="back-button"
                onClick={() => {
                  setShowForgotPassword(false);
                  setForgotEmail('');
                }}
              >
                Quay lại
              </button>
            </div>
          </form>
        )}

        <div className="login-footer">
          <p>© 2025 Khoa Công nghệ Thông tin</p>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;