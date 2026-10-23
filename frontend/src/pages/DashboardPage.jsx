import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import StatCard from '../components/common/StatCard';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import StatusBadge from '../components/common/StatusBadge';
import DataTable from '../components/common/DataTable';
import { healthService } from '../services/healthService';
import { useAuth } from '../context/AuthContext';
import { ROUTES } from '../constants/routes';
import {
  MOCK_DASHBOARD_METRICS,
  MOCK_REVIEWS,
} from '../mock/mockData';
import {
  FileText,
  Clock,
  AlertTriangle,
  CheckCircle2,
  ArrowRight,
  Activity,
  PlusCircle,
} from 'lucide-react';

export default function DashboardPage() {
  const { activeBrand } = useAuth();
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

  const reviewColumns = [
    {
      key: 'title',
      header: 'Review Submission',
      render: (val, row) => (
        <div>
          <Link
            to={`/reviews/${row.id}/findings`}
            className="font-semibold text-ink-900 hover:text-brand-600 transition-colors"
          >
            {val}
          </Link>
          <div className="text-[11px] text-ink-500 mt-0.5">
            {row.contentType} • Submitted by {row.author}
          </div>
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'complianceScore',
      header: 'Compliance',
      render: (val) => (
        <span
          className={`font-semibold ${
            val >= 90 ? 'text-emerald-600' : val >= 75 ? 'text-amber-600' : 'text-rose-600'
          }`}
        >
          {val}%
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Action',
      className: 'text-right',
      render: (_, row) => (
        <Link to={`/reviews/${row.id}/findings`}>
          <Button size="sm" variant="outline" className="text-xs">
            Inspect Findings
          </Button>
        </Link>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <PageHeader
        title={`${activeBrand?.name || 'Workspace'} Governance`}
        description="Comprehensive dashboard overviewing submitted communications, active claims compliance, and human approval queues."
        badge={activeBrand?.activeGuidelinesVersion || 'v3.2'}
        actions={
          <div className="flex items-center space-x-2">
            <Link to={ROUTES.REVIEW_EDITOR}>
              <Button size="sm" variant="primary" className="font-semibold text-xs">
                <PlusCircle className="w-4 h-4 mr-1.5" />
                Submit Content for Review
              </Button>
            </Link>
          </div>
        }
      />

      {/* Backend API Live Status Banner */}
      <Card className="border-ink-200">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-full bg-emerald-50 border border-emerald-200 flex items-center justify-center text-emerald-600 flex-shrink-0">
              <Activity className="w-4 h-4" />
            </div>
            <div>
              <div className="font-semibold text-ink-900">
                Spring Boot REST Backend Status:{' '}
                {isLoadingHealth ? (
                  <span className="text-ink-400">Pinging /api/v1/health...</span>
                ) : healthError ? (
                  <span className="text-rose-600">Offline ({healthError})</span>
                ) : (
                  <span className="text-emerald-600 font-bold">{health?.data?.status || 'UP'} (Port 8080)</span>
                )}
              </div>
              <p className="text-ink-500 text-[11px] mt-0.5">
                Database: {health?.data?.database || 'Pending connection'} • Environment: {health?.data?.environment || 'dev'}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-[10px] text-ink-400 italic">
              (Development mock analytics active below)
            </span>
            <Button size="sm" variant="ghost" onClick={fetchHealth} className="text-xs py-1">
              Refresh Status
            </Button>
          </div>
        </div>
      </Card>

      {/* 4 Core Governance Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Total Reviews"
          value={MOCK_DASHBOARD_METRICS.totalReviews}
          change="+12 this week"
          isPositive={true}
          helper="across all campaigns"
          icon={FileText}
          iconColor="text-brand-600 bg-brand-50 border-brand-200"
        />

        <StatCard
          label="Pending Reviews"
          value={MOCK_DASHBOARD_METRICS.pendingReviews}
          change="-3 from yesterday"
          isPositive={true}
          helper="currently in pipeline"
          icon={Clock}
          iconColor="text-amber-600 bg-amber-50 border-amber-200"
        />

        <StatCard
          label="Issues Identified"
          value={MOCK_DASHBOARD_METRICS.issuesIdentified}
          change="-8.4%"
          isPositive={true}
          helper="violations flagged"
          icon={AlertTriangle}
          iconColor="text-rose-600 bg-rose-50 border-rose-200"
        />

        <StatCard
          label="Pending Approvals"
          value={MOCK_DASHBOARD_METRICS.pendingApprovals}
          change="Requires human action"
          isPositive={false}
          helper="in reviewer queue"
          icon={CheckCircle2}
          iconColor="text-emerald-600 bg-emerald-50 border-emerald-200"
        />
      </div>

      {/* Two-column layout: Recent Reviews & Issue Trends */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Recent Reviews Table (2 cols) */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-ink-900 tracking-tight">Recent Content Submissions</h2>
            <Link to={ROUTES.APPROVALS} className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center">
              View Approval Queue <ArrowRight className="w-3.5 h-3.5 ml-1" />
            </Link>
          </div>

          <DataTable
            columns={reviewColumns}
            data={MOCK_REVIEWS}
            keyField="id"
          />
        </div>

        {/* Right: Issue Category Trends & Activity Feed */}
        <div className="space-y-6">
          {/* Issue Category Breakdown */}
          <Card
            title="Issue-Category Trends"
            subtitle="Most frequent violations detected across submissions"
          >
            <div className="space-y-3 mt-1">
              {MOCK_DASHBOARD_METRICS.categoryTrends.map((cat, idx) => (
                <div key={idx} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-medium text-ink-800">{cat.category}</span>
                    <span className="font-bold text-ink-900">{cat.count} ({cat.percentage}%)</span>
                  </div>
                  <div className="w-full h-1.5 bg-ink-100 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${
                        cat.severity === 'CRITICAL' ? 'bg-rose-500' :
                        cat.severity === 'WARNING' ? 'bg-amber-500' : 'bg-brand-500'
                      }`}
                      style={{ width: `${cat.percentage}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-4 pt-3 border-t border-ink-100 text-center">
              <Link to={ROUTES.HEATMAP} className="text-xs font-semibold text-brand-600 hover:text-brand-700 inline-flex items-center">
                Explore Clarity Heatmap <ArrowRight className="w-3 h-3 ml-1" />
              </Link>
            </div>
          </Card>

          {/* Activity Feed */}
          <Card
            title="Recent Activity"
            subtitle="Governance actions & revisions logged in real time"
          >
            <div className="space-y-3.5">
              {MOCK_DASHBOARD_METRICS.recentActivity.map((act) => (
                <div key={act.id} className="flex items-start space-x-2.5 text-xs">
                  <span className="w-2 h-2 rounded-full bg-brand-500 mt-1.5 flex-shrink-0" />
                  <div className="flex-1">
                    <div className="text-ink-800">
                      <strong className="text-ink-900">{act.user}</strong> ({act.role}) {act.action.toLowerCase()}:{' '}
                      <span className="italic text-ink-700">&ldquo;{act.target}&rdquo;</span>
                    </div>
                    <span className="text-[10px] text-ink-400 mt-0.5 block">{act.timestamp}</span>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
