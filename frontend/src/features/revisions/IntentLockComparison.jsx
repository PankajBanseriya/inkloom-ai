import { useState } from 'react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import { GitCompare, Check, Edit3, X, Sparkles, ShieldCheck } from 'lucide-react';

export default function IntentLockComparison({
  originalText = '',
  corrections = {
    light: '',
    balanced: '',
    strong: '',
  },
  rationale = '',
  onAccept,
  onReject,
  _onEdit,
  className = '',
}) {
  const [selectedTier, setSelectedTier] = useState('balanced');
  const [isEditing, setIsEditing] = useState(false);
  const [customText, setCustomText] = useState(corrections[selectedTier] || '');

  const tierDescriptions = {
    light: 'Minimal surgical changes. Preserves author syntax, replacing only prohibited terms.',
    balanced: 'Optimal clarity and tone compliance while retaining author structure and flow.',
    strong: 'Comprehensive editorial polish aligned with brand voice and corporate positioning.',
  };

  const handleSelectTier = (tier) => {
    setSelectedTier(tier);
    setCustomText(corrections[tier] || '');
    setIsEditing(false);
  };

  const activeProposedText = isEditing ? customText : (corrections[selectedTier] || '');

  return (
    <Card className={`border border-ink-200 shadow-sm ${className}`}>
      {/* Title & Mode Switcher */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-ink-100">
        <div className="flex items-center space-x-2">
          <span className="p-1.5 rounded-lg bg-brand-50 text-brand-600 border border-brand-200">
            <GitCompare className="w-4 h-4" />
          </span>
          <div>
            <h3 className="text-sm font-bold text-ink-900">Intent Lock Multi-Tier Comparison</h3>
            <p className="text-[11px] text-ink-500">
              Select desired correction intensity before applying updates.
            </p>
          </div>
        </div>

        {/* Tier Buttons */}
        <div className="inline-flex rounded-lg p-1 bg-ink-100 border border-ink-200">
          {(['light', 'balanced', 'strong']).map((tier) => (
            <button
              key={tier}
              type="button"
              onClick={() => handleSelectTier(tier)}
              className={`px-3 py-1 text-xs font-semibold rounded-md capitalize transition-all ${
                selectedTier === tier
                  ? 'bg-white text-brand-700 shadow-xs'
                  : 'text-ink-600 hover:text-ink-900'
              }`}
            >
              {tier}
            </button>
          ))}
        </div>
      </div>

      {/* Tier Explanation Banner */}
      <div className="mt-3 px-3.5 py-2 bg-ink-50 rounded-lg border border-ink-200 text-xs text-ink-600 flex items-center justify-between">
        <span className="font-medium">
          <strong className="text-ink-800 capitalize">{selectedTier} Mode:</strong> {tierDescriptions[selectedTier]}
        </span>
        <Badge variant={selectedTier === 'strong' ? 'brand' : selectedTier === 'balanced' ? 'success' : 'neutral'} size="sm">
          {selectedTier.toUpperCase()}
        </Badge>
      </div>

      {/* Side-by-side or stacked diff view */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
        {/* Original Content (Preserved) */}
        <div className="p-4 bg-ink-50/70 border border-ink-200 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-[11px] font-bold text-ink-500 uppercase tracking-wider">
                Original Text (Untouched)
              </span>
              <span className="text-[10px] text-ink-400 font-mono">Protected State</span>
            </div>
            <p className="text-xs text-ink-800 leading-relaxed font-normal">
              {originalText}
            </p>
          </div>
          <div className="mt-4 pt-3 border-t border-ink-200/60 text-[10px] text-ink-400 italic">
            Original content is strictly immutable until an explicit action is taken.
          </div>
        </div>

        {/* Proposed Intent Lock Fix */}
        <div className="p-4 bg-brand-50/40 border border-brand-200 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-[11px] font-bold text-brand-700 uppercase tracking-wider flex items-center">
                <Sparkles className="w-3.5 h-3.5 mr-1 text-brand-600" />
                Proposed {selectedTier} Revision
              </span>
              <Badge variant="brand" size="sm">
                Intent Locked
              </Badge>
            </div>

            {isEditing ? (
              <textarea
                rows={4}
                value={customText}
                onChange={(e) => setCustomText(e.target.value)}
                className="w-full text-xs p-2.5 bg-white border border-brand-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
                placeholder="Adjust the proposed wording..."
              />
            ) : (
              <p className="text-xs text-ink-900 font-medium leading-relaxed">
                {activeProposedText}
              </p>
            )}
          </div>

          {rationale && (
            <div className="mt-4 pt-2.5 border-t border-brand-200/60 text-[11px] text-brand-800">
              <span className="font-semibold">Rationale: </span>
              {rationale}
            </div>
          )}
        </div>
      </div>

      {/* Comparison Action Bar */}
      <div className="mt-5 pt-4 border-t border-ink-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center space-x-2 text-xs text-ink-500">
          <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          <span>Accepting this correction will record a new revision in the audit ledger.</span>
        </div>

        <div className="flex items-center space-x-2.5">
          {onReject && (
            <Button
              size="sm"
              variant="outline"
              onClick={onReject}
              className="text-xs border-ink-300 text-ink-700 hover:bg-ink-100"
            >
              <X className="w-3.5 h-3.5 mr-1" />
              Reject All
            </Button>
          )}

          <Button
            size="sm"
            variant="outline"
            onClick={() => {
              if (isEditing) {
                setIsEditing(false);
              } else {
                setIsEditing(true);
              }
            }}
            className="text-xs"
          >
            <Edit3 className="w-3.5 h-3.5 mr-1" />
            {isEditing ? 'Cancel Edit' : 'Edit Fix'}
          </Button>

          {onAccept && (
            <Button
              size="sm"
              variant="primary"
              onClick={() => onAccept({ text: activeProposedText, tier: selectedTier })}
              className="text-xs font-semibold"
            >
              <Check className="w-3.5 h-3.5 mr-1" />
              Accept {selectedTier} Revision
            </Button>
          )}
        </div>
      </div>
    </Card>
  );
}
