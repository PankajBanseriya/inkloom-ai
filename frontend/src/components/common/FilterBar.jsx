import SearchInput from './SearchInput';
import Button from './Button';
import { RotateCcw } from 'lucide-react';

export default function FilterBar({
  searchValue = '',
  onSearchChange,
  filters = [], // Array of { label, value, options: [{ label, value }], onChange }
  onReset,
  children,
  className = '',
}) {
  return (
    <div className={`flex flex-col md:flex-row md:items-center justify-between gap-3 p-3 bg-white border border-ink-200/80 rounded-xl mb-4 ${className}`}>
      <div className="flex flex-1 flex-col sm:flex-row items-stretch sm:items-center gap-3">
        {onSearchChange && (
          <SearchInput
            value={searchValue}
            onChange={onSearchChange}
          />
        )}
        {filters.map((filter, idx) => (
          <div key={idx} className="flex items-center space-x-1.5">
            {filter.label && <span className="text-[11px] font-semibold text-ink-500 uppercase">{filter.label}:</span>}
            <select
              value={filter.value}
              onChange={(e) => filter.onChange(e.target.value)}
              className="px-2.5 py-1.5 text-xs bg-white border border-ink-300 rounded-lg text-ink-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              {filter.options.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        ))}
      </div>

      <div className="flex items-center space-x-2 self-end sm:self-auto">
        {children}
        {onReset && (
          <Button
            size="sm"
            variant="ghost"
            onClick={onReset}
            className="text-xs text-ink-600 hover:text-ink-900"
            title="Reset Filters"
          >
            <RotateCcw className="w-3.5 h-3.5 mr-1" />
            Reset
          </Button>
        )}
      </div>
    </div>
  );
}
