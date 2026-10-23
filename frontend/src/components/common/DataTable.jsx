import LoadingSpinner from '../feedback/LoadingSpinner';
import EmptyState from '../feedback/EmptyState';

export default function DataTable({
  columns = [],
  data = [],
  keyField = 'id',
  emptyTitle = 'No records found',
  emptyDescription = 'There are currently no items matching your criteria.',
  onRowClick,
  isLoading = false,
  className = '',
}) {
  if (isLoading) {
    return (
      <div className="bg-white border border-ink-200/80 rounded-xl p-8">
        <LoadingSpinner size="md" message="Loading records..." />
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <EmptyState
        title={emptyTitle}
        description={emptyDescription}
        className="my-4"
      />
    );
  }

  return (
    <div className={`overflow-x-auto bg-white border border-ink-200/80 rounded-xl shadow-sm ${className}`}>
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-ink-200 bg-ink-50/70 text-[11px] font-semibold text-ink-600 uppercase tracking-wider">
            {columns.map((col, idx) => (
              <th key={col.key || idx} className={`px-5 py-3.5 ${col.className || ''}`}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-ink-100 text-xs text-ink-800">
          {data.map((row, rowIdx) => (
            <tr
              key={row[keyField] || rowIdx}
              onClick={() => onRowClick && onRowClick(row)}
              className={`transition-colors ${
                onRowClick ? 'cursor-pointer hover:bg-brand-50/30' : 'hover:bg-ink-50/40'
              }`}
            >
              {columns.map((col, colIdx) => (
                <td key={col.key || colIdx} className={`px-5 py-3.5 align-middle ${col.className || ''}`}>
                  {col.render ? col.render(row[col.key], row, rowIdx) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
