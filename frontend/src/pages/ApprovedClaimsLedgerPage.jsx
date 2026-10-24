import { useState, useEffect, useCallback } from 'react';
import PageHeader from '../components/common/PageHeader';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import StatusBadge from '../components/common/StatusBadge';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import EmptyState from '../components/feedback/EmptyState';
import Card from '../components/common/Card';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import claimsService from '../services/claimsService';
import { Plus, CheckCircle2, AlertTriangle, Trash2, Clock, RefreshCw } from 'lucide-react';

export default function ApprovedClaimsLedgerPage() {
  const { activeBrand, user } = useAuth();
  const { success, error: toastError, info } = useToast();
  const canManageClaims = user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [newClaimText, setNewClaimText] = useState('');
  const [newSource, setNewSource] = useState('');
  const [newScope, setNewScope] = useState('');
  const [newOwner, setNewOwner] = useState('');
  const [newExpiry, setNewExpiry] = useState('');

  const fetchClaims = useCallback(() => {
    if (!activeBrand?.id) {
      setLoading(false);
      return;
    }
    setLoading(true);
    claimsService.getClaims(activeBrand.id)
      .then((data) => {
        setClaims(data || []);
      })
      .catch((err) => {
        console.warn('Could not load claims from API:', err.message);
        setClaims([]);
      })
      .finally(() => setLoading(false));
  }, [activeBrand?.id]);

  useEffect(() => {
    fetchClaims();
  }, [fetchClaims]);

  const filtered = claims.filter((c) => {
    const text = c.claimText || '';
    const source = c.supportingSource || '';
    const scope = c.scope || '';
    const matchesSearch =
      text.toLowerCase().includes(search.toLowerCase()) ||
      source.toLowerCase().includes(search.toLowerCase()) ||
      scope.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleAddClaim = async (e) => {
    e.preventDefault();
    if (!newClaimText.trim()) return;

    const payload = {
      claimText: newClaimText.trim(),
      supportingSource: newSource.trim() || 'General Enterprise Specifications',
      scope: newScope.trim() || 'All standard enterprise communications.',
      owner: newOwner.trim() || user?.name || 'Product Marketing',
      expiryDate: newExpiry ? new Date(newExpiry).toISOString() : new Date(Date.now() + 365 * 86400000).toISOString(),
    };

    if (!activeBrand?.id) {
      toastError('No workspace selected', 'Please select a brand workspace before registering a claim.');
      return;
    }

    try {
      const saved = await claimsService.createClaim(activeBrand.id, payload);
      if (saved) {
        setClaims((prev) => [saved, ...prev]);
      }
    } catch (err) {
      toastError('Failed to register claim', err.message);
      return;
    }

    setIsModalOpen(false);
    setNewClaimText('');
    setNewSource('');
    setNewScope('');
    setNewOwner('');
    setNewExpiry('');
    success('Approved Claim Recorded', 'Claim registered in the central governance ledger.');
  };

  const handleDeleteClaim = async (claimId) => {
    if (!canManageClaims) {
      toastError('Access Denied', 'Only Brand Managers and Admins can delete claims.');
      return;
    }

    if (!activeBrand?.id) return;
    try {
      await claimsService.deleteClaim(activeBrand.id, claimId);
      setClaims((prev) => prev.filter((c) => c.id !== claimId));
      info('Claim Removed', 'Approved claim record deleted from ledger.');
    } catch (err) {
      toastError('Deletion Failed', err.message);
    }
  };

  const handleToggleStatus = async (claim) => {
    if (!canManageClaims) {
      toastError('Access Denied', 'Only Brand Managers and Admins can modify claim statuses.');
      return;
    }

    if (!activeBrand?.id || !claim.id) return;
    const nextStatus = claim.status === 'APPROVED' ? 'EXPIRED' : 'APPROVED';
    try {
      const updated = await claimsService.updateClaimStatus(activeBrand.id, claim.id, nextStatus);
      setClaims((prev) => prev.map((c) => (c.id === claim.id ? updated : c)));
      success('Claim Status Updated', `Status changed to ${nextStatus}.`);
    } catch (err) {
      toastError('Status Update Failed', err.message);
    }
  };

  const columns = [
    {
      key: 'claimText',
      header: 'Approved Claim Statement',
      render: (val, row) => (
        <div className="max-w-md">
          <span className="font-semibold text-ink-900 leading-snug">
            &ldquo;{val}&rdquo;
          </span>
          <div className="text-[11px] text-ink-500 mt-1 flex items-center space-x-1.5 flex-wrap gap-y-1">
            <span className="font-mono text-[10px] text-ink-400">{row.id}</span>
            <span>•</span>
            <span className="text-brand-600 font-medium">{row.supportingSource || 'Missing citation'}</span>
            {row.isEvidenceMissing && (
              <Badge variant="warning" size="sm">
                No Verified Source
              </Badge>
            )}
            {row.isExpired && (
              <Badge variant="danger" size="sm">
                Expired
              </Badge>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'scope',
      header: 'Scope & Conditions of Use',
      render: (val) => (
        <span className="text-xs text-ink-700 leading-relaxed block max-w-xs">{val}</span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'owner',
      header: 'Claim Owner',
      render: (val) => <span className="font-medium text-ink-800 text-xs">{val || 'Unassigned'}</span>,
    },
    {
      key: 'dates',
      header: 'Review & Expiration',
      render: (_, row) => {
        const reviewDateStr = row.reviewDate ? String(row.reviewDate).split('T')[0] : 'N/A';
        const expiryDateStr = row.expiryDate ? String(row.expiryDate).split('T')[0] : 'N/A';
        const isExp = row.isExpired || row.status === 'EXPIRED';

        return (
          <div className="text-[11px] text-ink-500">
            <div>Reviewed: {reviewDateStr}</div>
            <div className={isExp ? 'text-rose-600 font-semibold' : 'text-ink-600'}>
              Expires: {expiryDateStr}
            </div>
          </div>
        );
      },
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (_, row) => (
        canManageClaims ? (
          <div className="flex items-center space-x-1.5">
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleToggleStatus(row)}
              className="text-[11px] px-2 py-1 h-7"
              title={row.status === 'APPROVED' ? 'Mark claim as expired' : 'Reactivate claim'}
            >
              <Clock className="w-3 h-3 mr-1" />
              {row.status === 'APPROVED' ? 'Expire' : 'Activate'}
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleDeleteClaim(row.id)}
              className="text-[11px] px-2 py-1 h-7 text-rose-600 hover:text-rose-700 hover:bg-rose-50"
              title="Delete claim from ledger"
            >
              <Trash2 className="w-3 h-3" />
            </Button>
          </div>
        ) : (
          <span className="text-[11px] text-ink-400 italic">View only</span>
        )
      ),
    },
  ];

  const expiredCount = claims.filter((c) => c.status === 'EXPIRED' || c.isExpired).length;
  const missingEvidenceCount = claims.filter((c) => c.isEvidenceMissing).length;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${activeBrand?.name || 'Workspace'} Approved Claims Ledger`}
        description="Centralized organization ledger of approved marketing and product claims. Content referencing unapproved or expired claims is automatically flagged."
        actions={
          <div className="flex items-center space-x-2">
            <Button size="sm" variant="outline" onClick={fetchClaims}>
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              Sync Ledger
            </Button>
            {canManageClaims && (
              <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                <Plus className="w-4 h-4 mr-1.5" />
                Register Approved Claim
              </Button>
            )}
          </div>
        }
      />

      {!canManageClaims && (
        <div className="p-3 bg-ink-100/70 rounded-xl border border-ink-200 text-xs text-ink-600 flex items-center justify-between">
          <span>
            <strong>Read-only mode:</strong> You are viewing approved claims with <strong>{user?.role?.replace('_', ' ')}</strong> permissions. Registering or updating claims in the ledger requires Brand Manager or Administrator privileges.
          </span>
        </div>
      )}

      {/* Advisory Banners */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="p-4 bg-emerald-50/70 rounded-xl border border-emerald-200 flex items-center space-x-3 text-xs text-emerald-950">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          <div>
            <div className="font-bold">Human Verified Claims Only</div>
            <p className="text-emerald-800 mt-0.5">
              AI engines are prohibited from independently validating claims without registered evidence.
              {missingEvidenceCount > 0 && ` (${missingEvidenceCount} claims missing evidence)`}
            </p>
          </div>
        </div>

        <div className="p-4 bg-amber-50/70 rounded-xl border border-amber-200 flex items-center space-x-3 text-xs text-amber-950">
          <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0" />
          <div>
            <div className="font-bold">{expiredCount} Expired Claims Flagged</div>
            <p className="text-amber-800 mt-0.5">Expired claims trigger critical warnings when detected in content submissions.</p>
          </div>
        </div>
      </div>

      <FilterBar
        searchValue={search}
        onSearchChange={setSearch}
        filters={[
          {
            label: 'Claim Status',
            value: statusFilter,
            onChange: setStatusFilter,
            options: [
              { label: 'All Claim Statuses', value: 'ALL' },
              { label: 'Approved & Active', value: 'APPROVED' },
              { label: 'Expired', value: 'EXPIRED' },
              { label: 'Under Review', value: 'UNDER_REVIEW' },
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
          <LoadingSpinner label="Loading claims ledger..." />
        </div>
      ) : filtered.length === 0 ? (
        <Card className="p-8">
          <EmptyState
            title="No approved claims found"
            description={
              search || statusFilter !== 'ALL'
                ? 'No claims match your search filters.'
                : 'No verified claims have been registered for this workspace yet.'
            }
            action={
              canManageClaims && !search && statusFilter === 'ALL' ? (
                <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                  <Plus className="w-4 h-4 mr-1.5" />
                  Register First Claim
                </Button>
              ) : null
            }
          />
        </Card>
      ) : (
        <DataTable
          columns={columns}
          data={filtered}
          keyField="id"
        />
      )}

      {/* Register Claim Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Register Approved Brand Claim"
        description="Add a verified claim, link it to an approved source, and define the conditions under which it can be cited."
      >
        <form onSubmit={handleAddClaim} className="space-y-4">
          <FormField label="Exact Claim Text" id="claim" required>
            <textarea
              id="claim"
              rows={2}
              value={newClaimText}
              onChange={(e) => setNewClaimText(e.target.value)}
              placeholder='e.g. "Acme Cloud delivers 99.995% guaranteed uptime across multi-region clusters."'
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              required
            />
          </FormField>

          <Input
            label="Supporting Source Document Reference"
            value={newSource}
            onChange={(e) => setNewSource(e.target.value)}
            placeholder="e.g. Acme Cloud SLA Specification v4.1 (src-101)"
            required
          />

          <FormField label="Scope & Conditions of Use" id="scope" required>
            <textarea
              id="scope"
              rows={2}
              value={newScope}
              onChange={(e) => setNewScope(e.target.value)}
              placeholder="e.g. Valid only for Enterprise Tier customers in US and EU regions."
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              required
            />
          </FormField>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Owner / Department"
              value={newOwner}
              onChange={(e) => setNewOwner(e.target.value)}
              placeholder="e.g. Product Marketing"
            />

            <Input
              label="Expiry / Re-certification Date"
              type="date"
              value={newExpiry}
              onChange={(e) => setNewExpiry(e.target.value)}
            />
          </div>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" variant="primary" type="submit">
              Register Claim
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
