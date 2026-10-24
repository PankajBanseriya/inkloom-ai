import { useState, useEffect, useCallback } from 'react';
import PageHeader from '../components/common/PageHeader';
import DataTable from '../components/common/DataTable';
import FilterBar from '../components/common/FilterBar';
import StatusBadge from '../components/common/StatusBadge';
import Button from '../components/common/Button';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import EmptyState from '../components/feedback/EmptyState';
import Card from '../components/common/Card';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import brandService from '../services/brandService';
import { UserPlus, RefreshCw } from 'lucide-react';

export default function TeamManagementPage() {
  const { activeBrand, user } = useAuth();
  const { success, error: toastError } = useToast();
  const canManageTeam = user?.role === 'ADMIN' || user?.role === 'BRAND_MANAGER';

  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [newName, setNewName] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [newRole, setNewRole] = useState('CONTENT_CREATOR');
  const [errors, setErrors] = useState({});

  const fetchMembers = useCallback(() => {
    if (!activeBrand?.id) {
      setLoading(false);
      return;
    }
    setLoading(true);
    brandService.getMembers(activeBrand.id)
      .then((data) => {
        setMembers(data || []);
      })
      .catch((err) => {
        console.warn('Could not load team members from API:', err.message);
        setMembers([]);
      })
      .finally(() => setLoading(false));
  }, [activeBrand?.id]);

  useEffect(() => {
    fetchMembers();
  }, [fetchMembers]);

  const filtered = members.filter((m) => {
    const name = m.fullName || m.name || '';
    const email = m.email || '';
    const matchesSearch =
      name.toLowerCase().includes(search.toLowerCase()) ||
      email.toLowerCase().includes(search.toLowerCase());
    const matchesRole = roleFilter === 'ALL' || m.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  const handleInvite = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!newName.trim()) errs.name = 'Full name is required.';
    if (!newEmail.trim()) {
      errs.email = 'Corporate email is required.';
    } else if (!/\S+@\S+\.\S+/.test(newEmail)) {
      errs.email = 'Enter a valid corporate email.';
    }

    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    if (!activeBrand?.id) {
      toastError('No workspace selected', 'Please select a workspace before inviting members.');
      return;
    }

    try {
      const saved = await brandService.inviteMember(activeBrand.id, {
        fullName: newName.trim(),
        email: newEmail.trim(),
        role: newRole,
      });
      if (saved) {
        setMembers((prev) => [saved, ...prev]);
      }
      setIsModalOpen(false);
      setNewName('');
      setNewEmail('');
      setErrors({});
      success('Invitation Dispatched', `Invited ${newName} with ${newRole} privileges.`);
    } catch (err) {
      toastError('Invitation Failed', err.message);
    }
  };

  const columns = [
    {
      key: 'name',
      header: 'Team Member',
      render: (_, row) => {
        const displayName = row.fullName || row.name || 'Member';
        const initials = displayName.split(' ').map((n) => n[0]).join('').slice(0, 2);
        return (
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 rounded-full bg-brand-100 border border-brand-200 text-brand-700 font-bold text-xs flex items-center justify-center">
              {initials}
            </div>
            <div>
              <div className="font-bold text-ink-900">{displayName}</div>
              <div className="text-[11px] text-ink-500">{row.email}</div>
            </div>
          </div>
        );
      },
    },
    {
      key: 'role',
      header: 'Assigned Role',
      render: (val) => {
        const roleColor = {
          ADMIN: 'bg-rose-50 text-rose-800 border-rose-200',
          BRAND_MANAGER: 'bg-brand-50 text-brand-800 border-brand-200',
          REVIEWER: 'bg-emerald-50 text-emerald-800 border-emerald-200',
          CONTENT_CREATOR: 'bg-ink-100 text-ink-800 border-ink-200',
        }[val] || 'bg-ink-100 text-ink-800 border-ink-200';

        return (
          <span className={`px-2.5 py-1 rounded-md text-xs font-semibold border ${roleColor}`}>
            {(val || '').replace(/_/g, ' ')}
          </span>
        );
      },
    },
    {
      key: 'workspace',
      header: 'Workspace Scope',
      render: () => <span className="text-xs text-ink-700 font-medium">{activeBrand?.name || 'Workspace'}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (_, row) => {
        const statusVal = row.status || (row.active !== false ? 'ACTIVE' : 'INACTIVE');
        return <StatusBadge status={statusVal} />;
      },
    },
    {
      key: 'createdAt',
      header: 'Created / Joined',
      render: (_, row) => {
        const dateStr = row.createdAt ? String(row.createdAt).split('T')[0] : (row.lastLogin || 'Recent');
        return <span className="text-xs text-ink-500">{dateStr}</span>;
      },
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Team & Role-Based Access Control"
        description="Manage workspace team members, assign brand governance roles, and audit workspace authorizations."
        actions={
          <div className="flex items-center space-x-2">
            <Button size="sm" variant="outline" onClick={fetchMembers}>
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              Sync Team
            </Button>
            {canManageTeam && (
              <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                <UserPlus className="w-4 h-4 mr-1.5" />
                Invite Member
              </Button>
            )}
          </div>
        }
      />

      {!canManageTeam && (
        <div className="p-3 bg-ink-100/70 rounded-xl border border-ink-200 text-xs text-ink-600 flex items-center justify-between">
          <span>
            <strong>Read-only access:</strong> You are currently signed in as <strong>{user?.role?.replace('_', ' ')}</strong>. Provisioning team accounts and reassigning governance privileges requires Brand Manager or Administrator access.
          </span>
        </div>
      )}

      {/* Role explanation */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
        <div className="p-3 bg-white rounded-xl border border-ink-200">
          <span className="font-bold text-ink-900 block mb-0.5">Admin</span>
          <p className="text-ink-500 text-[11px]">System config, user provisioning & global audit logs.</p>
        </div>
        <div className="p-3 bg-white rounded-xl border border-ink-200">
          <span className="font-bold text-brand-700 block mb-0.5">Brand Manager</span>
          <p className="text-ink-500 text-[11px]">Owns guidelines, approved sources, claims & tone rules.</p>
        </div>
        <div className="p-3 bg-white rounded-xl border border-ink-200">
          <span className="font-bold text-emerald-700 block mb-0.5">Reviewer & Approver</span>
          <p className="text-ink-500 text-[11px]">Inspects evidence, writes comments & signs off publication.</p>
        </div>
        <div className="p-3 bg-white rounded-xl border border-ink-200">
          <span className="font-bold text-ink-700 block mb-0.5">Content Creator</span>
          <p className="text-ink-500 text-[11px]">Drafts content, applies Intent Lock fixes & submits for sign-off.</p>
        </div>
      </div>

      <FilterBar
        searchValue={search}
        onSearchChange={setSearch}
        filters={[
          {
            label: 'Role',
            value: roleFilter,
            onChange: setRoleFilter,
            options: [
              { label: 'All Roles', value: 'ALL' },
              { label: 'Brand Manager', value: 'BRAND_MANAGER' },
              { label: 'Reviewer', value: 'REVIEWER' },
              { label: 'Content Creator', value: 'CONTENT_CREATOR' },
              { label: 'Admin', value: 'ADMIN' },
            ],
          },
        ]}
        onReset={() => {
          setSearch('');
          setRoleFilter('ALL');
        }}
      />

      {loading ? (
        <div className="py-12 flex justify-center">
          <LoadingSpinner label="Loading team members..." />
        </div>
      ) : filtered.length === 0 ? (
        <Card className="p-8">
          <EmptyState
            title="No team members found"
            description={
              search || roleFilter !== 'ALL'
                ? 'No team members match your filter criteria.'
                : 'No members have been invited to this workspace yet.'
            }
            action={
              canManageTeam && !search && roleFilter === 'ALL' ? (
                <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                  <UserPlus className="w-4 h-4 mr-1.5" />
                  Invite First Member
                </Button>
              ) : null
            }
          />
        </Card>
      ) : (
        <DataTable
          columns={columns}
          data={filtered}
          keyField="id"
        />
      )}

      {/* Invite Member Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Invite Workspace Member"
        description="Assign a corporate email and role privileges to an existing or new collaborator."
      >
        <form onSubmit={handleInvite} className="space-y-4">
          <Input
            label="Full Name"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            error={errors.name}
            placeholder="e.g. Jordan Miller"
            required
          />

          <Input
            label="Corporate Email"
            type="email"
            value={newEmail}
            onChange={(e) => setNewEmail(e.target.value)}
            error={errors.email}
            placeholder="jordan.miller@company.com"
            required
          />

          <FormField label="Assigned Governance Role" id="role">
            <select
              id="role"
              value={newRole}
              onChange={(e) => setNewRole(e.target.value)}
              className="w-full px-3 py-2 text-xs bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
              <option value="CONTENT_CREATOR">Content Creator (Author & submit drafts)</option>
              <option value="REVIEWER">Reviewer & Approver (Official sign-offs)</option>
              <option value="BRAND_MANAGER">Brand Manager (Manage guidelines & claims)</option>
              <option value="ADMIN">Administrator (Full workspace controls)</option>
            </select>
          </FormField>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" variant="primary" type="submit">
              Dispatch Invitation
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
