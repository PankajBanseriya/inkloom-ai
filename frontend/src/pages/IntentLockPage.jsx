import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import EmptyState from '../components/feedback/EmptyState';
import IntentLockComparison from '../features/revisions/IntentLockComparison';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import reviewService from '../services/reviewService';
import { ArrowLeft, CheckCircle2, History } from 'lucide-react';

export default function IntentLockPage() {
  const { reviewId } = useParams();
  const { success, info, error: toastError } = useToast();

  const [review, setReview] = useState(null);
  const [findings, setFindings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFindingIndex, setActiveFindingIndex] = useState(0);

  useEffect(() => {
    if (!reviewId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    reviewService.getReviewById(reviewId)
      .then((data) => {
        if (data) {
          setReview(data);
          setFindings(data.findings || []);
        }
      })
      .catch((err) => {
        console.warn('Could not load review:', err.message);
      })
      .finally(() => setLoading(false));
  }, [reviewId]);

  const activeFinding = findings[activeFindingIndex] || findings[0];

  const handleAccept = async ({ text, tier }) => {
    if (reviewId && activeFinding?.id) {
      try {
        await reviewService.acceptCorrection(reviewId, {
          findingId: activeFinding.id,
          replacementText: text,
          tier: tier || 'balanced',
          rationale: `Accepted ${tier || 'balanced'} Intent Lock fix in comparison view.`,
        });
      } catch (err) {
        toastError('Failed to apply correction', err.message);
        return;
      }
    }

    success('Intent Lock Revision Saved', `Accepted ${tier} correction: "${text}"`);
    // Advance to next finding or complete
    if (findings.length > 1) {
      setFindings((prev) => prev.filter((_, idx) => idx !== activeFindingIndex));
      setActiveFindingIndex(0);
    } else {
      setFindings([]);
    }
  };

  const handleReject = async () => {
    if (reviewId && activeFinding?.id) {
      try {
        await reviewService.rejectCorrection(reviewId, activeFinding.id, 'Author preserved original phrasing.');
      } catch (err) {
        console.warn('Could not record reject feedback:', err.message);
      }
    }
    info('Correction Rejected', 'Original author wording preserved in document and logged for clarity review.');
    if (findings.length > 1) {
      setFindings((prev) => prev.filter((_, idx) => idx !== activeFindingIndex));
      setActiveFindingIndex(0);
    } else {
      setFindings([]);
    }
  };

  if (loading) {
    return (
      <div className="py-16 flex justify-center">
        <LoadingSpinner label="Loading Intent Lock workbench..." />
      </div>
    );
  }

  if (!review) {
    return (
      <div className="p-8">
        <EmptyState
          title="Review not found"
          description="The requested review could not be retrieved from the database."
          action={
            <Link to={ROUTES.DASHBOARD}>
              <Button size="sm" variant="outline">
                Back to Dashboard
              </Button>
            </Link>
          }
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-2 text-xs text-ink-500 mb-1">
        <Link to={`/reviews/${review.id}/findings`} className="hover:text-ink-800 flex items-center">
          <ArrowLeft className="w-3.5 h-3.5 mr-1" />
          Back to Findings
        </Link>
        <span>/</span>
        <span>Intent Lock Comparison Workbench</span>
      </div>

      <PageHeader
        title="Intent Lock Correction Workbench"
        description="Preserve the author's meaning, target audience, and call-to-action while systematically resolving brand violations across Light, Balanced, and Strong tiers."
        badge={findings.length > 0 ? `${findings.length} Pending Corrections` : 'All Resolved'}
        badgeVariant={findings.length > 0 ? 'brand' : 'success'}
        actions={
          <Link to={`/reviews/${review.id}/revisions`}>
            <Button size="sm" variant="outline" className="text-xs">
              <History className="w-3.5 h-3.5 mr-1.5" />
              View Revision History
            </Button>
          </Link>
        }
      />

      {/* Main Workbench */}
      {findings.length === 0 ? (
        <Card className="text-center py-16">
          <CheckCircle2 className="w-12 h-12 text-emerald-500 mx-auto mb-3" />
          <h3 className="text-base font-bold text-ink-900">All Suggested Corrections Resolved</h3>
          <p className="text-xs text-ink-500 mt-1 max-w-sm mx-auto">
            You have evaluated and resolved all Intent Lock suggestions for this document. You can now submit it for reviewer sign-off.
          </p>
          <div className="mt-6 flex justify-center space-x-3">
            <Link to={ROUTES.APPROVALS}>
              <Button size="sm" variant="primary">
                Proceed to Approval Queue
              </Button>
            </Link>
          </div>
        </Card>
      ) : (
        <div className="space-y-6">
          {/* Finding Selector Carousel Tabs */}
          <div className="flex items-center space-x-2 overflow-x-auto pb-2">
            {findings.map((f, idx) => (
              <button
                key={f.id}
                type="button"
                onClick={() => setActiveFindingIndex(idx)}
                className={`px-3 py-2 text-xs font-semibold rounded-lg border whitespace-nowrap transition-colors ${
                  activeFindingIndex === idx
                    ? 'bg-brand-600 text-white border-brand-600 shadow-xs'
                    : 'bg-white text-ink-700 border-ink-200 hover:border-ink-300'
                }`}
              >
                Issue #{idx + 1}: {f.title}
              </button>
            ))}
          </div>

          {/* Finding Context Header */}
          <div className="p-4 bg-white rounded-xl border border-ink-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
            <div>
              <span className="text-[10px] font-bold uppercase tracking-wider text-brand-600">
                Active Issue ({activeFindingIndex + 1} of {findings.length})
              </span>
              <h3 className="text-sm font-bold text-ink-900 mt-0.5">{activeFinding.title}</h3>
              <p className="text-xs text-ink-500 mt-0.5">{activeFinding.explanation}</p>
            </div>
            <div className="flex items-center space-x-2 flex-shrink-0">
              <span className="text-[11px] font-medium text-ink-600">Citation:</span>
              <span className="text-[11px] font-semibold text-ink-800 bg-ink-100 px-2 py-0.5 rounded border border-ink-200">
                {activeFinding.supportingDoc}
              </span>
            </div>
          </div>

          {/* The Multi-Tier Comparison Component */}
          <IntentLockComparison
            originalText={activeFinding.excerpt}
            corrections={{
              light: activeFinding.suggestedCorrections?.light || activeFinding.suggestedCorrection?.terse || '',
              balanced: activeFinding.suggestedCorrections?.balanced || activeFinding.suggestedCorrection?.balanced || '',
              strong: activeFinding.suggestedCorrections?.strong || activeFinding.suggestedCorrection?.comprehensive || '',
            }}
            rationale={activeFinding.suggestedCorrection?.intentRationale || activeFinding.explanation}
            onAccept={handleAccept}
            onReject={handleReject}
          />
        </div>
      )}
    </div>
  );
}
