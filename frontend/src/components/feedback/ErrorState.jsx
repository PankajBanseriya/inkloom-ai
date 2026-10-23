import ErrorMessage from './ErrorMessage';

export default function ErrorState({
  title = 'Unable to load workspace data',
  message = 'An unexpected network error occurred while connecting to the platform services.',
  onRetry,
  className = '',
}) {
  return (
    <div className={`p-6 bg-white rounded-xl border border-ink-200/80 my-4 ${className}`}>
      <ErrorMessage title={title} message={message} onRetry={onRetry} />
    </div>
  );
}
