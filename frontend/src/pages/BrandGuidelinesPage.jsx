import { useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import StatusBadge from '../components/common/StatusBadge';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { MOCK_GUIDELINES } from '../mock/mockData';
import { BookOpen, Plus, ShieldAlert, Sparkles, AlertCircle } from 'lucide-react';

export default function BrandGuidelinesPage() {
  const { activeBrand, user } = useAuth();
  const { success } = useToast();
  const canEditGuidelines = user?.role === 'BRAND_MANAGER' || user?.role === 'ADMIN';

  const [activeTab, setActiveTab] = useState('prohibited');
  const [search, setSearch] = useState('');
  const [severityFilter, setSeverityFilter] = useState('ALL');

  const [prohibitedTerms, setProhibitedTerms] = useState(MOCK_GUIDELINES.prohibitedTerms);
  const requiredPhrases = MOCK_GUIDELINES.requiredPhrases;
  const toneRules = MOCK_GUIDELINES.toneRules;

  const [isTermModalOpen, setIsTermModalOpen] = useState(false);
  const [newTerm, setNewTerm] = useState('');
  const [newReason, setNewReason] = useState('');
  const [newAlternative, setNewAlternative] = useState('');
  const [newSeverity, setNewSeverity] = useState('CRITICAL');

  const filteredTerms = prohibitedTerms.filter((item) => {
    const matchesSearch =
      item.term.toLowerCase().includes(search.toLowerCase()) ||
      item.reason.toLowerCase().includes(search.toLowerCase());
    const matchesSeverity = severityFilter === 'ALL' || item.severity === severityFilter;
    return matchesSearch && matchesSeverity;
  });

  const handleAddTerm = (e) => {
    e.preventDefault();
    if (!newTerm.trim()) return;

    const termObj = {
      id: `term-${Date.now().toString(36)}`,
      term: newTerm,
      severity: newSeverity,
      reason: newReason || 'Prohibited by brand communication standards.',
      suggestedAlternative: newAlternative || 'Use compliant enterprise phrasing.',
    };

    setProhibitedTerms((prev) => [termObj, ...prev]);
    setIsTermModalOpen(false);
    setNewTerm('');
    setNewReason('');
    setNewAlternative('');
    success('Guideline Rule Added', `Added "${termObj.term}" to prohibited vocabulary.`);
  };

  const termColumns = [
    {
      key: 'term',
      header: 'Prohibited Expression',
      render: (val, row) => (
        <div>
          <span className="font-bold text-rose-900 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
            &ldquo;{val}&rdquo;
          </span>
          <div className="text-[11px] text-ink-500 mt-1">{row.id}</div>
        </div>
      ),
    },
    {
      key: 'severity',
      header: 'Severity',
      render: (val) => <StatusBadge status={val} />,
    },
    {
      key: 'reason',
      header: 'Governance Rationale',
      render: (val) => <span className="text-xs text-ink-700 leading-relaxed">{val}</span>,
    },
    {
      key: 'suggestedAlternative',
      header: 'Approved Alternative',
      render: (val) => (
        <span className="font-medium text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
          {val}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`${activeBrand?.name || 'Acme'} Brand Guidelines`}
        description="Active vocabulary standards, prohibited terminology, required legal disclaimers, and corporate tone constraints."
        badge={MOCK_GUIDELINES.version}
        actions={
          canEditGuidelines ? (
            <Button size="sm" variant="primary" onClick={() => setIsTermModalOpen(true)}>
              <Plus className="w-4 h-4 mr-1.5" />
              Add Prohibited Term
            </Button>
          ) : null
        }
      />

      {!canEditGuidelines && (
        <div className="p-3 bg-ink-100/70 rounded-xl border border-ink-200 text-xs text-ink-600 flex items-center justify-between">
          <span>
            <strong>Read-only mode:</strong> You are viewing brand guidelines with <strong>{user?.role?.replace('_', ' ')}</strong> permissions. Updating guidelines and prohibited terms requires Brand Manager or Administrator privileges.
          </span>
        </div>
      )}

      {/* Guideline metadata banner */}
      <div className="p-4 bg-white rounded-xl border border-ink-200/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
        <div className="flex items-center space-x-2">
          <BookOpen className="w-4 h-4 text-brand-600" />
          <span className="text-ink-600">
            Active Version: <strong className="text-ink-900">{MOCK_GUIDELINES.version}</strong> • Last reviewed:{' '}
            <span className="text-ink-700">{MOCK_GUIDELINES.lastUpdated}</span> by{' '}
            <strong className="text-ink-900">{MOCK_GUIDELINES.updatedBy}</strong>
          </span>
        </div>
        <Badge variant="brand" size="sm">Deterministic Engine Active</Badge>
      </div>

      {/* Tabs */}
      <div className="border-b border-ink-200 flex space-x-6 text-xs font-semibold">
        <button
          type="button"
          onClick={() => setActiveTab('prohibited')}
          className={`pb-3 border-b-2 transition-colors flex items-center space-x-1.5 ${
            activeTab === 'prohibited'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-ink-500 hover:text-ink-800'
          }`}
        >
          <ShieldAlert className="w-4 h-4" />
          <span>Prohibited Expressions ({prohibitedTerms.length})</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('required')}
          className={`pb-3 border-b-2 transition-colors flex items-center space-x-1.5 ${
            activeTab === 'required'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-ink-500 hover:text-ink-800'
          }`}
        >
          <AlertCircle className="w-4 h-4" />
          <span>Required Phrases & Disclaimers ({requiredPhrases.length})</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('tone')}
          className={`pb-3 border-b-2 transition-colors flex items-center space-x-1.5 ${
            activeTab === 'tone'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-ink-500 hover:text-ink-800'
          }`}
        >
          <Sparkles className="w-4 h-4" />
          <span>Tone & Voice Rules ({toneRules.length})</span>
        </button>
      </div>

      {/* Tab 1: Prohibited Expressions */}
      {activeTab === 'prohibited' && (
        <div className="space-y-4">
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

          <DataTable
            columns={termColumns}
            data={filteredTerms}
            keyField="id"
          />
        </div>
      )}

      {/* Tab 2: Required Phrases */}
      {activeTab === 'required' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {requiredPhrases.map((req) => (
            <Card key={req.id} className="border-ink-200">
              <div className="flex items-center justify-between pb-2 border-b border-ink-100">
                <span className="text-xs font-semibold text-ink-600 uppercase tracking-wider">
                  {req.context}
                </span>
                <Badge variant="brand" size="sm">Mandatory</Badge>
              </div>
              <div className="mt-3 p-3 bg-brand-50/50 border border-brand-200 rounded-lg text-xs font-medium text-ink-900 leading-relaxed">
                &ldquo;{req.phrase}&rdquo;
              </div>
              <div className="mt-2 text-[11px] text-ink-500">
                Must be included verbatim when publishing forward-looking or SLA statements.
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Tab 3: Tone & Voice Rules */}
      {activeTab === 'tone' && (
        <div className="space-y-4">
          {toneRules.map((rule, idx) => (
            <Card key={idx} title={rule.attribute} subtitle={rule.description}>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
                <div className="p-3 bg-emerald-50/60 border border-emerald-200 rounded-lg">
                  <span className="text-[10px] font-bold uppercase text-emerald-800 tracking-wider block mb-1">
                    ✓ Recommended (Brand-Aligned):
                  </span>
                  <p className="text-xs text-emerald-950 font-medium leading-relaxed">
                    &ldquo;{rule.doExample}&rdquo;
                  </p>
                </div>

                <div className="p-3 bg-rose-50/60 border border-rose-200 rounded-lg">
                  <span className="text-[10px] font-bold uppercase text-rose-800 tracking-wider block mb-1">
                    ✗ Avoid (Violates Tone):
                  </span>
                  <p className="text-xs text-rose-950 font-medium leading-relaxed">
                    &ldquo;{rule.dontExample}&rdquo;
                  </p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Add Prohibited Term Modal */}
      <Modal
        isOpen={isTermModalOpen}
        onClose={() => setIsTermModalOpen(false)}
        title="Add Prohibited Expression"
        description="Define a prohibited word or phrase to be automatically flagged by the governance engine."
      >
        <form onSubmit={handleAddTerm} className="space-y-4">
          <Input
            label="Prohibited Expression"
            value={newTerm}
            onChange={(e) => setNewTerm(e.target.value)}
            placeholder='e.g. "magic AI" or "unbreakable"'
            required
          />

          <FormField label="Violation Severity" id="sev">
            <select
              id="sev"
              value={newSeverity}
              onChange={(e) => setNewSeverity(e.target.value)}
              className="w-full px-3 py-2 text-xs bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="CRITICAL">Critical (Blocks submission / high risk)</option>
              <option value="WARNING">Warning (Tone degradation)</option>
              <option value="INFO">Info (Advisory)</option>
            </select>
          </FormField>

          <Input
            label="Approved Alternative"
            value={newAlternative}
            onChange={(e) => setNewAlternative(e.target.value)}
            placeholder="e.g. enterprise-grade machine learning"
          />

          <FormField label="Governance Rationale" id="reason">
            <textarea
              id="reason"
              rows={3}
              value={newReason}
              onChange={(e) => setNewReason(e.target.value)}
              placeholder="Why this phrase creates risk, confuses customers, or contradicts brand voice..."
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </FormField>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsTermModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" variant="primary" type="submit">
              Save Rule
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
