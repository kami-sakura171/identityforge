/* Avatar upload with drag-and-drop support */

function initAvatarUpload() {
    const dropzone = document.getElementById('avatarDropzone');
    const fileInput = document.getElementById('avatarFileInput');
    const preview = document.getElementById('avatarPreview');

    if (!dropzone || !fileInput) return;

    // Click to upload
    dropzone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) handleFile(e.target.files[0]);
    });

    // Drag and drop
    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('dragover');
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('dragover');
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
        if (e.dataTransfer.files.length > 0) handleFile(e.dataTransfer.files[0]);
    });
}

function handleFile(file) {
    // Validate type
    const allowedTypes = ['image/jpeg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
        Toast.error('仅支持 JPEG 和 PNG 格式的图片。');
        return;
    }

    // Validate size (max 2MB)
    const maxSize = 2 * 1024 * 1024;
    if (file.size > maxSize) {
        Toast.error('文件过大。最大支持 2MB。');
        return;
    }

    // Preview
    const reader = new FileReader();
    reader.onload = (e) => {
        const preview = document.getElementById('avatarPreview');
        if (preview) preview.src = e.target.result;
    };
    reader.readAsDataURL(file);

    // Upload
    uploadAvatar(file);
}

async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);

    try {
        const btn = document.getElementById('uploadAvatarBtn');
        if (btn) setButtonLoading(btn, true);
        const result = await api.upload('/api/customer/avatar', formData);
        Toast.success('头像上传成功！');
    } catch (e) {
        Toast.error(e.message || '头像上传失败');
    } finally {
        const btn = document.getElementById('uploadAvatarBtn');
        if (btn) setButtonLoading(btn, false);
    }
}

document.addEventListener('DOMContentLoaded', initAvatarUpload);
