import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import InstructionSubmissionPage from './pages/InstructionSubmissionPage';
import PendingApprovalsPage from './pages/PendingApprovalsPage';
import TaskHistoryPage from './pages/TaskHistoryPage';
import SettingsPage from './pages/SettingsPage';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';

function App() {
  const { user, logout } = useAuth();

  return (
    <div>
      <header className="navbar">
        <div>
          <strong>AI Trimmer Control Center</strong>
        </div>
        <nav className="nav-links">
          <NavLink to="/submit">Submit Instruction</NavLink>
          <NavLink to="/pending">Pending Approvals</NavLink>
          <NavLink to="/history">Task History</NavLink>
          <NavLink to="/settings">Settings</NavLink>
        </nav>
        <div>
          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span className="badge">{user.role.toUpperCase()}</span>
              <button className="primary" onClick={logout} type="button">
                Sign out
              </button>
            </div>
          ) : (
            <NavLink to="/login">Sign in</NavLink>
          )}
        </div>
      </header>
      <main>
        <div className="container">
          <Routes>
            <Route element={<ProtectedRoute allowedRoles={['creator', 'approver', 'admin']} />}>
              <Route path="/" element={<InstructionSubmissionPage />} />
              <Route path="/submit" element={<InstructionSubmissionPage />} />
              <Route path="/history" element={<TaskHistoryPage />} />
            </Route>
            <Route element={<ProtectedRoute allowedRoles={['approver', 'admin']} />}>
              <Route path="/pending" element={<PendingApprovalsPage />} />
            </Route>
            <Route element={<ProtectedRoute allowedRoles={['admin']} />}>
              <Route path="/settings" element={<SettingsPage />} />
            </Route>
            <Route path="/login" element={<LoginPage />} />
            <Route path="*" element={<Navigate to="/submit" replace />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

export default App;
