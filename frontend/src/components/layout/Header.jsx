import { Bell } from 'lucide-react';
import Badge from '../common/Badge';

export default function Header({ currentBrand = 'Acme Global Brand' }) {
  return (
    <header className="h-16 border-b border-ink-200/80 bg-white px-6 flex items-center justify-between sticky top-0 z-10 shadow-sm/50">
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2">
          <span className="text-xs uppercase font-semibold text-ink-500 tracking-wider">Active Workspace:</span>
          <span className="text-sm font-semibold text-ink-900 bg-ink-100 px-2.5 py-1 rounded-md border border-ink-200">
            {currentBrand}
          </span>
        </div>
        <Badge variant="brand" size="sm">Brand Governance Active</Badge>
      </div>

      <div className="flex items-center space-x-4">
        <button
          type="button"
          className="p-2 text-ink-500 hover:text-ink-900 hover:bg-ink-100 rounded-lg transition-colors relative"
          title="Notifications"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-brand-500 rounded-full"></span>
        </button>

        <div className="flex items-center space-x-3 pl-3 border-l border-ink-200">
          <div className="w-8 h-8 rounded-full bg-brand-100 border border-brand-300 flex items-center justify-center text-brand-700 font-semibold text-xs">
            IB
          </div>
          <div className="hidden md:block text-left">
            <div className="text-xs font-semibold text-ink-900">Governance Officer</div>
            <div className="text-[10px] text-ink-500">Brand Manager</div>
          </div>
        </div>
      </div>
    </header>
  );
}
