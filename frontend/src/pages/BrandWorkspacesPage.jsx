import { useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import Modal from '../components/common/Modal';
import Input from '../components/common/Input';
import FormField from '../components/common/FormField';
import SearchInput from '../components/common/SearchInput';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Building2, Plus, Users, ShieldCheck, Check } from 'lucide-react';

export default function BrandWorkspacesPage() {
  const { brands, activeBrand, switchBrand } = useAuth();
  const { success } = useToast();

  const [workspaceList, setWorkspaceList] = useState(brands);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newName, setNewName] = useState('');
  const [newIndustry, setNewIndustry] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [errors, setErrors] = useState({});

  const filtered = workspaceList.filter(
    (b) =>
      b.name.toLowerCase().includes(search.toLowerCase()) ||
      b.industry.toLowerCase().includes(search.toLowerCase())
  );

  const handleCreate = (e) => {
    e.preventDefault();
    const errs = {};
    if (!newName.trim()) errs.name = 'Workspace name is required.';
    if (!newIndustry.trim()) errs.industry = 'Industry category is required.';

    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    const created = {
      id: `brand-${Date.now().toString(36)}`,
      name: newName,
      slug: newName.toLowerCase().replace(/\s+/g, '-'),
      industry: newIndustry,
      description: newDescription || 'Enterprise brand communications workspace.',
      activeGuidelinesVersion: 'v1.0 (Draft)',
      memberCount: 1,
      complianceScore: 100.0,
    };

    setWorkspaceList((prev) => [created, ...prev]);
    setIsModalOpen(false);
    setNewName('');
    setNewIndustry('');
    setNewDescription('');
    setErrors({});
    success('Workspace Created', `Brand workspace "${created.name}" is now available.`);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Brand Workspaces"
        description="Partition guidelines, claims ledgers, and reviewer approvals across business units, brands, or client organizations."
        actions={
          <Button size="sm" variant="primary" onClick={() => setIsModalOpen(true)}>
            <Plus className="w-4 h-4 mr-1.5" />
            New Workspace
          </Button>
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

      {/* Grid of Workspaces */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filtered.map((brand) => {
          const isActive = activeBrand?.id === brand.id;

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
                      {brand.activeGuidelinesVersion}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] uppercase font-semibold text-ink-400 block">
                      Team Members
                    </span>
                    <span className="font-semibold text-ink-800 flex items-center">
                      <Users className="w-3.5 h-3.5 mr-1 text-ink-500" />
                      {brand.memberCount} users
                    </span>
                  </div>
                </div>
              </div>

              <div className="mt-5 pt-3 border-t border-ink-100 flex items-center justify-between">
                <span className="text-xs text-emerald-600 font-semibold flex items-center">
                  <ShieldCheck className="w-3.5 h-3.5 mr-1" />
                  {brand.complianceScore}% score
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
            <Button size="sm" variant="primary" type="submit">
              Provision Workspace
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
