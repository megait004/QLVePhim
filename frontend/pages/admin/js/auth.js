const API_URL = 'http://localhost:8080';
const token = localStorage.getItem('token');

function checkAdminAccess() {
    if (!token) {
        window.location.href = '../login.html';
        return;
    }

    fetch(`${API_URL}/api/users/profile`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(user => {
        console.log('User roles:', user.roles);
        if (!user.roles || !user.roles.includes('ROLE_ADMIN')) {
            alert('Bạn không có quyền truy cập trang này!');
            window.location.href = '../../index.html';
        }
        window.currentUser = user;
    })
    .catch(error => {
        console.error('Error checking admin access:', error);
        window.location.href = '../login.html';
    });
}

function showSection(sectionName) {
    document.getElementById('movies-section').classList.add('hidden');
    document.getElementById('screenings-section').classList.add('hidden');
    document.getElementById('users-section').classList.add('hidden');
    document.getElementById(`${sectionName}-section`).classList.remove('hidden');

    switch(sectionName) {
        case 'movies':
            loadMovies();
            break;
        case 'screenings':
            loadScreenings();
            break;
        case 'users':
            loadUsers();
            break;
    }
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = '../login.html';
}