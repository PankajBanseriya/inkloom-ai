import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import FormField from '../components/common/FormField';
import EvidenceCard from '../features/evidence/EvidenceCard';
import IntentLockComparison from '../features/revisions/IntentLockComparison';
import Modal from '../components/common/Modal';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ROUTES } from '../constants/routes';
import { MOCK_REVIEWS } from '../mock/mockData';
import {
  Sparkles,
  Send,
  Save,
  RotateCcw,
  ShieldCheck,
} from 'lucide-react';

export default function ContentReviewPage() {
  const navigate = useNavigate();
  const { activeBrand, brands, switchBrand } = useAuth();
  const { success, warning, info } = useToast();

  const defaultReview = MOCK_REVIEWS[0];

  const [contentType, setContentType] = useState('Press Release');
  const [contentTitle, setContentTitle] = useState(defaultReview.title);
  const [content, setContent] = useState(defaultReview.originalContent);
  const originalContent = defaultReview.originalContent;

  const [status, setStatus] = useState('DRAFT'); // DRAFT, REVIEWING, COMPLETED
  const [findings, setFindings] = useState(defaultReview.findings);
  const [selectedStrengths, setSelectedStrengths] = useState({
    'find-01': 'balanced',
    'find-02': 'balanced',
    'find-03': 'balanced',
    'find-04': 'balanced',
  });

  const [severityFilter, setSeverityFilter] = useState('ALL');
  const [activeComparisonFinding, setActiveComparisonFinding] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingFindingId, setEditingFindingId] = useState(null);
  const [customEditText, setCustomEditText] = useState('');

  // Handle Review Content Trigger
  const handleReviewContent = () => {
    if (!content.trim()) {
      warning('Empty Content', 'Please provide or paste text before running brand review.');
      return;
    }

    setStatus('REVIEWING');
    info('Running Brand Governance Pipeline', 'Evaluating deterministic rules and verified claims...');

    setTimeout(() => {
      setStatus('COMPLETED');
      setFindings(defaultReview.findings);
      success('Brand Analysis Complete', `Identified ${defaultReview.findings.length} findings requiring governance review.`);
    }, 900);
  };

  // Intent Lock: Accept Suggestion
  const handleAcceptCorrection = (findingId, replacementText) => {
    const targetFinding = findings.find((f) => f.id === findingId);
    if (!targetFinding) return;

    // Surgical replacement of the excerpt in currentContent
    const updatedContent = content.replace(targetFinding.excerpt, replacementText);
    setContent(updatedContent);

    // Remove finding from list
    setFindings((prev) => prev.filter((f) => f.id !== findingId));
    setActiveComparisonFinding(null);
    success('Correction Accepted', `Replaced "${targetFinding.excerpt}" with compliant phrasing.`);
  };

  // Reject Finding
  const handleRejectCorrection = (findingId) => {
    setFindings((prev) => prev.filter((f) => f.id !== findingId));
    if (activeComparisonFinding?.id === findingId) {
      setActiveComparisonFinding(null);
    }
    info('Finding Dismissed', `Finding ${findingId} was dismissed without changes to text.`);
  };

  // Open Edit Dialog for Finding
  const handleEditCorrection = (findingId, currentText) => {
    setEditingFindingId(findingId);
    setCustomEditText(currentText);
    setIsEditModalOpen(true);
  };

  // Save Custom Edit
  const handleSaveCustomEdit = () => {
    if (!editingFindingId || !customEditText.trim()) return;
    handleAcceptCorrection(editingFindingId, customEditText);
    setIsEditModalOpen(false);
  };

  // Save Revision
  const handleSaveRevision = () => {
    success('Revision Saved', 'New revision record stored in audit ledger.');
  };

  // Submit for Approval
  const handleSubmitForApproval = () => {
    if (findings.some((f) => f.severity === 'CRITICAL')) {
      warning(
        'Critical Issues Pending',
        'Your submission contains critical brand violations that should be resolved or explicitly justified before requesting approval.'
      );
    }
    success('Submitted to Approval Queue', 'Governance approver has been notified.');
    navigate(ROUTES.APPROVALS);
  };

  // Strength Selection per Finding
  const handleSelectStrength = (findingId, strength) => {
    setSelectedStrengths((prev) => ({ ...prev, [findingId]: strength }));
  };

  const filteredFindings = findings.filter((f) => {
    if (severityFilter === 'ALL') return true;
    return f.severity === severityFilter;
  });

  const wordCount = content.trim() ? content.trim().split(/\s+/).length : 0;
  const charCount = content.length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Content Review Workspace"
        description="Author, paste, and evaluate communications against brand guidelines, approved claims, and legal disclaimers before human approval."
        badge={status === 'COMPLETED' ? 'Review Complete' : status === 'REVIEWING' ? 'Analyzing...' : 'Draft Mode'}
        badgeVariant={status === 'COMPLETED' ? 'success' : status === 'REVIEWING' ? 'warning' : 'neutral'}
        actions={
          <div className="flex items-center space-x-2">
            <Button size="sm" variant="outline" onClick={handleSaveRevision}>
              <Save className="w-4 h-4 mr-1.5" />
              Save Revision
            </Button>
            <Button size="sm" variant="primary" onClick={handleSubmitForApproval}>
              <Send className="w-4 h-4 mr-1.5" />
              Submit for Approval
            </Button>
          </div>
        }
      />

      {/* Editor & Findings Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Editor & Controls (7 cols) */}
        <div className="lg:col-span-7 space-y-4">
          <Card className="border-ink-200">
            {/* Top configuration row */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pb-4 border-b border-ink-100">
              <FormField label="Target Brand Workspace" id="brand-select">
                <select
                  id="brand-select"
                  value={activeBrand?.id || ''}
                  onChange={(e) => switchBrand(e.target.value)}
                  className="w-full px-3 py-1.5 text-xs bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
                >
                  {brands.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.name}
                    </option>
                  ))}
                </select>
              </FormField>

              <FormField label="Content Type" id="content-type">
                <select
                  id="content-type"
                  value={contentType}
                  onChange={(e) => setContentType(e.target.value)}
                  className="w-full px-3 py-1.5 text-xs bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
                >
                  <option value="Press Release">Press Release</option>
                  <option value="Marketing Copy">Marketing Copy</option>
                  <option value="Technical Blog Post">Technical Blog Post</option>
                  <option value="Product Documentation">Product Documentation</option>
                  <option value="Email Newsletter">Email Newsletter</option>
                </select>
              </FormField>
            </div>

            <div className="pt-3">
              <input
                type="text"
                value={contentTitle}
                onChange={(e) => setContentTitle(e.target.value)}
                placeholder="Submission Title..."
                className="w-full text-base font-bold text-ink-900 border-b border-transparent hover:border-ink-200 focus:border-brand-500 focus:outline-none pb-1 transition-colors"
              />
            </div>

            {/* Text Editor Area */}
            <div className="mt-3 relative">
              <textarea
                rows={12}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Paste or compose your marketing copy, announcement, or technical communication here..."
                className="w-full p-4 text-xs font-sans leading-relaxed text-ink-900 bg-white border border-ink-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500 resize-y"
              />
            </div>

            {/* Editor Stats Footer & Review Trigger */}
            <div className="mt-3 pt-3 border-t border-ink-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-ink-500">
              <div className="flex items-center space-x-3">
                <span>{wordCount} words</span>
                <span>•</span>
                <span>{charCount} characters</span>
                <span>•</span>
                <span className="text-emerald-700 font-medium">Original Preserved</span>
              </div>

              <div className="flex items-center space-x-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setContent(originalContent);
                    info('Content Reset', 'Restored to original draft text.');
                  }}
                  className="text-xs"
                >
                  <RotateCcw className="w-3.5 h-3.5 mr-1 text-ink-500" />
                  Reset
                </Button>
                <Button
                  size="sm"
                  variant="primary"
                  onClick={handleReviewContent}
                  isLoading={status === 'REVIEWING'}
                  className="text-xs font-semibold"
                >
                  <Sparkles className="w-3.5 h-3.5 mr-1.5" />
                  Review Content
                </Button>
              </div>
            </div>
          </Card>

          {/* Active Intent Lock Comparison Component (When Finding Selected) */}
          {activeComparisonFinding && (
            <IntentLockComparison
              originalText={activeComparisonFinding.excerpt}
              corrections={activeComparisonFinding.suggestedCorrections}
              rationale={activeComparisonFinding.explanation}
              onAccept={({ text }) => handleAcceptCorrection(activeComparisonFinding.id, text)}
              onReject={() => setActiveComparisonFinding(null)}
              onEdit={({ text }) => handleEditCorrection(activeComparisonFinding.id, text)}
            />
          )}
        </div>

        {/* Right Column: Governance Findings & Evidence Cards (5 cols) */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-white p-4 rounded-xl border border-ink-200 shadow-sm flex items-center justify-between">
            <div>
              <h2 className="text-sm font-bold text-ink-900">Governance Findings</h2>
              <p className="text-[11px] text-ink-500">
                {findings.length === 0
                  ? 'All clear! No brand violations detected.'
                  : `${findings.length} issues identified in draft`}
              </p>
            </div>

            {/* Severity Filter */}
            {findings.length > 0 && (
              <select
                value={severityFilter}
                onChange={(e) => setSeverityFilter(e.target.value)}
                className="px-2 py-1 text-xs bg-ink-50 border border-ink-300 rounded-md text-ink-800 focus:outline-none"
              >
                <option value="ALL">All Severities</option>
                <option value="CRITICAL">Critical Only</option>
                <option value="WARNING">Warning Only</option>
              </select>
            )}
          </div>

          {/* Findings List */}
          {findings.length === 0 ? (
            <Card className="text-center py-12">
              <ShieldCheck className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-ink-900">Compliant with Guidelines</h3>
              <p className="text-xs text-ink-500 mt-1 max-w-xs mx-auto">
                No prohibited terminology, tone violations, or unverified claims detected. You can submit this content for final sign-off.
              </p>
            </Card>
          ) : (
            <div className="space-y-4">
              {filteredFindings.map((finding) => (
                <div key={finding.id} className="relative">
                  <EvidenceCard
                    finding={finding}
                    selectedStrength={selectedStrengths[finding.id] || 'balanced'}
                    onSelectStrength={handleSelectStrength}
                    onAcceptCorrection={handleAcceptCorrection}
                    onRejectCorrection={handleRejectCorrection}
                    onEditCorrection={handleEditCorrection}
                  />
                  <div className="mt-1.5 text-right">
                    <button
                      type="button"
                      onClick={() => setActiveComparisonFinding(finding)}
                      className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 hover:underline inline-flex items-center"
                    >
                      Compare in Intent Lock Diff View →
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Manual Edit Suggestion Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title="Custom Intent-Locked Correction"
        description="Fine-tune the proposed fix while keeping the author's meaning and brand rules aligned."
      >
        <div className="space-y-4">
          <FormField label="Adjust Proposed Phrasing" id="custom-text">
            <textarea
              id="custom-text"
              rows={4}
              value={customEditText}
              onChange={(e) => setCustomEditText(e.target.value)}
              className="w-full text-xs p-3 bg-white border border-brand-300 rounded-xl text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </FormField>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" variant="primary" onClick={handleSaveCustomEdit}>
              Apply Custom Fix
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
