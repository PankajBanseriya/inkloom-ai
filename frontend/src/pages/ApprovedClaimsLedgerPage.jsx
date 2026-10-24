import { useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import StatusBadge from '../components/common/StatusBadge';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { MOCK_APPROVED_CLAIMS } from '../mock/mockData';
import { Plus, CheckCircle2, AlertTriangle } from 'lucide-react';

export default function ApprovedClaimsLedgerPage() {
  const { activeBrand, user } = useAuth();
  const { success } = useToast();
  const canManageClaims = user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [claims, setClaims] = useState(MOCK_APPROVED_CLAIMS);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [newClaimText, setNewClaimText] = useState('');
  const [newSource, setNewSource] = useState('');
  const [newScope, setNewScope] = useState('');
  const [newOwner, setNewOwner] = useState('');
  const [newExpiry, setNewExpiry] = useState('');

  const filtered = claims.filter((c) => {
    const matchesSearch =
      c.claimText.toLowerCase().includes(search.toLowerCase()) ||
      c.supportingSource.toLowerCase().includes(search.toLowerCase()) ||
      c.scope.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleAddClaim = (e) => {
    e.preventDefault();
    if (!newClaimText.trim()) return;

    const claimObj = {
      id: `clm-${Date.now().toString(36)}`,
      claimText: newClaimText,
      supportingSource: newSource || 'General Company Specifications',
      scope: newScope || 'All standard enterprise communications.',
      status: 'APPROVED',
      owner: newOwner || 'Product Marketing',
      reviewDate: new Date().toISOString().split('T')[0],
      expiryDate: newExpiry || '2027-10-24',
    };

    setClaims((prev) => [claimObj, ...prev]);
    setIsModalOpen(false);
    setNewClaimText('');
    setNewSource('');
    setNewScope('');
    setNewOwner('');
    setNewExpiry('');
    success('Approved Claim Recorded', `Claim "${claimObj.id}" added to the central governance ledger.`);
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
          <div className="text-[11px] text-ink-500 mt-1 flex items-center space-x-1.5">
            <span className="font-mono text-[10px] text-ink-400">{row.id}</span>
            <span>•</span>
            <span className="text-brand-600 font-medium">{row.supportingSource}</span>
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
      render: (val) => <span className="font-medium text-ink-800">{val}</span>,
    },
    {
      key: 'dates',
      header: 'Review & Expiration',
      render: (_, row) => (
        <div className="text-[11px] text-ink-500">
          <div>Reviewed: {row.reviewDate}</div>
          <div className={row.status === 'EXPIRED' ? 'text-rose-600 font-semibold' : 'text-ink-600'}>
            Expires: {row.expiryDate}
          </div>
        </div>
      ),
    },
  ];

  const expiredCount = claims.filter((c) => c.status === 'EXPIRED').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${activeBrand?.name || 'Acme'} Approved Claims Ledger`}
        description="Centralized organization ledger of approved marketing and product claims. Content referencing unapproved or expired claims is automatically flagged."
        actions={
          canManageClaims ? (
            <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
              <Plus className="w-4 h-4 mr-1.5" />
              Register Approved Claim
            </Button>
          ) : null
        }
      />

      {!canManageClaims && (
        <div className="p-3 bg-ink-100/70 rounded-xl border border-ink-200 text-xs text-ink-600 flex items-center justify-between">
          <span>
            <strong>Read-only mode:</strong> You are viewing approved claims with <strong>{user?.role?.replace('_', ' ')}</strong> permissions. Registering or updating claims in the ledger requires Brand Manager or Administrator privileges.
          </span>
        </div>
      )}

      {/* Advisory Banner */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="p-4 bg-emerald-50/70 rounded-xl border border-emerald-200 flex items-center space-x-3 text-xs text-emerald-950">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          <div>
            <div className="font-bold">Human Verified Claims Only</div>
            <p className="text-emerald-800 mt-0.5">AI engines are prohibited from independently validating claims without registered evidence.</p>
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

      <DataTable
        columns={columns}
        data={filtered}
        keyField="id"
      />

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
