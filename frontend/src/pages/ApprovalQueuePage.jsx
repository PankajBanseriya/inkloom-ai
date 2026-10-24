import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import StatusBadge from '../components/common/StatusBadge';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';
import FormField from '../components/common/FormField';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { MOCK_APPROVAL_QUEUE } from '../mock/mockData';
import reviewService from '../services/reviewService';
import approvalService from '../services/approvalService';
import {
  CheckCircle2,
  MessageSquare,
  ShieldCheck,
  Eye,
  RefreshCw,
} from 'lucide-react';

export default function ApprovalQueuePage() {
  const { user, activeBrand } = useAuth();
  const { success, warning, error: toastError } = useToast();
  const canDecide = user?.role === 'REVIEWER' || user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [queue, setQueue] = useState(MOCK_APPROVAL_QUEUE);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Decision Modal State
  const [selectedItem, setSelectedItem] = useState(null);
  const [decisionType, setDecisionType] = useState('APPROVE'); // APPROVE, REQUEST_CHANGES, REJECT
  const [decisionNotes, setDecisionNotes] = useState('');
  const [isDecisionModalOpen, setIsDecisionModalOpen] = useState(false);

  // Comments Thread Modal State
  const [activeCommentsItem, setActiveCommentsItem] = useState(null);
  const [commentsList, setCommentsList] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [newComment, setNewComment] = useState('');

  const fetchQueue = useCallback(() => {
    setLoading(true);
    reviewService.getReviews(activeBrand?.id)
      .then((data) => {
        if (data && data.length > 0) {
          const mapped = data.map((r) => {
            const findings = r.findings || [];
            const criticalCount = findings.filter((f) => String(f.severity).toUpperCase() === 'CRITICAL').length;
            const warningCount = findings.filter((f) => String(f.severity).toUpperCase() === 'WARNING').length;

            return {
              id: r.id,
              title: r.title || 'Untitled Document',
              submittedBy: r.authorUserId || 'Content Team',
              submittedAt: r.createdAt ? String(r.createdAt).split('T')[0] : 'Today',
              status: r.status || 'IN_REVIEW',
              complianceScore: r.complianceScore != null ? r.complianceScore : 90.0,
              criticalCount,
              warningCount,
              activeRevisionNumber: r.activeRevisionNumber || 1,
              commentsCount: 0,
            };
          });
          setQueue(mapped);
        }
      })
      .catch((err) => {
        console.warn('Could not load reviews from API, using cached data:', err.message);
      })
      .finally(() => setLoading(false));
  }, [activeBrand?.id]);

  useEffect(() => {
    fetchQueue();
  }, [fetchQueue]);

  const filtered = queue.filter((item) => {
    const title = item.title || '';
    const author = item.submittedBy || '';
    const matchesSearch =
      title.toLowerCase().includes(search.toLowerCase()) ||
      author.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || item.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const openDecisionModal = (item, type) => {
    if (!canDecide) {
      warning('Access Restricted', 'Content creators cannot record sign-off decisions.');
      return;
    }
    setSelectedItem(item);
    setDecisionType(type);
    setDecisionNotes('');
    setIsDecisionModalOpen(true);
  };

  const handleConfirmDecision = async () => {
    if (!selectedItem) return;

    const action = decisionType === 'APPROVE'
      ? 'APPROVE'
      : decisionType === 'REQUEST_CHANGES'
      ? 'REQUEST_CHANGES'
      : 'REJECT';

    try {
      await approvalService.makeDecision(selectedItem.id, action, decisionNotes);
      const newStatus = action === 'APPROVE' ? 'APPROVED' : action === 'REQUEST_CHANGES' ? 'CHANGES_REQUESTED' : 'REJECTED';
      setQueue((prev) =>
        prev.map((q) => (q.id === selectedItem.id ? { ...q, status: newStatus } : q))
      );
      setIsDecisionModalOpen(false);

      if (action === 'APPROVE') {
        success('Content Approved', `"${selectedItem.title}" has been authorized for publication.`);
      } else if (action === 'REQUEST_CHANGES') {
        warning('Changes Requested', `Feedback sent to author for "${selectedItem.title}".`);
      } else {
        warning('Submission Rejected', `"${selectedItem.title}" was rejected according to brand policy.`);
      }
    } catch (err) {
      toastError('Decision Failed', err.message);
    }
  };

  const openCommentsModal = async (item) => {
    setActiveCommentsItem(item);
    setCommentsLoading(true);
    try {
      const data = await approvalService.getComments(item.id);
      setCommentsList(data || []);
    } catch {
      setCommentsList(item.comments || []);
    } finally {
      setCommentsLoading(false);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || !activeCommentsItem) return;

    try {
      const saved = await approvalService.addComment(activeCommentsItem.id, newComment.trim());
      setCommentsList((prev) => [...prev, saved]);
      setNewComment('');
      success('Comment Added', 'Your review feedback was added to the audit thread.');
    } catch (err) {
      toastError('Failed to post comment', err.message);
    }
  };

  const columns = [
    {
      key: 'title',
      header: 'Submission Title & Details',
      render: (val, row) => (
        <div className="max-w-xs sm:max-w-sm">
          <Link
            to={`/reviews/${row.id}/findings`}
            className="font-bold text-ink-900 hover:text-brand-600 transition-colors block leading-tight text-xs sm:text-sm"
          >
            {val}
          </Link>
          <div className="text-[11px] text-ink-500 mt-1 flex items-center space-x-1.5 flex-wrap gap-y-0.5">
            <span className="font-mono text-[10px] text-ink-400">{row.id}</span>
            <span>•</span>
            <span>Rev #{row.activeRevisionNumber || 1}</span>
            <span>•</span>
            <span>By {row.submittedBy}</span>
            <span>•</span>
            <span>{row.submittedAt}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'complianceScore',
      header: 'Compliance Health',
      render: (score, row) => {
        const scoreNum = Math.round(score != null ? score : 100);
        const scoreColor =
          scoreNum >= 95
            ? 'text-emerald-700 bg-emerald-50 border-emerald-200'
            : scoreNum >= 80
            ? 'text-amber-700 bg-amber-50 border-amber-200'
            : 'text-rose-700 bg-rose-50 border-rose-200';

        return (
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <span className={`text-xs font-bold px-2 py-0.5 rounded border ${scoreColor}`}>
                {scoreNum}%
              </span>
            </div>
            {(row.criticalCount > 0 || row.warningCount > 0) && (
              <div className="text-[10px] text-ink-500 flex items-center space-x-1">
                {row.criticalCount > 0 && (
                  <span className="text-rose-600 font-semibold">{row.criticalCount} Critical</span>
                )}
                {row.criticalCount > 0 && row.warningCount > 0 && <span>•</span>}
                {row.warningCount > 0 && (
                  <span className="text-amber-600 font-semibold">{row.warningCount} Warning</span>
                )}
              </div>
            )}
          </div>
        );
      },
    },
    {
      key: 'status',
      header: 'Review Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'actions',
      header: 'Governance Decisions',
      render: (_, row) => (
        <div className="flex items-center space-x-1.5 flex-wrap gap-y-1">
          <Link to={`/reviews/${row.id}/findings`}>
            <Button size="sm" variant="outline" className="text-[11px] px-2 py-1 h-7">
              <Eye className="w-3 h-3 mr-1" />
              Findings
            </Button>
          </Link>

          <Button
            size="sm"
            variant="outline"
            onClick={() => openCommentsModal(row)}
            className="text-[11px] px-2 py-1 h-7"
          >
            <MessageSquare className="w-3 h-3 mr-1" />
            Notes
          </Button>

          {canDecide && row.status !== 'APPROVED' && row.status !== 'REJECTED' && (
            <>
              <Button
                size="sm"
                variant="outline"
                onClick={() => openDecisionModal(row, 'REQUEST_CHANGES')}
                className="text-[11px] px-2 py-1 h-7 text-amber-700 hover:bg-amber-50"
              >
                Request Changes
              </Button>
              <Button
                size="sm"
                variant="primary"
                onClick={() => openDecisionModal(row, 'APPROVE')}
                className="text-[11px] px-2 py-1 h-7 bg-emerald-600 hover:bg-emerald-700"
              >
                <CheckCircle2 className="w-3 h-3 mr-1" />
                Approve
              </Button>
            </>
          )}

          {!canDecide && row.status !== 'APPROVED' && (
            <span className="text-[11px] text-ink-400 font-medium px-2 py-1 italic bg-ink-50 rounded">
              Awaiting Sign-off
            </span>
          )}
        </div>
      ),
    },
  ];

  const pendingCount = queue.filter((q) => q.status === 'IN_REVIEW' || q.status === 'SUBMITTED').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Human Review & Approval Queue"
        description="Authorized human gatekeeping queue. Final publication approval requires explicit sign-off by a qualified brand reviewer or administrator."
        badge={`${pendingCount} Pending Review`}
        badgeVariant="warning"
        actions={
          <Button size="sm" variant="outline" onClick={fetchQueue}>
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh Queue
          </Button>
        }
      />

      {/* Security notice */}
      <div className="p-4 bg-white rounded-xl border border-ink-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs">
        <div className="flex items-center space-x-2">
          <ShieldCheck className="w-4 h-4 text-brand-600 flex-shrink-0" />
          <span className="text-ink-600">
            <strong>Mandatory Human Governance Policy:</strong> Content is never automatically approved or published solely by AI evaluations.
          </span>
        </div>
        {!canDecide && (
          <span className="px-2 py-0.5 rounded bg-ink-100 text-ink-600 font-medium text-[11px] self-start sm:self-auto">
            Content Creator View (Read-Only Decisions)
          </span>
        )}
      </div>

      <FilterBar
        searchValue={search}
        onSearchChange={setSearch}
        filters={[
          {
            label: 'Review Status',
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { label: 'All Submissions', value: 'ALL' },
              { label: 'In Review / Submitted', value: 'IN_REVIEW' },
              { label: 'Changes Requested', value: 'CHANGES_REQUESTED' },
              { label: 'Approved', value: 'APPROVED' },
              { label: 'Rejected', value: 'REJECTED' },
            ],
          },
        ]}
        onReset={() => {
          setSearch('');
          setStatusFilter('ALL');
        }}
      />

      {loading ? (
        <div className="py-12 flex justify-center">
          <LoadingSpinner label="Loading approval queue..." />
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={filtered}
          keyField="id"
        />
      )}

      {/* Decision Action Modal */}
      <Modal
        isOpen={isDecisionModalOpen}
        onClose={() => setIsDecisionModalOpen(false)}
        title={
          decisionType === 'APPROVE'
            ? 'Approve Document for Publication'
            : decisionType === 'REQUEST_CHANGES'
            ? 'Request Author Revisions'
            : 'Reject Content Submission'
        }
        description={`Record official governance decision for "${selectedItem?.title}".`}
      >
        <div className="space-y-4">
          <div
            className={`p-3 rounded-lg text-xs leading-relaxed ${
              decisionType === 'APPROVE'
                ? 'bg-emerald-50 text-emerald-950 border border-emerald-200'
                : 'bg-amber-50 text-amber-950 border border-amber-200'
            }`}
          >
            {decisionType === 'APPROVE'
              ? 'By approving, you certify that this communication follows all brand guidelines, uses verified claims, and complies with required disclosures.'
              : 'Specify the required adjustments, missing disclaimers, or prohibited wording that the author must resolve.'}
          </div>

          <FormField label="Decision Notes & Feedback" id="dec-notes" required={decisionType !== 'APPROVE'}>
            <textarea
              id="dec-notes"
              rows={3}
              value={decisionNotes}
              onChange={(e) => setDecisionNotes(e.target.value)}
              placeholder="Provide context or guidance for the audit trail..."
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </FormField>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsDecisionModalOpen(false)}>
              Cancel
            </Button>
            <Button
              size="sm"
              variant={decisionType === 'APPROVE' ? 'primary' : decisionType === 'REQUEST_CHANGES' ? 'secondary' : 'danger'}
              onClick={handleConfirmDecision}
            >
              {decisionType === 'APPROVE' ? 'Sign & Approve' : decisionType === 'REQUEST_CHANGES' ? 'Send Change Request' : 'Reject Submission'}
            </Button>
          </div>
        </div>
      </Modal>

      {/* Review Comments Thread Modal */}
      <Modal
        isOpen={!!activeCommentsItem}
        onClose={() => setActiveCommentsItem(null)}
        title={`Review Comments — ${activeCommentsItem?.title}`}
        description="Collaboration thread between author and brand governance reviewers."
      >
        <div className="space-y-4">
          {commentsLoading ? (
            <div className="py-6 flex justify-center">
              <LoadingSpinner label="Loading comments..." />
            </div>
          ) : (
            <div className="max-h-60 overflow-y-auto space-y-2.5 p-2">
              {commentsList.length === 0 ? (
                <p className="text-xs text-ink-400 italic text-center py-4">
                  No reviewer comments recorded yet.
                </p>
              ) : (
                commentsList.map((c, idx) => (
                  <div key={c.id || idx} className="p-3 bg-ink-50 rounded-lg border border-ink-200 text-xs">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-bold text-ink-900">{c.authorName || c.author || 'Reviewer'}</span>
                      <span className="text-[10px] text-ink-400 font-mono">
                        {c.createdAt ? String(c.createdAt).split('T')[0] : c.time || 'Recent'}
                      </span>
                    </div>
                    <p className="text-ink-700 leading-relaxed">{c.commentText || c.text}</p>
                  </div>
                ))
              )}
            </div>
          )}

          <form onSubmit={handleAddComment} className="pt-3 border-t border-ink-100 space-y-2">
            <textarea
              rows={2}
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add your reviewer comment or instructions..."
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              required
            />
            <div className="flex justify-end">
              <Button size="sm" variant="primary" type="submit">
                Post Comment
              </Button>
            </div>
          </form>
        </div>
      </Modal>
    </div>
  );
}
