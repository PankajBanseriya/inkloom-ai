import Badge from './Badge';

export default function PageHeader({
  title,
  description,
  badge,
  badgeVariant = 'brand',
  actions,
  breadcrumbs,
  className = '',
}) {
  return (
    <div className={`mb-6 pb-5 border-b border-ink-200/80 flex flex-col md:flex-row md:items-center md:justify-between gap-4 ${className}`}>
      <div>
        {breadcrumbs && (
          <nav className="text-xs text-ink-500 mb-1.5 flex items-center space-x-1.5" aria-label="Breadcrumb">
            {breadcrumbs.map((crumb, idx) => (
              <span key={idx} className="flex items-center space-x-1.5">
                {idx > 0 && <span>/</span>}
                <span className={idx === breadcrumbs.length - 1 ? 'font-medium text-ink-800' : 'hover:text-ink-700'}>
                  {crumb}
                </span>
              </span>
            ))}
          </nav>
        )}
        <div className="flex items-center space-x-3">
          <h1 className="text-xl md:text-2xl font-bold tracking-tight text-ink-900">{title}</h1>
          {badge && <Badge variant={badgeVariant} size="sm">{badge}</Badge>}
        </div>
        {description && <p className="text-xs md:text-sm text-ink-500 mt-1 max-w-3xl">{description}</p>}
      </div>
      {actions && <div className="flex items-center space-x-2.5 flex-shrink-0">{actions}</div>}
    </div>
  );
}
