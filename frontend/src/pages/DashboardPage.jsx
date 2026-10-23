import React, { useState, useEffect } from 'react';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { healthService } from '../services/healthService';
import { ShieldCheck, FileCheck, CheckCircle2, AlertTriangle, Activity } from 'lucide-react';

export default function DashboardPage() {
  const [health, setHealth] = useState(null);
  const [isLoadingHealth, setIsLoadingHealth] = useState(true);
  const [healthError, setHealthError] = useState(null);

  const fetchHealth = async () => {
    setIsLoadingHealth(true);
    setHealthError(null);
    try {
      const response = await healthService.checkHealth();
      setHealth(response);
    } catch (err) {
      setHealthError(err.message || 'Unable to reach backend service');
    } finally {
      setIsLoadingHealth(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="space-y-8">
      {/* Page Title & Welcome Banner */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink-900">Brand Governance Dashboard</h1>
          <p className="text-sm text-ink-500 mt-1">
            Monitoring active brand compliance, approved claims ledger, and human-in-the-loop review queues.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Badge variant="brand" size="md">Phase 1 Foundation</Badge>
          <Button size="sm" variant="outline" onClick={fetchHealth}>
            Refresh Health
          </Button>
        </div>
      </div>

      {/* Backend Health & Connectivity Card */}
      <Card
        title="Backend Connectivity Status"
        subtitle="Verifies real REST API communication with Spring Boot (/api/v1/health)"
      >
        {isLoadingHealth ? (
          <LoadingSpinner size="sm" message="Querying Spring Boot /api/v1/health endpoint..." />
        ) : healthError ? (
          <ErrorMessage
            title="Backend Offline or Unreachable"
            message={`${healthError}. Verify the Spring Boot backend is running on port 8080.`}
            onRetry={fetchHealth}
          />
        ) : (
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-lg bg-emerald-50/60 border border-emerald-200">
            <div className="flex items-center space-x-3">
              <div className="w-9 h-9 rounded-full bg-emerald-100 border border-emerald-300 flex items-center justify-center text-emerald-700">
                <Activity className="w-5 h-5" />
              </div>
              <div>
                <div className="text-sm font-semibold text-emerald-900">
                  {health?.data?.status || 'HEALTHY'} — {health?.data?.application || 'Inkloom AI Backend'}
                </div>
                <div className="text-xs text-emerald-700">
                  Timestamp: {health?.data?.timestamp || new Date().toISOString()} • Environment: {health?.data?.environment || 'dev'}
                </div>
              </div>
            </div>
            <Badge variant="success" size="md">REST API Connected</Badge>
          </div>
        )}
      </Card>

      {/* Overview Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="hover:border-ink-300 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-ink-500">Guidelines Version</span>
            <span className="p-2 rounded-lg bg-brand-50 text-brand-600 border border-brand-200">
              <ShieldCheck className="w-4 h-4" />
            </span>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-ink-900">v2.4</div>
            <p className="text-xs text-ink-500 mt-0.5">38 active rules & tone constraints</p>
          </div>
        </Card>

        <Card className="hover:border-ink-300 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-ink-500">Approved Claims</span>
            <span className="p-2 rounded-lg bg-emerald-50 text-emerald-600 border border-emerald-200">
              <CheckCircle2 className="w-4 h-4" />
            </span>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-ink-900">124</div>
            <p className="text-xs text-emerald-600 mt-0.5">100% verified with citations</p>
          </div>
        </Card>

        <Card className="hover:border-ink-300 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-ink-500">Pending Approvals</span>
            <span className="p-2 rounded-lg bg-amber-50 text-amber-600 border border-amber-200">
              <FileCheck className="w-4 h-4" />
            </span>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-ink-900">7</div>
            <p className="text-xs text-amber-700 mt-0.5">Awaiting human reviewer sign-off</p>
          </div>
        </Card>

        <Card className="hover:border-ink-300 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-ink-500">Compliance Rate</span>
            <span className="p-2 rounded-lg bg-ink-100 text-ink-600 border border-ink-200">
              <AlertTriangle className="w-4 h-4" />
            </span>
          </div>
          <div className="mt-3">
            <div className="text-2xl font-bold text-ink-900">94.8%</div>
            <p className="text-xs text-ink-500 mt-0.5">+3.2% from last cycle</p>
          </div>
        </Card>
      </div>

      {/* Foundation Architecture Notice */}
      <div className="p-6 rounded-xl bg-ink-900 text-white shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div>
          <Badge variant="brand" size="sm" className="bg-brand-500/20 text-brand-300 border-brand-500/40 mb-2">
            Architecture Milestone
          </Badge>
          <h2 className="text-lg font-bold text-white">Phase 1 Foundation Operational</h2>
          <p className="text-xs text-ink-300 mt-1 max-w-2xl leading-relaxed">
            The foundation layer establishes the React + Vite single page frontend with strict JavaScript/JSX, centralized API client, reusable design components, and Java 21+ Spring Boot MVC backend ready for MongoDB persistence in Phase 2.
          </p>
        </div>
      </div>
    </div>
  );
}
