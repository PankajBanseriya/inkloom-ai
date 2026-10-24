import { useState } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import StatusBadge from '../components/common/StatusBadge';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';
import FormField from '../components/common/FormField';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { MOCK_APPROVAL_QUEUE } from '../mock/mockData';
import {
  CheckCircle2,
  MessageSquare,
  ShieldCheck,
  Eye,
} from 'lucide-react';

export default function ApprovalQueuePage() {
  const { user } = useAuth();
  const { success, warning } = useToast();
  const canDecide = user?.role === 'REVIEWER' || user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [queue, setQueue] = useState(MOCK_APPROVAL_QUEUE);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Decision Modal State
  const [selectedItem, setSelectedItem] = useState(null);
  const [decisionType, setDecisionType] = useState('APPROVE'); // APPROVE, REQUEST_CHANGES, REJECT
  const [decisionNotes, setDecisionNotes] = useState('');
  const [isDecisionModalOpen, setIsDecisionModalOpen] = useState(false);

  // Comments Thread Modal State
  const [activeCommentsItem, setActiveCommentsItem] = useState(null);
  const [newComment, setNewComment] = useState('');

  const filtered = queue.filter((item) => {
    const matchesSearch =
      item.title.toLowerCase().includes(search.toLowerCase()) ||
      item.submittedBy.toLowerCase().includes(search.toLowerCase());
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

  const handleConfirmDecision = () => {
    if (!selectedItem) return;

    const newStatus =
      decisionType === 'APPROVE'
        ? 'APPROVED'
        : decisionType === 'REQUEST_CHANGES'
        ? 'CHANGES_REQUESTED'
        : 'REJECTED';

    setQueue((prev) =>
      prev.map((q) => (q.id === selectedItem.id ? { ...q, status: newStatus } : q))
    );

    setIsDecisionModalOpen(false);

    if (decisionType === 'APPROVE') {
      success('Content Approved', `"${selectedItem.title}" has been authorized for publication by ${user?.name || 'Reviewer'}.`);
    } else if (decisionType === 'REQUEST_CHANGES') {
      warning('Changes Requested', `Feedback sent to ${selectedItem.submittedBy}.`);
    } else {
      warning('Submission Rejected', `"${selectedItem.title}" rejected according to brand policy.`);
    }
  };

  const handleAddComment = (e) => {
    e.preventDefault();
    if (!newComment.trim() || !activeCommentsItem) return;

    const commentObj = {
      id: `c-${Date.now().toString(36)}`,
      author: user?.name || 'Reviewer',
      role: user?.role || 'REVIEWER',
      text: newComment,
      time: 'Just now',
    };

    setQueue((prev) =>
      prev.map((q) =>
        q.id === activeCommentsItem.id
          ? { ...q, comments: [...q.comments, commentObj] }
          : q
      )
    );

    setActiveCommentsItem((prev) => ({
      ...prev,
      comments: [...prev.comments, commentObj],
    }));

    setNewComment('');
    success('Comment Posted', 'Reviewer comment added to document history.');
  };

  const columns = [
    {
      key: 'title',
      header: 'Submitted Document',
      render: (val, row) => (
        <div>
          <div className="font-bold text-ink-900">{val}</div>
          <div className="text-[11px] text-ink-500 mt-0.5">
            {row.contentType} • Submitted by <span className="font-medium text-ink-700">{row.submittedBy}</span> ({row.submittedAt})
          </div>
          <div className="text-[10px] text-ink-400 mt-1">
            Reviewer: <span className="text-ink-600 font-medium">{row.reviewerAssigned}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'complianceScore',
      header: 'Compliance Score',
      render: (val) => (
        <span
          className={`font-bold ${
            val >= 90 ? 'text-emerald-600' : val >= 75 ? 'text-amber-600' : 'text-rose-600'
          }`}
        >
          {val}%
        </span>
      ),
    },
    {
      key: 'issues',
      header: 'Pending Issues',
      render: (_, row) => (
        <div className="flex items-center space-x-1.5 text-[11px]">
          {row.criticalIssues > 0 && (
            <span className="px-1.5 py-0.5 rounded bg-rose-50 text-rose-800 font-bold border border-rose-200">
              {row.criticalIssues} Critical
            </span>
          )}
          {row.warningIssues > 0 && (
            <span className="px-1.5 py-0.5 rounded bg-amber-50 text-amber-800 font-medium border border-amber-200">
              {row.warningIssues} Warning
            </span>
          )}
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'actions',
      header: 'Review Actions',
      className: 'text-right',
      render: (_, row) => (
        <div className="flex items-center justify-end space-x-1.5">
          <button
            type="button"
            onClick={() => setActiveCommentsItem(row)}
            className="p-1.5 text-ink-500 hover:text-brand-600 hover:bg-ink-100 rounded-md transition-colors"
            title="Review Comments"
          >
            <MessageSquare className="w-4 h-4" />
          </button>

          <Link to={`/reviews/${row.reviewId}/findings`}>
            <Button size="sm" variant="outline" className="text-xs px-2 py-1">
              <Eye className="w-3.5 h-3.5 mr-1" />
              Inspect
            </Button>
          </Link>

          {canDecide && row.status !== 'APPROVED' && (
            <>
              <Button
                size="sm"
                variant="outline"
                onClick={() => openDecisionModal(row, 'REQUEST_CHANGES')}
                className="text-xs px-2 py-1 border-amber-300 text-amber-800 hover:bg-amber-50"
              >
                Request Changes
              </Button>

              <Button
                size="sm"
                variant="primary"
                onClick={() => openDecisionModal(row, 'APPROVE')}
                className="text-xs px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700"
              >
                <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
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

  return (
    <div className="space-y-6">
      <PageHeader
        title="Human Review & Approval Queue"
        description="Authorized human gatekeeping queue. Final publication approval requires explicit sign-off by a qualified brand reviewer."
        badge={`${queue.filter((q) => q.status === 'PENDING_REVIEW').length} Pending Review`}
        badgeVariant="warning"
      />

      {/* Security notice */}
      <div className="p-4 bg-white rounded-xl border border-ink-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-xs">
        <div className="flex items-center space-x-2">
          <ShieldCheck className="w-4 h-4 text-brand-600 flex-shrink-0" />
          <span className="text-ink-600">
            Mandatory Human Review Policy: Content is never automatically approved or published solely by AI evaluations.
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
              { label: 'Pending Review', value: 'PENDING_REVIEW' },
              { label: 'Changes Requested', value: 'CHANGES_REQUESTED' },
              { label: 'Approved', value: 'APPROVED' },
            ],
          },
        ]}
        onReset={() => {
          setSearch('');
          setStatusFilter('ALL');
        }}
      />

      <DataTable
        columns={columns}
        data={filtered}
        keyField="id"
      />

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
          <div className="max-h-60 overflow-y-auto space-y-2.5 p-2">
            {activeCommentsItem?.comments?.length === 0 ? (
              <p className="text-xs text-ink-400 italic text-center py-4">
                No reviewer comments yet.
              </p>
            ) : (
              activeCommentsItem?.comments?.map((c) => (
                <div key={c.id} className="p-3 bg-ink-50 rounded-lg border border-ink-200 text-xs">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-ink-900">{c.author}</span>
                    <span className="text-[10px] text-ink-400">{c.time}</span>
                  </div>
                  <p className="text-ink-700 leading-relaxed">{c.text}</p>
                </div>
              ))
            )}
          </div>

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
