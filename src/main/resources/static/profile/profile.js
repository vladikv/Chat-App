const TOKEN = localStorage.getItem('token');
const BASE_URL = window.location.origin;

if (!TOKEN) window.location.href = '/login/login.html';

async function loadProfile() {
    const res = await fetch(BASE_URL + '/api/profile', {
        headers: { Authorization: 'Bearer ' + TOKEN }
    });
    const profile = await res.json();

    // Set avatar
    const avatar = document.getElementById('avatar');
    if (profile.avatarUrl) {
        avatar.style.background = 'transparent';
        avatar.innerHTML = `<img src="${profile.avatarUrl}" style="width:100%;height:100%;object-fit:cover;border-radius:50%"/>`;
    } else {
        avatar.style.background = profile.avatarColor;
        avatar.textContent = profile.username.charAt(0).toUpperCase();
    }

    // Set display info
    document.getElementById('display-username').textContent = profile.username;
    document.getElementById('display-email').textContent = profile.email;

    // Set inputs
    document.getElementById('username').value = profile.username;
    document.getElementById('email').value = profile.email;
}

async function uploadAvatar(input) {
    const file = input.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
        // Upload file first
        const uploadRes = await fetch(BASE_URL + '/api/files/upload', {
            method: 'POST',
            headers: { Authorization: 'Bearer ' + TOKEN },
            body: formData
        });
        if (!uploadRes.ok) { showError('Upload failed'); return; }
        const data = await uploadRes.json();

        // Save avatar URL to profile
        const res = await fetch(BASE_URL + '/api/profile/avatar', {
            method: 'PUT',
            headers: { Authorization: 'Bearer ' + TOKEN, 'Content-Type': 'application/json' },
            body: JSON.stringify({ avatarUrl: data.url })
        });
        if (!res.ok) { showError('Failed to save avatar'); return; }

        showSuccess('Avatar updated!');
        loadProfile();
    } catch (e) {
        showError('Server error');
    }
}

async function save() {
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;

    const dto = {};
    if (username) dto.username = username;
    if (email) dto.email = email;
    if (currentPassword) dto.currentPassword = currentPassword;
    if (newPassword) dto.newPassword = newPassword;

    try {
        const res = await fetch(BASE_URL + '/api/profile', {
            method: 'PUT',
            headers: {
                Authorization: 'Bearer ' + TOKEN,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const err = await res.text();
            showError(err || 'Update failed');
            return;
        }

        const profile = await res.json();
        localStorage.setItem('username', profile.username);
        showSuccess('Profile updated!');
        loadProfile();

    } catch (e) {
        showError('Server error');
    }
}

function showError(msg) {
    document.getElementById('error').textContent = msg;
    document.getElementById('success').textContent = '';
}

function showSuccess(msg) {
    document.getElementById('success').textContent = msg;
    document.getElementById('error').textContent = '';
}

loadProfile();