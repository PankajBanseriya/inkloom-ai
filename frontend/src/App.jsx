import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext';
import { AuthProvider } from './context/AuthContext';
import AppLayout from './components/layout/AppLayout';
import ProtectedRoute from './features/auth/ProtectedRoute';
import { ROUTES } from './constants/routes';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import BrandWorkspacesPage from './pages/BrandWorkspacesPage';
import BrandGuidelinesPage from './pages/BrandGuidelinesPage';
import ApprovedSourcesPage from './pages/ApprovedSourcesPage';
import ApprovedClaimsLedgerPage from './pages/ApprovedClaimsLedgerPage';
import ContentReviewPage from './pages/ContentReviewPage';
import ReviewFindingsPage from './pages/ReviewFindingsPage';
import IntentLockPage from './pages/IntentLockPage';
import ApprovalQueuePage from './pages/ApprovalQueuePage';
import RevisionHistoryPage from './pages/RevisionHistoryPage';
import ClarityHeatmapPage from './pages/ClarityHeatmapPage';
import TeamManagementPage from './pages/TeamManagementPage';
import SettingsPage from './pages/SettingsPage';
import NotFoundPage from './pages/NotFoundPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            {/* Public Auth Routes */}
            <Route path={ROUTES.LOGIN} element={<LoginPage />} />
            <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

            {/* Protected Workspace Layout */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Navigate to={ROUTES.DASHBOARD} replace />} />
              <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
              <Route path={ROUTES.BRANDS} element={<BrandWorkspacesPage />} />
              <Route path={ROUTES.GUIDELINES} element={<BrandGuidelinesPage />} />
              <Route path={ROUTES.SOURCES} element={<ApprovedSourcesPage />} />
              <Route path={ROUTES.CLAIMS} element={<ApprovedClaimsLedgerPage />} />
              <Route path={ROUTES.REVIEW_EDITOR} element={<ContentReviewPage />} />
              <Route path={ROUTES.FINDINGS} element={<ReviewFindingsPage />} />
              <Route path={ROUTES.REVIEW_FINDINGS} element={<ReviewFindingsPage />} />
              <Route path={ROUTES.INTENT_LOCK} element={<IntentLockPage />} />
              <Route path={ROUTES.REVIEW_INTENT_LOCK} element={<IntentLockPage />} />
              <Route path={ROUTES.APPROVALS} element={<ApprovalQueuePage />} />
              <Route path={ROUTES.REVISIONS} element={<RevisionHistoryPage />} />
              <Route path={ROUTES.REVIEW_REVISIONS} element={<RevisionHistoryPage />} />
              <Route path={ROUTES.HEATMAP} element={<ClarityHeatmapPage />} />
              <Route path={ROUTES.TEAM} element={<TeamManagementPage />} />
              <Route path={ROUTES.SETTINGS} element={<SettingsPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
