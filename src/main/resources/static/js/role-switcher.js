/* Contextual role switching */
async function switchRole(roleName) {
    try {
        const result = await api.post('/api/customer/roles/switch', { contextualRole: roleName });
        if (result.data?.accessToken) {
            setTokens(result.data.accessToken, result.data.refreshToken);
            Toast.success(`已切换到角色：${formatRoleName(roleName)}`);
            setTimeout(() => location.reload(), 500);
        }
    } catch (e) { Toast.error('切换角色失败'); }
}

async function loadAvailableRoles() {
    const container = document.getElementById('roleList');
    if (!container) return;
    try {
        const result = await api.get('/api/customer/roles');
        const roles = result.data || [];
        container.innerHTML = roles.map(role => `
            <div class="toggle-item">
                <span>${formatRoleName(role)}</span>
                <button class="btn btn-sm btn-primary" onclick="switchRole('${role}')">切换到 ${formatRoleName(role)}</button>
            </div>
        `).join('');
    } catch (e) { container.innerHTML = '<p>加载角色失败</p>'; }
}

function formatRoleName(role) {
    const map = { STANDARD_USER: '标准用户', VERIFIED_USER: '已认证用户', ORG_DELEGATE: '组织代表' };
    return map[role] || role;
}

document.addEventListener('DOMContentLoaded', loadAvailableRoles);
