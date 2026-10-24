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
import { MOCK_APPROVED_SOURCES } from '../mock/mockData';
import { Plus, ExternalLink, ShieldCheck } from 'lucide-react';

export default function ApprovedSourcesPage() {
  const { activeBrand, user } = useAuth();
  const { success } = useToast();
  const canManageSources = user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [sources, setSources] = useState(MOCK_APPROVED_SOURCES);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [newTitle, setNewTitle] = useState('');
  const [newType, setNewType] = useState('POLICY');
  const [newOwner, setNewOwner] = useState('');
  const [newVersion, setNewVersion] = useState('1.0');
  const [newExcerpt, setNewExcerpt] = useState('');
  const [newUrl, setNewUrl] = useState('');

  const filtered = sources.filter((s) => {
    const matchesSearch =
      s.title.toLowerCase().includes(search.toLowerCase()) ||
      s.excerpt.toLowerCase().includes(search.toLowerCase()) ||
      s.owner.toLowerCase().includes(search.toLowerCase());
    const matchesType = typeFilter === 'ALL' || s.sourceType === typeFilter;
    return matchesSearch && matchesType;
  });

  const handleAddSource = (e) => {
    e.preventDefault();
    if (!newTitle.trim()) return;

    const sourceObj = {
      id: `src-${Date.now().toString(36)}`,
      title: newTitle,
      sourceType: newType,
      owner: newOwner || 'Compliance Department',
      status: 'APPROVED',
      version: newVersion || '1.0',
      lastReviewed: new Date().toISOString().split('T')[0],
      referenceUrl: newUrl || 'https://docs.acme.internal/spec.pdf',
      excerpt: newExcerpt || 'Approved factual benchmark record.',
    };

    setSources((prev) => [sourceObj, ...prev]);
    setIsModalOpen(false);
    setNewTitle('');
    setNewExcerpt('');
    setNewUrl('');
    success('Approved Source Registered', `Registered "${sourceObj.title}" for evidence verification.`);
  };

  const columns = [
    {
      key: 'title',
      header: 'Source Document Title',
      render: (val, row) => (
        <div className="max-w-md">
          <div className="font-bold text-ink-900">{val}</div>
          <div className="text-[11px] text-ink-500 mt-0.5 line-clamp-2 italic">
            &ldquo;{row.excerpt}&rdquo;
          </div>
          <div className="flex items-center space-x-2 mt-1.5 text-[10px] text-ink-400">
            <span className="font-mono">{row.id}</span>
            <span>•</span>
            <span>v{row.version}</span>
            <span>•</span>
            <span className="text-brand-600 flex items-center hover:underline cursor-pointer">
              <ExternalLink className="w-3 h-3 mr-0.5" />
              Reference Link
            </span>
          </div>
        </div>
      ),
    },
    {
      key: 'sourceType',
      header: 'Document Type',
      render: (val) => (
        <span className="font-semibold text-xs text-ink-700 bg-ink-100 px-2 py-0.5 rounded border border-ink-200">
          {val.replace(/_/g, ' ')}
        </span>
      ),
    },
    {
      key: 'owner',
      header: 'Department Owner',
      render: (val) => <span className="font-medium text-ink-800">{val}</span>,
    },
    {
      key: 'status',
      header: 'Verification Status',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'lastReviewed',
      header: 'Last Reviewed',
      render: (val) => <span className="text-xs text-ink-500">{val}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${activeBrand?.name || 'Acme'} Approved Sources & Evidence`}
        description="Ground-truth documentation repository used to substantiate factual claims and verify evidence citations."
        actions={
          canManageSources ? (
            <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
              <Plus className="w-4 h-4 mr-1.5" />
              Register Approved Source
            </Button>
          ) : null
        }
      />

      {!canManageSources && (
        <div className="p-3 bg-ink-100/70 rounded-xl border border-ink-200 text-xs text-ink-600 flex items-center justify-between">
          <span>
            <strong>Read-only mode:</strong> You are viewing approved sources with <strong>{user?.role?.replace('_', ' ')}</strong> permissions. Registering or updating ground-truth sources requires Brand Manager or Administrator privileges.
          </span>
        </div>
      )}

      <div className="p-4 bg-white rounded-xl border border-ink-200/80 flex items-center justify-between text-xs">
        <div className="flex items-center space-x-2">
          <ShieldCheck className="w-4 h-4 text-emerald-600" />
          <span className="text-ink-600">
            Zero-Hallucination Evidence Rule: Every factual claim made in marketing communications must trace to one of these approved sources.
          </span>
        </div>
        <span className="text-ink-500 font-semibold">{sources.length} Verified Sources Active</span>
      </div>

      <FilterBar
        searchValue={search}
        onSearchChange={setSearch}
        filters={[
          {
            label: 'Document Type',
            value: typeFilter,
            onChange: setTypeFilter,
            options: [
              { label: 'All Document Types', value: 'ALL' },
              { label: 'Policy Specification', value: 'POLICY' },
              { label: 'Research Report', value: 'RESEARCH_REPORT' },
              { label: 'Legal Disclosure', value: 'LEGAL_DISCLOSURE' },
              { label: 'Product Spec', value: 'PRODUCT_SPEC' },
            ],
          },
        ]}
        onReset={() => {
          setSearch('');
          setTypeFilter('ALL');
        }}
      />

      <DataTable
        columns={columns}
        data={filtered}
        keyField="id"
      />

      {/* Register Source Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Register Approved Evidence Source"
        description="Add a verified technical report, legal disclosure, or policy document into the brand governance ledger."
      >
        <form onSubmit={handleAddSource} className="space-y-4">
          <Input
            label="Document Title"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            placeholder="e.g. Q4 2026 Enterprise Security Whitepaper"
            required
          />

          <div className="grid grid-cols-2 gap-3">
            <FormField label="Document Type" id="doctype">
              <select
                id="doctype"
                value={newType}
                onChange={(e) => setNewType(e.target.value)}
                className="w-full px-3 py-2 text-xs bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              >
                <option value="POLICY">Policy Document</option>
                <option value="RESEARCH_REPORT">Research / Benchmark Report</option>
                <option value="LEGAL_DISCLOSURE">Legal Disclosure</option>
                <option value="PRODUCT_SPEC">Product Specification</option>
              </select>
            </FormField>

            <Input
              label="Version String"
              value={newVersion}
              onChange={(e) => setNewVersion(e.target.value)}
              placeholder="e.g. 2.4"
            />
          </div>

          <Input
            label="Department Owner"
            value={newOwner}
            onChange={(e) => setNewOwner(e.target.value)}
            placeholder="e.g. Infrastructure Security Council"
          />

          <Input
            label="Internal URL / URI Reference"
            value={newUrl}
            onChange={(e) => setNewUrl(e.target.value)}
            placeholder="https://docs.acme.internal/audit.pdf"
          />

          <FormField label="Key Factual Statements / Excerpt" id="excerpt" required>
            <textarea
              id="excerpt"
              rows={3}
              value={newExcerpt}
              onChange={(e) => setNewExcerpt(e.target.value)}
              placeholder="Paste exact statements or data points from this source that are permitted to be cited..."
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
              required
            />
          </FormField>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" variant="primary" type="submit">
              Register Document
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
