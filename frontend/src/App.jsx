import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/layout/AppLayout';
import DashboardPage from './pages/DashboardPage';
import NotFoundPage from './pages/NotFoundPage';
import { ROUTES } from './constants/routes';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Navigate to={ROUTES.DASHBOARD} replace />} />
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
          {/* Placeholder routes mapped to Dashboard until feature phases */}
          <Route path={ROUTES.GUIDELINES} element={<DashboardPage />} />
          <Route path={ROUTES.CLAIMS} element={<DashboardPage />} />
          <Route path={ROUTES.SOURCES} element={<DashboardPage />} />
          <Route path={ROUTES.REVIEWS} element={<DashboardPage />} />
          <Route path={ROUTES.APPROVALS} element={<DashboardPage />} />
          <Route path={ROUTES.HEATMAP} element={<DashboardPage />} />
          <Route path={ROUTES.SETTINGS} element={<DashboardPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
