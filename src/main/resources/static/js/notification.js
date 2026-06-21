/* Notification polling and tray management */

let unreadPollInterval = null;

function initNotifications() {
    const bell = document.getElementById('notificationBell');
    const tray = document.getElementById('notificationTray');
    const closeBtn = document.getElementById('closeTrayBtn');

    if (bell) {
        bell.addEventListener('click', () => {
            tray.classList.toggle('open');
            if (tray.classList.contains('open')) loadNotifications();
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', () => tray.classList.remove('open'));
    }

    // Poll unread count every 60 seconds
    pollUnreadCount();
    unreadPollInterval = setInterval(pollUnreadCount, 60000);
}

async function pollUnreadCount() {
    try {
        const result = await api.get('/api/customer/notifications/unread-count');
        const count = result.data?.unreadCount || 0;
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = count > 0 ? 'flex' : 'none';
        }
    } catch (e) {
        // Silently ignore polling errors
    }
}

async function loadNotifications() {
    const body = document.getElementById('notificationTrayBody');
    if (!body) return;

    try {
        const result = await api.get('/api/customer/notifications?page=0&size=50');
        const notifications = result.data?.content || [];
        body.innerHTML = '';

        if (notifications.length === 0) {
            body.innerHTML = '<div style="padding:24px;text-align:center;color:#5f6368">暂无通知</div>';
            return;
        }

        notifications.forEach(n => {
            const item = document.createElement('div');
            item.className = `notification-item ${n.isRead ? '' : 'unread'}`;
            item.innerHTML = `
                <div style="font-weight:${n.isRead ? '400' : '600'}">${escapeHtml(n.title)}</div>
                <div style="font-size:0.8rem;color:#5f6368;margin-top:2px">${escapeHtml(n.message)}</div>
                <div class="time">${formatTimeAgo(n.createdAt)}</div>
            `;
            item.addEventListener('click', async () => {
                await api.put(`/api/customer/notifications/${n.id}/read`);
                item.classList.remove('unread');
                pollUnreadCount();
            });
            body.appendChild(item);
        });
    } catch (e) {
        body.innerHTML = '<div style="padding:24px;text-align:center;color:#d93025">加载通知失败</div>';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatTimeAgo(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return '刚刚';
    if (diffMin < 60) return `${diffMin}分钟前`;
    const diffHr = Math.floor(diffMin / 60);
    if (diffHr < 24) return `${diffHr}小时前`;
    const diffDay = Math.floor(diffHr / 24);
    return `${diffDay}天前`;
}

document.addEventListener('DOMContentLoaded', initNotifications);
