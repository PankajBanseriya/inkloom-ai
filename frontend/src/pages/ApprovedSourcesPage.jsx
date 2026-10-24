import { useState, useEffect, useCallback } from 'react';
import PageHeader from '../components/common/PageHeader';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import StatusBadge from '../components/common/StatusBadge';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import EmptyState from '../components/feedback/EmptyState';
import Card from '../components/common/Card';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import brandService from '../services/brandService';
import { Plus, ExternalLink, ShieldCheck, RefreshCw, Trash2 } from 'lucide-react';

export default function ApprovedSourcesPage() {
  const { activeBrand, user } = useAuth();
  const { success, error: toastError, info } = useToast();
  const canManageSources = user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [sources, setSources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [newTitle, setNewTitle] = useState('');
  const [newType, setNewType] = useState('POLICY');
  const [newOwner, setNewOwner] = useState('');
  const [newVersion, setNewVersion] = useState('1.0');
  const [newExcerpt, setNewExcerpt] = useState('');
  const [newUrl, setNewUrl] = useState('');

  const fetchSources = useCallback(async () => {
    if (!activeBrand?.id) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const data = await brandService.getSources(activeBrand.id);
      setSources(data || []);
    } catch (err) {
      console.warn('Failed to load approved sources:', err.message);
      setSources([]);
    } finally {
      setLoading(false);
    }
  }, [activeBrand?.id]);

  useEffect(() => {
    fetchSources();
  }, [fetchSources]);

  const filtered = sources.filter((s) => {
    const titleStr = s.title || '';
    const excerptStr = s.excerpt || '';
    const ownerStr = s.owner || '';
    const matchesSearch =
      titleStr.toLowerCase().includes(search.toLowerCase()) ||
      excerptStr.toLowerCase().includes(search.toLowerCase()) ||
      ownerStr.toLowerCase().includes(search.toLowerCase());
    const matchesType = typeFilter === 'ALL' || s.sourceType === typeFilter;
    return matchesSearch && matchesType;
  });

  const handleAddSource = async (e) => {
    e.preventDefault();
    if (!newTitle.trim() || !activeBrand?.id) return;

    setIsSubmitting(true);
    const payload = {
      title: newTitle.trim(),
      sourceType: newType,
      owner: newOwner.trim() || 'Compliance Department',
      version: newVersion.trim() || '1.0',
      excerpt: newExcerpt.trim() || 'Approved factual benchmark record.',
      referenceUrl: newUrl.trim() || 'https://docs.acme.internal/spec.pdf',
    };

    try {
      const saved = await brandService.createSource(activeBrand.id, payload);
      setSources((prev) => [saved, ...prev]);
      setIsModalOpen(false);
      setNewTitle('');
      setNewExcerpt('');
      setNewUrl('');
      setNewOwner('');
      success('Approved Source Registered', `Registered "${saved.title}" for evidence verification.`);
    } catch (err) {
      toastError('Registration Failed', err.message || 'Unable to register source');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteSource = async (sourceId) => {
    if (!canManageSources || !activeBrand?.id) return;
    try {
      await brandService.deleteSource(activeBrand.id, sourceId);
      setSources((prev) => prev.filter((s) => s.id !== sourceId));
      info('Source Deleted', 'Approved source removed from governance ledger.');
    } catch (err) {
      toastError('Deletion Failed', err.message || 'Unable to delete source');
    }
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
            <span>v{row.version || '1.0'}</span>
            {row.referenceUrl && (
              <>
                <span>•</span>
                <a
                  href={row.referenceUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-brand-600 flex items-center hover:underline"
                >
                  <ExternalLink className="w-3 h-3 mr-0.5" />
                  Reference Link
                </a>
              </>
            )}
          </div>
        </div>
      ),
    },
    {
      key: 'sourceType',
      header: 'Document Type',
      render: (val) => (
        <span className="font-semibold text-xs text-ink-700 bg-ink-100 px-2 py-0.5 rounded border border-ink-200">
          {(val || 'POLICY').replace(/_/g, ' ')}
        </span>
      ),
    },
    {
      key: 'owner',
      header: 'Department Owner',
      render: (val) => <span className="font-medium text-ink-800 text-xs">{val || 'Unassigned'}</span>,
    },
    {
      key: 'status',
      header: 'Verification Status',
      render: (val) => <StatusBadge status={val || 'APPROVED'} />,
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (_, row) => (
        canManageSources ? (
          <Button
            size="sm"
            variant="outline"
            onClick={() => handleDeleteSource(row.id)}
            className="text-[11px] px-2 py-1 h-7 text-rose-600 hover:text-rose-700 hover:bg-rose-50"
            title="Delete approved source"
          >
            <Trash2 className="w-3 h-3" />
          </Button>
        ) : (
          <span className="text-[11px] text-ink-400 italic">View only</span>
        )
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${activeBrand?.name || 'Workspace'} Approved Sources & Evidence`}
        description="Ground-truth documentation repository used to substantiate factual claims and verify evidence citations."
        actions={
          <div className="flex items-center space-x-2">
            <Button size="sm" variant="outline" onClick={fetchSources} className="text-xs">
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
            {canManageSources && (
              <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                <Plus className="w-4 h-4 mr-1.5" />
                Register Approved Source
              </Button>
            )}
          </div>
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

      {loading ? (
        <div className="py-16 flex justify-center bg-white rounded-xl border border-ink-200">
          <LoadingSpinner label="Loading approved sources from database..." />
        </div>
      ) : filtered.length === 0 ? (
        <Card className="py-12">
          <EmptyState
            title="No Approved Sources Registered"
            description="Verified documents and technical specifications ground claims in empirical evidence. Register source documents to enable evidence verification."
            action={
              canManageSources ? (
                <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                  <Plus className="w-4 h-4 mr-1.5" />
                  Register First Source
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
            <Button size="sm" variant="primary" type="submit" isLoading={isSubmitting}>
              Register Document
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
