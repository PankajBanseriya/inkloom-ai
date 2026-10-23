import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Shield,
  FileText,
  Database,
  CheckCircle2,
  FileCheck2,
  Sparkles,
  BarChart3,
  Settings,
} from 'lucide-react';
import { ROUTES } from '../../constants/routes';

export default function Sidebar() {
  const navItems = [
    { label: 'Dashboard', icon: LayoutDashboard, path: ROUTES.DASHBOARD },
    { label: 'Brand Guidelines', icon: Shield, path: ROUTES.GUIDELINES },
    { label: 'Approved Claims', icon: Database, path: ROUTES.CLAIMS },
    { label: 'Approved Sources', icon: FileText, path: ROUTES.SOURCES },
    { label: 'Content Review', icon: Sparkles, path: ROUTES.REVIEWS },
    { label: 'Approval Queue', icon: CheckCircle2, path: ROUTES.APPROVALS },
    { label: 'Clarity Heatmap', icon: BarChart3, path: ROUTES.HEATMAP },
    { label: 'Settings', icon: Settings, path: ROUTES.SETTINGS },
  ];

  return (
    <aside className="w-64 bg-ink-900 text-white flex flex-col flex-shrink-0 min-h-screen">
      {/* Brand logo header */}
      <div className="h-16 flex items-center px-6 border-b border-ink-800">
        <div className="w-7 h-7 rounded-lg bg-brand-600 flex items-center justify-center text-white mr-3 shadow-sm">
          <FileCheck2 className="w-4 h-4" />
        </div>
        <div>
          <span className="font-bold text-base tracking-tight text-white">Inkloom</span>
          <span className="text-brand-400 font-semibold text-xs ml-1.5 px-1.5 py-0.5 rounded bg-brand-950/60 border border-brand-800">AI</span>
        </div>
      </div>

      {/* Navigation links */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="px-3 pb-2 text-[10px] font-semibold tracking-wider text-ink-400 uppercase">
          Brand Governance
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                  isActive
                    ? 'bg-brand-600/90 text-white font-semibold shadow-sm'
                    : 'text-ink-300 hover:text-white hover:bg-ink-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4 flex-shrink-0" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      {/* System status footer */}
      <div className="p-4 border-t border-ink-800/80 bg-ink-950/40 text-[11px] text-ink-400">
        <div className="flex items-center space-x-2">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          <span className="font-medium text-ink-300">Phase 1 Foundation</span>
        </div>
        <p className="mt-1 text-[10px] text-ink-500">Intelligent Governance Core</p>
      </div>
    </aside>
  );
}
