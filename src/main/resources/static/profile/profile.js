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
    avatar.style.background = profile.avatarColor;
    avatar.textContent = profile.username.charAt(0).toUpperCase();

    // Set display info
    document.getElementById('display-username').textContent = profile.username;
    document.getElementById('display-email').textContent = profile.email;

    // Set inputs
    document.getElementById('username').value = profile.username;
    document.getElementById('email').value = profile.email;
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