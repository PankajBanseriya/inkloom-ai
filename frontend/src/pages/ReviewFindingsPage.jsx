import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import StatusBadge from '../components/common/StatusBadge';
import FilterBar from '../components/common/FilterBar';
import EvidenceCard from '../features/evidence/EvidenceCard';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import { MOCK_REVIEWS } from '../mock/mockData';
import reviewService from '../services/reviewService';
import { ShieldAlert, ArrowLeft, GitCompare, Send } from 'lucide-react';

export default function ReviewFindingsPage() {
  const { reviewId } = useParams();
  const { success, info, error: toastError } = useToast();

  const [review, setReview] = useState(() => MOCK_REVIEWS.find((r) => r.id === reviewId) || MOCK_REVIEWS[0]);
  const [findings, setFindings] = useState(review.findings || []);
  const [selectedStrengths, setSelectedStrengths] = useState({
    'find-01': 'balanced',
    'find-02': 'balanced',
    'find-03': 'balanced',
    'find-04': 'balanced',
  });

  const [search, setSearch] = useState('');
  const [severityFilter, setSeverityFilter] = useState('ALL');

  useEffect(() => {
    if (reviewId) {
      reviewService.getReviewById(reviewId)
        .then((data) => {
          if (data) {
            setReview(data);
            setFindings(data.findings || []);
          }
        })
        .catch((err) => {
          console.warn('Could not load live review, using mock fallback:', err.message);
        });
    }
  }, [reviewId]);

  const handleSelectStrength = (id, str) => {
    setSelectedStrengths((prev) => ({ ...prev, [id]: str }));
  };

  const handleAccept = async (findingId, replacement) => {
    const tier = selectedStrengths[findingId] || 'balanced';
    if (reviewId) {
      try {
        await reviewService.acceptCorrection(reviewId, {
          findingId,
          replacementText: replacement,
          tier,
          rationale: `Accepted ${tier} Intent Lock fix from Evidence Card.`,
        });
      } catch (err) {
        toastError('Failed to apply correction', err.message);
        return;
      }
    }
    setFindings((prev) => prev.filter((f) => f.id !== findingId));
    success('Suggestion Accepted', `Applied ${tier} fix: "${replacement}"`);
  };

  const handleReject = async (findingId) => {
    if (reviewId) {
      try {
        await reviewService.rejectCorrection(reviewId, findingId, 'Author dismissed suggestion in Evidence view.');
      } catch (err) {
        console.warn('Could not record reject feedback:', err.message);
      }
    }
    setFindings((prev) => prev.filter((f) => f.id !== findingId));
    info('Finding Dismissed', `Preserved author wording for finding ${findingId}`);
  };

  const filtered = findings.filter((f) => {
    const matchesSearch =
      f.title.toLowerCase().includes(search.toLowerCase()) ||
      f.explanation.toLowerCase().includes(search.toLowerCase()) ||
      f.excerpt.toLowerCase().includes(search.toLowerCase());
    const matchesSeverity = severityFilter === 'ALL' || f.severity === severityFilter;
    return matchesSearch && matchesSeverity;
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-2 text-xs text-ink-500 mb-1">
        <Link to={ROUTES.DASHBOARD} className="hover:text-ink-800 flex items-center">
          <ArrowLeft className="w-3.5 h-3.5 mr-1" />
          Back to Dashboard
        </Link>
        <span>/</span>
        <span>Review Findings & Evidence</span>
      </div>

      <PageHeader
        title={review.title}
        description={`Content Type: ${review.contentType} • Submitted by ${review.author} on ${review.submissionDate}`}
        badge={`${review.complianceScore}% Compliance`}
        badgeVariant={review.complianceScore >= 80 ? 'brand' : 'warning'}
        actions={
          <div className="flex items-center space-x-2">
            <Link to={`/reviews/${review.id}/intent-lock`}>
              <Button size="sm" variant="outline" className="text-xs">
                <GitCompare className="w-3.5 h-3.5 mr-1.5" />
                Open Intent Lock Diff
              </Button>
            </Link>
            <Link to={ROUTES.APPROVALS}>
              <Button size="sm" variant="primary" className="text-xs">
                <Send className="w-3.5 h-3.5 mr-1.5" />
                Go to Approval Queue
              </Button>
            </Link>
          </div>
        }
      />

      {/* Review Summary Score Card */}
      <Card className="border-ink-200">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-brand-50 border border-brand-200 flex items-center justify-center font-bold text-brand-700 text-lg">
              {review.complianceScore}%
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h3 className="text-sm font-bold text-ink-900">Governance Evaluation Summary</h3>
                <StatusBadge status={review.status} />
              </div>
              <p className="text-xs text-ink-500 mt-0.5">
                {findings.length} pending findings require resolution before human sign-off.
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3 text-xs">
            <div className="text-center px-3 py-1.5 bg-rose-50 border border-rose-200 rounded-lg">
              <span className="text-[10px] uppercase font-bold text-rose-800 block">Critical</span>
              <span className="font-bold text-rose-900">
                {findings.filter((f) => f.severity === 'CRITICAL').length}
              </span>
            </div>
            <div className="text-center px-3 py-1.5 bg-amber-50 border border-amber-200 rounded-lg">
              <span className="text-[10px] uppercase font-bold text-amber-800 block">Warning</span>
              <span className="font-bold text-amber-900">
                {findings.filter((f) => f.severity === 'WARNING').length}
              </span>
            </div>
            <div className="text-center px-3 py-1.5 bg-brand-50 border border-brand-200 rounded-lg">
              <span className="text-[10px] uppercase font-bold text-brand-800 block">Info</span>
              <span className="font-bold text-brand-900">
                {findings.filter((f) => f.severity === 'INFO').length}
              </span>
            </div>
          </div>
        </div>
      </Card>

      {/* Filters */}
      <FilterBar
        searchValue={search}
        onSearchChange={setSearch}
        filters={[
          {
            label: 'Severity',
            value: severityFilter,
            onChange: setSeverityFilter,
            options: [
              { label: 'All Severities', value: 'ALL' },
              { label: 'Critical Only', value: 'CRITICAL' },
              { label: 'Warning Only', value: 'WARNING' },
            ],
          },
        ]}
        onReset={() => {
          setSearch('');
          setSeverityFilter('ALL');
        }}
      />

      {/* Evidence Cards Stack */}
      {filtered.length === 0 ? (
        <Card className="text-center py-12">
          <ShieldAlert className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
          <h3 className="text-sm font-bold text-ink-900">No Findings Matching Filter</h3>
          <p className="text-xs text-ink-500 mt-1 max-w-sm mx-auto">
            All findings in this category have been resolved or filtered out.
          </p>
        </Card>
      ) : (
        <div className="space-y-4">
          {filtered.map((finding) => (
            <EvidenceCard
              key={finding.id}
              finding={finding}
              selectedStrength={selectedStrengths[finding.id] || 'balanced'}
              onSelectStrength={handleSelectStrength}
              onAcceptCorrection={handleAccept}
              onRejectCorrection={handleReject}
            />
          ))}
        </div>
      )}
    </div>
  );
}
