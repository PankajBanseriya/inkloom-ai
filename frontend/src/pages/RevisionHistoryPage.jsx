import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import StatusBadge from '../components/common/StatusBadge';
import Badge from '../components/common/Badge';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import { MOCK_REVIEWS, MOCK_REVISIONS } from '../mock/mockData';
import reviewService from '../services/reviewService';
import approvalService from '../services/approvalService';
import { ArrowLeft, RotateCcw, FileText, GitBranch, Clock } from 'lucide-react';

export default function RevisionHistoryPage() {
  const { reviewId } = useParams();
  const { success, error: toastError } = useToast();

  const [review, setReview] = useState(() => MOCK_REVIEWS.find((r) => r.id === reviewId) || MOCK_REVIEWS[0]);
  const [revisions, setRevisions] = useState(MOCK_REVISIONS);
  const [selectedRevision, setSelectedRevision] = useState(MOCK_REVISIONS[0]);
  const [decisions, setDecisions] = useState([]);

  useEffect(() => {
    if (reviewId) {
      reviewService.getReviewById(reviewId)
        .then((data) => {
          if (data) setReview(data);
        })
        .catch(() => {});

      reviewService.getRevisionHistory(reviewId)
        .then((data) => {
          if (data && data.length > 0) {
            setRevisions(data);
            setSelectedRevision(data[0]);
          }
        })
        .catch(() => {});

      approvalService.getDecisions(reviewId)
        .then((data) => {
          if (data) setDecisions(data);
        })
        .catch(() => {});
    }
  }, [reviewId]);

  const handleRollback = async (rev) => {
    if (!rev?.content) return;

    if (reviewId) {
      try {
        const newRev = await reviewService.saveRevision(reviewId, {
          content: rev.content,
          changeSummary: `Restored content snapshot from Revision #${rev.revisionNumber}`,
        });
        setRevisions((prev) => [newRev, ...prev]);
        setSelectedRevision(newRev);
        success('Revision Snapshot Restored', `Created Revision #${newRev.revisionNumber} from Revision #${rev.revisionNumber}.`);
        return;
      } catch (err) {
        toastError('Rollback Failed', err.message);
        return;
      }
    }

    success('Revision Activated', `Restored content snapshot from Revision #${rev.revisionNumber}.`);
    setSelectedRevision(rev);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-2 text-xs text-ink-500 mb-1">
        <Link to={`/reviews/${review.id}/findings`} className="hover:text-ink-800 flex items-center">
          <ArrowLeft className="w-3.5 h-3.5 mr-1" />
          Back to Findings
        </Link>
        <span>/</span>
        <span>Audit Revision History</span>
      </div>

      <PageHeader
        title={`${review.title} — Revision Timeline`}
        description="Immutable audit history of all author edits, accepted Intent Lock suggestions, and reviewer change requests."
        badge={`${revisions.length} Snapshots Saved`}
        actions={
          <Link to={ROUTES.REVIEW_EDITOR}>
            <Button size="sm" variant="outline" className="text-xs">
              <FileText className="w-3.5 h-3.5 mr-1.5" />
              Open in Review Editor
            </Button>
          </Link>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Revision Timeline List (5 cols) */}
        <div className="lg:col-span-5 space-y-3">
          <h2 className="text-xs font-bold uppercase tracking-wider text-ink-500 px-1">
            Chronological Revisions
          </h2>

          {revisions.map((rev) => {
            const isSelected = selectedRevision?.revisionNumber === rev.revisionNumber;
            const dateStr = rev.createdAt ? String(rev.createdAt).split('T')[0] : rev.date || 'Recent';

            return (
              <div
                key={rev.revisionNumber}
                onClick={() => setSelectedRevision(rev)}
                className={`p-4 rounded-xl border transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-white border-brand-500 shadow-sm ring-1 ring-brand-500'
                    : 'bg-white/80 border-ink-200 hover:border-ink-300'
                }`}
              >
                <div className="flex items-center justify-between pb-2 border-b border-ink-100">
                  <div className="flex items-center space-x-2">
                    <span className="font-bold text-xs text-brand-700 bg-brand-50 px-2 py-0.5 rounded border border-brand-200">
                      Revision #{rev.revisionNumber}
                    </span>
                    <StatusBadge status={rev.status || 'SAVED'} size="sm" />
                  </div>
                  <span className="text-[11px] text-ink-400 font-mono">{dateStr}</span>
                </div>

                <p className="text-xs font-medium text-ink-800 mt-2.5 leading-relaxed">
                  {rev.changeSummary}
                </p>

                <div className="mt-3 flex items-center justify-between text-[11px] text-ink-500">
                  <span>Author: {rev.authorUserId || rev.author || 'Author'}</span>
                  {rev.sourceRevisionNumber != null && (
                    <span className="flex items-center text-ink-400 font-mono text-[10px]">
                      <GitBranch className="w-3 h-3 mr-1" />
                      from Rev #{rev.sourceRevisionNumber}
                    </span>
                  )}
                </div>
              </div>
            );
          })}

          {/* Reviewer Decisions Timeline */}
          {decisions.length > 0 && (
            <div className="pt-4 mt-4 border-t border-ink-200 space-y-2.5">
              <h2 className="text-xs font-bold uppercase tracking-wider text-ink-500 px-1 flex items-center">
                <Clock className="w-3.5 h-3.5 mr-1 text-ink-400" />
                Reviewer Sign-off Decisions
              </h2>
              {decisions.map((dec, idx) => (
                <div key={dec.id || idx} className="p-3 bg-white rounded-lg border border-ink-200 text-xs">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-ink-900">{dec.reviewerName || 'Reviewer'}</span>
                    <Badge
                      variant={dec.action === 'APPROVE' ? 'success' : dec.action === 'REJECT' ? 'danger' : 'warning'}
                      size="sm"
                    >
                      {dec.action}
                    </Badge>
                  </div>
                  <div className="text-[11px] text-ink-500 mb-1">
                    Decision for Revision #{dec.revisionNumber || 'Active'}
                  </div>
                  {dec.decisionNotes && (
                    <p className="text-ink-700 italic bg-ink-50 p-2 rounded border border-ink-100 text-[11px]">
                      &ldquo;{dec.decisionNotes}&rdquo;
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right Column: Revision Snapshot Inspection (7 cols) */}
        <div className="lg:col-span-7">
          <Card
            title={`Revision #${selectedRevision?.revisionNumber} Content Snapshot`}
            subtitle={`Recorded by ${selectedRevision?.authorUserId || selectedRevision?.author || 'Author'}`}
            action={
              <Button
                size="sm"
                variant="outline"
                onClick={() => handleRollback(selectedRevision)}
                className="text-xs"
              >
                <RotateCcw className="w-3.5 h-3.5 mr-1" />
                Restore Snapshot
              </Button>
            }
          >
            <div className="space-y-4">
              <div className="p-3 bg-ink-50 rounded-lg border border-ink-200 text-xs">
                <span className="font-bold text-ink-700 uppercase tracking-wider text-[10px] block mb-1">
                  Change Rationale / Description:
                </span>
                <p className="text-ink-800">{selectedRevision?.changeSummary}</p>
              </div>

              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-ink-500 block mb-1.5">
                  Content Body Snapshot:
                </span>
                <div className="p-4 bg-white border border-ink-200 rounded-xl text-xs font-sans leading-relaxed text-ink-900 min-h-[220px] whitespace-pre-wrap">
                  {selectedRevision?.content || review.currentContent}
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
