import { useState, useEffect, useCallback } from 'react';
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
import reviewService from '../services/reviewService';
import {
  Sparkles,
  Send,
  Save,
  RotateCcw,
  ShieldCheck,
  AlertTriangle,
  History,
  FileText,
  RefreshCw,
} from 'lucide-react';

const SAMPLE_DRAFT_TEXT = `Acme Enterprise announces our revolutionary next-generation cloud infrastructure platform. Our best-in-class multi-tenant database delivers unmatched 100% unbreakable security and guarantees 99.99% uptime for Fortune 500 enterprises. Independent benchmark evaluations prove our distributed architecture outperforms every competing database in the galaxy without exception.`;

export default function ContentReviewPage() {
  const navigate = useNavigate();
  const { activeBrand, brands, switchBrand } = useAuth();
  const { success, warning, error: toastError, info } = useToast();

  const [reviewId, setReviewId] = useState(null);
  const [contentType, setContentType] = useState('Press Release');
  const [contentTitle, setContentTitle] = useState('Q4 Enterprise Infrastructure Announcement');
  const [content, setContent] = useState(SAMPLE_DRAFT_TEXT);
  const [originalContent, setOriginalContent] = useState(SAMPLE_DRAFT_TEXT);

  const [status, setStatus] = useState('DRAFT'); // DRAFT, REVIEWING, COMPLETED, ERROR
  const [errorMessage, setErrorMessage] = useState(null);
  const [complianceScore, setComplianceScore] = useState(null);
  const [activeRevisionNumber, setActiveRevisionNumber] = useState(1);
  const [findings, setFindings] = useState([]);
  const [selectedStrengths, setSelectedStrengths] = useState({});

  const [severityFilter, setSeverityFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [activeComparisonFinding, setActiveComparisonFinding] = useState(null);

  // Custom Edit Modal State
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingFindingId, setEditingFindingId] = useState(null);
  const [customEditText, setCustomEditText] = useState('');

  // History Drawer / Modal State
  const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);
  const [recentReviews, setRecentReviews] = useState([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);

  // Fetch recent reviews for active workspace
  const fetchRecentReviews = useCallback(async () => {
    if (!activeBrand?.id) return;
    try {
      setIsLoadingHistory(true);
      const reviews = await reviewService.getReviews(activeBrand.id);
      setRecentReviews(reviews || []);
    } catch (err) {
      // Non-fatal background fetch
      console.warn('Failed to load past reviews:', err.message);
    } finally {
      setIsLoadingHistory(false);
    }
  }, [activeBrand?.id]);

  useEffect(() => {
    fetchRecentReviews();
  }, [fetchRecentReviews]);

  // Load a previously submitted review into the workspace
  const handleLoadExistingReview = (prevReview) => {
    setReviewId(prevReview.id);
    setContentTitle(prevReview.title || 'Loaded Review');
    setContentType(prevReview.contentType || 'Press Release');
    setContent(prevReview.currentContent || prevReview.originalContent || '');
    setOriginalContent(prevReview.originalContent || '');
    setFindings(prevReview.findings || []);
    setComplianceScore(prevReview.complianceScore || 100);
    setActiveRevisionNumber(prevReview.activeRevisionNumber || 1);
    setStatus('COMPLETED');
    setIsHistoryModalOpen(false);
    info('Review Loaded', `Loaded "${prevReview.title}" (Revision #${prevReview.activeRevisionNumber})`);
  };

  // Real API Review Trigger
  const handleReviewContent = async () => {
    if (!content.trim()) {
      warning('Empty Content', 'Please provide or paste text before running brand review.');
      return;
    }

    if (!activeBrand?.id) {
      warning('No Workspace Selected', 'Please select an active brand workspace.');
      return;
    }

    setStatus('REVIEWING');
    setErrorMessage(null);
    info('Governance Pipeline Triggered', 'Running deterministic rules, claim validation, and Gemini AI review...');

    try {
      const payload = {
        brandId: activeBrand.id,
        title: contentTitle.trim() || 'Untitled Submission',
        contentType,
        content: content.trim(),
      };

      const reviewData = await reviewService.submitReview(payload);

      setReviewId(reviewData.id);
      setOriginalContent(reviewData.originalContent);
      setContent(reviewData.currentContent);
      setFindings(reviewData.findings || []);
      setComplianceScore(reviewData.complianceScore);
      setActiveRevisionNumber(reviewData.activeRevisionNumber || 1);
      setStatus('COMPLETED');

      // Initialize default strength selection for each finding
      const strengths = {};
      (reviewData.findings || []).forEach((f) => {
        strengths[f.id] = 'balanced';
      });
      setSelectedStrengths(strengths);

      success(
        'Review Evaluation Complete',
        `Identified ${(reviewData.findings || []).length} findings. Compliance score: ${reviewData.complianceScore}%`
      );

      // Refresh recent reviews list in background
      fetchRecentReviews();
    } catch (err) {
      setStatus('ERROR');
      const msg = err.message || 'An error occurred during review analysis.';
      setErrorMessage(msg);
      toastError('Governance Evaluation Failed', msg);
    }
  };

  // Intent Lock: Accept Suggestion (Real API Call)
  const handleAcceptCorrection = async (findingId, replacementText, tier = 'balanced') => {
    const targetFinding = findings.find((f) => f.id === findingId);
    if (!targetFinding) return;

    // If review exists in backend, call real acceptCorrection endpoint
    if (reviewId) {
      try {
        const payload = {
          findingId,
          replacementText,
          tier: (tier || selectedStrengths[findingId] || 'balanced').toLowerCase(),
          rationale: `Accepted ${tier || 'balanced'} Intent Lock fix for: "${targetFinding.excerpt}"`,
        };

        const updatedReview = await reviewService.acceptCorrection(reviewId, payload);

        setContent(updatedReview.currentContent);
        setFindings(updatedReview.findings || []);
        setComplianceScore(updatedReview.complianceScore);
        setActiveRevisionNumber(updatedReview.activeRevisionNumber);
        setActiveComparisonFinding(null);

        success(
          'Intent Lock Fix Applied',
          `Created Revision #${updatedReview.activeRevisionNumber} with surgical replacement.`
        );
      } catch (err) {
        toastError('Failed to apply correction', err.message);
      }
    } else {
      // Offline fallback: surgical replacement in local state
      const updatedContent = content.replace(targetFinding.excerpt, replacementText);
      setContent(updatedContent);
      setFindings((prev) => prev.filter((f) => f.id !== findingId));
      setActiveComparisonFinding(null);
      success('Correction Accepted', `Replaced "${targetFinding.excerpt}" with compliant phrasing.`);
    }
  };

  // Dismiss / Reject Finding
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
    handleAcceptCorrection(editingFindingId, customEditText, 'custom');
    setIsEditModalOpen(false);
  };

  // Save Manual Revision via Real API
  const handleSaveRevision = async () => {
    if (!reviewId) {
      warning('Save Revision', 'Please run a content review first to initialize document revision history.');
      return;
    }

    try {
      const payload = {
        updatedContent: content,
        changeSummary: `Manual author revisions saved at ${new Date().toLocaleTimeString()}`,
      };
      const newRev = await reviewService.saveRevision(reviewId, payload);
      setActiveRevisionNumber(newRev.revisionNumber);
      success('Revision Saved', `Stored non-destructive Revision #${newRev.revisionNumber} in audit ledger.`);
    } catch (err) {
      toastError('Save Revision Failed', err.message);
    }
  };

  // Submit for Approval
  const handleSubmitForApproval = () => {
    if (findings.some((f) => f.severity === 'CRITICAL')) {
      warning(
        'Critical Violations Pending',
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

  // Filtered Findings
  const filteredFindings = findings.filter((f) => {
    const matchesSeverity = severityFilter === 'ALL' || f.severity === severityFilter;
    const matchesCategory = categoryFilter === 'ALL' || f.category === categoryFilter;
    return matchesSeverity && matchesCategory;
  });

  const wordCount = content.trim() ? content.trim().split(/\s+/).length : 0;
  const charCount = content.length;
  const criticalCount = findings.filter((f) => f.severity === 'CRITICAL').length;
  const warningCount = findings.filter((f) => f.severity === 'WARNING').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Content Review Workspace"
        description="Evaluate communications against brand guidelines, approved claims, and ground-truth sources before human sign-off."
        badge={
          status === 'COMPLETED'
            ? `Review Complete (${complianceScore}% Compliance)`
            : status === 'REVIEWING'
            ? 'Analyzing Guidelines & AI Context...'
            : status === 'ERROR'
            ? 'Evaluation Error'
            : 'Draft Mode'
        }
        badgeVariant={
          status === 'COMPLETED' ? 'success' : status === 'REVIEWING' ? 'warning' : status === 'ERROR' ? 'danger' : 'neutral'
        }
        actions={
          <div className="flex items-center space-x-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => setIsHistoryModalOpen(true)}
              className="text-xs"
            >
              <History className="w-3.5 h-3.5 mr-1" />
              Past Reviews ({recentReviews.length})
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={handleSaveRevision}
              disabled={!reviewId}
              className="text-xs"
            >
              <Save className="w-4 h-4 mr-1.5" />
              Save Revision {activeRevisionNumber > 1 ? `#${activeRevisionNumber}` : ''}
            </Button>
            <Button
              size="sm"
              variant="primary"
              onClick={handleSubmitForApproval}
              disabled={status !== 'COMPLETED'}
              className="text-xs"
            >
              <Send className="w-4 h-4 mr-1.5" />
              Submit for Approval
            </Button>
          </div>
        }
      />

      {/* Error Retry Banner */}
      {status === 'ERROR' && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl flex items-start justify-between text-xs text-rose-900">
          <div className="flex items-start space-x-3">
            <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0 mt-0.5" />
            <div>
              <div className="font-bold text-sm">Review Evaluation Failed</div>
              <p className="mt-0.5 text-rose-700">{errorMessage}</p>
            </div>
          </div>
          <Button size="sm" variant="outline" onClick={handleReviewContent} className="text-xs border-rose-300 text-rose-800">
            <RefreshCw className="w-3.5 h-3.5 mr-1" />
            Retry
          </Button>
        </div>
      )}

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

            <div className="pt-3 flex items-center justify-between">
              <input
                type="text"
                value={contentTitle}
                onChange={(e) => setContentTitle(e.target.value)}
                placeholder="Submission Title..."
                className="w-full text-base font-bold text-ink-900 border-b border-transparent hover:border-ink-200 focus:border-brand-500 focus:outline-none pb-1 transition-colors"
              />
              <button
                type="button"
                onClick={() => {
                  setContent(SAMPLE_DRAFT_TEXT);
                  info('Sample Loaded', 'Populated draft with governance test text.');
                }}
                className="text-[11px] font-semibold text-brand-600 hover:text-brand-700 whitespace-nowrap ml-2 hover:underline"
                title="Populate test copy that exercises prohibited terms and claim verification"
              >
                Load Sample Draft
              </button>
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
                {reviewId && (
                  <>
                    <span>•</span>
                    <span className="text-brand-700 font-semibold">Revision #{activeRevisionNumber}</span>
                  </>
                )}
                {complianceScore !== null && (
                  <>
                    <span>•</span>
                    <span className={`font-bold ${complianceScore >= 80 ? 'text-emerald-700' : 'text-amber-700'}`}>
                      {complianceScore}% Score
                    </span>
                  </>
                )}
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
                  {status === 'COMPLETED' ? 'Re-Evaluate Content' : 'Review Content'}
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
              onAccept={({ text, tier }) => handleAcceptCorrection(activeComparisonFinding.id, text, tier)}
              onReject={() => setActiveComparisonFinding(null)}
              onEdit={({ text }) => handleEditCorrection(activeComparisonFinding.id, text)}
            />
          )}
        </div>

        {/* Right Column: Governance Findings & Evidence Cards (5 cols) */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-white p-4 rounded-xl border border-ink-200 shadow-sm space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-sm font-bold text-ink-900">Governance Findings</h2>
                <p className="text-[11px] text-ink-500">
                  {status === 'DRAFT'
                    ? 'Click "Review Content" to evaluate against brand rules'
                    : findings.length === 0
                    ? 'All clear! No brand violations detected.'
                    : `${findings.length} findings (${criticalCount} Critical, ${warningCount} Warning)`}
                </p>
              </div>

              {complianceScore !== null && (
                <div className="text-right">
                  <div className="text-xs text-ink-500">Compliance</div>
                  <div className={`text-base font-extrabold ${complianceScore >= 80 ? 'text-emerald-700' : 'text-amber-700'}`}>
                    {complianceScore}%
                  </div>
                </div>
              )}
            </div>

            {/* Filter controls row */}
            {findings.length > 0 && (
              <div className="grid grid-cols-2 gap-2 pt-2 border-t border-ink-100">
                <select
                  value={severityFilter}
                  onChange={(e) => setSeverityFilter(e.target.value)}
                  className="px-2 py-1 text-xs bg-ink-50 border border-ink-300 rounded-md text-ink-800 focus:outline-none"
                >
                  <option value="ALL">All Severities</option>
                  <option value="CRITICAL">Critical Only</option>
                  <option value="WARNING">Warning Only</option>
                  <option value="INFO">Info / Advisory</option>
                </select>

                <select
                  value={categoryFilter}
                  onChange={(e) => setCategoryFilter(e.target.value)}
                  className="px-2 py-1 text-xs bg-ink-50 border border-ink-300 rounded-md text-ink-800 focus:outline-none"
                >
                  <option value="ALL">All Categories</option>
                  <option value="PROHIBITED_TERMINOLOGY">Prohibited Terms</option>
                  <option value="BRAND_TONE">Brand Tone</option>
                  <option value="UNSUPPORTED_CLAIM">Unsupported Claims</option>
                  <option value="CLAIM_CONFLICT">Claim Conflicts</option>
                  <option value="REQUIRED_TERMINOLOGY">Missing Disclaimers</option>
                  <option value="ACCESSIBILITY">Accessibility</option>
                </select>
              </div>
            )}
          </div>

          {/* Findings List */}
          {status === 'DRAFT' ? (
            <Card className="text-center py-10">
              <FileText className="w-10 h-10 text-ink-400 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-ink-900">Ready for Governance Review</h3>
              <p className="text-xs text-ink-500 mt-1 max-w-xs mx-auto">
                Author or paste content on the left, then click <strong>Review Content</strong> to trigger the automated governance pipeline.
              </p>
            </Card>
          ) : findings.length === 0 ? (
            <Card className="text-center py-12">
              <ShieldCheck className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-ink-900">Compliant with Brand Guidelines</h3>
              <p className="text-xs text-ink-500 mt-1 max-w-xs mx-auto">
                No prohibited expressions, unapproved claims, or tone violations detected. You can submit this content for final sign-off.
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

      {/* Past Reviews Modal */}
      <Modal
        isOpen={isHistoryModalOpen}
        onClose={() => setIsHistoryModalOpen(false)}
        title="Brand Workspace Review History"
        description="Select a past submission to load its text, audit findings, and revision history."
      >
        <div className="space-y-3 max-h-96 overflow-y-auto">
          {isLoadingHistory ? (
            <div className="p-8 text-center text-xs text-ink-500">Loading reviews...</div>
          ) : recentReviews.length === 0 ? (
            <div className="p-8 text-center text-xs text-ink-500">
              No previous reviews found for {activeBrand?.name || 'this brand workspace'}.
            </div>
          ) : (
            recentReviews.map((rev) => (
              <div
                key={rev.id}
                onClick={() => handleLoadExistingReview(rev)}
                className="p-3 bg-ink-50 hover:bg-brand-50/50 border border-ink-200 hover:border-brand-300 rounded-xl cursor-pointer transition-colors flex items-center justify-between"
              >
                <div>
                  <div className="font-bold text-xs text-ink-900">{rev.title}</div>
                  <div className="text-[11px] text-ink-500 mt-0.5">
                    {rev.contentType} • Revision #{rev.activeRevisionNumber} • {rev.findingsCount} findings
                  </div>
                </div>
                <div className="text-right">
                  <span className={`text-xs font-bold ${rev.complianceScore >= 80 ? 'text-emerald-700' : 'text-amber-700'}`}>
                    {rev.complianceScore}%
                  </span>
                  <div className="text-[10px] text-ink-400 capitalize">{rev.status?.toLowerCase().replace('_', ' ')}</div>
                </div>
              </div>
            ))
          )}
        </div>
      </Modal>
    </div>
  );
}
