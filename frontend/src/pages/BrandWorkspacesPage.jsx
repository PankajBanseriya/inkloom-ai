import { useState, useEffect, useCallback } from 'react';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import SearchInput from '../components/common/SearchInput';
import LoadingSpinner from '../components/feedback/LoadingSpinner';
import EmptyState from '../components/feedback/EmptyState';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import brandService from '../services/brandService';
import { Building2, Plus, Users, ShieldCheck, Check, RefreshCw } from 'lucide-react';

export default function BrandWorkspacesPage() {
  const { activeBrand, switchBrand, refreshBrands } = useAuth();
  const { success, error: toastError } = useToast();

  const [workspaceList, setWorkspaceList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [newName, setNewName] = useState('');
  const [newIndustry, setNewIndustry] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [errors, setErrors] = useState({});

  const fetchWorkspaces = useCallback(async () => {
    setLoading(true);
    try {
      const data = await brandService.getBrands();
      setWorkspaceList(data || []);
    } catch (err) {
      console.warn('Failed to load workspaces:', err.message);
      setWorkspaceList([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWorkspaces();
  }, [fetchWorkspaces]);

  const filtered = workspaceList.filter(
    (b) =>
      (b.name || '').toLowerCase().includes(search.toLowerCase()) ||
      (b.industry || '').toLowerCase().includes(search.toLowerCase())
  );

  const handleCreate = async (e) => {
    e.preventDefault();
    const errs = {};
    if (!newName.trim()) errs.name = 'Workspace name is required.';
    if (!newIndustry.trim()) errs.industry = 'Industry category is required.';

    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    setIsSubmitting(true);
    try {
      const payload = {
        name: newName.trim(),
        industry: newIndustry.trim(),
        description: newDescription.trim() || 'Enterprise brand communications workspace.',
      };

      const created = await brandService.createBrand(payload);
      setWorkspaceList((prev) => [created, ...prev]);
      if (refreshBrands) {
        refreshBrands();
      }

      setIsModalOpen(false);
      setNewName('');
      setNewIndustry('');
      setNewDescription('');
      setErrors({});
      success('Workspace Created', `Brand workspace "${created.name}" is now available.`);
    } catch (err) {
      toastError('Creation Failed', err.message || 'Unable to provision workspace');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Brand Workspaces"
        description="Partition guidelines, claims ledgers, and reviewer approvals across business units, brands, or client organizations."
        actions={
          <div className="flex items-center space-x-2">
            <Button size="sm" variant="outline" onClick={fetchWorkspaces} className="text-xs">
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
            <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
              <Plus className="w-4 h-4 mr-1.5" />
              New Workspace
            </Button>
          </div>
        }
      />

      {/* Filter / Search Bar */}
      <div className="flex items-center justify-between gap-4">
        <SearchInput
          value={search}
          onChange={setSearch}
          placeholder="Filter workspaces by name or industry..."
        />
        <div className="text-xs text-ink-500 font-medium">
          Showing {filtered.length} of {workspaceList.length} workspaces
        </div>
      </div>

      {loading ? (
        <div className="py-16 flex justify-center bg-white rounded-xl border border-ink-200">
          <LoadingSpinner label="Loading brand workspaces from database..." />
        </div>
      ) : filtered.length === 0 ? (
        <Card className="py-12">
          <EmptyState
            title="No Brand Workspaces Found"
            description="Provision a brand workspace to isolate governance rules, verified sources, and human approvals."
            action={
              <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
                <Plus className="w-4 h-4 mr-1.5" />
                Provision First Workspace
              </Button>
            }
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((brand) => {
            const isActive = activeBrand?.id === brand.id;
            const memberCount = brand.memberCount ?? 1;
            const complianceScore = brand.complianceScore ?? 100.0;

            return (
              <Card
                key={brand.id}
                className={`flex flex-col justify-between transition-all ${
                  isActive
                    ? 'ring-2 ring-brand-500 border-transparent shadow-sm'
                    : 'hover:border-ink-300'
                }`}
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <div className="p-2.5 rounded-xl bg-ink-100 text-ink-800 border border-ink-200">
                      <Building2 className="w-5 h-5 text-brand-600" />
                    </div>
                    <div className="flex items-center space-x-1.5">
                      {isActive ? (
                        <Badge variant="brand" size="sm" className="font-semibold">
                          <Check className="w-3 h-3 mr-1" />
                          Current Active
                        </Badge>
                      ) : (
                        <Badge variant="neutral" size="sm">
                          {brand.industry}
                        </Badge>
                      )}
                    </div>
                  </div>

                  <h3 className="text-base font-bold text-ink-900 mt-3">{brand.name}</h3>
                  <p className="text-xs text-ink-500 mt-1 leading-relaxed line-clamp-2">
                    {brand.description}
                  </p>

                  <div className="mt-4 pt-3 border-t border-ink-100 grid grid-cols-2 gap-2 text-xs">
                    <div>
                      <span className="text-[10px] uppercase font-semibold text-ink-400 block">
                        Guidelines
                      </span>
                      <span className="font-semibold text-ink-800">
                        {brand.activeGuidelinesVersion || 'v1.0'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] uppercase font-semibold text-ink-400 block">
                        Team Members
                      </span>
                      <span className="font-semibold text-ink-800 flex items-center">
                        <Users className="w-3.5 h-3.5 mr-1 text-ink-500" />
                        {memberCount} users
                      </span>
                    </div>
                  </div>
                </div>

                <div className="mt-5 pt-3 border-t border-ink-100 flex items-center justify-between">
                  <span className="text-xs text-emerald-600 font-semibold flex items-center">
                    <ShieldCheck className="w-3.5 h-3.5 mr-1" />
                    {complianceScore}% score
                  </span>

                  {isActive ? (
                    <span className="text-xs font-semibold text-brand-700">Active Context</span>
                  ) : (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        switchBrand(brand.id);
                        success('Context Switched', `Switched active workspace to "${brand.name}"`);
                      }}
                      className="text-xs"
                    >
                      Switch Context
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create Workspace Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Provision New Brand Workspace"
        description="Set up isolated guidelines, approved claims, and reviewer teams for a new brand."
      >
        <form onSubmit={handleCreate} className="space-y-4">
          <Input
            label="Workspace / Brand Name"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            error={errors.name}
            placeholder="e.g. Acme Consumer Electronics"
            required
          />

          <Input
            label="Industry Domain"
            value={newIndustry}
            onChange={(e) => setNewIndustry(e.target.value)}
            error={errors.industry}
            placeholder="e.g. Consumer Hardware / IoT"
            required
          />

          <FormField label="Brand Description & Purpose" id="desc">
            <textarea
              id="desc"
              rows={3}
              value={newDescription}
              onChange={(e) => setNewDescription(e.target.value)}
              placeholder="Primary communication objectives, target audience, and legal guidelines scope..."
              className="w-full text-xs p-2.5 bg-white border border-ink-300 rounded-lg text-ink-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </FormField>

          <div className="flex items-center justify-end space-x-2 pt-4 border-t border-ink-100">
            <Button size="sm" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" variant="primary" type="submit" isLoading={isSubmitting}>
              Provision Workspace
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
