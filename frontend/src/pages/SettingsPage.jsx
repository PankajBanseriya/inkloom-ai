import { useState, useEffect } from 'react';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Badge from '../components/common/Badge';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Shield, Cpu, Save } from 'lucide-react';

export default function SettingsPage() {
  const { user, activeBrand } = useAuth();
  const { success } = useToast();

  const [name, setName] = useState(user?.fullName || user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [strictEnforcement, setStrictEnforcement] = useState(true);
  const [emailAlerts, setEmailAlerts] = useState(true);

  useEffect(() => {
    if (user) {
      setName(user.fullName || user.name || '');
      setEmail(user.email || '');
    }
  }, [user]);

  const handleSaveProfile = (e) => {
    e.preventDefault();
    success('Profile Updated', 'User preferences saved successfully.');
  };

  const handleSaveWorkspaceSettings = (e) => {
    e.preventDefault();
    success('Workspace Settings Saved', 'Brand governance enforcement policies updated.');
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Settings & Workspace Preferences"
        description="Configure account credentials, workspace governance enforcement rules, and inspect AI service parameters."
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left: User Profile (6 cols) */}
        <div className="lg:col-span-6 space-y-6">
          <Card title="User Profile" subtitle="Your corporate identity and assigned governance role">
            <form onSubmit={handleSaveProfile} className="space-y-4">
              <Input
                label="Full Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Full name"
                required
              />

              <Input
                label="Email Address"
                value={email}
                disabled
                helperText="Corporate email is managed by your organization's directory."
              />

              <div className="p-3 bg-ink-50 rounded-lg border border-ink-200 text-xs flex items-center justify-between">
                <div>
                  <span className="font-semibold text-ink-700 block">Assigned Role:</span>
                  <span className="text-ink-500">{user?.role || 'REVIEWER'}</span>
                </div>
                <Badge variant="brand" size="sm">Verified Session</Badge>
              </div>

              <div className="pt-2 flex justify-end">
                <Button size="sm" variant="primary" type="submit">
                  <Save className="w-3.5 h-3.5 mr-1.5" />
                  Save Profile Changes
                </Button>
              </div>
            </form>
          </Card>

          {/* Notifications */}
          <Card title="Notification Preferences" subtitle="Alerts on review findings and pending approvals">
            <div className="space-y-3 text-xs">
              <label className="flex items-start space-x-3 p-2.5 rounded-lg border border-ink-200 hover:bg-ink-50 cursor-pointer">
                <input
                  type="checkbox"
                  checked={emailAlerts}
                  onChange={(e) => setEmailAlerts(e.target.checked)}
                  className="rounded text-brand-600 focus:ring-brand-500 mt-0.5"
                />
                <div>
                  <div className="font-semibold text-ink-900">Review Request Alerts</div>
                  <p className="text-ink-500 text-[11px]">Notify me when content is assigned to my approval queue.</p>
                </div>
              </label>

              <label className="flex items-start space-x-3 p-2.5 rounded-lg border border-ink-200 hover:bg-ink-50 cursor-pointer">
                <input
                  type="checkbox"
                  defaultChecked
                  className="rounded text-brand-600 focus:ring-brand-500 mt-0.5"
                />
                <div>
                  <div className="font-semibold text-ink-900">Expired Claim Warnings</div>
                  <p className="text-ink-500 text-[11px]">Daily digest of approved claims approaching their expiration date.</p>
                </div>
              </label>
            </div>
          </Card>
        </div>

        {/* Right: Workspace Governance Settings & AI Architecture Status (6 cols) */}
        <div className="lg:col-span-6 space-y-6">
          <Card
            title={`${activeBrand?.name || 'Workspace'} Governance Policies`}
            subtitle="Configure governance rigor and rule enforcement strictness"
          >
            <form onSubmit={handleSaveWorkspaceSettings} className="space-y-4">
              <div className="p-3 bg-brand-50/50 rounded-lg border border-brand-200 text-xs">
                <div className="font-bold text-brand-950">Active Workspace: {activeBrand?.name}</div>
                <div className="text-brand-800 text-[11px] mt-0.5">Industry: {activeBrand?.industry}</div>
              </div>

              <div className="space-y-3 text-xs">
                <label className="flex items-start space-x-3 p-3 rounded-xl border border-ink-200 hover:bg-ink-50 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={strictEnforcement}
                    onChange={(e) => setStrictEnforcement(e.target.checked)}
                    className="rounded text-brand-600 focus:ring-brand-500 mt-0.5"
                  />
                  <div>
                    <div className="font-semibold text-ink-900">Strict Prohibited Term Blocking</div>
                    <p className="text-ink-500 text-[11px]">
                      Prevent submission for approval until all Critical prohibited terms and expired claims have been accepted or resolved.
                    </p>
                  </div>
                </label>
              </div>

              <div className="pt-2 flex justify-end">
                <Button size="sm" variant="primary" type="submit">
                  <Save className="w-3.5 h-3.5 mr-1.5" />
                  Save Workspace Policies
                </Button>
              </div>
            </form>
          </Card>

          {/* AI Architecture & Privacy Boundary */}
          <Card
            title="AI Architecture & Security Boundaries"
            subtitle="Server-side review pipeline and confidentiality parameters"
          >
            <div className="space-y-3 text-xs">
              <div className="p-3 bg-ink-50 rounded-lg border border-ink-200">
                <div className="flex items-center space-x-2 text-ink-900 font-bold mb-1">
                  <Cpu className="w-4 h-4 text-brand-600" />
                  <span>Zero Direct Client-to-AI Calls</span>
                </div>
                <p className="text-ink-600 leading-relaxed text-[11px]">
                  All AI calls are orchestrated strictly via the Java Spring Boot backend service layer. Client-side browser code never contacts external AI providers, and API keys are strictly kept server-side.
                </p>
              </div>

              <div className="p-3 bg-ink-50 rounded-lg border border-ink-200">
                <div className="flex items-center space-x-2 text-ink-900 font-bold mb-1">
                  <Shield className="w-4 h-4 text-emerald-600" />
                  <span>Deterministic Fallback Mode</span>
                </div>
                <p className="text-ink-600 leading-relaxed text-[11px]">
                  If external AI providers are unavailable, the platform automatically switches to the deterministic rule engine without fabricating AI review completion.
                </p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
