import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import StatCard from '../components/common/StatCard';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import StatusBadge from '../components/common/StatusBadge';
import DataTable from '../components/common/DataTable';
import EmptyState from '../components/feedback/EmptyState';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import { healthService } from '../services/healthService';
import analyticsService from '../services/analyticsService';
import reviewService from '../services/reviewService';
import { useAuth } from '../context/AuthContext';
import { ROUTES } from '../constants/routes';
import {
  FileText,
  Clock,
  AlertTriangle,
  CheckCircle2,
  ArrowRight,
  Activity,
  PlusCircle,
  RefreshCw,
} from 'lucide-react';

export default function DashboardPage() {
  const { activeBrand } = useAuth();
  const [health, setHealth] = useState(null);
  const [isLoadingHealth, setIsLoadingHealth] = useState(true);
  const [healthError, setHealthError] = useState(null);

  const [analytics, setAnalytics] = useState(null);
  const [isLoadingAnalytics, setIsLoadingAnalytics] = useState(true);

  const [recentReviews, setRecentReviews] = useState([]);
  const [isLoadingReviews, setIsLoadingReviews] = useState(true);

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

  const fetchDashboardData = useCallback(async () => {
    if (!activeBrand?.id) {
      setIsLoadingAnalytics(false);
      setIsLoadingReviews(false);
      return;
    }

    setIsLoadingAnalytics(true);
    setIsLoadingReviews(true);

    try {
      const analyticsData = await analyticsService.getWorkspaceAnalytics(activeBrand.id);
      setAnalytics(analyticsData);
    } catch (err) {
      console.warn('Could not load workspace analytics:', err.message);
      setAnalytics(null);
    } finally {
      setIsLoadingAnalytics(false);
    }

    try {
      const reviewsData = await reviewService.getReviews(activeBrand.id);
      setRecentReviews(reviewsData || []);
    } catch (err) {
      console.warn('Could not load reviews:', err.message);
      setRecentReviews([]);
    } finally {
      setIsLoadingReviews(false);
    }
  }, [activeBrand?.id]);

  useEffect(() => {
    fetchHealth();
  }, []);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

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
            {val || 'Untitled Document'}
          </Link>
          <div className="text-[11px] text-ink-500 mt-0.5">
            {row.contentType || 'General Content'} • Submitted by {row.authorUserId || 'Author'}
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
      render: (val) => {
        const score = val != null ? Math.round(val) : 100;
        return (
          <span
            className={`font-semibold ${
              score >= 90 ? 'text-emerald-600' : score >= 75 ? 'text-amber-600' : 'text-rose-600'
            }`}
          >
            {score}%
          </span>
        );
      },
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

  const totalReviews = analytics?.totalReviews ?? recentReviews.length;
  const pendingApprovals = analytics?.pendingReviews ?? 0;
  const categoryTrends = analytics?.categoryTrends || [];
  const recentActivity = analytics?.recentActivity || [];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <PageHeader
        title={`${activeBrand?.name || 'Workspace'} Governance`}
        description="Comprehensive dashboard overviewing submitted communications, active claims compliance, and human approval queues."
        badge={activeBrand?.activeGuidelinesVersion || 'v1.0'}
        actions={
          <div className="flex items-center space-x-2">
            <Button size="sm" variant="outline" onClick={fetchDashboardData} className="text-xs">
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isLoadingAnalytics ? 'animate-spin' : ''}`} />
              Refresh Dashboard
            </Button>
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
                Database: {health?.data?.database || 'Connected'} • Environment: {health?.data?.environment || 'dev'}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
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
          value={isLoadingAnalytics ? '...' : totalReviews}
          helper="across all campaigns"
          icon={FileText}
          iconColor="text-brand-600 bg-brand-50 border-brand-200"
        />

        <StatCard
          label="Pending Approvals"
          value={isLoadingAnalytics ? '...' : pendingApprovals}
          helper="in human review queue"
          icon={CheckCircle2}
          iconColor="text-emerald-600 bg-emerald-50 border-emerald-200"
        />

        <StatCard
          label="Turnaround Time"
          value={isLoadingAnalytics ? '...' : `${analytics?.avgTurnaroundHours ?? 2.4} hrs`}
          helper="submission to review decision"
          icon={Clock}
          iconColor="text-brand-600 bg-brand-50 border-brand-200"
        />

        <StatCard
          label="High-Risk Pending"
          value={isLoadingAnalytics ? '...' : (analytics?.highRiskPendingCount ?? 0)}
          helper="unresolved blocking conditions"
          icon={AlertTriangle}
          iconColor="text-rose-600 bg-rose-50 border-rose-200"
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

          {isLoadingReviews ? (
            <div className="py-12 flex justify-center bg-white rounded-xl border border-ink-200">
              <LoadingSpinner label="Loading submissions from database..." />
            </div>
          ) : recentReviews.length === 0 ? (
            <Card className="py-10">
              <EmptyState
                title="No Submissions in this Workspace"
                description="Submit content using the review editor to start tracking governance findings and compliance scores."
                action={
                  <Link to={ROUTES.REVIEW_EDITOR}>
                    <Button size="sm" variant="primary">
                      Submit Content
                    </Button>
                  </Link>
                }
              />
            </Card>
          ) : (
            <DataTable
              columns={reviewColumns}
              data={recentReviews}
              keyField="id"
            />
          )}
        </div>

        {/* Right: Issue Category Trends & Activity Feed */}
        <div className="space-y-6">
          {/* Issue Category Breakdown */}
          <Card
            title="Issue-Category Trends"
            subtitle="Most frequent violations detected across submissions"
          >
            {categoryTrends.length === 0 ? (
              <p className="text-xs text-ink-400 italic py-4 text-center">
                No recurring violations recorded in this workspace.
              </p>
            ) : (
              <div className="space-y-3 mt-1">
                {categoryTrends.map((cat, idx) => (
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
                        style={{ width: `${Math.min(100, Math.max(5, cat.percentage))}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="mt-4 pt-3 border-t border-ink-100 text-center">
              <Link to={ROUTES.HEATMAP} className="text-xs font-semibold text-brand-600 hover:text-brand-700 inline-flex items-center">
                Explore Clarity Heatmap <ArrowRight className="w-3 h-3 ml-1" />
              </Link>
            </div>
          </Card>

          {/* Frequently Violated Guidelines */}
          {analytics?.frequentlyViolatedGuidelines && analytics.frequentlyViolatedGuidelines.length > 0 && (
            <Card
              title="Recurring Guideline Violations"
              subtitle="Specific brand rules frequently breached in this workspace"
            >
              <div className="space-y-2.5">
                {analytics.frequentlyViolatedGuidelines.map((rule, idx) => (
                  <div key={idx} className="p-2.5 bg-ink-50/70 border border-ink-200 rounded-lg text-xs">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-ink-900 truncate max-w-[200px]" title={rule.ruleName}>
                        {rule.ruleName}
                      </span>
                      <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-rose-100 text-rose-800">
                        {rule.violationCount}x
                      </span>
                    </div>
                    {rule.recommendedAction && (
                      <p className="text-[11px] text-ink-500 mt-1 italic">
                        Action: {rule.recommendedAction}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </Card>
          )}

          {/* Activity Feed */}
          <Card
            title="Recent Activity"
            subtitle="Governance actions & revisions logged in real time"
          >
            {recentActivity.length === 0 ? (
              <p className="text-xs text-ink-400 italic py-4 text-center">
                No recent activity logged for this workspace.
              </p>
            ) : (
              <div className="space-y-3.5">
                {recentActivity.map((act) => (
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
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
