/* Admin dashboard 30-day registration trend chart
 * Requires Chart.js to be loaded from /js/lib/chart.min.js */

async function initRegistrationTrendChart() {
    const canvas = document.getElementById('registrationTrendChart');
    if (!canvas || typeof Chart === 'undefined') return;

    try {
        const result = await api.get('/api/admin/dashboard/registration-trend');
        const trendData = result.data || [];

        const labels = trendData.map(d => d.date);
        const counts = trendData.map(d => d.count);

        new Chart(canvas, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: '新注册用户',
                    data: counts,
                    borderColor: '#1a73e8',
                    backgroundColor: 'rgba(26,115,232,0.1)',
                    fill: true,
                    tension: 0.3,
                    pointRadius: 3,
                    pointBackgroundColor: '#1a73e8'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        });
    } catch (e) {
        console.error('加载图表数据失败:', e);
    }
}

document.addEventListener('DOMContentLoaded', initRegistrationTrendChart);
