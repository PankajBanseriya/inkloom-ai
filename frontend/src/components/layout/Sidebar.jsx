import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Building2,
  BookOpen,
  FileCheck2,
  Database,
  Sparkles,
  SearchCode,
  GitCompare,
  History,
  CheckCircle2,
  BarChart3,
  Users2,
  Settings,
  X,
} from 'lucide-react';
import { ROUTES } from '../../constants/routes';

export default function Sidebar({ isOpen = false, onClose }) {
  const sections = [
    {
      title: 'Overview',
      items: [
        { label: 'Dashboard', icon: LayoutDashboard, path: ROUTES.DASHBOARD },
        { label: 'Brand Workspaces', icon: Building2, path: ROUTES.BRANDS },
      ],
    },
    {
      title: 'Brand Standards & Evidence',
      items: [
        { label: 'Brand Guidelines', icon: BookOpen, path: ROUTES.GUIDELINES },
        { label: 'Approved Sources', icon: FileCheck2, path: ROUTES.SOURCES },
        { label: 'Approved Claims', icon: Database, path: ROUTES.CLAIMS },
      ],
    },
    {
      title: 'Review & Intent Lock',
      items: [
        { label: 'Content Review', icon: Sparkles, path: ROUTES.REVIEW_EDITOR },
        { label: 'Findings & Evidence', icon: SearchCode, path: ROUTES.FINDINGS },
        { label: 'Intent Lock Fixes', icon: GitCompare, path: ROUTES.INTENT_LOCK },
        { label: 'Revision History', icon: History, path: ROUTES.REVISIONS },
      ],
    },
    {
      title: 'Human Governance',
      items: [
        { label: 'Approval Queue', icon: CheckCircle2, path: ROUTES.APPROVALS },
        { label: 'Clarity Heatmap', icon: BarChart3, path: ROUTES.HEATMAP },
      ],
    },
    {
      title: 'System',
      items: [
        { label: 'Team Management', icon: Users2, path: ROUTES.TEAM },
        { label: 'Settings', icon: Settings, path: ROUTES.SETTINGS },
      ],
    },
  ];

  return (
    <>
      {/* Mobile backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-ink-950/60 z-30 md:hidden backdrop-blur-xs"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      <aside
        className={`fixed md:static inset-y-0 left-0 z-40 w-64 bg-ink-900 text-white flex flex-col flex-shrink-0 transition-transform duration-200 ease-in-out md:translate-x-0 ${
          isOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'
        }`}
      >
        {/* Brand header */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-ink-800">
          <div className="flex items-center space-x-2.5">
            <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center text-white shadow-sm font-bold text-sm">
              IL
            </div>
            <div>
              <span className="font-bold text-base tracking-tight text-white">Inkloom</span>
              <span className="text-brand-400 font-semibold text-[10px] ml-1.5 px-1.5 py-0.5 rounded bg-brand-950/80 border border-brand-800">
                AI
              </span>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="md:hidden p-1 text-ink-400 hover:text-white rounded"
            aria-label="Close sidebar"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation list */}
        <nav className="flex-1 px-3 py-4 space-y-6 overflow-y-auto">
          {sections.map((section, sIdx) => (
            <div key={sIdx} className="space-y-1">
              <div className="px-3 pb-1 text-[10px] font-semibold tracking-wider text-ink-400 uppercase">
                {section.title}
              </div>
              {section.items.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.path}
                    to={item.path}
                    onClick={() => onClose && onClose()}
                    className={({ isActive }) =>
                      `flex items-center space-x-3 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                        isActive
                          ? 'bg-brand-600/90 text-white font-semibold shadow-xs'
                          : 'text-ink-300 hover:text-white hover:bg-ink-800/60'
                      }`
                    }
                  >
                    <Icon className="w-4 h-4 flex-shrink-0" />
                    <span>{item.label}</span>
                  </NavLink>
                );
              })}
            </div>
          ))}
        </nav>

        {/* Footer status pill */}
        <div className="p-4 border-t border-ink-800 bg-ink-950/60 text-[11px] text-ink-400">
          <div className="flex items-center space-x-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
            <span className="font-medium text-ink-300">Brand Governance Engine</span>
          </div>
          <p className="mt-1 text-[10px] text-ink-500">Enterprise Edition v0.2.0</p>
        </div>
      </aside>
    </>
  );
}
