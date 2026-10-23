import Card from '../../components/common/Card';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import { ShieldAlert, BookOpen, Sparkles, Check, X, Edit3, AlertCircle } from 'lucide-react';

export default function EvidenceCard({
  finding,
  onAcceptCorrection,
  onRejectCorrection,
  onEditCorrection,
  selectedStrength = 'balanced',
  onSelectStrength,
  className = '',
}) {
  if (!finding) return null;

  const currentSuggestion = finding.suggestedCorrections?.[selectedStrength] || '';

  return (
    <Card className={`border-l-4 ${
      finding.severity === 'CRITICAL' ? 'border-l-rose-500' :
      finding.severity === 'WARNING' ? 'border-l-amber-500' : 'border-l-brand-500'
    } ${className}`}>
      {/* Header with Title and Badges */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-ink-100">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-xs font-semibold uppercase text-ink-500 tracking-wider">
              {finding.category}
            </span>
            <span>•</span>
            <span className="text-xs text-ink-500">{finding.id}</span>
          </div>
          <h3 className="text-sm font-bold text-ink-900 mt-0.5">{finding.title}</h3>
        </div>

        <div className="flex items-center space-x-2 flex-wrap gap-y-1">
          <StatusBadge status={finding.severity} size="sm" />
          <StatusBadge status={finding.evidenceType} size="sm" />
        </div>
      </div>

      {/* Flagged Text Excerpt */}
      <div className="mt-4 p-3 bg-rose-50/60 border border-rose-200/80 rounded-lg">
        <div className="text-[10px] uppercase font-bold text-rose-800 tracking-wider mb-1 flex items-center">
          <ShieldAlert className="w-3.5 h-3.5 mr-1 text-rose-600" />
          Flagged Passage:
        </div>
        <p className="text-xs font-medium text-rose-950 italic">
          &ldquo;{finding.excerpt}&rdquo;
        </p>
      </div>

      {/* Explanation */}
      <div className="mt-3">
        <div className="text-[11px] font-semibold text-ink-700 uppercase tracking-wider mb-1">
          Governance Analysis:
        </div>
        <p className="text-xs text-ink-600 leading-relaxed">
          {finding.explanation}
        </p>
      </div>

      {/* Supporting Source or Missing Evidence */}
      <div className="mt-3 p-2.5 bg-ink-50 rounded-lg border border-ink-200 flex items-start space-x-2.5">
        <BookOpen className="w-4 h-4 text-ink-500 flex-shrink-0 mt-0.5" />
        <div className="flex-1">
          <div className="text-[10px] font-bold uppercase tracking-wider text-ink-600">
            Supporting Evidence & Citation:
          </div>
          <div className="text-xs font-medium text-ink-800 mt-0.5">
            {finding.supportingDoc || 'No approved reference document matches this statement.'}
          </div>
          {finding.evidenceType === 'UNSUPPORTED_CLAIM' && (
            <div className="mt-1 flex items-center text-[11px] text-rose-700 font-medium">
              <AlertCircle className="w-3 h-3 mr-1 text-rose-600" />
              Claim cannot be validated against current approved claims ledger.
            </div>
          )}
        </div>
      </div>

      {/* Suggested Fix (Intent Lock Multi-Tier) */}
      {finding.suggestedCorrections && (
        <div className="mt-4 pt-3 border-t border-ink-100">
          <div className="flex items-center justify-between mb-2">
            <span className="text-[11px] font-bold uppercase text-brand-700 tracking-wider flex items-center">
              <Sparkles className="w-3.5 h-3.5 mr-1 text-brand-600" />
              Proposed Intent-Locked Fix:
            </span>

            {/* Strength Switcher */}
            {onSelectStrength && (
              <div className="inline-flex rounded-md p-0.5 bg-ink-100 border border-ink-200 text-[11px]">
                {['light', 'balanced', 'strong'].map((str) => (
                  <button
                    key={str}
                    type="button"
                    onClick={() => onSelectStrength(finding.id, str)}
                    className={`px-2 py-0.5 rounded capitalize font-medium transition-all ${
                      selectedStrength === str
                        ? 'bg-white text-brand-700 shadow-xs'
                        : 'text-ink-600 hover:text-ink-900'
                    }`}
                  >
                    {str}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="p-2.5 bg-brand-50/60 border border-brand-200 rounded-lg text-xs text-brand-950 font-medium leading-relaxed">
            &ldquo;{currentSuggestion}&rdquo;
          </div>

          {/* Action buttons */}
          <div className="mt-3 flex items-center justify-end space-x-2">
            {onRejectCorrection && (
              <Button
                size="sm"
                variant="outline"
                onClick={() => onRejectCorrection(finding.id)}
                className="text-xs"
              >
                <X className="w-3.5 h-3.5 mr-1 text-ink-500" />
                Reject
              </Button>
            )}
            {onEditCorrection && (
              <Button
                size="sm"
                variant="outline"
                onClick={() => onEditCorrection(finding.id, currentSuggestion)}
                className="text-xs"
              >
                <Edit3 className="w-3.5 h-3.5 mr-1 text-ink-500" />
                Edit Fix
              </Button>
            )}
            {onAcceptCorrection && (
              <Button
                size="sm"
                variant="primary"
                onClick={() => onAcceptCorrection(finding.id, currentSuggestion)}
                className="text-xs"
              >
                <Check className="w-3.5 h-3.5 mr-1" />
                Accept {selectedStrength}
              </Button>
            )}
          </div>
        </div>
      )}
    </Card>
  );
}
