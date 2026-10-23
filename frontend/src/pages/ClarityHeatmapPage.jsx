import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import StatCard from '../components/common/StatCard';
import { MOCK_HEATMAP_DATA } from '../mock/mockData';
import { BarChart3, AlertTriangle, ShieldCheck, Sparkles, HelpCircle } from 'lucide-react';

export default function ClarityHeatmapPage() {
  const { summary, matrix } = MOCK_HEATMAP_DATA;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Clarity Heatmap & Guideline Intelligence"
        description="Data-driven diagnosis of recurring compliance friction points, frequently overridden suggestions, and potential ambiguities in brand guidelines."
        badge="Analytics Engine"
      />

      {/* Overview Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Total Findings Analyzed"
          value={summary.totalFindingsAnalyzed}
          change="+28 this month"
          isPositive={true}
          helper="across all reviews"
          icon={BarChart3}
        />

        <StatCard
          label="Avg Compliance Rate"
          value={summary.avgComplianceRate}
          change="+1.8%"
          isPositive={true}
          helper="post-correction rate"
          icon={ShieldCheck}
          iconColor="text-emerald-600 bg-emerald-50 border-emerald-200"
        />

        <StatCard
          label="Most Violated Category"
          value="Prohibited Terms"
          change="38% of all flags"
          isPositive={false}
          helper="highest violation density"
          icon={AlertTriangle}
          iconColor="text-rose-600 bg-rose-50 border-rose-200"
        />

        <StatCard
          label="Ambiguity Index"
          value={summary.guidelineAmbiguityIndex}
          change="2 actionable signals"
          isPositive={false}
          helper="requires brand review"
          icon={HelpCircle}
          iconColor="text-amber-600 bg-amber-50 border-amber-200"
        />
      </div>

      {/* Metric Calculation Explanation Notice */}
      <div className="p-4 bg-brand-50/60 border border-brand-200 rounded-xl text-xs text-brand-950 flex items-start space-x-3">
        <Sparkles className="w-5 h-5 text-brand-600 flex-shrink-0 mt-0.5" />
        <div>
          <div className="font-bold">How Clarity Signals Are Calculated:</div>
          <p className="mt-0.5 text-brand-800 leading-relaxed">
            The Clarity Heatmap correlates author rejection rates, reviewer manual overrides, and repeated phrase violations. When authors frequently modify an automated suggestion (e.g. over 30% of the time), the engine generates a <strong>Guideline Conflict Signal</strong> for brand managers to review whether the underlying guideline is ambiguous or missing practical positive examples.
          </p>
        </div>
      </div>

      {/* Heatmap Matrix Table */}
      <Card
        title="Category Heatmap Matrix"
        subtitle="Distribution of violations, author acceptance rates, and human review signals"
      >
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-ink-200 text-ink-500 uppercase tracking-wider text-[11px] bg-ink-50/50">
                <th className="py-3 px-4 font-semibold">Violation Category</th>
                <th className="py-3 px-4 font-semibold">Occurrences</th>
                <th className="py-3 px-4 font-semibold">Frequently Flagged Phrases</th>
                <th className="py-3 px-4 font-semibold">Author Acceptance Rate</th>
                <th className="py-3 px-4 font-semibold">Guideline Conflict / Ambiguity Signal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {matrix.map((item, idx) => (
                <tr key={idx} className="hover:bg-ink-50/50 transition-colors">
                  <td className="py-3.5 px-4 font-bold text-ink-900">
                    {item.category}
                  </td>
                  <td className="py-3.5 px-4 font-semibold text-ink-800">
                    {item.occurrences} flags
                  </td>
                  <td className="py-3.5 px-4">
                    <div className="flex flex-wrap gap-1">
                      {item.repeatedPhrases.map((p, pIdx) => (
                        <span
                          key={pIdx}
                          className="bg-ink-100 text-ink-700 px-2 py-0.5 rounded text-[11px] font-mono border border-ink-200"
                        >
                          &ldquo;{p}&rdquo;
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="py-3.5 px-4 font-medium text-ink-700">
                    {item.acceptanceRate}
                  </td>
                  <td className="py-3.5 px-4 max-w-sm">
                    <div className="flex items-start space-x-1.5">
                      {item.status === 'NEEDS_ATTENTION' || item.status === 'SOURCE_GAP' ? (
                        <AlertTriangle className="w-3.5 h-3.5 text-amber-500 flex-shrink-0 mt-0.5" />
                      ) : (
                        <ShieldCheck className="w-3.5 h-3.5 text-emerald-500 flex-shrink-0 mt-0.5" />
                      )}
                      <span className="text-[11px] text-ink-600 leading-relaxed">
                        {item.conflictSignal}
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
