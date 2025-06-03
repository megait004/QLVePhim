function loadUsers() {
    fetch(`${API_URL}/api/users`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(users => {
        const usersList = document.getElementById('users-list');
        usersList.innerHTML = users.map(user => `
            <div class="bg-white rounded-xl shadow-md p-6 card-hover">
                <div class="flex justify-between items-center">
                    <div>
                        <h3 class="text-xl font-semibold text-gray-900">${user.username}</h3>
                        <p class="text-gray-600 mt-1">Email: ${user.email}</p>
                        <p class="text-gray-600 mt-1">Vai trò: ${user.roles.join(', ')}</p>
                    </div>
                    <div class="flex space-x-3">
                        <button onclick="toggleUserStatus(${user.id})"
                                class="btn bg-yellow-500 text-white px-4 py-2 rounded-lg hover:bg-yellow-600">
                            ${user.enabled ? 'Khóa' : 'Mở khóa'}
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('users-list').innerHTML = `
            <div class="text-center text-red-500">
                <p>Có lỗi xảy ra khi tải danh sách người dùng.</p>
                <button onclick="loadUsers()" class="mt-3 btn bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700">
                    Thử lại
                </button>
            </div>
        `;
    });
}

function toggleUserStatus(userId) {
    // Thêm chức năng khóa/mở khóa người dùng nếu cần
    alert('Chức năng này đang được phát triển');
}