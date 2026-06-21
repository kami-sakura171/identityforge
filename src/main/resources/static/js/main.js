/* IdentityForge Main JavaScript */

// CSRF token helper
function getCsrfToken() {
    const meta = document.querySelector('meta[name="_csrf"]');
    return meta ? meta.getAttribute('content') : '';
}

function getCsrfHeader() {
    const meta = document.querySelector('meta[name="_csrf_header"]');
    return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
}

// Toast notification system
const Toast = {
    container: null,

    init() {
        this.container = document.createElement('div');
        this.container.className = 'toast-container';
        document.body.appendChild(this.container);
    },

    show(message, type = 'info') {
        if (!this.container) this.init();
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        this.container.appendChild(toast);
        setTimeout(() => toast.remove(), 4000);
    },

    success(msg) { this.show(msg, 'success'); },
    error(msg) { this.show(msg, 'error'); },
    warning(msg) { this.show(msg, 'warning'); },
    info(msg) { this.show(msg, 'info'); }
};

// API Helper
const api = {
    async get(url) {
        const res = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${getAccessToken()}`,
                'Accept': 'application/json'
            }
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async post(url, body) {
        const res = await fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${getAccessToken()}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(body)
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async put(url, body) {
        const res = await fetch(url, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${getAccessToken()}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: body ? JSON.stringify(body) : undefined
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async delete(url) {
        const res = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${getAccessToken()}`,
                'Accept': 'application/json'
            }
        });
        if (!res.ok) throw await res.json();
        return res.json();
    },

    async upload(url, formData) {
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${getAccessToken()}` },
            body: formData
        });
        if (!res.ok) throw await res.json();
        return res.json();
    }
};

function getAccessToken() {
    return localStorage.getItem('accessToken') || '';
}

function setTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
}

function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
}

// Form validation helper
function validateForm(formElement) {
    const errors = [];
    const inputs = formElement.querySelectorAll('[required]');
    inputs.forEach(input => {
        if (!input.value.trim()) {
            errors.push(`${input.previousElementSibling?.textContent || input.name} is required`);
        }
    });
    return errors;
}

function clearFormErrors(formElement) {
    formElement.querySelectorAll('.form-error').forEach(el => el.remove());
    formElement.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
}

function showFormErrors(formElement, errors) {
    clearFormErrors(formElement);
    errors.forEach(err => {
        const errorEl = document.createElement('div');
        errorEl.className = 'form-error';
        errorEl.textContent = err;
        formElement.appendChild(errorEl);
    });
}

// Button loading state
function setButtonLoading(btn, loading) {
    if (loading) {
        btn.disabled = true;
        btn.dataset.originalText = btn.textContent;
        btn.innerHTML = '<span class="spinner"></span> ' + (btn.dataset.loadingText || '处理中...');
    } else {
        btn.disabled = false;
        btn.textContent = btn.dataset.originalText || btn.textContent;
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    Toast.init();
});
