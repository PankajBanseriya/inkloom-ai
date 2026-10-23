import Badge from './Badge';

export default function StatusBadge({ status = '', size = 'sm', className = '' }) {
  const normalized = (status || '').toUpperCase();

  const configMap = {
    // General approval & lifecycle
    APPROVED: { label: 'Approved', variant: 'success' },
    ACTIVE: { label: 'Active', variant: 'success' },
    DRAFT: { label: 'Draft', variant: 'neutral' },
    IN_REVIEW: { label: 'In Review', variant: 'brand' },
    PENDING_REVIEW: { label: 'Pending Review', variant: 'warning' },
    CHANGES_REQUESTED: { label: 'Changes Requested', variant: 'warning' },
    REJECTED: { label: 'Rejected', variant: 'danger' },
    EXPIRED: { label: 'Expired', variant: 'danger' },
    UNDER_REVIEW: { label: 'Under Review', variant: 'warning' },
    DEPRECATED: { label: 'Deprecated', variant: 'neutral' },

    // Severities
    CRITICAL: { label: 'Critical', variant: 'danger' },
    WARNING: { label: 'Warning', variant: 'warning' },
    INFO: { label: 'Info', variant: 'neutral' },

    // Evidence Confidence Types
    VERIFIED_RULE: { label: 'Verified Rule', variant: 'brand' },
    SUBJECTIVE_SUGGESTION: { label: 'Subjective Suggestion', variant: 'warning' },
    UNSUPPORTED_CLAIM: { label: 'Unsupported Claim', variant: 'danger' },

    // Correction Levels
    LIGHT: { label: 'Light Fix', variant: 'neutral' },
    BALANCED: { label: 'Balanced Fix', variant: 'brand' },
    STRONG: { label: 'Strong Fix', variant: 'success' },
  };

  const item = configMap[normalized] || {
    label: status.replace(/_/g, ' '),
    variant: 'neutral',
  };

  return (
    <Badge variant={item.variant} size={size} className={className}>
      {item.label}
    </Badge>
  );
}
