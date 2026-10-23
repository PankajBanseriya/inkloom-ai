import Card from './Card';

export default function StatCard({
  label,
  value,
  helper,
  change,
  isPositive = true,
  icon: Icon,
  iconColor = 'text-brand-600 bg-brand-50 border-brand-200',
  className = '',
}) {
  return (
    <Card className={`hover:border-ink-300 transition-all ${className}`}>
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold uppercase tracking-wider text-ink-500">{label}</span>
        {Icon && (
          <span className={`p-2 rounded-lg border flex items-center justify-center ${iconColor}`}>
            <Icon className="w-4 h-4" />
          </span>
        )}
      </div>
      <div className="mt-3">
        <div className="text-2xl font-bold tracking-tight text-ink-900">{value}</div>
        <div className="flex items-center space-x-1.5 mt-1 text-xs">
          {change && (
            <span className={`font-semibold ${isPositive ? 'text-emerald-600' : 'text-rose-600'}`}>
              {change}
            </span>
          )}
          {helper && <span className="text-ink-500">{helper}</span>}
        </div>
      </div>
    </Card>
  );
}
