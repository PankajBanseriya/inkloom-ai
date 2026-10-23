import { useAuth } from '../../context/AuthContext';
import { Shield, Bell, Menu, LogOut, ChevronDown } from 'lucide-react';
import Badge from '../common/Badge';

export default function Topbar({ onToggleMobileMenu }) {
  const { user, activeBrand, brands, switchBrand, logout } = useAuth();

  return (
    <header className="h-16 border-b border-ink-200/80 bg-white px-4 md:px-8 flex items-center justify-between sticky top-0 z-20 shadow-xs">
      <div className="flex items-center space-x-3">
        {/* Mobile menu trigger */}
        <button
          type="button"
          onClick={onToggleMobileMenu}
          className="md:hidden p-2 text-ink-600 hover:text-ink-900 rounded-lg hover:bg-ink-100 focus:outline-none"
          aria-label="Toggle navigation menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        {/* Brand Workspace Switcher Dropdown */}
        <div className="flex items-center space-x-2">
          <div className="hidden sm:flex items-center text-xs font-semibold text-ink-400 uppercase tracking-wider">
            Workspace:
          </div>
          <div className="relative inline-block text-left">
            <select
              value={activeBrand?.id || ''}
              onChange={(e) => switchBrand(e.target.value)}
              className="appearance-none bg-ink-50 hover:bg-ink-100 text-ink-900 font-semibold text-xs py-1.5 pl-3 pr-8 rounded-lg border border-ink-300 focus:outline-none focus:ring-2 focus:ring-brand-500 cursor-pointer transition-colors"
            >
              {brands.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-ink-500">
              <ChevronDown className="w-3.5 h-3.5" />
            </div>
          </div>
          <Badge variant="brand" size="sm" className="hidden lg:inline-flex">
            <Shield className="w-3 h-3 mr-1" />
            Governance Active
          </Badge>
        </div>
      </div>

      {/* Right controls: Notifications, User, Logout */}
      <div className="flex items-center space-x-3">
        <button
          type="button"
          className="p-2 text-ink-500 hover:text-ink-900 hover:bg-ink-100 rounded-lg transition-colors relative"
          title="Notifications"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-brand-600 rounded-full"></span>
        </button>

        {/* User profile capsule */}
        <div className="flex items-center space-x-2.5 pl-3 border-l border-ink-200">
          <div className="w-8 h-8 rounded-full bg-brand-100 border border-brand-300 flex items-center justify-center text-brand-700 font-bold text-xs">
            {user?.name ? user.name.split(' ').map((n) => n[0]).join('').slice(0, 2) : 'U'}
          </div>
          <div className="hidden sm:block text-left">
            <div className="text-xs font-semibold text-ink-900">{user?.name || 'Authorized User'}</div>
            <div className="text-[10px] text-ink-500 font-medium">{user?.role?.replace('_', ' ') || 'REVIEWER'}</div>
          </div>
          <button
            type="button"
            onClick={logout}
            className="p-1.5 text-ink-400 hover:text-rose-600 rounded-md transition-colors"
            title="Log Out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
}
