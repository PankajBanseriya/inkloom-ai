import LoadingSpinner from './LoadingSpinner';

export default function LoadingState({ message = 'Loading brand governance data...', height = 'min-h-[300px]' }) {
  return (
    <div className={`flex items-center justify-center bg-white rounded-xl border border-ink-200/80 p-8 ${height}`}>
      <LoadingSpinner size="md" message={message} />
    </div>
  );
}
